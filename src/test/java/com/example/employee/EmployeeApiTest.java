package com.example.employee;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeApiTest {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    private static String employeeJson(String name, String department, String cardNumber, String issueDate) {
        return """
                {"employeeName":"%s","department":"%s","idCard":{"cardNumber":"%s","issueDate":"%s"}}
                """.formatted(name, department, cardNumber, issueDate);
    }

    private MvcResult createEmployee(String json) throws Exception {
        return mvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private static int readInt(MvcResult result, String path) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), path);
    }

    private int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    @Test
    void crudFlowKeepsEmployeeAndCardTogether() throws Exception {
        // CREATE: one request inserts a row in id_cards and a row in employees
        MvcResult created = createEmployee(employeeJson("Ravi Kumar", "Engineering", "WMT-1001", "2026-01-15"));
        int employeeId = readInt(created, "$.employeeId");
        int cardId = readInt(created, "$.idCard.cardId");
        assertEquals("http://localhost/employees/" + employeeId, created.getResponse().getHeader("Location"));
        assertEquals(1, count("select count(*) from employees where employee_id = ? and card_id = ?", employeeId, cardId));

        // READ
        mvc.perform(get("/employees")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].idCard.cardNumber", hasItem("WMT-1001")));
        mvc.perform(get("/employees/" + employeeId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("Ravi Kumar"))
                .andExpect(jsonPath("$.department").value("Engineering"))
                .andExpect(jsonPath("$.idCard.issueDate").value("2026-01-15"));

        // UPDATE: the same card row is updated in place (cardId unchanged)
        mvc.perform(put("/employees/" + employeeId).contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Ravi Kumar", "Platform", "WMT-1001-R", "2026-06-01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("Platform"))
                .andExpect(jsonPath("$.idCard.cardId").value(cardId))
                .andExpect(jsonPath("$.idCard.cardNumber").value("WMT-1001-R"));
        assertEquals(1, count("select count(*) from id_cards where card_id = ? and card_number = 'WMT-1001-R'", cardId));
        assertEquals(1, count("select count(*) from id_cards where card_number like 'WMT-1001%'"));

        // card side -> its single owner
        mvc.perform(get("/idcards/" + cardId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("WMT-1001-R"))
                .andExpect(jsonPath("$.employee.employeeId").value(employeeId))
                .andExpect(jsonPath("$.employee.department").value("Platform"));
        mvc.perform(get("/idcards")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].employee.employeeName", hasItem("Ravi Kumar")));

        // DELETE: cascades to the ID card
        mvc.perform(delete("/employees/" + employeeId)).andExpect(status().isNoContent());
        mvc.perform(get("/employees/" + employeeId)).andExpect(status().isNotFound());
        mvc.perform(get("/idcards/" + cardId)).andExpect(status().isNotFound());
        assertEquals(0, count("select count(*) from id_cards where card_id = ?", cardId));
        mvc.perform(delete("/employees/" + employeeId)).andExpect(status().isNotFound());
    }

    @Test
    void employeeMustHaveValidIdCard() throws Exception {
        mvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeName\":\"No Card\",\"department\":\"HR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.errors.idCard").value("every employee must have exactly one ID card"));

        mvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("", "HR", "", "2026-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.employeeName").exists())
                .andExpect(jsonPath("$.errors['idCard.cardNumber']").exists());

        mvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Future", "HR", "WMT-FUTURE", "2999-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['idCard.issueDate']").exists());

        mvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Bad Date", "HR", "WMT-BADDATE", "15-01-2026")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed Request"));
    }

    @Test
    void cardNumberCannotBeSharedByTwoEmployees() throws Exception {
        createEmployee(employeeJson("Asha", "Finance", "WMT-2001", "2026-02-01"));
        mvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Bala", "Finance", "WMT-2001", "2026-02-02")))
                .andExpect(status().isConflict());

        int bala = readInt(createEmployee(employeeJson("Bala", "Finance", "WMT-2002", "2026-02-02")), "$.employeeId");
        mvc.perform(put("/employees/" + bala).contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Bala", "Finance", "WMT-2001", "2026-02-02")))
                .andExpect(status().isConflict());
        // keeping your own card number on update is fine
        mvc.perform(put("/employees/" + bala).contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Bala K", "Finance", "WMT-2002", "2026-02-02")))
                .andExpect(status().isOk());
    }

    @Test
    void databaseEnforcesExactlyOneCardPerEmployee() throws Exception {
        int cardId = readInt(createEmployee(employeeJson("Chitra", "Sales", "WMT-3001", "2026-03-01")), "$.idCard.cardId");

        // UNIQUE(card_id): a second employee cannot point to the same card
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "insert into employees (employee_name, department, card_id) values ('Copy', 'Sales', ?)", cardId));
        // NOT NULL(card_id): an employee cannot exist without a card
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "insert into employees (employee_name, department) values ('No Card', 'Sales')"));
    }

    @Test
    void unknownIdsReturn404() throws Exception {
        mvc.perform(get("/employees/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Employee not found with id 999999"));
        mvc.perform(put("/employees/999999").contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson("Ghost", "None", "WMT-GHOST", "2026-01-01")))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/employees/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/idcards/999999")).andExpect(status().isNotFound());
    }
}

# Q1 – Employee ID Card Management System (One-to-One)

Spring Boot 3.5 + Spring Data JPA + MySQL. Every **Employee** has exactly one **IDCard**, and every
IDCard belongs to exactly one Employee. The two are stored in separate tables.

## Mapping

```
employees                              id_cards
+-------------+                        +-------------+
| employee_id | PK                     | card_id     | PK
| employee_name                        | card_number | UNIQUE
| department  |                        | issue_date  |
| card_id     | FK, UNIQUE, NOT NULL ->| card_id     |
+-------------+                        +-------------+
```

| Side | Code | Meaning |
|------|------|---------|
| Owning (`Employee`) | `@OneToOne(cascade = ALL, orphanRemoval = true, optional = false)`<br>`@JoinColumn(name = "card_id", nullable = false, unique = true)` | Holds the FK. `NOT NULL` means every employee has a card. `UNIQUE` means a card can't be shared. |
| Inverse (`IDCard`) | `@OneToOne(mappedBy = "idCard")` | No FK column. Lets you go from a card to its employee (bidirectional). |

- **Cascade:** `POST /employees` inserts the card and the employee in one transaction. `DELETE` removes both.
- **Card lifecycle:** cards are created, updated and deleted only through their employee, so a card can never exist on its own.
- **Update:** `PUT` updates the existing card row in place, so the `cardId` stays the same.
- **N+1 avoided:** `findAll`/`findById` use `@EntityGraph`, so the card is loaded in the same SQL join.

## Project structure

```
src/main/java/com/example/employee
├── entity/       Employee (owning side), IDCard (inverse side)
├── repository/   EmployeeRepository, IDCardRepository
├── dto/          request / response records (with validation)
├── service/      EmployeeService (CRUD), IDCardService (read-only)
├── controller/   EmployeeController (/employees), IDCardController (/idcards)
└── exception/    404 / 409 / 400 handling (RFC 7807 problem+json)
```

## Run

Needs Java 17+, Maven, and MySQL on `localhost:3306`. The `employeedb` database is created automatically.

```bash
# default credentials root / <empty>; override if needed
export DB_USERNAME=root DB_PASSWORD=yourPassword
mvn spring-boot:run          # app on http://localhost:8080
mvn test                     # automated API tests (in-memory H2, MySQL not needed)
```

## REST API

| Method | Endpoint | Description | Success |
|--------|----------|-------------|---------|
| POST | `/employees` | Create an employee **with** its ID card | 201 + `Location` |
| GET | `/employees` | List all employees with their cards | 200 |
| GET | `/employees/{id}` | Get one employee with its card | 200 |
| PUT | `/employees/{id}` | Update employee + card details | 200 |
| DELETE | `/employees/{id}` | Delete employee (its card is deleted by cascade) | 204 |
| GET | `/idcards` | List all cards with their owner | 200 |
| GET | `/idcards/{cardId}` | Get a card and the employee it belongs to | 200 |

Errors: `400` validation (e.g. missing `idCard`, future `issueDate`), `404` unknown id, `409` duplicate `cardNumber`.

### Sample request – `POST /employees`

```json
{
  "employeeName": "Ravi Kumar",
  "department": "Engineering",
  "idCard": {
    "cardNumber": "WMT-EMP-1001",
    "issueDate": "2026-01-15"
  }
}
```

Response `201 Created`:

```json
{
  "employeeId": 1,
  "employeeName": "Ravi Kumar",
  "department": "Engineering",
  "idCard": { "cardId": 1, "cardNumber": "WMT-EMP-1001", "issueDate": "2026-01-15" }
}
```

`GET /idcards/1` (card → employee):

```json
{
  "cardId": 1,
  "cardNumber": "WMT-EMP-1001",
  "issueDate": "2026-01-15",
  "employee": { "employeeId": 1, "employeeName": "Ravi Kumar", "department": "Engineering" }
}
```

## Postman

Import `postman/Employee-IDCard-OneToOne.postman_collection.json`, then run the requests in order, or run the
whole collection with **Run collection**. Each request has test assertions, and the ids are saved
automatically (`employeeId`, `cardId`, `cardNumber`). Change the `baseUrl` collection variable if the
app isn't running on `http://localhost:8080`.

# Q1 - Employee ID Card (One-to-One) — Run output

| | |
|---|---|
| Base URL | `http://localhost:8080` |
| Collection | `postman/Employee-IDCard-OneToOne.postman_collection.json` |
| Date | 2026-10-30 |
| Requests | 11 |
| Test results | **21 passed, 0 failed** |

Real output from running this project against MySQL: every request of the Postman collection, with what was sent and what came back.

---

## API requests and responses

### 1. Create Employee with ID Card

`POST /employees` → **201 Created**

**Request body**

```json
{
  "employeeName": "Ravi Kumar",
  "department": "Engineering",
  "idCard": {
    "cardNumber": "WMT-1793335368",
    "issueDate": "2026-01-15"
  }
}
```

**Response** — headers: `location: http://localhost:8080/employees/1`

```json
{
  "employeeId": 1,
  "employeeName": "Ravi Kumar",
  "department": "Engineering",
  "idCard": {
    "cardId": 1,
    "cardNumber": "WMT-1793335368",
    "issueDate": "2026-01-15"
  }
}
```

**Tests**

- ✅ 201 Created
- ✅ Location header present
- ✅ ID card saved with the employee

### 2. Get All Employees

`GET /employees` → **200 OK**

**Response**

```json
[
  {
    "employeeId": 1,
    "employeeName": "Ravi Kumar",
    "department": "Engineering",
    "idCard": {
      "cardId": 1,
      "cardNumber": "WMT-1793335368",
      "issueDate": "2026-01-15"
    }
  }
]
```

**Tests**

- ✅ 200 OK
- ✅ returns a list

### 3. Get Employee by ID

`GET /employees/1` → **200 OK**

**Response**

```json
{
  "employeeId": 1,
  "employeeName": "Ravi Kumar",
  "department": "Engineering",
  "idCard": {
    "cardId": 1,
    "cardNumber": "WMT-1793335368",
    "issueDate": "2026-01-15"
  }
}
```

**Tests**

- ✅ 200 OK
- ✅ employee has its ID card

### 4. Update Employee and ID Card

`PUT /employees/1` → **200 OK**

**Request body**

```json
{
  "employeeName": "Ravi Kumar",
  "department": "Platform Engineering",
  "idCard": {
    "cardNumber": "WMT-1793335368",
    "issueDate": "2026-06-01"
  }
}
```

**Response**

```json
{
  "employeeId": 1,
  "employeeName": "Ravi Kumar",
  "department": "Platform Engineering",
  "idCard": {
    "cardId": 1,
    "cardNumber": "WMT-1793335368",
    "issueDate": "2026-06-01"
  }
}
```

**Tests**

- ✅ 200 OK
- ✅ department updated
- ✅ same card updated in place
- ✅ issue date updated

### 5. Get All ID Cards (with owner)

`GET /idcards` → **200 OK**

**Response**

```json
[
  {
    "cardId": 1,
    "cardNumber": "WMT-1793335368",
    "issueDate": "2026-06-01",
    "employee": {
      "employeeId": 1,
      "employeeName": "Ravi Kumar",
      "department": "Platform Engineering"
    }
  }
]
```

**Tests**

- ✅ 200 OK
- ✅ returns a list

### 6. Get ID Card by ID (belongs to one employee)

`GET /idcards/1` → **200 OK**

**Response**

```json
{
  "cardId": 1,
  "cardNumber": "WMT-1793335368",
  "issueDate": "2026-06-01",
  "employee": {
    "employeeId": 1,
    "employeeName": "Ravi Kumar",
    "department": "Platform Engineering"
  }
}
```

**Tests**

- ✅ 200 OK
- ✅ card belongs to the employee

### 7. Duplicate Card Number (409)

`POST /employees` → **409 Conflict**

**Request body**

```json
{
  "employeeName": "Priya Sharma",
  "department": "HR",
  "idCard": {
    "cardNumber": "WMT-1793335368",
    "issueDate": "2026-02-10"
  }
}
```

**Response**

```json
{
  "type": "about:blank",
  "title": "Duplicate Resource",
  "status": 409,
  "detail": "ID card number 'WMT-1793335368' is already assigned to another employee",
  "instance": "/employees"
}
```

**Tests**

- ✅ 409 Conflict

### 8. Employee without ID Card (400)

`POST /employees` → **400 Bad Request**

**Request body**

```json
{
  "employeeName": "No Card",
  "department": "HR"
}
```

**Response**

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/employees",
  "errors": {
    "idCard": "every employee must have exactly one ID card"
  }
}
```

**Tests**

- ✅ 400 Bad Request
- ✅ idCard error reported

### 9. Delete Employee (cascades to ID Card)

`DELETE /employees/1` → **204 No Content**

**Response**

_(empty body)_

**Tests**

- ✅ 204 No Content

### 10. Get Deleted Employee (404)

`GET /employees/1` → **404 Not Found**

**Response**

```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Employee not found with id 1",
  "instance": "/employees/1"
}
```

**Tests**

- ✅ 404 Not Found

### 11. Get Deleted ID Card (404 - removed by cascade)

`GET /idcards/1` → **404 Not Found**

**Response**

```json
{
  "type": "about:blank",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "ID card not found with id 1",
  "instance": "/idcards/1"
}
```

**Tests**

- ✅ 404 Not Found

## MySQL tables

```text
Sample API calls: POST /employees (x2), PUT /employees/1

mysql> SHOW CREATE TABLE employees;
CREATE TABLE `employees` (
  `employee_id` bigint NOT NULL AUTO_INCREMENT,
  `department` varchar(50) NOT NULL,
  `employee_name` varchar(100) NOT NULL,
  `card_id` bigint NOT NULL,
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `UK53r4tdp16o3p5dedvb2mklols` (`card_id`),
  CONSTRAINT `FK7912m2tdkcosplte6c29b5db9` FOREIGN KEY (`card_id`) REFERENCES `id_cards` (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

mysql> SHOW CREATE TABLE id_cards;
CREATE TABLE `id_cards` (
  `card_id` bigint NOT NULL AUTO_INCREMENT,
  `card_number` varchar(30) NOT NULL,
  `issue_date` date NOT NULL,
  PRIMARY KEY (`card_id`),
  UNIQUE KEY `UKjqi5ml7valx7j8fuyxsqi8rq1` (`card_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

mysql> SELECT * FROM id_cards;
+---------+--------------+------------+
| card_id | card_number  | issue_date |
+---------+--------------+------------+
|       1 | WMT-EMP-1001 | 2026-06-01 |
|       2 | WMT-EMP-1002 | 2026-02-10 |
+---------+--------------+------------+

mysql> SELECT * FROM employees;
+-------------+----------------------+---------------+---------+
| employee_id | department           | employee_name | card_id |
+-------------+----------------------+---------------+---------+
|           1 | Platform Engineering | Ravi Kumar    |       1 |
|           2 | HR                   | Priya Sharma  |       2 |
+-------------+----------------------+---------------+---------+

mysql> SELECT e.employee_id, e.employee_name, e.department, c.card_id, c.card_number, c.issue_date FROM employees e JOIN id_cards c ON c.card_id = e.card_id;
+-------------+---------------+----------------------+---------+--------------+------------+
| employee_id | employee_name | department           | card_id | card_number  | issue_date |
+-------------+---------------+----------------------+---------+--------------+------------+
|           1 | Ravi Kumar    | Platform Engineering |       1 | WMT-EMP-1001 | 2026-06-01 |
|           2 | Priya Sharma  | HR                   |       2 | WMT-EMP-1002 | 2026-02-10 |
+-------------+---------------+----------------------+---------+--------------+------------+

After DELETE /employees/2: the employee AND its ID card are removed (cascade)

mysql> SELECT * FROM employees;
+-------------+----------------------+---------------+---------+
| employee_id | department           | employee_name | card_id |
+-------------+----------------------+---------------+---------+
|           1 | Platform Engineering | Ravi Kumar    |       1 |
+-------------+----------------------+---------------+---------+

mysql> SELECT * FROM id_cards;
+---------+--------------+------------+
| card_id | card_number  | issue_date |
+---------+--------------+------------+
|       1 | WMT-EMP-1001 | 2026-06-01 |
+---------+--------------+------------+

```

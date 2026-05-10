# Banking System

A Java web-based banking application built with Jakarta Servlet, JDBC, PostgreSQL, and Maven.

## Tech Stack

- Java 17
- Maven (WAR packaging)
- Jakarta Servlet 6
- PostgreSQL
- Gson

## Features

- User registration, login, logout, and profile overview
- Account listing and account creation
- Deposit, withdraw, transfer, and transaction history
- Loan application and loan listing
- Reports:
  - Account statement download (CSV)
  - Summary report (JSON)

## Project Structure

```text
src/main/java/com/bank
  config/
  dao/
  exception/
  filter/
  model/
  service/
  servlet/

src/main/resources/
  db.properties

src/main/webapp/
  index.html
  static/
  WEB-INF/web.xml

database/
  init/01_schema.sql
  seed-demo.sql
```

## Prerequisites

- JDK 17+
- Maven 3.8+
- PostgreSQL 16+ (or compatible)
- Servlet container (Tomcat 10.1+ recommended)

## Database Setup

1. Create a database:

```sql
CREATE DATABASE banking_db;
```

2. Run schema script:

- `database/init/01_schema.sql`

3. (Optional) Load demo data:

- `database/seed-demo.sql`

4. Create your local DB config file at:

- `src/main/resources/db.properties`

Example:

```properties
db.url=jdbc:postgresql://localhost:5432/banking_db
db.username=postgres
db.password=your_password
```

Note: `db.properties` is intentionally ignored in `.gitignore` so credentials are not uploaded.

## Build

```bash
mvn clean package
```

Generated artifact:

- `target/banking-system.war`

## Run

Deploy `target/banking-system.war` to your servlet container (for example, Tomcat).

Typical local URL:

- `http://localhost:8080/banking-system/`

## API Routes

Base context assumes `/banking-system`.

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/profile`

### Accounts

- `GET /api/accounts`
- `POST /api/accounts`

### Transactions

- `GET /api/transactions?accountId={id}&from=YYYY-MM-DD&to=YYYY-MM-DD`
- `POST /api/transactions/deposit`
- `POST /api/transactions/withdraw`
- `POST /api/transactions/transfer`

### Loans

- `GET /api/loans`
- `POST /api/loans/apply`

### Reports

- `GET /api/reports/statement?accountId={id}&from=YYYY-MM-DD&to=YYYY-MM-DD` (CSV download)
- `GET /api/reports/summary`

## Authentication Notes

- Session-based authentication is used.
- `AuthFilter` protects `/api/*` routes (except routes it allows internally).
- Keep cookies enabled when testing authenticated flows.

## GitHub Upload Checklist

1. Ensure `src/main/resources/db.properties` is not tracked.
2. Commit source files, SQL scripts, and documentation.
3. Do not commit build outputs (`target/`, `bin/`, `out/`) or IDE metadata.

## License

Add a license file (for example, MIT) if you plan to make this repository public.

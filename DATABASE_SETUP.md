# PostgreSQL Setup

This project now includes a ready-to-run PostgreSQL setup for local development.

## Default local database settings

The application is configured to use:

- Database: `banking_db`
- Host: `localhost`
- Port: `5432`
- Username: `postgres`
- Password: `postgres`

Those values match `src/main/resources/db.properties` and `docker-compose.yml`.

## Option 1: Fastest setup with Docker

1. Install Docker Desktop.
2. From the project root, run:

```powershell
docker compose up -d
```

3. PostgreSQL will start and automatically run:
   - `database/init/01_schema.sql`
4. Optional demo data:

```powershell
docker exec -i banking-system-postgres psql -U postgres -d banking_db < database/seed-demo.sql
```

5. Start Tomcat and run the app.

## Option 2: Manual PostgreSQL installation

1. Install PostgreSQL 16 or newer.
2. Create the database:

```sql
CREATE DATABASE banking_db;
```

3. Open `psql` or pgAdmin and run:
   - `database/init/01_schema.sql`
4. Optional: run `database/seed-demo.sql`
5. Make sure your PostgreSQL username/password match `src/main/resources/db.properties`.

## How someone from GitHub can use it

1. Clone the repo.
2. Run `docker compose up -d`.
3. Start the app in IntelliJ/Tomcat.
4. Open `http://localhost:8080/banking-system/`.
5. Register a new user, or load the optional demo seed and use:
   - Email: `demo@vaultx.local`
   - Password: `password123`

## Notes

- The app uses plain JDBC with the PostgreSQL driver already declared in `pom.xml`.
- The schema file is idempotent, so re-running it is safe.
- If port `5432` is already in use on your machine, update both `docker-compose.yml` and `src/main/resources/db.properties`.

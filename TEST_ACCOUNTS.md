# Development test accounts

Development-only fixtures are loaded with `SPRING_PROFILES_ACTIVE=dev`. All
accounts below use `Test1234!`; the password is encoded through the normal
Spring `PasswordEncoder` when seeded.

| Role | Email | Organisation | Department | Purpose |
| --- | --- | --- | --- | --- |
| PLATFORM_OWNER | owner@projectum.local | Platform | — | Cross-tenant platform visibility |
| PLATFORM_ADMIN | platform.admin@projectum.local | Platform | — | Platform administration |
| ORG_ADMIN | org.admin@projectum.local | ERCOPAC Demo | Operations | Organisation administration |
| GENERAL_MANAGER | gm@projectum.local | ERCOPAC Demo | Operations | Portfolio, projects, capacity and GM dashboard |
| DEPARTMENT_MANAGER | department.manager@projectum.local | ERCOPAC Demo | Engineering | Department timeline and workload |
| EMPLOYEE | employee@projectum.local | ERCOPAC Demo | Engineering | Personal assigned tasks and availability |
| SALES_MANAGER | sales.manager@projectum.local | ERCOPAC Demo | Sales | Ticket and CRM workflows |
| CLIENT | client@projectum.local | ERCOPAC Demo | — | Client-scoped tickets; internal notes are hidden |

Tenant-isolation counterparts are also seeded in **Projectum Test Industries**:
`org.admin@testindustries.local`, `gm@testindustries.local`,
`department.manager@testindustries.local`, `employee@testindustries.local`,
`sales.manager@testindustries.local`, and `client@testindustries.local`.

Run locally:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:SPRING_DATASOURCE_PASSWORD = '<local-postgres-password>'
mvn spring-boot:run
```

The seeder uses stable organisation codes, email addresses, project codes,
WBS codes, ticket numbers, and holiday dates. Re-running it updates development
accounts and fills only missing fixtures; it does not delete user data.

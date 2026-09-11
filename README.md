# Barber

## Start the local site

Barber is deployed alongside the other Eclipse projects on the shared Tomcat server at port 8081. Double-click `Start-Barber.cmd` to verify the shared server is running and open Barber.

Java 21 + Jakarta Servlet/JSP MVC + PostgreSQL, using Tomcat 11. There is no Maven or Gradle build.

## Local URL

`http://localhost:8081/Barber/`

Start or restart **Tomcat v11.0 Server at localhost** from Eclipse's Servers view. Barber, ClinicSystem, and AgriCoop are modules on that same server. Port 8082 is not used by Barber.

`Start-Barber.ps1` remains available only as an isolated-server diagnostic fallback; it is not the normal launcher for this Eclipse deployment.

## Database and administrator setup

1. Copy `database/database.properties.example` to `.local/database.properties`.
2. Set your local PostgreSQL username and password there. The URL must be `jdbc:postgresql://localhost:5432/barber` (adjust the PostgreSQL port if needed). The web port is NOT the database port.
3. Run `powershell -NoProfile -ExecutionPolicy Bypass -File .\Setup-Database.ps1`.
4. Setup creates the local `barber` database if absent, adds tables and sample service/add-on entries, and creates the first administrator if none exists. It never drops existing database tables or records. The account needs database-create permission for first setup.
5. The first administrator email is `admin@barber.local`. Its generated password is in `.local/admin-login.txt`; keep this file private. Credentials are never served by Tomcat or printed by setup. Existing administrator passwords are not reset when setup runs again.
6. Restart the shared Eclipse Tomcat after changing database settings. Its launch configuration includes `-Dbarber.config=C:\ALvin\Barber\.local\database.properties`.

Alternatively provide `BARBER_DB_URL`, `BARBER_DB_USER`, and `BARBER_DB_PASSWORD` environment variables. They override the configuration file. Do not commit secrets; `.local` is ignored by Git.

## First use

Sign in as the administrator at `/Barber/login`. Add barber accounts under **Barbers**, then configure their **Schedules**. There are no default barber passwords. Public customers register their own accounts. Barbers and administrators use the same login page and receive the appropriate dashboard. Registration creates customer accounts only.

The included catalog is starter data. Change names, prices and durations before taking real appointments. The shop timezone is **Asia/Manila**, booking horizon is **90 days**, and slots begin every **30 minutes** from each barber's opening time. Each service and selected add-on contributes to appointment duration and price. Payment is at the shop; there is no online payment gateway.

## Features

- Public Salone pages with gray primary and muted-gold secondary colors.
- Customer registration, login, logout and personal appointment history.
- Available slots based on active barbers, weekly hours, exceptions and existing bookings.
- Booking with optional add-ons, server-calculated duration and price, and concurrent-booking locks.
- Customer cancellation of their own upcoming confirmed bookings.
- Waitlist entries for fully booked times, FIFO in-app offers after cancellation, and customer claim flow.
- Atomic recurring appointment series (2-12 occurrences, 1-12 week intervals) within the 90-day horizon.
- One-click rebooking from appointment history, preserving the previous barber, service and add-ons.
- Administrator-managed barber specialties, biographies and profile photos, shown in booking choices and the public team page.
- Digital loyalty progress derived from completed visits (five visits toward the next reward milestone).
- Barber dashboard restricted to assigned appointments.
- Administrator management of services, add-ons, barber accounts, weekly hours and date exceptions.
- Confirmed to in-progress to completed workflow, or cancellation before service starts.
- Appointment status history, profile identity and sign-out menu.
- Dashboard cards, charts, appointment table, next appointments and calendar using the original dashboard template components. Summary figures cover the latest 250 records, not an accounting ledger. The shift checklist is intentionally per-page, not a persisted task system.
- Salted PBKDF2 password hashes, server-side role and ownership checks, CSRF protection, session rotation at login, session expiry and a basic sign-in attempt limit.

## Original templates

The actual HTML/CSS/JS/image files are used, not recreated as look-alikes. Original files remain untouched in `sourceTemplate`.

- Public pages originate from `sourceTemplate/Salone/*.html`; new account and booking forms use its contact-page layout.
- Dashboard originates from `sourceTemplate/bootstrap-admin-template-free/bootstrap-admin-template-free/index.html`; original layout, classes, assets and blue dashboard colors are retained. Demo content is replaced with real application controls and metrics.
- `Prepare-Views.ps1` makes repeatable mechanical adaptations into JSP pages. It changes navigation, app branding and inserts MVC view fragments. Public colors are a separate `barber-theme.css` layer. To adjust app forms, edit the `.jspf` fragments. Generated `.jsp` edits are overwritten by the next build.
- Dashboard JavaScript has a guard for its original sample charts; app charts use live booking data.
- Both HTML Codex licenses and attribution links are preserved. Review image rights before commercial deployment.
- Public template photographs and sample marketing copy, contact details, testimonials and counters are retained as requested. They are NOT verified shop information. Replace sample business content before launch. Template contact/newsletter/social controls are not connected to backend services.
- jQuery and Bootstrap JavaScript are vendored from the template's original CDN URLs. Fonts and icon stylesheets still use the original external providers; an internet connection is needed for those resources.

## Eclipse import

Use Eclipse IDE for Enterprise Java and Web Developers. Choose **File > Import > General > Existing Projects into Workspace** and select `C:\ALvin\Barber`. Leave **Copy projects into workspace** unchecked to work here.

Configure JDK 21 and JavaSE-21. Select the existing **Apache Tomcat v11.0** runtime in **Project Properties > Targeted Runtimes**. If servlet imports remain unresolved, add **Java Build Path > Libraries > Server Runtime**. Add Barber to **Tomcat v11.0 Server at localhost**, publish it, and use **Run As > Run on Server**. The shared server uses port 8081.

## Layout and verification

- `src/com/barber/controller`: HTTP controllers and security headers.
- `src/com/barber/model`: user identity model.
- `src/com/barber/dao`: JDBC access.
- `src/com/barber/service`: booking, pricing, schedules and administration rules.
- `src/com/barber/util`: password hashing, output escaping and database setup.
- `WebContent/WEB-INF/views`: template-based JSP views; not directly accessible by browser URL.
- `database/schema.sql`: additive schema and sample catalog.
- `Build.ps1`: javac compilation and view preparation.
- `Test.ps1`: unit checks for hashing, escaping and validation.
- `tests/browser-check.cjs`: public pages, phone overflow and basic access-control smoke checks (requires Playwright and Edge; dependency path is local to this computer).
- `tests/workflow-check.cjs`: database-backed registration, login, roles, administration, concurrent booking, price tampering, ownership, staff status changes, cancellation and exceptions. Requires completed database setup and the initial administrator credential file. Creates unique QA fixtures and removes only that run's fixtures in cleanup. Run `Test.ps1` first to compile the cleanup helper.

## Verification completed

Java compilation, JSP compilation, unit tests, eight public-page smoke checks and the database-backed workflow suite passed locally. The phone layout was checked at 400 x 861, and the dashboard was checked at desktop and tablet sizes. QA records were cleaned up; only the initial administrator and starter catalog remain. Add your real barber accounts and schedules to enable customer availability.

## Before public deployment

This is a local development application. Configure HTTPS and secure cookies, a least-privilege database account, a managed connection pool, backup/restore, monitoring, privacy/retention policies, stronger distributed rate limiting and an account recovery process before exposing it publicly. Verify database-backed workflows against your configured database. Schedule edits affect new bookings; existing booked times stay unchanged. Customer lists and reports are limited to the newest 250 appointments.
# Deployment

For Git upload, Render deployment, and PostgreSQL transfer instructions, see [DEPLOYMENT.md](DEPLOYMENT.md).

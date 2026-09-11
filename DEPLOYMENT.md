# Barber: Git, Render, and Aiven PostgreSQL

## Before uploading

The repository intentionally keeps Eclipse files (`.project`, `.classpath`, and `.settings`) but excludes local credentials, compiled output, logs, and database dumps. Never commit `.local/database.properties`.

Run locally before the first push:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\Test.ps1
git status --short
```

## Upload to Git

```powershell
git init -b main
git add .
git status
git commit -m "Prepare Barber for deployment"
git remote add origin YOUR_REPOSITORY_URL
git push -u origin main
```

Review `git status` before committing. No password, `.env`, dump, or `.local` file should appear.

## Create the Render web service

In Render, create a **Blueprint** from this repository. `render.yaml` creates only the Docker web service. The app is deployed at the Render service root, and `/health` is used for health checks.

When prompted, set `BARBER_DATABASE_URL` to the Aiven PostgreSQL service URI for the `barber` database. It must include `?sslmode=require`. Keep this value in Render only; never commit it. Local Eclipse/Tomcat continues to use `.local/database.properties`.

## Transfer the local PostgreSQL data to Aiven

Install PostgreSQL client tools, then run:

```powershell
.\deploy\Export-Local-Database.ps1
.\deploy\Import-To-Aiven.ps1 -AivenDatabaseUrl 'PASTE_AIVEN_SERVICE_URI'
```

Get the service URI from Aiven's **Overview > Quick connect** area and select a PostgreSQL URI for the `barber` database. The import uses `--clean --if-exists`; run it only when you intend to replace the target database objects. The dump file is ignored and must not be committed.

After import, redeploy or restart the web service and open `/health`.

# Barber: Git, Render, and PostgreSQL transfer

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

## Create the Render services

In Render, create a **Blueprint** from this repository. `render.yaml` creates the Docker web service and its own PostgreSQL database. The app is deployed at the Render service root, and `/health` is used for health checks.

The Blueprint injects the private PostgreSQL connection string as `BARBER_DATABASE_URL`. Local Eclipse/Tomcat continues to use `.local/database.properties`.

## Transfer the local PostgreSQL data

Install PostgreSQL client tools, then run:

```powershell
.\deploy\Export-Local-Database.ps1
.\deploy\Import-To-Render.ps1 -ExternalDatabaseUrl 'PASTE_RENDER_EXTERNAL_DATABASE_URL'
```

Get the **External Database URL** from the Render database Connect menu. The import uses `--clean --if-exists`; run it only when you intend to replace the target database objects. The dump file is ignored and must not be committed.

After import, redeploy or restart the web service and open `/health`.

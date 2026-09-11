@echo off
setlocal
powershell.exe -NoProfile -Command "try { $r = Invoke-WebRequest 'http://localhost:8081/Barber/health' -UseBasicParsing -TimeoutSec 5; if ($r.StatusCode -ne 200) { exit 1 } } catch { exit 1 }"
if errorlevel 1 (
  echo.
  echo Barber is not running on the shared Eclipse Tomcat at port 8081.
  echo Start or restart "Tomcat v11.0 Server at localhost" in Eclipse, then publish Barber.
  pause
  exit /b 1
)
start "" "http://localhost:8081/Barber/"
endlocal

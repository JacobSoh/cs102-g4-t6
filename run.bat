@echo off
setlocal EnableExtensions

set "PORT=3000"
set "RULE_NAME=Splendor 3000"

for %%I in ("%~dp0.") do set "REPO_DIR=%%~fI"

set "LAUNCH_MODE=WINDOWS"
if defined WSL_DISTRO_NAME set "LAUNCH_MODE=WSL_INTEROP"
if defined WSL_INTEROP set "LAUNCH_MODE=WSL_INTEROP"

echo [run.bat] Launch mode: %LAUNCH_MODE%
echo [run.bat] Repo dir: %REPO_DIR%

where netsh >nul 2>nul
if errorlevel 1 (
    echo [run.bat] ERROR: netsh.exe is not available on PATH.
    exit /b 1
)

where wsl >nul 2>nul
if errorlevel 1 (
    echo [run.bat] ERROR: wsl.exe is not available on PATH.
    exit /b 1
)

echo [run.bat] Resolving WSL IP address...
set "WSL_IP="
for /f "usebackq delims=" %%I in (`wsl.exe sh -lc "hostname -I | awk '{print $1}'" 2^>nul`) do set "WSL_IP=%%I"
if not defined WSL_IP (
    echo [run.bat] ERROR: failed to resolve WSL IP address.
    exit /b 1
)

echo [run.bat] WSL IP: %WSL_IP%
echo [run.bat] Applying Windows portproxy for TCP %PORT%...

netsh interface portproxy delete v4tov4 listenport=%PORT% listenaddress=0.0.0.0 >nul 2>nul
netsh interface portproxy add v4tov4 listenport=%PORT% listenaddress=0.0.0.0 connectport=%PORT% connectaddress=%WSL_IP%
if errorlevel 1 (
    echo [run.bat] ERROR: failed to configure portproxy.
    exit /b 1
)

echo [run.bat] Applying Windows firewall rule "%RULE_NAME%"...
netsh advfirewall firewall delete rule name="%RULE_NAME%" >nul 2>nul
netsh advfirewall firewall add rule name="%RULE_NAME%" dir=in action=allow protocol=TCP localport=%PORT%
if errorlevel 1 (
    echo [run.bat] ERROR: failed to configure firewall rule.
    exit /b 1
)

set "WSL_REPO_DIR="
for /f "usebackq delims=" %%I in (`wsl.exe wslpath -a "%REPO_DIR%" 2^>nul`) do set "WSL_REPO_DIR=%%I"
if not defined WSL_REPO_DIR (
    echo [run.bat] ERROR: failed to convert repo path to a WSL path.
    exit /b 1
)

echo [run.bat] Switching to WSL path: %WSL_REPO_DIR%
echo [run.bat] Running: mvn clean javafx:run

wsl.exe sh -lc "cd \"%WSL_REPO_DIR%\" && mvn clean javafx:run"
set "RUN_EXIT=%ERRORLEVEL%"
if not "%RUN_EXIT%"=="0" (
    echo [run.bat] ERROR: WSL Maven run failed with exit code %RUN_EXIT%.
    exit /b %RUN_EXIT%
)

echo [run.bat] WSL Maven run completed successfully.
exit /b 0

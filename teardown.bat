@echo off
setlocal EnableExtensions

set "PORT=3000"
set "RULE_NAME=Splendor 3000"

echo [teardown.bat] Removing Windows portproxy for TCP %PORT%...
netsh interface portproxy delete v4tov4 listenport=%PORT% listenaddress=0.0.0.0
if errorlevel 1 (
    echo [teardown.bat] WARNING: portproxy delete may have failed or no mapping existed.
)

echo [teardown.bat] Removing Windows firewall rule "%RULE_NAME%"...
netsh advfirewall firewall delete rule name="%RULE_NAME%"
if errorlevel 1 (
    echo [teardown.bat] WARNING: firewall rule delete may have failed or no rule existed.
)

echo [teardown.bat] Teardown complete.
exit /b 0

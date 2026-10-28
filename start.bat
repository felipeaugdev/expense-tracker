@echo off
title Expense Tracker
echo Starting Expense Tracker...
echo.
echo Keep this window open while you use the app.
echo Closing this window will stop the application.
echo.

docker compose up -d

echo Waiting for the app to be ready...
:wait
timeout /t 1 >nul
curl -s -o nul -w "%%{http_code}" http://localhost:8080 | findstr /r "200 302 403" >nul
if errorlevel 1 goto wait
echo Ready!

start http://localhost:8080

echo.
echo App is running at http://localhost:8080
echo.
echo Press any key (or close this window) to stop the app...
pause >nul

echo.
echo Stopping...
docker compose down
echo Done.

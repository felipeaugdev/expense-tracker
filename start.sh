#!/bin/bash
echo "Starting Expense Tracker..."
echo
echo "Keep this terminal open while you use the app."
echo "Closing this terminal (or pressing Ctrl+C) will stop the application."
echo

sudo docker compose up -d

echo -n "Waiting for the app to be ready"
until curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 | grep -q "200\|302\|403"; do
  echo -n "."
  sleep 1
done
echo " ready!"

xdg-open http://localhost:8080 2>/dev/null || open http://localhost:8080 2>/dev/null

echo
echo "App is running at http://localhost:8080"
echo
echo "Press Enter to stop the app..."
read

echo
echo "Stopping..."
sudo docker compose down
echo "Done."

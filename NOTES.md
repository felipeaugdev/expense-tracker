# Developer Notes

## Project Overview
This expense tracker was built primarily with Java. It uses Spring Boot and JPA for business logic and data persistence, exposing REST endpoints that feed a JavaScript/HTML front-end interface.

## Month 1 Recap
* **Initial Concept:** This project originally started as a simple CLI application. My purpose was to learn and demonstrate back-end logic, as well as some programming concepts like loose coupling, separation of responsibilities, etc.
* **Overhaul:** After adding extra functionality and migrating a lot of the verbose logic to Spring Boot, I started seeing this application as more than just an exercise in back-end programming. That's when I decided to simplify the user experience by containerizing the whole thing in Docker and even adding a Web UI.
* **Current State:** The app now handles CRUD operations through a visual interface. All the information is available at a glance, including an expense table and a category chart (using MariaDB and Chart.js). Data flows from the front-end UI, through REST endpoints, down to the repository layer.
* **AI Usage Note:** Built with AI assistance for code implementation and boilerplate generation. But the architecture, data modeling, feature scope and debugging are directed manually.

---

## Dev Log

### [September 14–16, 2026] - Web UI Implementation
* **What changed:** Added a visual interface that handles adding, deleting, viewing and filtering expenses.
* **Why:** The back-end functionality was complete when I finished the REST endpoints, but the program didn't feel like a real expense tracker without a visual UI. My main priority was making all crucial information accessible in a single modern dashboard.
* **Future Plans:** Add a currency selector, language switcher and dark mode toggle.

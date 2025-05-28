# 4-Quadrant Task Matrix Application

## Overview

This application is a task management tool designed to help users organize and prioritize their tasks using the Eisenhower Matrix (also known as the 4-Quadrant Matrix). Tasks are categorized based on their importance and urgency, allowing for effective decision-making and productivity.

**Core Features:**
*   **CRUD Operations:** Create, Read, Update, and Delete tasks.
*   **Quadrant Visualization:** Tasks are displayed in a 4-quadrant view (Important/Urgent, Important/Not Urgent, Not Important/Urgent, Not Important/Not Urgent).
*   **Filtering:** Filter tasks by label, due date range, importance, urgency, or quadrant.
*   **Sorting:** Sort tasks by various fields (ID, title, due date, label, importance, urgency) in ascending or descending order.
*   **Web Interface:** User-friendly web interface built with Thymeleaf.
*   **REST API:** A RESTful API for programmatic access to task data.

**Technologies Used:**
*   Java (Version 17)
*   Spring Boot (Version 3.2.x)
*   Spring Data JPA
*   Spring Web
*   H2 Database (In-memory)
*   Thymeleaf
*   Lombok
*   Apache Maven (for build and dependency management)
*   Bean Validation (for data validation)

## Prerequisites

Before you begin, ensure you have the following installed:
*   JDK 17 or later (e.g., OpenJDK, Oracle JDK)
*   Apache Maven (e.g., version 3.6.x or later)

## Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Building the Application

1.  Clone the repository (if applicable) or navigate to the project's root directory.
2.  Open a terminal or command prompt in the root directory.
3.  Run the following Maven command to clean the project and build the JAR file:
    ```bash
    mvn clean install
    ```
    This will compile the source code, run tests, and package the application into a JAR file located in the `target` directory.

### Running the Application

Once the application is successfully built, you can run it in one of the following ways:

**Option 1: Using the Spring Boot Maven Plugin (Recommended for development)**
```bash
mvn spring-boot:run
```
This command starts the embedded Tomcat server, and the application will be accessible.

**Option 2: Running the Executable JAR**
```bash
java -jar target/taskmatrix-0.0.1-SNAPSHOT.jar
```
Make sure to replace `taskmatrix-0.0.1-SNAPSHOT.jar` with the actual name of the JAR file generated in your `target` directory if it differs (check your `pom.xml` for `artifactId` and `version`).

The application will start, and by default, it will be running on port `8080`.

## Accessing the Application

*   **Main Web Application:**
    Open your web browser and navigate to: `http://localhost:8080/`

*   **H2 Database Console:**
    The application uses an in-memory H2 database. You can access its console to view data or run queries:
    *   URL: `http://localhost:8080/h2-console`
    *   **Settings:**
        *   JDBC URL: `jdbc:h2:mem:taskmatrixdb` (This should be pre-filled)
        *   User Name: `sa`
        *   Password: `password`
    Click "Connect" to access the console.

## API Endpoints Overview

The application provides a REST API for managing tasks. All endpoints are prefixed with `/api/tasks`.

*   **`GET /api/tasks`**:
    *   Retrieves a list of all tasks.
    *   Supports filtering by `label`, `dueDateBefore`, `dueDateAfter`, `importance`, `urgency`, `quadrant`.
    *   Supports sorting by `sortBy` (e.g., `title`, `dueDate`) and `sortDir` (`asc`, `desc`).
    *   Example: `/api/tasks?importance=IMPORTANT&sortBy=dueDate&sortDir=desc`

*   **`POST /api/tasks`**:
    *   Creates a new task.
    *   Request body should contain the task details in JSON format.

*   **`GET /api/tasks/{id}`**:
    *   Retrieves a specific task by its ID.

*   **`PUT /api/tasks/{id}`**:
    *   Updates an existing task identified by its ID.
    *   Request body should contain the updated task details in JSON format.

*   **`DELETE /api/tasks/{id}`**:
    *   Deletes a specific task by its ID.

## Project Structure

The project follows a standard Spring Boot application structure:

*   `src/main/java/com/example/taskmatrix/`
    *   `TaskmatrixApplication.java`: Main Spring Boot application class.
    *   `entity/` (Implicitly `Task.java`, `Importance.java`, `Urgency.java`, `Quadrant.java` are entities/enums): Contains JPA entities and enums.
    *   `repository/` (`TaskRepository.java`): Spring Data JPA repositories.
    *   `service/` (`TaskService.java`): Business logic layer.
    *   `controller/` (`TaskController.java`, `PageController.java`): Spring MVC controllers for REST API and web pages.
    *   `exception/` (`ResourceNotFoundException.java`): Custom exceptions.
    *   `specification/` (Implicitly `TaskSpecification.java`): For building dynamic JPA queries.
*   `src/main/resources/`
    *   `application.properties`: Application configuration (database, H2 console).
    *   `static/`: Static web resources (CSS, JavaScript).
        *   `css/style.css`: Stylesheet for the web interface.
    *   `templates/`: Thymeleaf templates for web pages (`index.html`, `add-task.html`, `edit-task.html`).
*   `src/test/java/com/example/taskmatrix/`: Contains unit and integration tests.
    *   `TaskServiceTest.java`: Unit tests for the service layer.
    *   `TaskControllerTest.java`: Integration tests for the REST controller.
*   `pom.xml`: Maven project configuration file, including dependencies and build settings.

---
This README provides a comprehensive guide for users and developers to understand, build, run, and use the Task Matrix application.

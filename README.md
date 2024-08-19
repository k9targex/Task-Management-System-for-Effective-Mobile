# Task Management System

## Overview

This Task Management System is a Java-based application built with Spring Boot, designed to manage tasks, users, and their roles. It allows users to create, edit, delete, and view tasks. Each task has a title, description, status (e.g., "pending", "in progress", "completed"), and priority (e.g., "high", "medium", "low"). The system also supports user authentication and authorization via JWT tokens.

## Features

- **User Authentication and Authorization**: Users can register, sign in, and manage their tasks securely using JWT tokens.
- **Task Management**: Users can create, edit, delete, and view their tasks. They can also assign tasks to other users and manage task statuses.
- **Commenting**: Users can add comments to their tasks.
- **Filtering and Pagination**: Tasks can be filtered by user id, status, and priority, with support for pagination.
- **OpenAPI and Swagger Documentation**: The API is documented using OpenAPI and is accessible via Swagger UI.

## Technologies Used

- **Java 17+**
- **Spring Boot**
- **Spring Security**
- **PostgreSQL**
- **JWT for Authentication**
- **Swagger/OpenAPI for API Documentation**

## Endpoints

### Authentication

- **`POST /auth/signup`** - Register a new user.
- **`POST /auth/signin`** - Sign in an existing user.

### Task Management

#### For Authors
- **`POST /users/tasks`** - Create a new task for the authors.
- **`POST /users/tasks/{taskId}/performers/{performerId}`** - Add performer for the task.
- **`PATCH /users/tasks/edit/{taskId}`** - Update details of an existing task.
- **`DELETE /users/tasks/{taskId}`** - Delete an existing task by its ID.
  

#### For Performers
- **`PATCH /users/tasks/status/{taskId}`** - Update the status of a specific task.

#### For Authors and Performers
- **`GET /users`** - Retrieve all authors.
- **`GET /tasks`** - Retrieve a list of all tasks.
- **`GET /users/tasks`** - Retrieve tasks for the authenticated user.
- **`GET /users/tasks/user/{userId}?page={page}&size={size}&priority={LOW,MEDIUM,HIGH}&status={PENDING,IN_PROGRESS,COMPLETED}`** - Retrieve tasks for a specific user with optional filters.
- **`GET /users/tasks/comments/{taskId}`** - Retrieve comments for the task.
- **`POST /users/tasks/comments/{taskId}`** - Add comment for your task.

### Comments

- **`POST /users/tasks/comments/{taskId}`** - Add a comment to a specific task.
- **`GET /users/tasks/comments/{taskId}`** - Retrieve all comments for a specific task.

## Setup Instructions

### Prerequisites

- Java 17+
- Maven 3.9.6 and higher
- PostgreSQL

## Building and Running the Application 

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/task-management-system.git
   cd task-management-system

2. Set password and username for postgreSQL in file ```.inv``` :
   ```bash
   POSTGRES_DB=TaskManagement_db
   POSTGRES_USER={your_username}
   POSTGRES_PASSWORD={your_password}
   ```

3. Build application:
   ```bash
   mvn clean package -DskipTests
   ```

### Run in container
1. Build and start the Docker containers:
   ```bash
   docker-compose up --build
   ```

### Run manually

1. Switch postgresql path in ```application.properties```:
   ```
   #for docker container
   spring.datasource.url = jdbc:postgresql://postgresql:5432/TaskManagement_db
   ```
   ```
   #for running manual
   #spring.datasource.url=jdbc:postgresql://localhost:5432/TaskManagement
   ```

2. Run application
   ```
   java -jar target/TaskManagement-0.0.1-SNAPSHOT.jar
   ```
3. Access the Swagger UI for API documentation at:
   ```bash
   http://localhost:8080/swagger-ui/index.html#/
   ```
   
   

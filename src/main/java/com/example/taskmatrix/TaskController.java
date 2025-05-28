package com.example.taskmatrix;

import com.example.taskmatrix.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.taskmatrix.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing tasks.
 * Provides REST endpoints for CRUD operations on tasks.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    /**
     * Constructs a TaskController with the given TaskService.
     * @param taskService The service for task management.
     */
    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Creates a new task.
     * @param task The task to create, validated from the request body.
     * @return The created task with HTTP status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        try {
            Task createdTask = taskService.createTask(task);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // Or return a custom error response
        }
    }

    /**
     * Retrieves a task by its ID.
     * @param id The ID of the task to retrieve.
     * @return The task if found, or HTTP status 404 (Not Found) if not present.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all tasks, with optional filtering and sorting.
     *
     * @param label Optional filter by label.
     * @param dueDateBefore Optional filter for due date before or on this date.
     * @param dueDateAfter Optional filter for due date after or on this date.
     * @param importance Optional filter by importance.
     * @param urgency Optional filter by urgency.
     * @param quadrant Optional filter by quadrant.
     * @param sortBy Field to sort by (e.g., "dueDate", "title"). Defaults to "id".
     * @param sortDir Sort direction ("asc" or "desc"). Defaults to "asc".
     * @return A list of tasks matching the criteria, sorted as specified.
     */
    @GetMapping
    public List<Task> getAllTasks(
            @RequestParam(required = false) String label,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateAfter,
            @RequestParam(required = false) Importance importance,
            @RequestParam(required = false) Urgency urgency,
            @RequestParam(required = false) Quadrant quadrant,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        return taskService.getAllTasks(label, dueDateBefore, dueDateAfter, importance, urgency, quadrant, sortBy, sortDir);
    }

    /**
     * Updates an existing task.
     * @param id The ID of the task to update.
     * @param taskDetails The task details to update, validated from the request body.
     * @return The updated task, or HTTP status 404 (Not Found) if the task doesn't exist,
     *         or HTTP status 400 (Bad Request) if validation fails (e.g. title is empty).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @Valid @RequestBody Task taskDetails) {
        try {
            Task updatedTask = taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok(updatedTask);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build(); // Or return a custom error response
        }
    }

    /**
     * Deletes a task by its ID.
     * @param id The ID of the task to delete.
     * @return HTTP status 204 (No Content) on successful deletion, or HTTP status 404 (Not Found) if the task doesn't exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

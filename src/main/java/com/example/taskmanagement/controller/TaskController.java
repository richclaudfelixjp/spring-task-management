package com.example.taskmanagement.controller;

import com.example.taskmanagement.dto.TaskCreationRequest;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * REST controller for managing tasks.
 * This class defines the API endpoints for CRUD operations on tasks.
 */
@RestController
@RequestMapping("/tasks") // Base path for all endpoints in this controller
public class TaskController {

    private final TaskService taskService;

    /**
     * Constructor for TaskController.
     * Spring will inject the TaskService dependency here.
     *
     * @param taskService The service for handling task business logic.
     */
    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Handles GET requests to /api/tasks.
     * - If no parameters are provided, retrieves a list of all tasks.
     * - If an 'id' parameter is provided, retrieves a single task by its ID.
     * - Any other parameters (e.g., title, description, status) will result in a 400 Bad Request.
     * @param id          Optional ID of the task to retrieve.
     * @param completed   Optional completion status to filter tasks.
     * @return A list of all tasks, a single task, or a 400/404 error.
     */
    @GetMapping
    public ResponseEntity<?> getTasks(@RequestParam(required = false) Long id,
                                      @RequestParam(required = false) Boolean completed) {
        // Case 1: An 'id' parameter is provided.
        if (id != null) {
            return taskService.getTaskById(id)
                        .<ResponseEntity<?>>map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
        } else if (completed != null) {
            // Case 2: A 'completed' parameter is provided. Return tasks filtered by completion status.
            return ResponseEntity.ok(taskService.getTasksByCompletionStatus(completed));
        } else {
            return ResponseEntity.ok(taskService.getAllTasks());
        }
    }

    /**
     * Handles POST requests to /api/tasks.
     * Creates a new task.
     *
     * @param taskRequest The task creation request object from the request body.
     * @return The created task with a 201 Created status.
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskCreationRequest taskRequest) {
        Task createdTask = taskService.createTask(taskRequest);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    /**
     * Handles PUT requests to /api/tasks/{id}.
     * Updates an existing task.
     *
     * @param id          The ID of the task to update.
     * @param taskDetails The new details for the task from the request body.
     * @return A ResponseEntity containing the updated task, or a 404 Not Found status if not.
     */
    @PutMapping
    public ResponseEntity<Task> updateTask(@RequestBody Task taskDetails) {
        return taskService.updateTask(taskDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Handles DELETE requests to /api/tasks.
     * - If an 'id' parameter is provided, deletes a single task by its ID.
     * - If no parameters are provided, deletes all tasks.
     * - Providing title, description, or status parameters will result in a 400 Bad Request.
     *
     * @param id          Optional ID of the task to delete.
     * @return A ResponseEntity with a 204 No Content status on success, or an error status.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteTasks(@RequestParam(required = false) Long id) {
        // Case 1: An 'id' parameter is provided. Delete the specific task.
        if (id != null) {
            if (!taskService.getTaskById(id).isPresent()) {
                return ResponseEntity.noContent().build();
            }
            taskService.deleteTask(id);
        } else {
            // If no ID is provided, delete all tasks.
            taskService.deleteAllTasks();
        }
        return ResponseEntity.noContent().build();
    }
}
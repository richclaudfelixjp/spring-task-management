package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.TaskCreationRequest;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * The service layer for the Task entity.
 * This class contains the business logic for task management.
 * It acts as an intermediary between the controller and the repository.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Constructor-based dependency injection.
     * Spring will automatically "inject" an instance of TaskRepository here.
     *
     * @param taskRepository The repository for accessing task data.
     */
    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Retrieves all tasks from the database.
     *
     * @return a list of all tasks.
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Retrieves a single task by its ID.
     *
     * @param id The ID of the task to retrieve.
     * @return an Optional containing the task if found, or an empty Optional if not.
     */
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Creates and saves a new task.
     *
     * @param taskRequest The task creation request object containing task details.
     * @return the saved task, including the generated ID.
     */
    public Task createTask(TaskCreationRequest taskRequest) {
        Task newTask = new Task();
        newTask.setTitle(taskRequest.getTitle());
        newTask.setDescription(taskRequest.getDescription());
        newTask.setCompleted(false); // New tasks are not completed by default
        return taskRepository.save(newTask);
    }

    /**
     * Updates an existing task.
     *
     * @param taskDetails The new details for the task.
     * @return the updated task, or null if the task was not found.
     */
    public Optional<Task> updateTask(Task taskDetails) {
        return taskRepository.findById(taskDetails.getId()).map(taskToUpdate -> {
            if (taskDetails.getTitle() != null) {
                taskToUpdate.setTitle(taskDetails.getTitle());
            }
            if (taskDetails.getDescription() != null) {
                taskToUpdate.setDescription(taskDetails.getDescription());
            }
            // Only update 'completed' when the client explicitly provided it
            if (taskDetails.getCompleted() != null) {
                taskToUpdate.setCompleted(taskDetails.getCompleted());
            }
            return taskRepository.save(taskToUpdate);
        });
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public void deleteAllTasks() {
        taskRepository.deleteAll();
    }
}
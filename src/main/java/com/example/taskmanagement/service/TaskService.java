package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.TaskCreationRequest;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.User;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;

    /**
     * Constructor-based dependency injection.
     * Spring will automatically "inject" an instance of TaskRepository here.
     *
     * @param taskRepository The repository for accessing task data.
     */
    @Autowired
    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Retrieves all tasks from the database.
     *
     * @return a list of all tasks.
     */
    public List<Task> getAllTasks(String username) {
        User user = getUserByUsername(username);
        return taskRepository.findByUser(user);
    }

    /**
     * Retrieves a single task by its ID.
     *
     * @param id The ID of the task to retrieve.
     * @return an Optional containing the task if found, or an empty Optional if not.
     */
    public Optional<Task> getTaskById(Long id, String username) {
        User user = getUserByUsername(username);
        return taskRepository.findByIdAndUser(id, user);
    }

    public List<Task> getTasksByCompletionStatus(Boolean completed, String username) {
        User user = getUserByUsername(username);
        return taskRepository.findByCompletedAndUser(completed, user);
    }

    /**
     * Creates and saves a new task.
     *
     * @param taskRequest The task creation request object containing task details.
     * @return the saved task, including the generated ID.
     */
    public Task createTask(TaskCreationRequest taskRequest, String username) {
        User user = getUserByUsername(username);
        Task newTask = new Task();
        newTask.setTitle(taskRequest.getTitle());
        newTask.setDescription(taskRequest.getDescription());
        newTask.setCompleted(false);
        newTask.setUser(user);
        return taskRepository.save(newTask);
    }

    /**
     * Updates an existing task.
     *
     * @param taskDetails The new details for the task.
     * @return the updated task, or null if the task was not found.
     */
    public Optional<Task> updateTask(Task taskDetails, String username) {
        User user = getUserByUsername(username);
        return taskRepository.findByIdAndUser(taskDetails.getId(), user).map(taskToUpdate -> {
            if (taskDetails.getTitle() != null) {
                taskToUpdate.setTitle(taskDetails.getTitle());
            }
            if (taskDetails.getDescription() != null) {
                taskToUpdate.setDescription(taskDetails.getDescription());
            }
            if (taskDetails.getCompleted() != null) {
                taskToUpdate.setCompleted(taskDetails.getCompleted());
            }
            return taskRepository.save(taskToUpdate);
        });
    }

    @Transactional
    public boolean deleteTask(Long id, String username) {
        User user = getUserByUsername(username);
        return taskRepository.findByIdAndUser(id, user).map(task -> {
            taskRepository.delete(task);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void deleteAllTasks(String username) {
        User user = getUserByUsername(username);
        taskRepository.deleteByUser(user);
    }
}
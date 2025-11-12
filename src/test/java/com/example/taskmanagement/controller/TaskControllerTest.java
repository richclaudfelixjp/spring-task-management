package com.example.taskmanagement.controller;
import com.example.taskmanagement.dto.TaskCreationRequest;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // 1. Add this import
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getTasks_whenNoId_shouldReturnAllTasks() throws Exception {
        // Arrange
        Task task1 = new Task();
        Task task2 = new Task();

        task1.setId(1L);
        task1.setTitle("title1");
        task1.setDescription("description1");
        task1.setCompleted(false);

        task2.setId(2L);
        task2.setTitle("title2");
        task2.setDescription("description2");
        task2.setCompleted(false);
        
        List<Task> allTasks = Arrays.asList(task1, task2);

        when(taskService.getAllTasks()).thenReturn(allTasks);

        // Act & Assert
        mockMvc.perform(get("/task"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("title1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].title").value("title2"));
    }

    @Test
    void getTasks_whenValidId_shouldReturnTask() throws Exception {
        // Arrange
        Task task = new Task();
        task.setId(1L);
        task.setTitle("title1");
        task.setDescription("description1");
        task.setCompleted(false);

        when(taskService.getTaskById(task.getId())).thenReturn(Optional.of(task));

        // Act & Assert
        mockMvc.perform(get("/task").param("id", String.valueOf(task.getId())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("title1"))
                .andExpect(jsonPath("$.description").value("description1"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void getTasks_whenInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        Long invalidId = 99L;
        when(taskService.getTaskById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/task").param("id", String.valueOf(invalidId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTasks_whenValidCompleted_shouldReturnCompletedTasks() throws Exception {
        Task task1 = new Task();
        Task task2 = new Task();

        task1.setId(1L);
        task1.setTitle("title1");
        task1.setDescription("description1");
        task1.setCompleted(true);

        task2.setId(2L);
        task2.setTitle("title2");
        task2.setDescription("description2");
        task2.setCompleted(false);

        List<Task> completedTasks = Arrays.asList(task1);

        when(taskService.getTasksByCompletionStatus(true)).thenReturn(completedTasks);
        // Act & Assert
        mockMvc.perform(get("/task").param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("title1"))
                .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    void getTasks_whenInvalidCompleted_shouldBadRequest() throws Exception {
        mockMvc.perform(get("/task").param("completed", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTasks_whenValidTitle_shouldBeSuccessfulRequest() throws Exception {
        
        TaskCreationRequest request = new TaskCreationRequest();
        request.setTitle("New Test Task");
        request.setDescription("A description for the test task.");

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle(request.getTitle());
        savedTask.setDescription(request.getDescription());
        savedTask.setCompleted(false);

        // Use any() for a more robust mock
        when(taskService.createTask(any(TaskCreationRequest.class))).thenReturn(savedTask);
        
        // Act & Assert
        mockMvc.perform(post("/task")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // 2. Change to isCreated() for a 201 status
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Test Task"))
                .andExpect(jsonPath("$.description").value("A description for the test task."))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void createTasks_whenInvalidTitle_shouldBeBadRequest() throws Exception {
        
        TaskCreationRequest request = new TaskCreationRequest();
        request.setTitle(""); // Invalid title
        request.setDescription("A description for the test task.");

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle(request.getTitle());
        savedTask.setDescription(request.getDescription());
        savedTask.setCompleted(false);

        // Use any() for a more robust mock
        when(taskService.createTask(any(TaskCreationRequest.class))).thenReturn(savedTask);
        
        // Act & Assert
        mockMvc.perform(post("/task")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 2. Change to isBadRequest() for a 400 status
    }

    @Test
    void updateTasks_whenValidID_shouldBeSuccessful() throws Exception {
        // Arrange
        Task updateDetails = new Task();
        updateDetails.setId(1L);
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setCompleted(true);

        // Mock the one service method that is actually called.
        // It should return an Optional containing the updated task.
        when(taskService.updateTask(any(Task.class))).thenReturn(Optional.of(updateDetails));

        // Act & Assert
        mockMvc.perform(put("/task") // The URL is correct for your controller
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void updateTasks_whenInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        Task updateDetailsWithInvalidId = new Task();
        updateDetailsWithInvalidId.setId(99L); // Use an ID that doesn't exist
        updateDetailsWithInvalidId.setTitle("This should not be saved");

        // Mock the service to return an empty Optional, simulating "not found".
        when(taskService.updateTask(any(Task.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/task") // The URL is correct
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updateDetailsWithInvalidId)))
                .andExpect(status().isNotFound());                
    }
    
    @Test
    void deleteTasks_whenNoId_shouldDeleteAllTasks() throws Exception {
        // Arrange: Mock the service to do nothing when deleteAllTasks is called.
        doNothing().when(taskService).deleteAllTasks();

        // Act & Assert: Perform a DELETE request and expect a 204 No Content status.
        mockMvc.perform(delete("/task"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTasks_whenValidId_shouldDeleteTask() throws Exception {
        // Arrange
        Long taskId = 1L;
        // For a void method, use doNothing(). This says "when deleteTask(1L) is called, do nothing and don't throw an exception."
        doNothing().when(taskService).deleteTask(taskId);

        // Act & Assert: Perform a DELETE request and expect a 204 No Content status.
        mockMvc.perform(delete("/task/{id}", taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTasks_whenInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        Long invalidTaskId = 99L;
        // For the negative case, mock the service to throw an exception when the ID is not found.
        // This is a common pattern for services.
        doThrow(new RuntimeException("Task not found")).when(taskService).deleteTask(invalidTaskId);

        // Act & Assert: Perform a DELETE request and expect a 404 Not Found status.
        mockMvc.perform(delete("/task/{id}", invalidTaskId))
                .andExpect(status().isNotFound());
    }
}
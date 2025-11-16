package com.example.taskmanagement.controller;

import com.example.taskmanagement.config.JwtRequestFilter;
import com.example.taskmanagement.config.SecurityConfig;
import com.example.taskmanagement.dto.TaskCreationRequest;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.service.TaskService;
import com.example.taskmanagement.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, JwtUtil.class, JwtRequestFilter.class})
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private com.example.taskmanagement.service.UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;
    private final String testUser = "testuser";

    @BeforeAll
    static void setupEnv() {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
    }

    @BeforeEach
    void setup() {
        UserDetails userDetails = new User(testUser, "password", new ArrayList<>());
        when(userService.loadUserByUsername(testUser)).thenReturn(userDetails);
        token = jwtUtil.generateToken(userDetails);
    }

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

        when(taskService.getAllTasks(testUser)).thenReturn(allTasks);

        // Act & Assert
        mockMvc.perform(get("/task").header("Authorization", "Bearer " + token))
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

        when(taskService.getTaskById(task.getId(), testUser)).thenReturn(Optional.of(task));

        // Act & Assert
        mockMvc.perform(get("/task").param("id", String.valueOf(task.getId())).header("Authorization", "Bearer " + token))
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
        when(taskService.getTaskById(invalidId, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/task").param("id", String.valueOf(invalidId)).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTasks_whenValidCompleted_shouldReturnCompletedTasks() throws Exception {
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("title1");
        task1.setDescription("description1");
        task1.setCompleted(true);

        List<Task> completedTasks = Collections.singletonList(task1);

        when(taskService.getTasksByCompletionStatus(true, testUser)).thenReturn(completedTasks);
        // Act & Assert
        mockMvc.perform(get("/task").param("completed", "true").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("title1"))
                .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    void getTasks_whenInvalidCompleted_shouldBadRequest() throws Exception {
        mockMvc.perform(get("/task").param("completed", "invalid").header("Authorization", "Bearer " + token))
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

        when(taskService.createTask(any(TaskCreationRequest.class), eq(testUser))).thenReturn(savedTask);

        // Act & Assert
        mockMvc.perform(post("/task")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
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

        // Act & Assert
        mockMvc.perform(post("/task")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTasks_whenValidID_shouldBeSuccessful() throws Exception {
        // Arrange
        Task updateDetails = new Task();
        updateDetails.setId(1L);
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setCompleted(true);

        when(taskService.updateTask(any(Task.class), eq(testUser))).thenReturn(Optional.of(updateDetails));

        // Act & Assert
        mockMvc.perform(put("/task")
                .header("Authorization", "Bearer " + token)
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

        when(taskService.updateTask(any(Task.class), eq(testUser))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/task")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updateDetailsWithInvalidId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTasks_whenNoId_shouldDeleteAllTasks() throws Exception {
        // Arrange: Mock the service to do nothing when deleteAllTasks is called.
        doNothing().when(taskService).deleteAllTasks(testUser);

        // Act & Assert: Perform a DELETE request and expect a 204 No Content status.
        mockMvc.perform(delete("/task").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTasks_whenValidId_shouldDeleteTask() throws Exception {
        // Arrange
        Long taskId = 1L;
        when(taskService.deleteTask(taskId, testUser)).thenReturn(true);

        mockMvc.perform(delete("/task").param("id", String.valueOf(taskId)).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTasks_whenInvalidId_shouldReturnNotFound() throws Exception {
        // Arrange
        Long invalidTaskId = 99L;
        when(taskService.deleteTask(invalidTaskId, testUser)).thenReturn(false);

        mockMvc.perform(delete("/task").param("id", String.valueOf(invalidTaskId)).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
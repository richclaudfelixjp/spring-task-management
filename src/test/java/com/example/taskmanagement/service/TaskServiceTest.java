package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.TaskCreationRequest;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.model.User;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_shouldReturnSavedTask() {
        // Arrange
        TaskCreationRequest request = new TaskCreationRequest();
        request.setTitle("New Test Task");
        request.setDescription("A description for the test task.");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle(request.getTitle());
        savedTask.setDescription(request.getDescription());
        savedTask.setCompleted(false);
        savedTask.setUser(mockUser);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // Act
        Task result = taskService.createTask(request, "testuser");

        // Assert
        assertEquals(savedTask.getTitle(), result.getTitle());
        assertEquals(savedTask.getDescription(), result.getDescription());
        assertEquals(false, result.getCompleted());
        assertEquals(mockUser, result.getUser());
    }

    @Test
    void updateTask_shouldUpdateSavedTask() {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Description");
        existingTask.setCompleted(false);
        existingTask.setUser(mockUser);

        Task updateDetails = new Task();
        updateDetails.setId(1L);
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setCompleted(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Task> resultOpt = taskService.updateTask(updateDetails, "testuser");

        // Assert
        assertTrue(resultOpt.isPresent());
        Task savedTask = resultOpt.get();
        assertEquals(updateDetails.getTitle(), savedTask.getTitle());
        assertEquals(updateDetails.getDescription(), savedTask.getDescription());
        assertEquals(true, savedTask.getCompleted());
        assertEquals(existingTask.getId(), savedTask.getId());
    }
}
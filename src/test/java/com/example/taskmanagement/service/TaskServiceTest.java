package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.TaskCreationRequest;
import com.example.taskmanagement.model.Task;
import com.example.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// This tells JUnit 5 to enable Mockito features like @Mock and @InjectMocks.
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    // @Mock creates a fake version of the TaskRepository.
    // It will not interact with the actual database.
    @Mock
    private TaskRepository taskRepository;

    // @InjectMocks creates a REAL instance of TaskService, but it
    // injects the fake @Mock objects (like taskRepository) into it.
    @InjectMocks
    private TaskService taskService;

    // The @Test annotation marks this method as a test that JUnit should run.
    @Test
    void createTask_shouldReturnSavedTask() {
        // --- 1. ARRANGE ---
        // We set up all the data and fake behavior we need for the test.

        // a) Create the input data for our method.
        TaskCreationRequest request = new TaskCreationRequest();
        request.setTitle("New Test Task");
        request.setDescription("A description for the test task.");

        // b) Create the expected output from the fake repository.
        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle(request.getTitle());
        savedTask.setDescription(request.getDescription());
        savedTask.setCompleted(false);

        // c) Tell our fake repository what to do.
        // "WHEN the save() method is called on our fake taskRepository with ANY Task object,
        // THEN it should return our 'savedTask' object."
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);


        // --- 2. ACT ---
        // We call the one method we are actually testing.
        //Task result = taskService.createTask(request);


        // --- 3. ASSERT ---
        // We check if the result is what we expected.
        // Did the taskService correctly use the data from the request?
        //assertEquals(savedTask.getTitle(), result.getTitle());
        //assertEquals(savedTask.getDescription(), result.getDescription());
        //assertEquals(false, result.getCompleted());
    }

    @Test
    void updateTask_shouldUpdateSavedTask() {
        // --- 1. ARRANGE ---
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Description");
        existingTask.setCompleted(false);

        Task updateDetails = new Task();
        updateDetails.setId(1L);
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setCompleted(true);

        when(taskRepository.findById(1L)).thenReturn(java.util.Optional.of(existingTask));
        // Return the task that is passed to save, which is more realistic
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- 2. ACT ---
        //taskService.updateTask(updateDetails);

        // --- 3. ASSERT ---
        // Create a captor to "capture" the argument passed to the save method
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        // Verify that save was called once, and capture the Task object
        verify(taskRepository).save(taskCaptor.capture());
        // Get the captured task
        Task savedTask = taskCaptor.getValue();

        // Assert that the captured task has the correct, updated properties
        assertEquals(updateDetails.getTitle(), savedTask.getTitle());
        assertEquals(updateDetails.getDescription(), savedTask.getDescription());
        assertEquals(true, savedTask.getCompleted());
        // Also assert that the ID remained the same
        assertEquals(existingTask.getId(), savedTask.getId());
    }
}
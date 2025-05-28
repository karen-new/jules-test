package com.example.taskmatrix;

import com.example.taskmatrix.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("Test Task");
        sampleTask.setDetails("Test Details");
        sampleTask.setImportance(Importance.IMPORTANT);
        sampleTask.setUrgency(Urgency.URGENT);
        sampleTask.setDueDate(LocalDate.now().plusDays(5));
        sampleTask.setLabel("TestLabel");
    }

    private Task createTask(Long id, String title, Importance importance, Urgency urgency) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setImportance(importance);
        task.setUrgency(urgency);
        return task;
    }

    @Test
    void createTask_success() {
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);
        Task created = taskService.createTask(sampleTask);
        assertNotNull(created);
        assertEquals("Test Task", created.getTitle());
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    void createTask_nullTitle_throwsException() {
        sampleTask.setTitle(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(sampleTask);
        });
        assertEquals("Task title cannot be null or empty.", exception.getMessage());
    }

    @Test
    void createTask_emptyTitle_throwsException() {
        sampleTask.setTitle("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(sampleTask);
        });
        assertEquals("Task title cannot be null or empty.", exception.getMessage());
    }


    @Test
    void createTask_nullImportance_throwsException() {
        sampleTask.setImportance(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(sampleTask);
        });
        assertEquals("Task importance cannot be null.", exception.getMessage());
    }

    @Test
    void createTask_nullUrgency_throwsException() {
        sampleTask.setUrgency(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(sampleTask);
        });
        assertEquals("Task urgency cannot be null.", exception.getMessage());
    }

    @Test
    void getAllTasks_noFilters_returnsAllTasks() {
        when(taskRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(sampleTask));

        List<Task> tasks = taskService.getAllTasks(null, null, null, null, null, null, "id", "asc");
        assertFalse(tasks.isEmpty());
        assertEquals(1, tasks.size());
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class));
    }

    @Test
    void getAllTasks_withFilters_returnsFilteredTasks() {
        // This test verifies that a specification is built and passed to the repository.
        // The actual filtering logic is within TaskSpecification, which could be tested separately if needed.
        when(taskRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(sampleTask));

        List<Task> tasks = taskService.getAllTasks("TestLabel", null, null, Importance.IMPORTANT, null, null, "title", "desc");
        assertFalse(tasks.isEmpty());
        assertEquals(1, tasks.size());
        // We can't easily verify the content of the Specification itself without a more complex setup or custom argument captor.
        // For this test, we mainly ensure the call is made.
        verify(taskRepository, times(1)).findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class));
    }


    @Test
    void getTaskById_exists_returnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        Optional<Task> foundTask = taskService.getTaskById(1L);
        assertTrue(foundTask.isPresent());
        assertEquals("Test Task", foundTask.get().getTitle());
    }

    @Test
    void getTaskById_notFound_returnsEmpty() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());
        Optional<Task> foundTask = taskService.getTaskById(2L);
        assertFalse(foundTask.isPresent());
    }

    @Test
    void updateTask_exists_updatesAndReturnsTask() {
        Task updatedDetails = createTask(1L, "Updated Task", Importance.NOT_IMPORTANT, Urgency.NOT_URGENT);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0)); // return the saved entity

        Task updatedTask = taskService.updateTask(1L, updatedDetails);

        assertNotNull(updatedTask);
        assertEquals("Updated Task", updatedTask.getTitle());
        assertEquals(Importance.NOT_IMPORTANT, updatedTask.getImportance());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    void updateTask_notFound_throwsResourceNotFoundException() {
        Task updatedDetails = createTask(2L, "Updated Task", Importance.IMPORTANT, Urgency.URGENT);
        when(taskRepository.findById(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(2L, updatedDetails);
        });
        assertEquals("Task not found with id: 2", exception.getMessage());
    }
    
    @Test
    void updateTask_blankTitle_throwsIllegalArgumentException() {
        Task updatedDetails = new Task();
        updatedDetails.setTitle(""); // Blank title
        updatedDetails.setImportance(Importance.IMPORTANT);
        updatedDetails.setUrgency(Urgency.URGENT);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(1L, updatedDetails);
        });
        assertEquals("Task title cannot be null or empty.", exception.getMessage());
    }
    
    @Test
    void updateTask_nullImportance_doesNotThrowAndUsesExisting() {
        // As per current updateTask logic, if importance/urgency in taskDetails is null, it keeps existing.
        // If the requirement was to throw error if taskDetails.getImportance() is null, this test would change.
        Task existingTask = createTask(1L, "Existing", Importance.IMPORTANT, Urgency.URGENT);
        Task taskDetails = new Task();
        taskDetails.setTitle("Title Updated");
        taskDetails.setImportance(null); // Importance not provided in update
        taskDetails.setUrgency(null);   // Urgency not provided in update

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        Task updatedTask = taskService.updateTask(1L, taskDetails);

        assertEquals(Importance.IMPORTANT, updatedTask.getImportance());
        assertEquals(Urgency.URGENT, updatedTask.getUrgency());
        assertEquals("Title Updated", updatedTask.getTitle());
    }


    @Test
    void deleteTask_exists_deletesTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);
        taskService.deleteTask(1L);
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_notFound_throwsResourceNotFoundException() {
        when(taskRepository.existsById(2L)).thenReturn(false);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(2L);
        });
        assertEquals("Task not found with id: 2", exception.getMessage());
        verify(taskRepository, times(1)).existsById(2L);
        verify(taskRepository, never()).deleteById(anyLong());
    }
}

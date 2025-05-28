package com.example.taskmatrix;

import com.example.taskmatrix.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;


@WebMvcTest(TaskController.class) // Focus only on the TaskController
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Mocks the TaskService dependency
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    private Task sampleTask1;
    private Task sampleTask2;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule()); // Ensure LocalDate is serialized correctly

        sampleTask1 = new Task();
        sampleTask1.setId(1L);
        sampleTask1.setTitle("Task 1");
        sampleTask1.setImportance(Importance.IMPORTANT);
        sampleTask1.setUrgency(Urgency.URGENT);
        sampleTask1.setDueDate(LocalDate.now().plusDays(1));

        sampleTask2 = new Task();
        sampleTask2.setId(2L);
        sampleTask2.setTitle("Task 2");
        sampleTask2.setImportance(Importance.NOT_IMPORTANT);
        sampleTask2.setUrgency(Urgency.NOT_URGENT);
        sampleTask2.setDueDate(LocalDate.now().plusDays(2));
    }

    private static Task createTask(Long id, String title, Importance importance, Urgency urgency) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setImportance(importance);
        task.setUrgency(urgency);
        return task;
    }

    @Test
    void createTask_validTask_returnsCreated() throws Exception {
        given(taskService.createTask(any(Task.class))).willReturn(sampleTask1);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleTask1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is(sampleTask1.getTitle())));
    }

    @Test
    void createTask_invalidTask_returnsBadRequest() throws Exception {
        Task invalidTask = createTask(null, "", null, null); // Blank title, null importance/urgency
        // The @Valid annotation on the controller's request body should trigger a 400
        // before it even hits the service in this case.
        // If service throws IllegalArgumentException for some reason, the controller handles it as 400 too.

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void createTask_serviceThrowsIllegalArgument_returnsBadRequest() throws Exception {
        Task taskWithValidStructureButServiceRejects = createTask(null, "Valid Title", Importance.IMPORTANT, Urgency.URGENT);
        given(taskService.createTask(any(Task.class))).willThrow(new IllegalArgumentException("Service validation failed"));

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskWithValidStructureButServiceRejects)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getTaskById_exists_returnsOk() throws Exception {
        given(taskService.getTaskById(1L)).willReturn(Optional.of(sampleTask1));

        mockMvc.perform(get("/api/tasks/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(sampleTask1.getTitle())));
    }

    @Test
    void getTaskById_notFound_returnsNotFound() throws Exception {
        given(taskService.getTaskById(1L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTasks_returnsOkAndTaskList() throws Exception {
        List<Task> tasks = List.of(sampleTask1, sampleTask2);
        given(taskService.getAllTasks(null, null, null, null, null, null, "id", "asc")).willReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is(sampleTask1.getTitle())))
                .andExpect(jsonPath("$[1].title", is(sampleTask2.getTitle())));
    }

    @Test
    void getAllTasks_withFilters_returnsOkAndFilteredTaskList() throws Exception {
        List<Task> filteredTasks = List.of(sampleTask1);
        given(taskService.getAllTasks(eq("TestLabel"), any(), any(), any(), any(), any(), eq("title"), eq("desc")))
                .willReturn(filteredTasks);

        mockMvc.perform(get("/api/tasks?label=TestLabel&sortBy=title&sortDir=desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(sampleTask1.getTitle())));
    }


    @Test
    void updateTask_validUpdate_returnsOk() throws Exception {
        Task updatedTask = createTask(1L, "Updated Title", Importance.IMPORTANT, Urgency.URGENT);
        given(taskService.updateTask(eq(1L), any(Task.class))).willReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    @Test
    void updateTask_invalidUpdate_returnsBadRequest() throws Exception {
        Task invalidUpdate = createTask(1L, "", Importance.IMPORTANT, Urgency.URGENT); // Blank title

        // No need to mock service here as @Valid on controller should catch this.
        mockMvc.perform(put("/api/tasks/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTask_notFound_returnsNotFound() throws Exception {
        Task updateData = createTask(1L, "Non Existent Update", Importance.IMPORTANT, Urgency.URGENT);
        given(taskService.updateTask(eq(1L), any(Task.class))).willThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(put("/api/tasks/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_exists_returnsNoContent() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_notFound_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Task not found")).when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/{id}", 1L))
                .andExpect(status().isNotFound());
    }
}

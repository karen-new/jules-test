package com.example.taskmatrix;

import com.example.taskmatrix.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.taskmatrix.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing tasks.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Constructs a TaskService with the given TaskRepository.
     * @param taskRepository The repository for task data.
     */
    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Creates a new task.
     * Ensures importance and urgency are not null.
     * Throws IllegalArgumentException if the title is null or empty.
     * @param task The task to create.
     * @return The created task.
     */
    public Task createTask(Task task) {
        if (!StringUtils.hasText(task.getTitle())) {
            throw new IllegalArgumentException("Task title cannot be null or empty.");
        }
        if (task.getImportance() == null) {
            throw new IllegalArgumentException("Task importance cannot be null.");
        }
        if (task.getUrgency() == null) {
            throw new IllegalArgumentException("Task urgency cannot be null.");
        }
        return taskRepository.save(task);
    }

    /**
     * Retrieves a task by its ID.
     * @param id The ID of the task to retrieve.
     * @return An Optional containing the task if found, or an empty Optional if not.
     */
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Retrieves all tasks, with optional filtering and sorting.
     * @param label Optional filter by label (contains, case-insensitive).
     * @param dueDateBefore Optional filter for due date before or on this date.
     * @param dueDateAfter Optional filter for due date after or on this date.
     * @param importance Optional filter by importance.
     * @param urgency Optional filter by urgency.
     * @param quadrant Optional filter by quadrant (derived from importance and urgency).
     * @param sortBy Field to sort by (e.g., "dueDate", "title"). Defaults to "id".
     * @param sortDir Sort direction ("asc" or "desc"). Defaults to "asc".
     * @return A list of tasks matching the criteria, sorted as specified.
     */
    public List<Task> getAllTasks(
            String label,
            LocalDate dueDateBefore,
            LocalDate dueDateAfter,
            Importance importance,
            Urgency urgency,
            Quadrant quadrant,
            String sortBy,
            String sortDir) {

        Specification<Task> spec = TaskSpecification.getTasksByCriteria(
                label, dueDateBefore, dueDateAfter, importance, urgency, quadrant
        );

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        // Handle potential invalid sortBy field to prevent errors
        String sortField = (sortBy == null || sortBy.trim().isEmpty() || !isValidSortField(sortBy)) ? "id" : sortBy;

        return taskRepository.findAll(spec, Sort.by(direction, sortField));
    }

    private boolean isValidSortField(String fieldName) {
        // Simple validation: check against known Task fields.
        // For a more robust solution, one might use reflection or a predefined list.
        return List.of("id", "title", "dueDate", "label", "importance", "urgency").contains(fieldName);
    }


    /**
     * Updates an existing task.
     * If the task is not found, throws a ResourceNotFoundException.
     * Updates title (if not null/empty in taskDetails), details, label, dueDate, importance, and urgency.
     * Throws IllegalArgumentException if title is being set to null or empty, or if importance/urgency are set to null.
     * @param id The ID of the task to update.
     * @param taskDetails The details of the task to update.
     * @return The updated task.
     * @throws ResourceNotFoundException if the task with the given ID is not found.
     * @throws IllegalArgumentException if validation for title, importance, or urgency fails.
     */
    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Validate title if it's being updated
        if (taskDetails.getTitle() != null) {
            if (!StringUtils.hasText(taskDetails.getTitle())) {
                throw new IllegalArgumentException("Task title cannot be null or empty.");
            }
            task.setTitle(taskDetails.getTitle());
        }

        task.setDetails(taskDetails.getDetails());
        task.setLabel(taskDetails.getLabel());
        task.setDueDate(taskDetails.getDueDate());

        // Validate importance if it's being updated
        if (taskDetails.getImportance() != null) {
            task.setImportance(taskDetails.getImportance());
        } else {
            // If taskDetails.getImportance() is explicitly null, it might mean "don't update" or "set to null".
            // The original Task entity has @NotNull on importance, so it cannot be null in the DB.
            // If the intention is to allow partial updates where importance is not provided,
            // then task.getImportance() should remain. If it's to enforce it being non-null on update,
            // then this check is valid, or rely on @Valid in controller.
            // For now, we assume if it's in taskDetails, it's an attempt to set it.
            if (task.getImportance() == null) { // This case should ideally not happen due to @NotNull on entity
                 throw new IllegalArgumentException("Task importance cannot be null.");
            }
        }

        // Validate urgency if it's being updated
        if (taskDetails.getUrgency() != null) {
            task.setUrgency(taskDetails.getUrgency());
        } else {
             if (task.getUrgency() == null) { // Similar to importance
                throw new IllegalArgumentException("Task urgency cannot be null.");
            }
        }

        return taskRepository.save(task);
    }

    /**
     * Deletes a task by its ID.
     * If the task doesn't exist, throws a ResourceNotFoundException.
     * @param id The ID of the task to delete.
     * @throws ResourceNotFoundException if the task with the given ID is not found.
     */
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
}

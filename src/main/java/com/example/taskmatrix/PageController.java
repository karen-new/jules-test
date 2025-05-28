package com.example.taskmatrix;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class PageController {

    private final TaskService taskService;

    @Autowired
    public PageController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String getIndexPage(
            Model model,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateAfter,
            @RequestParam(required = false) Importance importance,
            @RequestParam(required = false) Urgency urgency,
            @RequestParam(required = false) Quadrant quadrant,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {

        List<Task> allTasks = taskService.getAllTasks(label, dueDateBefore, dueDateAfter, importance, urgency, quadrant, sortBy, sortDir);

        List<Task> importantUrgentTasks = new ArrayList<>();
        List<Task> importantNotUrgentTasks = new ArrayList<>();
        List<Task> notImportantUrgentTasks = new ArrayList<>();
        List<Task> notImportantNotUrgentTasks = new ArrayList<>();

        for (Task task : allTasks) {
            Quadrant q = task.getQuadrant(); // Assumes getQuadrant() is null-safe and always returns a valid Quadrant
            if (q != null) { // Should always be true given Task constraints
                switch (q) {
                    case IMPORTANT_URGENT:
                        importantUrgentTasks.add(task);
                        break;
                    case IMPORTANT_NOT_URGENT:
                        importantNotUrgentTasks.add(task);
                        break;
                    case NOT_IMPORTANT_URGENT:
                        notImportantUrgentTasks.add(task);
                        break;
                    case NOT_IMPORTANT_NOT_URGENT:
                        notImportantNotUrgentTasks.add(task);
                        break;
                }
            }
        }

        model.addAttribute("importantUrgentTasks", importantUrgentTasks);
        model.addAttribute("importantNotUrgentTasks", importantNotUrgentTasks);
        model.addAttribute("notImportantUrgentTasks", notImportantUrgentTasks);
        model.addAttribute("notImportantNotUrgentTasks", notImportantNotUrgentTasks);


        // Add filter/sort parameters to the model to repopulate the form
        model.addAttribute("currentLabel", label);
        model.addAttribute("currentDueDateBefore", dueDateBefore);
        model.addAttribute("currentDueDateAfter", dueDateAfter);
        model.addAttribute("currentImportance", importance);
        model.addAttribute("currentUrgency", urgency);
        model.addAttribute("currentQuadrant", quadrant); // This is for the filter dropdown
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSortDir", sortDir);

        // Add enum values for dropdowns
        model.addAttribute("importanceValues", Importance.values());
        model.addAttribute("urgencyValues", Urgency.values());
        model.addAttribute("quadrantValues", Quadrant.values()); // For the filter dropdown
        model.addAttribute("sortByOptions", List.of("id", "title", "dueDate", "label", "importance", "urgency"));

        return "index";
    }

    // Show Add Task Form
    @GetMapping("/tasks/add")
    public String showAddTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("importanceValues", Importance.values());
        model.addAttribute("urgencyValues", Urgency.values());
        return "add-task";
    }

    // Save New Task
    @PostMapping("/tasks/save")
    public String saveTask(@Valid @ModelAttribute("task") Task task, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("importanceValues", Importance.values());
            model.addAttribute("urgencyValues", Urgency.values());
            return "add-task";
        }
        try {
            taskService.createTask(task);
        } catch (IllegalArgumentException e) {
            // This can happen if title is blank or importance/urgency is null,
            // even if @Valid is used, depending on how @NotBlank vs @NotNull is handled by service.
            // The @Valid annotation on the Task object should catch these based on entity constraints.
            // Adding this for robustness or specific service-layer validation messages.
            model.addAttribute("errorMessage", e.getMessage()); // Example of passing a general error
            model.addAttribute("importanceValues", Importance.values());
            model.addAttribute("urgencyValues", Urgency.values());
            return "add-task";
        }
        return "redirect:/";
    }

    // Show Edit Task Form
    @GetMapping("/tasks/edit/{id}")
    public String showEditTaskForm(@PathVariable("id") Long id, Model model) {
        Optional<Task> taskOptional = taskService.getTaskById(id);
        if (taskOptional.isEmpty()) {
            // Optionally, add a flash attribute to show an error message on redirect
            return "redirect:/"; // Or to an error page
        }
        model.addAttribute("task", taskOptional.get());
        model.addAttribute("importanceValues", Importance.values());
        model.addAttribute("urgencyValues", Urgency.values());
        return "edit-task";
    }

    // Update Existing Task
    @PostMapping("/tasks/update/{id}")
    public String updateTask(@PathVariable("id") Long id, @Valid @ModelAttribute("task") Task task,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("importanceValues", Importance.values());
            model.addAttribute("urgencyValues", Urgency.values());
            // Need to ensure the ID is still available if we return to edit-task for error display
            task.setId(id); // Make sure id is set for the form action
            return "edit-task";
        }
        try {
            taskService.updateTask(id, task);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("importanceValues", Importance.values());
            model.addAttribute("urgencyValues", Urgency.values());
            task.setId(id); // Make sure id is set for the form action
            return "edit-task";
        }
        return "redirect:/";
    }

    // Delete Task
    @PostMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable("id") Long id) {
        taskService.deleteTask(id);
        return "redirect:/";
    }
}

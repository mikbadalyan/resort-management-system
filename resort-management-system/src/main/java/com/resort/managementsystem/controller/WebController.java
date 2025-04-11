package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.Staff;
import com.resort.managementsystem.entity.Task;
import com.resort.managementsystem.service.StaffService;
import com.resort.managementsystem.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List; // Add this import

@Controller
@RequestMapping("/web")
public class WebController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private StaffService staffService;

    @GetMapping("/tasks")
    public String listTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) LocalDateTime dueDate,
            Model model
    ) {
        List<Task> tasks;
        if (status != null || staffId != null || dueDate != null) {
            tasks = taskService.filterTasks(status, staffId, dueDate);
        } else {
            tasks = taskService.getAllTasks();
        }
        model.addAttribute("tasks", tasks);
        model.addAttribute("staffList", staffService.getAllStaff());
        model.addAttribute("staff", new Staff());
        model.addAttribute("task", new Task());
        return "tasks";
    }

    @PostMapping("/tasks")
    public String createTask(
            @Valid @ModelAttribute("task") Task task,
            BindingResult result,
            @RequestParam Long staffId,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("tasks", taskService.getAllTasks());
            model.addAttribute("staffList", staffService.getAllStaff());
            model.addAttribute("staff", new Staff());
            return "tasks";
        }
        task.setAssignedStaff(staffService.getStaffById(staffId));
        taskService.createTask(task, staffId);
        return "redirect:/web/tasks";
    }

    @PostMapping("/staff")
    public String createStaff(
            @Valid @ModelAttribute("staff") Staff staff,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("tasks", taskService.getAllTasks());
            model.addAttribute("staffList", staffService.getAllStaff());
            model.addAttribute("task", new Task());
            return "tasks";
        }
        try {
            staffService.saveStaff(staff);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("tasks", taskService.getAllTasks());
            model.addAttribute("staffList", staffService.getAllStaff());
            model.addAttribute("task", new Task());
            return "tasks";
        }
        return "redirect:/web/tasks";
    }

    @GetMapping("/staff/edit/{id}")
    public String editStaffForm(@PathVariable Long id, Model model) {
        Staff staff = staffService.getStaffById(id);
        model.addAttribute("staff", staff);
        return "edit-staff";
    }

    @PostMapping("/staff/update/{id}")
    public String updateStaff(
            @PathVariable Long id,
            @Valid @ModelAttribute("staff") Staff staff,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            staff.setId(id);
            return "edit-staff";
        }
        staff.setId(id);
        try {
            staffService.saveStaff(staff);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            staff.setId(id);
            return "edit-staff";
        }
        return "redirect:/web/tasks";
    }

    @PostMapping("/staff/delete/{id}")
    public String deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return "redirect:/web/tasks";
    }

    @GetMapping("/tasks/staff/{staffId}")
    public String listTasksByStaff(@PathVariable Long staffId, Model model) {
        model.addAttribute("tasks", taskService.getTasksByStaffId(staffId));
        model.addAttribute("staff", staffService.getStaffById(staffId));
        return "staff-tasks";
    }

    @PostMapping("/tasks/update/{id}")
    public String updateTaskStatus(@PathVariable Long id, @RequestParam String status) {
        Task task = taskService.getTaskById(id);
        task.setStatus(status);
        taskService.updateTask(id, task);
        return "redirect:/web/tasks/staff/" + task.getAssignedStaff().getId();
    }

    @GetMapping("/tasks/edit/{id}")
    public String editTaskForm(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id);
        model.addAttribute("task", task);
        model.addAttribute("staffList", staffService.getAllStaff());
        return "edit-task";
    }

    @PostMapping("/tasks/update-details/{id}")
    public String updateTaskDetails(
            @PathVariable Long id,
            @Valid @ModelAttribute("task") Task task,
            BindingResult result,
            @RequestParam Long staffId,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("staffList", staffService.getAllStaff());
            return "edit-task";
        }
        task.setId(id);
        task.setAssignedStaff(staffService.getStaffById(staffId));
        taskService.updateTask(id, task);
        return "redirect:/web/tasks/staff/" + task.getAssignedStaff().getId();
    }

    @PostMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        Long staffId = task.getAssignedStaff().getId();
        taskService.deleteTask(id);
        return "redirect:/web/tasks/staff/" + staffId;
    }
}
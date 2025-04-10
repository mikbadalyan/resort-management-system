package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.Task;
import com.resort.managementsystem.service.StaffService;
import com.resort.managementsystem.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/web") // Add a base path to avoid conflict
public class WebController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private StaffService staffService;

    @GetMapping("/tasks")
    public String listTasks(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        model.addAttribute("staffList", staffService.getAllStaff());
        return "tasks";
    }

    @PostMapping("/tasks")
    public String createTask(
            @RequestParam String description,
            @RequestParam String status,
            @RequestParam LocalDateTime dueDate,
            @RequestParam Long staffId
    ) {
        Task task = new Task(description, status, dueDate, staffService.getStaffById(staffId));
        taskService.createTask(task, staffId);
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
}
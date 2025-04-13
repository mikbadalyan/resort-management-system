package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.MaintenanceSchedule;
import com.resort.managementsystem.entity.Room;
import com.resort.managementsystem.service.MaintenanceScheduleService;
import com.resort.managementsystem.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/maintenance-schedules")
public class MaintenanceScheduleController {

    @Autowired
    private MaintenanceScheduleService maintenanceScheduleService;

    @Autowired
    private RoomService roomService;

    @GetMapping
    public String getAllSchedules(Model model) {
        model.addAttribute("schedules", maintenanceScheduleService.getAllSchedules());
        return "maintenance-schedules/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("schedule", new MaintenanceSchedule());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "maintenance-schedules/create";
    }

    @PostMapping
    public String createSchedule(@Valid @ModelAttribute("schedule") MaintenanceSchedule schedule, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        // Manually set the Room object based on the selected room.id
        Long roomId = schedule.getRoom() != null ? schedule.getRoom().getId() : null;
        if (roomId != null) {
            Room room = roomService.getRoomById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
            schedule.setRoom(room);
        }

        if (result.hasErrors()) {
            model.addAttribute("rooms", roomService.getAllRooms());
            return "maintenance-schedules/create";
        }

        try {
            maintenanceScheduleService.saveSchedule(schedule);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance schedule created successfully!");
            return "redirect:/web/maintenance-schedules";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while saving the maintenance schedule: " + e.getMessage());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "maintenance-schedules/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        MaintenanceSchedule schedule = maintenanceScheduleService.getScheduleById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid maintenance schedule ID: " + id));
        model.addAttribute("schedule", schedule);
        model.addAttribute("rooms", roomService.getAllRooms());
        return "maintenance-schedules/edit";
    }

    @PostMapping("/update/{id}")
    public String updateSchedule(@PathVariable Long id, @Valid @ModelAttribute("schedule") MaintenanceSchedule schedule, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        // Manually set the Room object based on the selected room.id
        Long roomId = schedule.getRoom() != null ? schedule.getRoom().getId() : null;
        if (roomId != null) {
            Room room = roomService.getRoomById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
            schedule.setRoom(room);
        }

        if (result.hasErrors()) {
            model.addAttribute("rooms", roomService.getAllRooms());
            return "maintenance-schedules/edit";
        }

        schedule.setId(id);
        try {
            maintenanceScheduleService.saveSchedule(schedule);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance schedule updated successfully!");
            return "redirect:/web/maintenance-schedules";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating the maintenance schedule: " + e.getMessage());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "maintenance-schedules/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            maintenanceScheduleService.deleteSchedule(id);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance schedule deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the maintenance schedule: " + e.getMessage());
        }
        return "redirect:/web/maintenance-schedules";
    }
}
package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.Room;
import com.resort.managementsystem.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/web/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public String getAllRooms(
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "capacity", required = false) Integer capacity,
            @RequestParam(value = "smokingPolicy", required = false) String smokingPolicy,
            @RequestParam(value = "viewType", required = false) String viewType,
            @RequestParam(value = "floorNumber", required = false) Integer floorNumber,
            Model model) {
            List<Room> rooms;
            if (roomType != null || status != null || capacity != null || smokingPolicy != null || viewType != null || floorNumber != null) {
                rooms = roomService.searchRooms(
                        roomType != null && !roomType.isEmpty() ? roomType : null,
                        status != null && !status.isEmpty() ? status : null,
                        capacity,
                        smokingPolicy != null && !smokingPolicy.isEmpty() ? smokingPolicy : null,
                        viewType != null && !viewType.isEmpty() ? viewType : null,
                        floorNumber
                );
            } else {
                rooms = roomService.getAllRooms();
            }
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomType", roomType);
        model.addAttribute("status", status);
        model.addAttribute("capacity", capacity);
        model.addAttribute("smokingPolicy", smokingPolicy);
        model.addAttribute("viewType", viewType);
        model.addAttribute("floorNumber", floorNumber);
        return "rooms/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("room", new Room()); // Do not set id here
        return "rooms/create";
    }

    @PostMapping
    public String createRoom(@Valid @ModelAttribute("room") Room room, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "rooms/create";
        }
        try {
            roomService.saveRoom(room); // Hibernate will generate the id
            redirectAttributes.addFlashAttribute("successMessage", "Room created successfully!");
            return "redirect:/web/rooms";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while saving the room: " + e.getMessage());
            return "rooms/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + id));
        model.addAttribute("room", room);
        return "rooms/edit";
    }

    @PostMapping("/update/{id}")
    public String updateRoom(@PathVariable Long id, @Valid @ModelAttribute("room") Room room, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "rooms/edit";
        }
        room.setId(id);
        try {
            roomService.saveRoom(room);
            redirectAttributes.addFlashAttribute("successMessage", "Room updated successfully!");
            return "redirect:/web/rooms";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating the room: " + e.getMessage());
            return "rooms/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the room: " + e.getMessage());
        }
        return "redirect:/web/rooms";
    }
}
package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.Guest;
import com.resort.managementsystem.service.GuestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/guests")
public class GuestController {

    @Autowired
    private GuestService guestService;

    @GetMapping
    public String getAllGuests(Model model) {
        model.addAttribute("guests", guestService.getAllGuests());
        return "guests/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("guest", new Guest()); // Do not set id here
        return "guests/create";
    }

    @PostMapping
    public String createGuest(@Valid @ModelAttribute("guest") Guest guest, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "guests/create";
        }
        try {
            guestService.saveGuest(guest); // Hibernate will generate the id
            redirectAttributes.addFlashAttribute("successMessage", "Guest created successfully!");
            return "redirect:/web/guests";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while saving the guest: " + e.getMessage());
            return "guests/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Guest guest = guestService.getGuestById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + id));
        model.addAttribute("guest", guest);
        return "guests/edit";
    }

    @PostMapping("/update/{id}")
    public String updateGuest(@PathVariable Long id, @Valid @ModelAttribute("guest") Guest guest, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "guests/edit";
        }
        guest.setId(id);
        try {
            guestService.saveGuest(guest);
            redirectAttributes.addFlashAttribute("successMessage", "Guest updated successfully!");
            return "redirect:/web/guests";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating the guest: " + e.getMessage());
            return "guests/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteGuest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            guestService.deleteGuest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Guest deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the guest: " + e.getMessage());
        }
        return "redirect:/web/guests";
    }
}
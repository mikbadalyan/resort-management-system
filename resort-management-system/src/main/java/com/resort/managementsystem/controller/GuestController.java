package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.Guest;
import com.resort.managementsystem.service.GuestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Base64;

@Controller
@RequestMapping("/web/guests")
public class GuestController {

    @Autowired
    private GuestService guestService;

    @GetMapping
    public String getAllGuests(
            @RequestParam(defaultValue = "list") String view,
            @RequestParam(required = false) String searchQuery,
            Model model
    ) {
        model.addAttribute("guests", guestService.searchGuests(searchQuery));
        model.addAttribute("view", view);
        model.addAttribute("searchQuery", searchQuery);
        return "guests/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("guest", new Guest());
        return "guests/create";
    }

    @PostMapping
    public String createGuest(
            @Valid @ModelAttribute("guest") Guest guest,
            BindingResult result,
            @RequestParam("photoFile") MultipartFile photoFile,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "guests/create";
        }
        try {
            if (!photoFile.isEmpty()) {
                guest.setPhoto(photoFile.getBytes());
            }
            guestService.saveGuest(guest);
            redirectAttributes.addFlashAttribute("successMessage", "Guest created successfully!");
            return "redirect:/web/guests";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "An error occurred while uploading the photo: " + e.getMessage());
            return "guests/create";
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
        if (guest.getPhoto() != null) {
            model.addAttribute("photoBase64", Base64.getEncoder().encodeToString(guest.getPhoto()));
        }
        return "guests/edit";
    }

    @PostMapping("/update/{id}")
    public String updateGuest(
            @PathVariable Long id,
            @Valid @ModelAttribute("guest") Guest guest,
            BindingResult result,
            @RequestParam("photoFile") MultipartFile photoFile,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "guests/edit";
        }
        guest.setId(id);
        try {
            if (!photoFile.isEmpty()) {
                guest.setPhoto(photoFile.getBytes());
            } else {
                // Retain existing photo if no new file is uploaded
                Guest existingGuest = guestService.getGuestById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + id));
                guest.setPhoto(existingGuest.getPhoto());
            }
            guestService.saveGuest(guest);
            redirectAttributes.addFlashAttribute("successMessage", "Guest updated successfully!");
            return "redirect:/web/guests";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "An error occurred while uploading the photo: " + e.getMessage());
            return "guests/edit";
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

    @GetMapping("/view/{id}")
    public String viewGuest(@PathVariable Long id, Model model) {
        Guest guest = guestService.getGuestById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + id));
        model.addAttribute("guest", guest);
        if (guest.getPhoto() != null) {
            model.addAttribute("photoBase64", Base64.getEncoder().encodeToString(guest.getPhoto()));
        }
        return "guests/view";
    }
}
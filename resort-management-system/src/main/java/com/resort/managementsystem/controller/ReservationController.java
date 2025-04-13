package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.Guest;
import com.resort.managementsystem.entity.Reservation;
import com.resort.managementsystem.entity.Room;
import com.resort.managementsystem.service.GuestService;
import com.resort.managementsystem.service.ReservationService;
import com.resort.managementsystem.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/web/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private GuestService guestService;

    @Autowired
    private RoomService roomService;

    @GetMapping
    public String getAllReservations(
            @RequestParam(value = "guestName", required = false) String guestName,
            @RequestParam(value = "roomNumber", required = false) String roomNumber,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        LocalDate effectiveStartDate = (startDate != null) ? startDate : LocalDate.of(1900, 1, 1);
        LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.of(9999, 12, 31);

        model.addAttribute("reservations", reservationService.searchReservations(guestName, roomNumber, startDate != null ? startDate : null, endDate != null ? endDate : null));
        model.addAttribute("guestName", guestName);
        model.addAttribute("roomNumber", roomNumber);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "reservations/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("reservation", new Reservation());
        model.addAttribute("guests", guestService.getAllGuests());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "reservations/create";
    }

    @PostMapping
    public String createReservation(@Valid @ModelAttribute("reservation") Reservation reservation, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        // Manually set the Guest and Room objects based on the selected IDs
        Long guestId = reservation.getGuest() != null ? reservation.getGuest().getId() : null;
        Long roomId = reservation.getRoom() != null ? reservation.getRoom().getId() : null;

        if (guestId != null) {
            Guest guest = guestService.getGuestById(guestId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + guestId));
            reservation.setGuest(guest);
        } else {
            model.addAttribute("errorMessage", "Please select a guest.");
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        }

        if (roomId != null) {
            Room room = roomService.getRoomById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
            reservation.setRoom(room);
        } else {
            model.addAttribute("errorMessage", "Please select a room.");
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        }

        if (result.hasErrors()) {
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        }

        try {
            reservationService.saveReservation(reservation);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation created successfully!");
            return "redirect:/web/reservations";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", "Failed to save reservation: Ensure all required fields are valid. " + e.getMessage());
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while saving the reservation: " + e.getMessage());
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.getReservationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation ID: " + id));
        model.addAttribute("reservation", reservation);
        model.addAttribute("guests", guestService.getAllGuests());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "reservations/edit";
    }

    @PostMapping("/update/{id}")
    public String updateReservation(@PathVariable Long id, @Valid @ModelAttribute("reservation") Reservation reservation, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Long guestId = reservation.getGuest() != null ? reservation.getGuest().getId() : null;
        Long roomId = reservation.getRoom() != null ? reservation.getRoom().getId() : null;

        if (guestId != null) {
            Guest guest = guestService.getGuestById(guestId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + guestId));
            reservation.setGuest(guest);
        }

        if (roomId != null) {
            Room room = roomService.getRoomById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
            reservation.setRoom(room);
        }

        if (result.hasErrors()) {
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/edit";
        }

        reservation.setId(id);
        try {
            reservationService.saveReservation(reservation);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation updated successfully!");
            return "redirect:/web/reservations";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating the reservation: " + e.getMessage());
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.deleteReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the reservation: " + e.getMessage());
        }
        return "redirect:/web/reservations";
    }
}
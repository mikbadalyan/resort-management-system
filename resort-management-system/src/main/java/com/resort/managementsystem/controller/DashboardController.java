package com.resort.managementsystem.controller;

import com.resort.managementsystem.service.GuestService;
import com.resort.managementsystem.service.ReservationService;
import com.resort.managementsystem.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web/dashboard")
public class DashboardController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private GuestService guestService;

    @GetMapping
    public String showDashboard(Model model) {
        // Existing statistics
        model.addAttribute("availableRooms", roomService.countAvailableRooms());
        model.addAttribute("totalReservations", reservationService.countTotalReservations());
        model.addAttribute("frequentGuests", guestService.findFrequentGuests(3)); // Top 3 frequent guests

        // New statistics
        model.addAttribute("totalRevenue", reservationService.calculateTotalRevenue());
        model.addAttribute("occupancyRate", roomService.calculateOccupancyRate());
        model.addAttribute("reservationTrends", reservationService.getReservationTrends());

        return "dashboard";
    }
}
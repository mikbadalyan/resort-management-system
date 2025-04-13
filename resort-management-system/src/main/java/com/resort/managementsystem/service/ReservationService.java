package com.resort.managementsystem.service;

import com.resort.managementsystem.entity.Guest;
import com.resort.managementsystem.entity.MaintenanceSchedule;
import com.resort.managementsystem.entity.Reservation;
import com.resort.managementsystem.entity.Room;
import com.resort.managementsystem.repository.GuestRepository;
import com.resort.managementsystem.repository.MaintenanceScheduleRepository;
import com.resort.managementsystem.repository.ReservationRepository;
import com.resort.managementsystem.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MaintenanceScheduleRepository maintenanceScheduleRepository;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> searchReservations(String guestName, String roomNumber, LocalDate startDate, LocalDate endDate) {
        List<Reservation> reservations = reservationRepository.findReservationsByCriteria(guestName, roomNumber, null, null);

        // Apply date filtering in Java if startDate or endDate is provided
        if (startDate != null || endDate != null) {
            reservations = reservations.stream()
                    .filter(reservation -> {
                        boolean matchesStart = startDate == null || !reservation.getCheckInDate().isBefore(startDate);
                        boolean matchesEnd = endDate == null || !reservation.getCheckOutDate().isAfter(endDate);
                        return matchesStart && matchesEnd;
                    })
                    .collect(Collectors.toList());
        }

        return reservations;
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public Reservation saveReservation(Reservation reservation) {
        // Validate guest and room are not null
        if (reservation.getGuest() == null || reservation.getRoom() == null) {
            throw new IllegalArgumentException("Guest and Room must be selected.");
        }

        // Validate check-out date is after check-in date
        if (reservation.getCheckOutDate().isBefore(reservation.getCheckInDate()) ||
                reservation.getCheckOutDate().isEqual(reservation.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        // Check for overlapping reservations
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                reservation.getRoom(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        // Exclude the current reservation if it’s being updated
        overlappingReservations.removeIf(r -> r.getId() != null && r.getId().equals(reservation.getId()));

        if (!overlappingReservations.isEmpty()) {
            throw new IllegalArgumentException("Room is not available for the selected dates due to an existing reservation");
        }

        // Check for overlapping maintenance schedules
        List<MaintenanceSchedule> overlappingMaintenanceSchedules = maintenanceScheduleRepository.findOverlappingSchedules(
                reservation.getRoom(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        if (!overlappingMaintenanceSchedules.isEmpty()) {
            throw new IllegalArgumentException("Room is scheduled for maintenance during the selected dates");
        }

        // Calculate the total cost
        reservation.calculateTotalCost();

        // Update guest's lastStayDate and totalStays
        Guest guest = reservation.getGuest();
        guest.setLastStayDate(reservation.getCheckOutDate());
        guest.setTotalStays(guest.getTotalStays() + 1);
        guestRepository.save(guest);

        // Save the reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        // Update room status
        updateRoomStatus(reservation.getRoom());

        return savedReservation;
    }

    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation ID: " + id));
        Guest guest = reservation.getGuest();
        Room room = reservation.getRoom();

        // Decrease totalStays (but not below 0)
        guest.setTotalStays(Math.max(0, guest.getTotalStays() - 1));
        // Update lastStayDate to the most recent reservation (excluding the one being deleted)
        List<Reservation> guestReservations = reservationRepository.findByGuestOrderByCheckOutDateDesc(guest);
        guestReservations.removeIf(r -> r.getId().equals(id));
        if (!guestReservations.isEmpty()) {
            guest.setLastStayDate(guestReservations.get(0).getCheckOutDate());
        } else {
            guest.setLastStayDate(null);
        }
        guestRepository.save(guest);

        // Delete the reservation
        reservationRepository.deleteById(id);

        // Update room status
        updateRoomStatus(room);
    }

    private void updateRoomStatus(Room room) {
        // Check if the room has any active reservations (current date is between check-in and check-out)
        LocalDate today = LocalDate.now();
        List<Reservation> activeReservations = reservationRepository.findOverlappingReservations(
                room, today, today
        );

        if (room.getStatus().equals("Under Maintenance")) {
            // If the room is under maintenance, don't change the status
            return;
        }

        if (!activeReservations.isEmpty()) {
            room.setStatus("Occupied");
        } else {
            room.setStatus("Available");
        }
        roomRepository.save(room);
    }

    public long countTotalReservations() {
        return reservationRepository.count();
    }

    public double calculateTotalRevenue() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .mapToDouble(Reservation::getTotalCost)
                .sum();
    }

    public Map<String, Long> getReservationTrends() {
        Map<String, Long> trends = new HashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.from(today.minusMonths(i));
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();
            long count = reservationRepository.findReservationsByCriteria(null, null, null, null)
                    .stream()
                    .filter(r -> !r.getCheckInDate().isBefore(startOfMonth) && !r.getCheckOutDate().isAfter(endOfMonth))
                    .count();
            trends.put(month.toString(), count);
        }
        return trends;
    }
}
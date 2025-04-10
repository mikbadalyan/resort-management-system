package com.resort.managementsystem.repository;

import com.resort.managementsystem.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    Guest findByEmail(String email);
}
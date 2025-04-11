package com.resort.managementsystem.service;

import com.resort.managementsystem.entity.Staff;
import com.resort.managementsystem.repository.StaffRepository;
import com.resort.managementsystem.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffService {
    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private TaskRepository taskRepository;

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Staff getStaffById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    public Staff saveStaff(Staff staff) throws IllegalArgumentException {
        // Check for duplicate email
        Optional<Staff> existingStaff = staffRepository.findByEmail(staff.getEmail());
        if (existingStaff.isPresent() && (staff.getId() == null || !existingStaff.get().getId().equals(staff.getId()))) {
            throw new IllegalArgumentException("A staff member with this email already exists.");
        }
        return staffRepository.save(staff);
    }

    public Staff updateStaff(Long id, Staff staff) {
        Staff existingStaff = getStaffById(id);
        existingStaff.setFirstName(staff.getFirstName());
        existingStaff.setLastName(staff.getLastName());
        existingStaff.setEmail(staff.getEmail());
        existingStaff.setRole(staff.getRole());
        return saveStaff(existingStaff); // Reuse saveStaff to handle email validation
    }

    public void deleteStaff(Long id) {
        taskRepository.deleteByAssignedStaffId(id);
        staffRepository.deleteById(id);
    }
}
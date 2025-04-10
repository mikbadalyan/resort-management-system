package com.resort.managementsystem;

import com.resort.managementsystem.entity.Staff;
import com.resort.managementsystem.repository.StaffRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ResortManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResortManagementSystemApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(StaffRepository staffRepository) {
        return args -> {
            // Check if the staff member already exists by email
            Staff existingStaff = staffRepository.findByEmail("john.doe@resort.com");
            if (existingStaff == null) {
                // Create a sample staff member only if it doesn't exist
                Staff staff = new Staff("John", "Doe", "john.doe@resort.com", "manager");
                staffRepository.save(staff);
                System.out.println("Created new staff: " + staff.getFirstName() + " " + staff.getLastName());
            } else {
                System.out.println("Staff already exists: " + existingStaff.getFirstName() + " " + existingStaff.getLastName());
            }

            // Retrieve and print the staff member
            Staff retrievedStaff = staffRepository.findByEmail("john.doe@resort.com");
            System.out.println("Retrieved Staff: " + retrievedStaff.getFirstName() + " " + retrievedStaff.getLastName());
        };
    }
}
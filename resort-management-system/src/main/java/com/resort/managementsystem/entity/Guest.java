package com.resort.managementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "guests")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    @Column(nullable = false)
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;

    @PastOrPresent(message = "Date of birth must be in the past or present")
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Nationality is required")
    @Column(nullable = false)
    private String nationality;

    @Column
    private String loyaltyProgramStatus; // e.g., "Bronze", "Silver", "Gold"

    @Column
    private String preferences; // e.g., "Non-smoking room, vegetarian meals"

    @NotBlank(message = "Emergency contact is required")
    @Column(nullable = false)
    private String emergencyContact;

    @Column
    private LocalDate lastStayDate;

    @Column
    private Integer totalStays = 0; // Default to 0 for new guests

    // Constructors
    public Guest() {}

    public Guest(String name, String email, String phoneNumber, String address, LocalDate dateOfBirth,
                 String nationality, String emergencyContact) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.emergencyContact = emergencyContact;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getLoyaltyProgramStatus() {
        return loyaltyProgramStatus;
    }

    public void setLoyaltyProgramStatus(String loyaltyProgramStatus) {
        this.loyaltyProgramStatus = loyaltyProgramStatus;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public LocalDate getLastStayDate() {
        return lastStayDate;
    }

    public void setLastStayDate(LocalDate lastStayDate) {
        this.lastStayDate = lastStayDate;
    }

    public Integer getTotalStays() {
        return totalStays;
    }

    public void setTotalStays(Integer totalStays) {
        this.totalStays = totalStays;
    }
}
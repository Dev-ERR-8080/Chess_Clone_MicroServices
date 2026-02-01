package org.example.user_profile_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "user_profiles")
@Data
public class UserProfile {
    @Id
    @Column(nullable = false,unique = true)
    private Long userId; // Matches the IAM userId

    private String username;
    private String fullName;
    private String country;
    private String pfpUrl;
    private Integer currentRating = 250; // Business logic lives here
}
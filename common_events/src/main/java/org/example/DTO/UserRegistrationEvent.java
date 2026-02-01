package org.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationEvent {
    private Long userId;
    private String email;
    private String fullName;
    private String country;
    private String userName;
    private String pfpUrl;

}
package org.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {

    Long userId;
    String username;
    String email;
    String role;
    String pfpUrl;
    String name;
    String country;

}

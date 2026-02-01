package org.example.iam_service.model.DTO;
import lombok.Data;
@Data

public class LoginRequest {
    String userEmailId;
    String password;
}

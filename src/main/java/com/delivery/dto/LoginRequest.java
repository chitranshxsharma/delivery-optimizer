package com.delivery.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class LoginRequest {
    @Email @NotBlank public String email;
    @NotBlank @Size(min = 6) public String password;
}

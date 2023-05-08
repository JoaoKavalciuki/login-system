package com.loginsystem.loginsystem.registration.password;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
    private String password;
    private String newPassword;
    private String confirmPassword;
}

package com.loginsystem.loginsystem.registration;

public record RegistrationRequest(
         String firstName,
         String lastName,
         String email,
         String password,
         String cpf,
         String role
) {
}

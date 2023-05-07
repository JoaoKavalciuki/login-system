package com.loginsystem.loginsystem.user;

import com.loginsystem.loginsystem.registration.RegistrationRequest;
import com.loginsystem.loginsystem.registration.token.VerificationToken;

import java.util.List;
import java.util.Optional;

public interface UserServiceInterface {
    List<User> getAllUsers();
    User registerUser(RegistrationRequest request);
    Optional<User> findByEmail(String email);

    void saveVerificationToken(User user, String verificationToken);

    String validateToken(String verificationToken);
}

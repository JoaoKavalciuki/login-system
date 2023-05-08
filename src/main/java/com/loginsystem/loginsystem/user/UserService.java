package com.loginsystem.loginsystem.user;

import com.loginsystem.loginsystem.exception.UserAlredyExistsException;
import com.loginsystem.loginsystem.registration.RegistrationRequest;
import com.loginsystem.loginsystem.registration.password.PasswordResetTokenRepository;
import com.loginsystem.loginsystem.registration.password.PasswordResetTokenService;
import com.loginsystem.loginsystem.registration.token.VerificationToken;
import com.loginsystem.loginsystem.registration.token.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements  UserServiceInterface {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User registerUser(RegistrationRequest request) {
        Optional<User> user = this.findByEmail(request.email());
        if(user.isPresent()){
            throw  new UserAlredyExistsException("Email já registrado.");
        }
        var newUser = new User();
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setCpf(request.cpf());
        newUser.setRole(request.role());
        return userRepository.save(newUser);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveVerificationToken(User user, String token) {
        var verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public String validateToken(String verificationToken) {
        VerificationToken token = verificationTokenRepository.findByToken(verificationToken);
        if(token == null){
            return "Token de verificação inválido, por favor gere outro token.";
        }

        User user = token.getUser();

        Calendar calendar = Calendar.getInstance();
        if(( token.getTokenEXpirationTime().getTime() - calendar.getTime().getTime()) <= 0){
            verificationTokenRepository.delete(token);
            return "Token requisitado expirado, por favor gere outro token";
        }

        user.setEnabled(true);
        userRepository.save(user);

        return "Token validado com sucesso!";
    }

    public void createUserPasswordResetToken(User user, String token) {
        passwordResetTokenService.createUserPasswordResetToke(user, token);
    }

    @Override
    public String validateResetPasswordToken(String passwordResetToken) {
        return passwordResetTokenService.validatePasswordResetToken(passwordResetToken);
    }

    @Override
    public User findUserByPasswordToken(String passwordResetToken) {
        return passwordResetTokenService.findUserByPasswordToken(passwordResetToken).get();
    }

    @Override
    public void resetPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

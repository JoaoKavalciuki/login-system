package com.loginsystem.loginsystem.registration.password;

import com.loginsystem.loginsystem.registration.token.VerificationToken;
import com.loginsystem.loginsystem.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository  repository;
    public void createUserPasswordResetToken(User user, String token){
        PasswordResetToken newToken = new PasswordResetToken(token, user);
        repository.save(newToken);
    }

    public String validatePasswordResetToken(String resetPasswordToken){
        PasswordResetToken token = repository.findByToken(resetPasswordToken);
        if(token == null){
            return "Token para redefinição de senha inválido, por favor gere outro token.";
        }
        Calendar calendar = Calendar.getInstance();
        if(( token.getTokenEXpirationTime().getTime() - calendar.getTime().getTime()) <= 0){
            repository.delete(token);
            return "Token requisitado expirado, por favor gere outro token";
        }

        return "Token válido";
    }

    public Optional<User> findUserByPasswordToken(String token){
        return Optional.ofNullable(repository.findByToken(token).getUser());
    }
}

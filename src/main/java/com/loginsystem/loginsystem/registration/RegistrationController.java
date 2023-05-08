package com.loginsystem.loginsystem.registration;

import com.loginsystem.loginsystem.event.RegistrationCompleteEvent;
import com.loginsystem.loginsystem.event.listener.RegistrationCompleteEventListener;
import com.loginsystem.loginsystem.registration.password.PasswordResetRequest;
import com.loginsystem.loginsystem.registration.password.PasswordResetToken;
import com.loginsystem.loginsystem.registration.token.VerificationToken;
import com.loginsystem.loginsystem.registration.token.VerificationTokenRepository;
import com.loginsystem.loginsystem.user.User;
import com.loginsystem.loginsystem.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequestMapping("/registrar")
@RequiredArgsConstructor
@RestController
public class RegistrationController {
    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final VerificationTokenRepository tokenRepository;
    private final RegistrationCompleteEventListener eventListener;
    @PostMapping
    public String registerUser(@RequestBody RegistrationRequest registrationRequest, HttpServletRequest requestURL){
        User user = userService.registerUser(registrationRequest);
        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationURL(requestURL)));
        return "Por favor, verifique seu email para completar seu registro.";
    }

    public String applicationURL(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    @GetMapping("/verificarEmail")
    public String verifyEmail(@RequestParam("token") String token){
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if(verificationToken.getUser().isEnabled()){
            return "Essa conta já está ativa, por favor faça seu login";
        }

        String verificationResult = userService.validateToken(token);
        if(verificationResult.equalsIgnoreCase("Token validado com sucesso!")){
            return "Email verificado com sucesso. Acesso à conta liberado";
        }

        return "Token de verificação inválido.";
    }

    @PostMapping("/pedidoMudarSenha")
    public String resetPasswordRequest(@RequestBody PasswordResetRequest resetPasswordRequest, final HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        Optional<User> user = userService.findByEmail(resetPasswordRequest.getEmail());

        String passwordResetUrl = "";
        if(user.isPresent()){
            String passwordResetToken = UUID.randomUUID().toString();
            userService.createUserPasswordResetToken(user.get(), passwordResetToken);
            passwordResetUrl =  passwordResetLink(user.get(), applicationURL(request), passwordResetToken);
        }

        return passwordResetUrl;
    }

    public String passwordResetLink(User user, String applicationURL, String passwordResetToken) throws MessagingException, UnsupportedEncodingException {
        String url = applicationURL + "/registrar/mudarSenha?token=" + passwordResetToken;
        eventListener.sendPasswordResetEmail(url);
        log.info("Click the link to reset your password: {}", url);
        return url;
    }

    @PostMapping("/mudarSenha")
    public String resetPassword(@RequestBody PasswordResetRequest passwordResetRequest,@RequestParam("token") String passwordResetToken){
        String tokenValidationResult = userService.validateResetPasswordToken(passwordResetToken);
        if(!tokenValidationResult.equalsIgnoreCase("Token válido")){
            return "Token de mudança de senha inválido, por favor gere outro token.";
        }

        User user = userService.findUserByPasswordToken(passwordResetToken);
        if(user != null){
            userService.resetPassword(user, passwordResetRequest.getNewPassword());
                    return "Sua senha foi mudada com sucesso";
        }
        return "Token de mudança de senha inválido.";
    }
}

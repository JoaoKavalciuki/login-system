package com.loginsystem.loginsystem.registration;

import com.loginsystem.loginsystem.event.RegistrationCompleteEvent;
import com.loginsystem.loginsystem.registration.token.VerificationToken;
import com.loginsystem.loginsystem.registration.token.VerificationTokenRepository;
import com.loginsystem.loginsystem.user.User;
import com.loginsystem.loginsystem.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/registrar")
@RequiredArgsConstructor
@RestController
public class RegistrationController {
    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final VerificationTokenRepository tokenRepository;
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
        if(verificationResult.equals("Token validado com sucesso!")){
            return "Email verificado com sucesso. Acesso à conta liberado";
        }

        return "Token de verificação inválido.";
    }


}

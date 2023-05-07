package com.loginsystem.loginsystem.event.listener;

import com.loginsystem.loginsystem.event.RegistrationCompleteEvent;
import com.loginsystem.loginsystem.user.User;
import com.loginsystem.loginsystem.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {
    private final UserService userService;
    private User user;
    private final JavaMailSender mailSender;
    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        user = event.getUser();
        String verificationToken = UUID.randomUUID().toString();
        userService.saveVerificationToken(user, verificationToken);
        String url = event.getVerificationURL() + "/registrar/verificarEmail?token=" + verificationToken;
        try{
            sendVerificationEmail(url);
        }catch (MessagingException | UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }

    }

public void sendVerificationEmail(String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Ative sua conta book shifter";
        String senderName = "Projeto Book Shifter";
        String content = "<p> Olá, " + user.getFirstName() + "</p>"
                + "<p>Obrigado por se registrar no Projeto Book Shifter," + " clique no link abaixo para efetivar seu registro</p>"
                + "<a href=\"" + url + "\">Verifique sua conta</a>";
        MimeMessage message = mailSender.createMimeMessage();
        var messageHelper = new MimeMessageHelper(message);
        messageHelper.setFrom("book.shifter.brazil@gmail.com", senderName);
        messageHelper.setTo(user.getEmail());
        messageHelper.setSubject(subject);
        messageHelper.setText(content,  true);
        mailSender.send(message);
}
}

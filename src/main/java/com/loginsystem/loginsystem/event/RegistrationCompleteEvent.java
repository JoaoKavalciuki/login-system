package com.loginsystem.loginsystem.event;

import com.loginsystem.loginsystem.user.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RegistrationCompleteEvent extends ApplicationEvent {
    private User user;
    private String verificationURL;

    public RegistrationCompleteEvent(User user, String verificationURL){
        super(user);
        this.user = user;
        this.verificationURL = verificationURL;
    }
}

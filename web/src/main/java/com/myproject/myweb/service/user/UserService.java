package com.myproject.myweb.service.user;

import com.myproject.myweb.dto.user.UserRequestDto;
import javax.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    Long join(UserRequestDto userRequestDto) throws IllegalStateException;

    String createToken();
    void certify(Long id) throws MessagingException;
    void expireToken(Long id);
    Boolean confirmToken(Long id, String token);

    // Object login(UserRequestDto userRequestDto) throws IllegalArgumentException, IllegalStateException;

}

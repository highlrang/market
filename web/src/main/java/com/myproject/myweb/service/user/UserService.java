package com.myproject.myweb.service.user;

import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

public interface UserService {

    Long join(UserRequestDto userRequestDto) throws IllegalStateException;

    String createToken();
    void certify(Long id) throws MessagingException;
    void expirateToken(Long id);
    Boolean confirmToken(Long id, String token);

    Object login(UserRequestDto userRequestDto) throws IllegalArgumentException, IllegalStateException;

}

package com.myproject.myweb.service;

import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.repository.CouponRepository;
import com.myproject.myweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final JavaMailSender emailSender; // Impl

    public UserResponseDto login(UserRequestDto userRequestDto) throws IllegalArgumentException, IllegalStateException {
        User user = userRepository.findByEmail(userRequestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));

        Boolean isUser = user.checkPassword(userRequestDto.getPassword());
        if(!isUser){
            throw new IllegalStateException("UserNotMatchedException");
        }

        return new UserResponseDto(user);
    }

    @Transactional
    public Long join(UserRequestDto userRequestDto){
        Boolean alreadyExist = userRepository.findByEmail(userRequestDto.getEmail()).isPresent();

        if(alreadyExist) throw new IllegalStateException("UserAlreadyExistException");

        User user = userRepository.save(userRequestDto.toEntity());

        Coupon coupon = Coupon.builder()
                .name("신규 회원 10% 할인 쿠폰")
                .expirationDate(LocalDateTime.now().plusMonths(5L))
                .discountPer(5)
                .user(user)
                .build();
        couponRepository.save(coupon);

        return user.getId();
    }

    @Transactional
    public void certify(Long userId) throws MessagingException {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        user.setCertificationToken(createToken());

        String name = "";
        if(user.getName() != null) name = user.getName() + "님께 ";
        String context = "<h3>이메일 인증을 위하여 " + name + "발송된 인증메일입니다. 하단의 링크를 클릭해서 인증을 완료해주세요.</h3>"
                + "<a href='http://127.0.0.1:8081/certified?user="+user.getId()+"&token="+user.getCertificationToken()
                +"'>여기를 클릭해주세요!</a>";

        // SimpleMailMessage message = new SimpleMailMessage();
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
        message.setSubject("테스트 이메일 인증");
        message.setText(context, "UTF-8", "html");

        emailSender.send(message);
    }

    private String createToken() {
        return UUID.randomUUID().toString();
    }

     @Transactional
    public void expirateToken(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        user.setCertificationToken(null);
    }

    @Transactional
    public Boolean confirmToken(Long userId, String token){
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        log.info("token = " + token + " getToken() = " + user.getCertificationToken());
        if(user.getCertificationToken().equals(token)) user.setCertified(true);
        return user.getCertified();
    }
}

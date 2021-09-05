package com.myproject.myweb.service.user;

import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.repository.CouponRepository;
import com.myproject.myweb.repository.CustomerRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService implements UserService{

    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final JavaMailSender emailSender; // Impl

    @Override
    public CustomerResponseDto login(UserRequestDto userRequestDto) throws IllegalArgumentException, IllegalStateException {
        Customer customer = customerRepository.findByEmail(userRequestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));

        Boolean isCorrect = customer.checkPassword(userRequestDto.getPassword());
        if(!isCorrect){
            throw new IllegalStateException("UserNotMatchedException");
        }

        return new CustomerResponseDto(customer);
    }

    @Override
    @Transactional
    public Long join(UserRequestDto userRequestDto) throws IllegalStateException{
        Boolean alreadyExist = customerRepository.findByEmail(userRequestDto.getEmail()).isPresent();
        if(alreadyExist) throw new IllegalStateException("UserAlreadyExistException");

        Customer customer = customerRepository.save(userRequestDto.toCustomer());

        Coupon coupon = Coupon.createCoupon("신규 회원 10% 할인 쿠폰", customer, 10, LocalDateTime.now().plusMonths(5L));
        couponRepository.save(coupon);

        return customer.getId();
    }

    @Override
    @Transactional
    public void certify(Long customerId) throws MessagingException {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        customer.setCertificationToken(createToken());

        String name = "";
        if(customer.getName() != null) name = customer.getName() + "님께 ";
        String context = "<h3>이메일 인증을 위하여 " + name
                + "발송된 인증메일입니다. 하단의 링크를 클릭해서 인증을 완료해주세요.</h3>"
                + "<a href='http://127.0.0.1:8081/certified?user="+customer.getId()
                + "&token="+customer.getCertificationToken()
                + "'>여기를 클릭해주세요!</a>";

        // SimpleMailMessage message = new SimpleMailMessage();
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(customer.getEmail()));
        message.setSubject("테스트 이메일 인증");
        message.setText(context, "UTF-8", "html");

        emailSender.send(message);
    }

    @Override
    public String createToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    @Transactional
    public void expirateToken(Long customerId){
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        customer.setCertificationToken(null);
    }

    @Override
    @Transactional
    public Boolean confirmToken(Long customerId, String token){
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        if(customer.getCertificationToken().equals(token)) customer.setCertified(true);
        return customer.getCertified();
    }

}

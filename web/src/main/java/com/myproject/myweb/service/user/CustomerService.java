package com.myproject.myweb.service.user;

import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.user.Address;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.SenderDto;
import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.repository.CouponRepository;
import com.myproject.myweb.repository.CustomerRepository;
import com.myproject.myweb.service.SenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.myproject.myweb.config.Constants.JOIN_MAIL_TEMPLATE;
import static com.myproject.myweb.config.Constants.MAIL_ADDRESS;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService implements UserService{

    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final SenderService senderService;
    private final JavaMailSender emailSender;
    private final BCryptPasswordEncoder passwordEncoder;


    public CustomerResponseDto findById(Long customerId){
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        return new CustomerResponseDto(customer);
    }

    @Override
    public CustomerResponseDto loadUserByUsername(String email) { // login에 사용됨
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        return new CustomerResponseDto(customer);
    }

    /*
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
    */

    @Override
    @Transactional
    public Long join(UserRequestDto userRequestDto) throws IllegalStateException{
        Boolean alreadyExist = customerRepository.findByEmail(userRequestDto.getEmail()).isPresent();
        if(alreadyExist) throw new IllegalStateException("UserAlreadyExistException");

        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
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

        Map<String, String> templateData = new HashMap<>();
        templateData.put("username", customer.getName());
        templateData.put("userId", String.valueOf(customer.getId()));
        templateData.put("token", customer.getCertificationToken());
        SenderDto senderDto = SenderDto.SenderTemplateDto(
                MAIL_ADDRESS, Arrays.asList(customer.getEmail()),
                JOIN_MAIL_TEMPLATE, templateData);
        senderService.sendTemplate(senderDto);

        /*
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(customer.getEmail()));
        message.setSubject(subject);
        message.setText(context, "UTF-8", "html");
        emailSender.send(message);
         */
    }

    @Override
    public String createToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    @Transactional
    public void expireToken(Long customerId){
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

    @Transactional
    public void updateAddress(Long customerId, Address address){
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));

        customer.setAddress(address);
    }
}

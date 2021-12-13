package com.myproject.myweb.service.user;

import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.SenderDto;
import com.myproject.myweb.dto.user.SellerResponseDto;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.repository.SellerRepository;
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
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService implements UserService{

    private final SellerRepository sellerRepository;
    private final SenderService senderService;
    private final JavaMailSender emailSender;
    private final BCryptPasswordEncoder passwordEncoder;

    public static String webUrl;
    @Value("${webUrl}")
    public void setWebUrl(String webUrl){
        this.webUrl = webUrl;
    }

    @Override
    public SellerResponseDto loadUserByUsername(String email) {
        Seller seller = sellerRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        return new SellerResponseDto(seller);
    }

    /*
    @Override
    public SellerResponseDto login(UserRequestDto userRequestDto) throws IllegalArgumentException, IllegalStateException {
        Seller seller = sellerRepository.findByEmail(userRequestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));

        Boolean isCorrect = seller.checkPassword(userRequestDto.getPassword());
        if(!isCorrect){
            throw new IllegalStateException("UserNotMatchedException");
        }

        return new SellerResponseDto(seller);
    }
    */

    @Override
    @Transactional
    public Long join(UserRequestDto userRequestDto) throws IllegalStateException{
        Boolean alreadyExist = sellerRepository.findByEmail(userRequestDto.getEmail()).isPresent();
        if(alreadyExist) throw new IllegalStateException("UserAlreadyExistException");

        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        return sellerRepository.save(userRequestDto.toSeller()).getId();
    }

    @Override
    @Transactional
    public void certify(Long sellerId) throws MessagingException {
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        seller.setCertificationToken(createToken());

        String subject = "쇼핑몰 웹사이트 회원가입 계정인증 이메일입니다.";
        String name = "";
        if(seller.getName() != null) name = seller.getName() + "님께 ";
        String content = "<h3>이메일 인증을 위하여 " + name
                + "발송된 인증메일입니다. 하단의 링크를 클릭해서 인증을 완료해주세요.</h3>"
                + "<a href='" + webUrl + "/seller/certified?user="+seller.getId()
                + "&token="+seller.getCertificationToken()
                + "'>여기를 클릭해주세요!</a>";

        SenderDto senderDto = SenderDto.builder()
                .to(Arrays.asList(seller.getEmail()))
                .subject(subject)
                .content(content)
                .build();
        senderService.send(senderDto);

        /*
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(seller.getEmail()));
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
    public void expireToken(Long sellerId){
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        seller.setCertificationToken(null);
    }

    @Override
    @Transactional
    public Boolean confirmToken(Long sellerId, String token){
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        if(seller.getCertificationToken().equals(token)) seller.setCertified(true);
        return seller.getCertified();
    }

    public SellerResponseDto findById(Long id){
        Seller seller = sellerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        return new SellerResponseDto(seller);
    }
}

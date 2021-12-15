package com.myproject.myweb.service.user;

import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.SenderDto;
import com.myproject.myweb.dto.user.SellerResponseDto;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.exception.AwsSesMailSendingException;
import com.myproject.myweb.repository.SellerRepository;
import com.myproject.myweb.service.SenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.myproject.myweb.config.Constants.JOIN_MAIL_TEMPLATE;
import static com.myproject.myweb.config.Constants.MAIL_ADDRESS;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService implements UserService{

    private final SellerRepository sellerRepository;
    private final SenderService senderService;
    private final BCryptPasswordEncoder passwordEncoder;
    @Value("${webUrl}")
    private String webUrl;

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
    public void certify(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        seller.setCertificationToken(createToken());

        Map<String, String> templateData = new HashMap<>();
        templateData.put("username", seller.getName());
        templateData.put("webUrl", webUrl);
        templateData.put("userId", String.valueOf(seller.getId()));
        templateData.put("token", seller.getCertificationToken());
        SenderDto senderDto = SenderDto.SenderTemplateDto(
                MAIL_ADDRESS, Arrays.asList(seller.getEmail()),
                JOIN_MAIL_TEMPLATE, templateData);
        try{
            senderService.sendTemplate(senderDto);
        }catch(AwsSesMailSendingException e){
            this.updateCertified(sellerId, false);
        }
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

    @Transactional
    public void updateCertified(Long sellerId, Boolean status){
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        seller.setCertified(status);
    }

    public SellerResponseDto findById(Long id){
        Seller seller = sellerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        return new SellerResponseDto(seller);
    }
}

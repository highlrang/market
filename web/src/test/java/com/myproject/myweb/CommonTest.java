package com.myproject.myweb;



import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.SenderDto;
import com.myproject.myweb.dto.notice.SellerNoticeDto;
import com.myproject.myweb.handler.FileHandler;
import com.myproject.myweb.repository.*;

import static com.myproject.myweb.config.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.SellerNoticeService;
import com.myproject.myweb.service.SenderService;
import org.apache.tomcat.jni.Local;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class CommonTest{

    @Autowired CouponRepository couponRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired SellerRepository sellerRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PhotoRepository photoRepository;
    @Autowired SellerNoticeRepository sellerNoticeRepository;
    @Autowired SellerNoticeService noticeService;
    @Autowired BCryptPasswordEncoder passwordEncoder;
    @Autowired FileHandler fileHandler;
    @Autowired SenderService senderService;
    @Value("${webUrl}")
    private String webUrl;

    @Test
    public void aws_ses_mail_test(){
        /*
        MimeMessage message = emailSender.createMimeMessage();
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(customer.getEmail()));
        message.setSubject(subject);
        message.setText(context, "UTF-8", "html");
        emailSender.send(message);
         */

        /*
        senderService.send(SenderDto.builder()
                .from("developoffi@gmail.com")
                .to(Arrays.asList("developoffi@gmail.com"))
                .subject("test email")
                .content("test content")
                .build()
        );
        */

        /*
        String subject = "쇼핑몰 웹사이트 회원가입 계정인증 이메일입니다.";
        String content = "<h3>이메일 인증을 위하여 {{ username }} 계정에 발송된 인증메일입니다. "
                + "하단의 링크를 클릭해서 인증을 완료해주세요.</h3>"
                + "<a href='{{ webUrl }}/seller/certified?user={{ userId }}"
                + "&token={{ token }}"
                + "'>여기를 클릭해주세요!</a>";
        senderService.updateTemplate(JOIN_MAIL_TEMPLATE, subject,null, content);
        */

        Map<String, String> templateData = new HashMap<>();
        templateData.put("username", "test name");
        templateData.put("webUrl", webUrl);
        templateData.put("userId", "test id");
        templateData.put("token", "test token");
        senderService.sendTemplate(SenderDto.SenderTemplateDto(
                MAIL_ADDRESS, Arrays.asList(MAIL_ADDRESS),
                JOIN_MAIL_TEMPLATE, templateData)
        );
    }

    @Test
    public void 쿠폰() {
        Customer customer = customerRepository.findByEmail("jhw127@naver.com").get();
        List<Coupon> coupons = couponRepository.saveAll(
                Arrays.asList(
                        Coupon.createCoupon("신규 회원 5% 할인 쿠폰", customer, 5, LocalDateTime.now().plusMonths(6)),
                        Coupon.createCoupon("1주년 기념 10% 할인 쿠폰", customer, 10, LocalDateTime.now().plusMonths(6))
                )
        );

    }

    @Test
    public void 판매자_알림(){
        Seller seller = sellerRepository.findByEmail("seller@seller.com").get();
        List<SellerNotice> notices = sellerNoticeRepository.saveAll(
                Arrays.asList(
                        SellerNotice.builder()
                                .seller(seller)
                                .title("판매 100개 돌파")
                                .content("축하합니다. A 카테고리의 A상품의 판매가 100개를 돌파했습니다.")
                                .build(),
                        SellerNotice.builder()
                                .seller(seller)
                                .title("재고 관련 알림")
                                .content("A 카테고리의 A 상품의 재고가 소진되었습니다.")
                                .build()
                )
        );

        notices.forEach(
                n -> {
                    System.out.println(n.getId() + n.getTitle());
                    if(n.getTitle().equals("판매 100개 돌파")){
                        n.confirmed();
                    }
                }
        );
    }


    @Test
    public void 페이징(){
        Seller seller = sellerRepository.save(
                Seller.builder().name("seller").build()
        );

        itemRepository.save(Item.createItem(
                Category.CLOTHES, seller, "남방", 10000, 1000)
        );

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Item> items = itemRepository.findAllByCategory(Category.CLOTHES, pageRequest);

        System.out.println(items.getTotalPages());
        items.getContent().forEach(
                item -> System.out.println(item.getName())
        );
    }

    @Test
    public void item_save(){
        Customer customer = customerRepository.save(
                Customer.builder()
                        .name("정혜우")
                        .build()
        );
        customer.setCertified(true);

        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("김사장")
                        .email("seller@seller.com")
                        .password("")
                        .build()
        );
        seller.setCertified(true);

        Item item = Item.createItem(Category.ANIMAL_GOODS, seller, "이동장", 50000, 100);
        Item snack = Item.createItem(Category.ANIMAL_GOODS, seller, "간식-츄르", 3000, 500);
        Item wash = Item.createItem(Category.ANIMAL_GOODS, seller, "워터리스 샴푸", 10000, 200);
        Item foot = Item.createItem(Category.ANIMAL_GOODS, seller, "발톱깎이", 5000, 500);
        Item clean = Item.createItem(Category.ANIMAL_GOODS, seller, "빗", 5000, 500);
        Item food = Item.createItem(Category.ANIMAL_GOODS, seller, "사료-다이어트 사료", 40000, 200);
        Item food2 = Item.createItem(Category.ANIMAL_GOODS, seller, "사료-키튼 사료", 20000, 200);
        Item food3 = Item.createItem(Category.ANIMAL_GOODS, seller, "사료", 30000, 300);
        Item play = Item.createItem(Category.ANIMAL_GOODS, seller, "장난감-낚시대", 3000, 500);
        Item play2 = Item.createItem(Category.ANIMAL_GOODS, seller, "장난감-캣닢인형", 3000, 500);
        Item play3 = Item.createItem(Category.ANIMAL_GOODS, seller, "스크레쳐-대형", 30000, 100);
        Item play4 = Item.createItem(Category.ANIMAL_GOODS, seller, "스크레처-원목", 30000, 100);

        itemRepository.saveAll(Arrays.asList(item, snack, wash, foot, clean, food, food2, food3, play, play2, play3, play4));
    }

}
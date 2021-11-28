package com.myproject.myweb;



import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.notice.SellerNoticeDto;
import com.myproject.myweb.handler.FileHandler;
import com.myproject.myweb.repository.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.SellerNoticeService;
import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
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
                                .content("축하합니다. 동물용품 카테고리의 A상품의 판매가 100개를 돌파했습니다.")
                                .build(),
                        SellerNotice.builder()
                                .seller(seller)
                                .title("재고 관련 알림")
                                .content("동물용품 카테고리의 A상품의 재고가 소진되었습니다.")
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

    @Test @Commit
    public void 데이터_저장(){
        String password = passwordEncoder.encode("test1234");
        Customer customer = customerRepository.save(
                Customer.builder()
                        .name("test customer")
                        .email("customer@test.com")
                        .password(password)
                        .build()
        );
        customer.setCertified(true);
        Coupon coupon = Coupon.createCoupon("신규회원 10% 할인 쿠폰", customer, 10, LocalDateTime.now().plusMonths(6));
        Coupon coupon2 = Coupon.createCoupon("1주년 기념 10% 할인 쿠폰", customer, 10, LocalDateTime.now().plusMonths(6));
        couponRepository.saveAll(Arrays.asList(coupon, coupon2));

        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("test seller")
                        .email("seller@test.com")
                        .password(password)
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
        // 이동장의 사진 저장은 계정 로그인해서 진행
        itemRepository.saveAll(Arrays.asList(item, snack, wash, foot, clean, food, food2, food3, play, play2, play3, play4));


        OrderItem orderItem1 = OrderItem.createOrderItem(item, item.getPrice(), 2);
        Order order1 = Order.createOrder(customer, Delivery.builder().status(DeliveryStatus.READY).build(), orderItem1);
        OrderItem orderItem2 = OrderItem.createOrderItem(food3, food3.getPrice(), 1);
        Coupon usingCoupon = couponRepository.findAll().stream().filter(c -> c.getCustomer().equals(customer)).findAny().get();
        orderItem2.setCoupon(usingCoupon);
        OrderItem orderItem3 = OrderItem.createOrderItem(play, play.getPrice(), 1);
        Order order2 = Order.createOrder(customer, Delivery.builder().status(DeliveryStatus.READY).build(), orderItem2, orderItem3);
        orderRepository.saveAll(Arrays.asList(order1, order2));
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
                        .password("xhxh5314")
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
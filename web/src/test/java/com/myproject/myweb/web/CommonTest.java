package com.myproject.myweb.web;



import com.myproject.myweb.domain.*;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.notice.SellerNoticeDto;
import com.myproject.myweb.repository.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.SellerNoticeService;
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

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Transactional
public class CommonTest{

    @Autowired CouponRepository couponRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired SellerRepository sellerRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired PhotoRepository photoRepository;
    @Autowired
    SellerNoticeRepository sellerNoticeRepository;
    @Autowired SellerNoticeService noticeService;
    @Autowired BCryptPasswordEncoder passwordEncoder;

    @Test
    public void 테스트(){
        // 쿠폰 발급 - 연관관계까지

        Seller seller = sellerRepository.findByEmail("seller@seller.com").get();
        ItemService.ListByPaging<SellerNoticeDto> notices =
                noticeService.findAllBySeller(seller.getId(), PageRequest.of(1, 5));
        System.out.println(notices.getTotalPage() + " " + notices.getList().size());

        sellerNoticeRepository.save(
                SellerNotice.builder()
                        .seller(seller)
                        .title("테스트용")
                        .build()
        );
        int count = noticeService.countUnreadBySeller(seller.getId());
        System.out.println(count);
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

    @Test @Commit
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
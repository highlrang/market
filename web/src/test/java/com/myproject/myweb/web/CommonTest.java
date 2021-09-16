package com.myproject.myweb.web;



import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.OrderStatus;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.repository.*;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.criterion.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired BCryptPasswordEncoder passwordEncoder;

    @Test
    public void 테스트(){
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
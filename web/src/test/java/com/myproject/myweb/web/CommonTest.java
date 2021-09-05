package com.myproject.myweb.web;



import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Coupon;
import com.myproject.myweb.domain.Item;
import com.myproject.myweb.domain.OrderStatus;
import com.myproject.myweb.domain.user.Customer;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.repository.*;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Transactional
public class CommonTest{

    @Autowired CouponRepository couponRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired SellerRepository sellerRepository;
    @Autowired ItemRepository itemRepository;

    @Test
    public void query_fetch_batch_test(){
        Customer customer = Customer.builder()
                .name("customer")
                .build();
        customerRepository.save(customer);


        Coupon coupon1 = Coupon.createCoupon("coupon1", customer, 5, LocalDateTime.now());
        Coupon coupon2 = Coupon.createCoupon("coupon2", customer, 5, LocalDateTime.now());
        couponRepository.saveAll(Arrays.asList(coupon1, coupon2));


        List<Customer> customers = customerRepository.findAll();
        customers.get(0).getCouponList().forEach(coupon -> System.out.println(coupon.getName()));
    }

    @Test
    public void 페이징(){
        Seller seller = sellerRepository.save(
                Seller.builder().name("seller").build()
        );

        itemRepository.save(Item.createItem(
                Category.CLOTHES, seller, "남방", 10000, 1000)
        );

        PageRequest pageRequest = PageRequest.of(0, 10);
        // Sort.Direction 과 Sort.Order 살펴보기
        // Sort vs Direction + Properties
        // controller, service도
        Page<Item> items = itemRepository.findAllByCategory(Category.CLOTHES, pageRequest);

        System.out.println(items.getTotalPages());
        items.getContent().forEach(
                item -> System.out.println(item.getName())
        );
    }

}
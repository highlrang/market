package com.myproject.myweb.domain;

import com.myproject.myweb.domain.user.Customer;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class Coupon { // 사용 true 또는 만료기간 지나면 삭제되게 스케줄러

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private int discountPer;

    private LocalDateTime expirationDate;

    private Boolean isUsed;

    @OneToOne(mappedBy = "coupon", fetch = FetchType.LAZY)
    private CartItem cartItem;

    public void setCustomer(Customer customer){
        this.customer = customer;
        customer.getCouponList().add(this);
    }

    public static Coupon createCoupon(String name, Customer customer, int discountPer, LocalDateTime expirationDate){
        Coupon coupon = new Coupon();
        coupon.name = name;
        coupon.setCustomer(customer);
        coupon.discountPer = discountPer;
        coupon.expirationDate = expirationDate;
        coupon.isUsed = false;
        return coupon;
    }

    public void setCartItem(CartItem cartItem){
        this.cartItem = cartItem;
    }

    // coupon을 orderItem에 사용할 때 cartItem과의 연관관계 끊기
    public void disableCartItem(){
        this.cartItem.removeCoupon();
        this.cartItem = null;
    }

    public void setIsUsed(Boolean isUsed){
        this.isUsed = isUsed;
    }
}

package com.myproject.myweb.dto.cart;

import com.myproject.myweb.domain.CartItem;
import com.myproject.myweb.dto.coupon.CouponDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class CartItemDto {

    private Long id;
    private Long itemId;
    private String itemName;
    private int itemCount;
    private int itemPrice;
    private CouponDto coupon;

    private Long userId;
    private Long cartId;

    public CartItemDto(CartItem entity){
        this.id = entity.getId();
        this.itemId = entity.getItem().getId();
        this.itemName = entity.getItem().getName();
        this.itemCount = entity.getCount();
        this.itemPrice = entity.getItem().getPrice(); // 현재 상품 가격으로 장바구니
        if(entity.getCoupon() != null) this.coupon = new CouponDto(entity.getCoupon());

        this.userId = entity.getCart().getUser().getId();
        this.cartId = entity.getCart().getId();

    }
}

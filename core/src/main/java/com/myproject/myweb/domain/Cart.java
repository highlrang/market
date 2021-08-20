package com.myproject.myweb.domain;

import com.myproject.myweb.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "cart") // mappedBy 안 해줄 경우 <cartItem> 테이블 생김 >> cartItem 필요해서 새로 생성
    private List<CartItem> cartItems;

    public void setUser(User user){
        this.user = user;
        user.setCart(this);
    }

    public static Cart createCart(User user, CartItem... cartItems){
        Cart cart = new Cart();
        cart.setUser(user);
        cart.addCartItem(cartItems);
        return cart;
    }

    public void addCartItem(CartItem... cartItems){ // Cart 최초 생성 시 + 추가로 장바구니 상품 추가 시
        for(CartItem cartItem: cartItems){
            cartItem.setCart(this); // cartItem에 cart setting && cart에 cartItem add
        }
    }

    public int getTotalPrice(){
        return cartItems.stream().mapToInt(CartItem::getTotalPrice).sum();
    }

}

package com.myproject.myweb.domain;

import com.myproject.myweb.domain.user.Customer;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    // mappedBy 안 해줄 경우 <cartItem> 테이블 생김 >> cartItem 필요해서 새로 생성
    private List<CartItem> cartItems = new ArrayList<>();

    public void setCustomer(Customer customer){
        this.customer = customer;
        customer.setCart(this);
    }

    public static Cart createCart(Customer customer, CartItem cartItem){
        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.addCartItem(cartItem);
        return cart;
    }

    public void addCartItem(CartItem cartItem){ // Cart 최초 생성 시 + 추가로 장바구니 상품 추가 시
        cartItems.add(cartItem);
        cartItem.setCart(this);
    }

    public void removeCartItems(List<CartItem> cartItems){
        cartItems.forEach(cartItem -> this.cartItems.remove(cartItem));
    }

    public int getTotalPrice(){
        return cartItems.stream().mapToInt(CartItem::getTotalPrice).sum();
    }

}

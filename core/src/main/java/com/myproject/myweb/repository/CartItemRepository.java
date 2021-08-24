package com.myproject.myweb.repository;

import com.myproject.myweb.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByCart_Id(Long cartId);

    @Query("select ci.count from CartItem ci where ci.user.id =:userId and ci.item.id in :ids")
    List<Integer> findCountByUser_IdAndItem_Id(Long userId, List<Long> ids);
}

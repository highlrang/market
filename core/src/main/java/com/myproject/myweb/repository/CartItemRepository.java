package com.myproject.myweb.repository;

import com.myproject.myweb.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByCart_Id(Long cartId);

    @Query("select ci.count from CartItem ci join ci.cart c join c.customer u join ci.item i where u.id =:customerId and i.id in :itemIds")
    List<Integer> findCountByCustomer_IdAndItem_Id(@Param("customerId") Long customerId, @Param("itemIds") List<Long> itemIds);
}

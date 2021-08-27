package com.myproject.myweb.repository;

import com.myproject.myweb.domain.Order;
import com.myproject.myweb.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o join fetch o.user u where u.id =:userId and o.orderStatus =:status")
    Optional<Order> findByUserAndStatusReady(@Param(value = "userId") Long userId, @Param(value = "status") OrderStatus orderStatus);

    List<Order> findAllByUser_Id(Long userId);
}

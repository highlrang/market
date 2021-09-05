package com.myproject.myweb.repository;

import com.myproject.myweb.domain.Order;
import com.myproject.myweb.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByCustomer_IdAndOrderStatus(Long customerId, OrderStatus status);

    List<Order> findAllByCustomer_Id(Long customerId);
}

package com.myproject.myweb.repository;

import com.myproject.myweb.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}

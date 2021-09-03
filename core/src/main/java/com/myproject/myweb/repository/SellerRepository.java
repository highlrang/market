package com.myproject.myweb.repository;

import com.myproject.myweb.domain.user.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByEmail(String email);
}

package com.myproject.myweb.repository;

import com.myproject.myweb.domain.Notice;
import com.myproject.myweb.domain.SellerNotice;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SellerNoticeRepository extends JpaRepository<SellerNotice, Long> {

    Integer countBySellerAndConfirmFalse(Seller seller);
    Page<SellerNotice> findAllBySeller(Seller seller, Pageable pageable);

    List<SellerNotice> findAllByDatetimeBefore(LocalDateTime dateTime);
}

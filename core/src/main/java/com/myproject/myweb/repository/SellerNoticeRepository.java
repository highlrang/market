package com.myproject.myweb.repository;

import com.myproject.myweb.domain.Notice;
import com.myproject.myweb.domain.SellerNotice;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerNoticeRepository extends JpaRepository<SellerNotice, Long> {

    List<SellerNotice> findAllBySeller(Seller seller);
}

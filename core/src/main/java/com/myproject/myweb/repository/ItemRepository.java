package com.myproject.myweb.repository;

import com.myproject.myweb.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>{

    List<Item> findAllBySeller_Id(Long sellerId);
}

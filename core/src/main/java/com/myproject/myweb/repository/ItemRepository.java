package com.myproject.myweb.repository;

import com.myproject.myweb.domain.Category;
import com.myproject.myweb.domain.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>{

    Page<Item> findAllBySeller_IdAndCategory(Long sellerId, Category category, Pageable pageable);

    Page<Item> findAllByCategory(Category category, Pageable pageable);
}

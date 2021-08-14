package com.myproject.myweb.repository;

import com.myproject.myweb.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long>{

}

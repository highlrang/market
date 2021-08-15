package com.myproject.myweb.service;

import com.myproject.myweb.domain.ItemDetail;
import com.myproject.myweb.domain.Photo;
import com.myproject.myweb.dto.item.ItemRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired ItemService itemService;
}
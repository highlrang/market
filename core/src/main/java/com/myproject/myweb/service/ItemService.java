package com.myproject.myweb.service;

import com.myproject.myweb.dto.item.ItemRequestDto;
import com.myproject.myweb.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

}

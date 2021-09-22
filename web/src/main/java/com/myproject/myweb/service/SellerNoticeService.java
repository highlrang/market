package com.myproject.myweb.service;

import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.notice.SellerNoticeDto;
import com.myproject.myweb.repository.SellerNoticeRepository;
import com.myproject.myweb.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerNoticeService {

    private final SellerRepository sellerRepository;
    private final SellerNoticeRepository sellerNoticeRepository;

    public Long countUnreadBySeller(Long sellerId){
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException")); // 에러 처리
        return sellerNoticeRepository.findAllBySeller(seller).stream()
                .filter(notice -> !notice.getConfirm())
                .count();
    }

    public List<SellerNoticeDto> findAllBySeller(Long sellerId){
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException")); // 에러 처리

        return sellerNoticeRepository.findAllBySeller(seller)
                .stream()
                .map(SellerNoticeDto::SellerNoticeResponseDto)
                .collect(Collectors.toList());
    }
}

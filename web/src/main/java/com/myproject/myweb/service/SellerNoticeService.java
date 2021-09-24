package com.myproject.myweb.service;

import com.myproject.myweb.domain.SellerNotice;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.notice.SellerNoticeDto;
import com.myproject.myweb.repository.SellerNoticeRepository;
import com.myproject.myweb.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerNoticeService {

    private final SellerRepository sellerRepository;
    private final SellerNoticeRepository sellerNoticeRepository;

    public Integer countUnreadBySeller(Long sellerId){
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));
        return sellerNoticeRepository.countBySellerAndConfirmFalse(seller);
                /*
                .filter(notice -> !notice.getConfirm())
                .count();
                 */
    }

    public ItemService.ListByPaging<SellerNoticeDto> findAllBySeller(Long sellerId, Pageable pageable){
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
        Page<SellerNotice> noticeList = sellerNoticeRepository.findAllBySeller(seller, pageRequest);

        return new ItemService.ListByPaging<>(
                noticeList.getTotalPages(),
                noticeList.getContent().stream()
                        .map(SellerNoticeDto::SellerNoticeResponseDto)
                        .collect(Collectors.toList())
        );
    }

    @Transactional
    public void readNotice(Long id){
        SellerNotice notice = sellerNoticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NoticeNotFoundException"));
        notice.confirmed();
    }

    @Transactional
    public void remove(Long id){
        sellerNoticeRepository.deleteById(id);
    }
}

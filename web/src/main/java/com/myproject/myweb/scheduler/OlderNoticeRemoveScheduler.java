package com.myproject.myweb.scheduler;

import com.myproject.myweb.domain.SellerNotice;
import com.myproject.myweb.repository.SellerNoticeRepository;
import com.myproject.myweb.service.SellerNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OlderNoticeRemoveScheduler {

    private final SellerNoticeRepository sellerNoticeRepository;
    // CustomerNoticeService

    @Scheduled(cron = "0 0 1 * * *")
    public void noticeRemove(){
        List<SellerNotice> sellerNoticeList = sellerNoticeRepository.findAllByDatetimeBefore(LocalDateTime.now().minusYears(1));
        sellerNoticeRepository.deleteAllInBatch(sellerNoticeList);
    }
}

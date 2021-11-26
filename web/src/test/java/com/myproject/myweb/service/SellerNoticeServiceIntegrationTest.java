package com.myproject.myweb.service;

import com.myproject.myweb.domain.Order;
import com.myproject.myweb.domain.SellerNotice;
import com.myproject.myweb.domain.user.Seller;
import com.myproject.myweb.dto.notice.SellerNoticeDto;
import com.myproject.myweb.repository.SellerNoticeRepository;
import com.myproject.myweb.repository.SellerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class SellerNoticeServiceIntegrationTest {

    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private SellerNoticeRepository sellerNoticeRepository;
    @Autowired
    private SellerNoticeService sellerNoticeService;

    @Test
    public void count_unread_notice(){
        Seller seller = sellerRepository.save(
                Seller.builder()
                        .name("test seller")
                        .email("test email")
                        .password("test pw")
                        .build()
        );
        SellerNotice sellerNotice = sellerNoticeRepository.save(
                SellerNotice.builder()
                        .seller(seller)
                        .title("test notice")
                        .build()
        );
        sellerNotice.setConfirm(true);

        Integer cnt = sellerNoticeService.countUnreadBySeller(seller.getId());
        assertThat(cnt).isEqualTo(0);
    }

    @Test
    public void list_by_seller(){
        int offset = 0;
        int size = 5;
        int total = 6;
        Seller seller = sellerRepository.save(Seller.builder()
                .name("test seller")
                .email("test email")
                .password("test pw")
                .build());

        for(int i=0; i<total; i++) {
            sellerNoticeRepository.save(SellerNotice.builder()
                    .seller(seller)
                    .title("test notice " + i)
                    .build()
            );
        }

        PageRequest pageRequest = PageRequest.of(offset, size, Sort.by(Sort.Direction.DESC, "id"));
        ItemService.ListByPaging<SellerNoticeDto> result =
                sellerNoticeService.findAllBySeller(seller.getId(), pageRequest);

        int page;
        if(total%size==0){
            page=total/size;
        }else{
            page=total/size + 1;
        }
        assertThat(result.getTotalPage()).isEqualTo(page);
        assertThat(result.getList().size()).isEqualTo(size);
        assertThat(result.getList().get(0).getTitle()).contains("test notice");
    }

}

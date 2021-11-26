package com.myproject.myweb.dto.notice;

import com.myproject.myweb.domain.SellerNotice;
import com.myproject.myweb.dto.user.SellerResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class SellerNoticeDto extends NoticeDto {
    private SellerResponseDto seller;

    public SellerNoticeDto(SellerResponseDto seller, String title, String content){ //request
        this.seller = seller;
        this.setTitle(title);
        this.setContent(content);
    }

    public static SellerNoticeDto SellerNoticeResponseDto(SellerNotice entity){
        SellerNoticeDto sellerNotice = new SellerNoticeDto();
        sellerNotice.setId(entity.getId());
        sellerNotice.seller = new SellerResponseDto(entity.getSeller());
        sellerNotice.setTitle(entity.getTitle());
        if (entity.getContent() != null) sellerNotice.setContent(entity.getContent());
        sellerNotice.setConfirm(entity.getConfirm());
        sellerNotice.setDatetime(entity.getDatetime());
        return sellerNotice;
    }
}

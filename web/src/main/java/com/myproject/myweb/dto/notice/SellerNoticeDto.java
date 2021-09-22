package com.myproject.myweb.dto.notice;

import com.myproject.myweb.domain.SellerNotice;
import com.myproject.myweb.dto.user.SellerResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SellerNoticeDto extends NoticeDto {
    private SellerResponseDto seller;

    public SellerNoticeDto(SellerResponseDto seller, String title, String content){ //request
        this.seller = seller;
        this.setTitle(title);
        this.setContent(content);
    }

    public static SellerNoticeDto SellerNoticeResponseDto(SellerNotice entity) { //response
        SellerNoticeDto dto = new SellerNoticeDto();
        dto.setId(entity.getId());
        dto.seller = new SellerResponseDto(entity.getSeller());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setConfirm(entity.getConfirm());
        dto.setDatetime(entity.getDatetime());
        return dto;
    }
}

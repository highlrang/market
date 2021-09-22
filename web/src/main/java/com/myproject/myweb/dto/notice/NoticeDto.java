package com.myproject.myweb.dto.notice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public abstract class NoticeDto {
    private Long id;
    private String title;
    private String content;
    private Boolean confirm;
    private LocalDateTime datetime;
}

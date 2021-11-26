package com.myproject.myweb.dto.notice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter @Setter
public abstract class NoticeDto {
    private Long id;
    @NotBlank(message = "필수 입력값입니다.")
    private String title;
    private String content;
    private Boolean confirm;
    private LocalDateTime datetime;
}

package com.myproject.myweb.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public enum DeliveryStatus {
    READY("배송 준비 중"), PROGRESS("배송 중"), COMP("배송 완료");

    private final String name;
}

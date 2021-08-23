package com.myproject.myweb.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public enum OrderStatus {

    READY("주문 중"), COMP("주문 완료"), CANCEL("주문 취소");

    private final String name;
}

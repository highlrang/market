package com.myproject.myweb.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {

    CLOTHES("의류"), HOUSEHOLD_GOODS("생활용품"), LEISURE_GOODS("여가용품"),
    ANIMAL_GOODS("동물용품"), CLEANING_SUPPLIES("청소용품"),
    KITCHEN_UTENSILS("주방용품"), BATHROOM_SUPPLIES("욕실용품"),

    ELECTRONICS("전자기기"), INTERIOR_GOODS("인테리어 용품"), FURNITURE("가구"),
    STATIONERY("문구"), COSMETICS("화장품"), FOOD("음식"), BOOKS("도서");

    private final String value;

    public String getKey(){
        return name();
    }

}

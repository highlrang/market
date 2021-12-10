package com.myproject.myweb.domain.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    private String postCode;
    private String address;
    private String detailAddress;
    private String extraAddress;

    @Builder
    public Address(String postCode, String address, String detailAddress, String extraAddress){
        this.postCode = postCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.extraAddress = extraAddress;
    }
}

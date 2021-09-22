package com.myproject.myweb.domain;


import com.myproject.myweb.domain.user.Seller;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
public class SellerNotice extends Notice {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @Builder
    public SellerNotice(Seller seller, String title, String content){
        this.seller = seller;
        this.setTitle(title);
        this.setContent(content);
        this.setConfirm(false);
    }
}

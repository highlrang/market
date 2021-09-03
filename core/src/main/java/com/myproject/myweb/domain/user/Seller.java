package com.myproject.myweb.domain.user;

import com.myproject.myweb.domain.Item;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Seller extends User{

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Item> itemList = new ArrayList<>();
}

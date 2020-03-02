package com.bos.register.entity.bosent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Builder
@Table(name = "seller", schema = "public")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SellerDim {
    @Id
    @Column(name = "id_seller")
    private Integer idSeller;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "card_number")
    private String cardNum;

    @Column(name = "phone")
    private String phone;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "flag")
    private Integer flag;
}

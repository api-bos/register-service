package com.bos.register.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import javax.persistence.*;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "flag")
    private Integer flag;

    @Column(name = "id_kab_kota")
    private Integer idKabKota;

    @Column(name = "account_no")
    private String accountNo;
}

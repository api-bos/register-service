package com.bos.register.entity;

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
@Table(name = "nasabah_dummy", schema = "public")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class NasabahDim {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "card_no", nullable = true)
    private String cardNo;

    @Column(name = "nama", nullable = true)
    private String nama;

    @Column(name = "phone", nullable = true)
    private String phone;
}

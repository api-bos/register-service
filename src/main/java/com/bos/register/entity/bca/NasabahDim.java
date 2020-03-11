package com.bos.register.entity.bca;

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
//@Table(name = "nasabah_dummy", schema = "public")
//@Table(name = "account_curr_dimension_dummy", schema = "public")
@Table(name = "bit9_acct_curr_dim_dummy", schema = "bit")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class NasabahDim {
    @Id
    @Column(name = "acct_curr_key")
    private Integer acctCurrKey;

    @Column(name = "acct_name", nullable = true)
    private String acctName;

    @Column(name = "atm_card_no", nullable = true)
    private String atmCardNo;

    @Column(name = "mobile_num", nullable = true)
    private String mobileNum;

    @Column(name = "acct_card_no", nullable = true)
    private String acctCardNo;
}

package com.bos.register.repository.bca;

import com.bos.register.entity.bca.NasabahDim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface NasabahRepo extends JpaRepository <NasabahDim,Integer> {
    List<NasabahDim> findAll();

    @Query("SELECT nd.atmCardNo FROM NasabahDim nd WHERE nd.atmCardNo = :cardNo")
    String findCardNo(@Param("cardNo") String cardNo);

    @Query("SELECT nd.mobileNum FROM NasabahDim nd WHERE nd.atmCardNo = :cardNo")
    String getPhoneByCardNo(@Param("cardNo") String cardNo);

    @Query(value = "SELECT * FROM bit9_acct_curr_dim_dummy WHERE atm_card_no = :cardNo", nativeQuery = true)
    NasabahDim getNasabahByCardNo(@Param("cardNo") String cardNo);
}

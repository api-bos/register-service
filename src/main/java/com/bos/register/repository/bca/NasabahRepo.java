package com.bos.register.repository.bca;

import com.bos.register.entity.NasabahDim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface NasabahRepo extends JpaRepository <NasabahDim,Integer> {
    List<NasabahDim> findAll();

    @Query("SELECT nd.atmCardNo FROM NasabahDim nd WHERE nd.atmCardNo = :cardNo")
    String findCardNo(@Param("cardNo") String cardNo);

    @Query("SELECT nd.mobileNum FROM NasabahDim nd WHERE nd.atmCardNo = :cardNo")
    String getPhoneByCardNo(@Param("cardNo") String cardNo);

    @Query(value = "SELECT * FROM account_curr_dimension_dummy WHERE atm_card_no = :cardNo", nativeQuery = true)
    NasabahDim getNasabahByCardNo(@Param("cardNo") String cardNo);
}

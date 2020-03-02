package com.bos.register.repository.bcadb;

import com.bos.register.entity.bcaent.NasabahDim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface NasabahRepo extends JpaRepository <NasabahDim,Integer> {
    List<NasabahDim> findAll();

    @Query("SELECT nd.cardNo FROM NasabahDim nd WHERE nd.cardNo = :cardNo")
    String findCardNo(@Param("cardNo") String cardNo);

    @Query("SELECT nd.phone FROM NasabahDim nd WHERE nd.cardNo = :cardNo")
    String getPhoneByCardNo(@Param("cardNo") String cardNo);

    @Query(value = "SELECT * FROM nasabah_dummy WHERE card_no = :cardNo", nativeQuery = true)
    NasabahDim getNasabahByCardNo(@Param("cardNo") String cardNo);
}

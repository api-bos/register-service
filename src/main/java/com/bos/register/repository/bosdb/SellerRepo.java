package com.bos.register.repository.bosdb;

import com.bos.register.entity.bosent.SellerDim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface SellerRepo extends JpaRepository<SellerDim,Integer> {
    @Query("SELECT cardNo FROM SellerDim WHERE cardNo = :cardNo")
    String findCardNo(@Param("cardNo") String cardNo);

    @Transactional
    @Modifying
    @Query("UPDATE SellerDim SET flag = :flag WHERE username = :username")
    void updateFlagByUsername(@Param("flag") int flag, @Param("username") String username);

    @Query("SELECT flag FROM SellerDim WHERE username = :username")
    Integer getFlagByUsername(@Param("username") String username);

    @Query("SELECT flag FROM SellerDim WHERE cardNo = :cardNo")
    Integer getFlagByCardNo(@Param("cardNo") String cardNo);
}

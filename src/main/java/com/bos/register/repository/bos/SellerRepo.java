package com.bos.register.repository.bos;

import com.bos.register.entity.bos.SellerDim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface SellerRepo extends JpaRepository<SellerDim,Integer> {
    List<SellerDim> findAll();

    @Query(value = "SELECT sd.cardNum FROM SellerDim sd WHERE sd.cardNum = :cardNum")
    String findCardNum(@Param("cardNum") String cardNum);

    @Transactional
    @Modifying
    @Query("UPDATE SellerDim SET flag = :flag WHERE username = :username")
    void updateFlagByUsername(@Param("flag") int flag, @Param("username") String username);

    @Query("SELECT flag FROM SellerDim WHERE username = :username")
    Integer getFlagByUsername(@Param("username") String username);

    @Query("SELECT flag FROM SellerDim WHERE cardNum = :cardNo")
    Integer getFlagByCardNo(@Param("cardNo") String cardNo);
}

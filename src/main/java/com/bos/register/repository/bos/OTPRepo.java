package com.bos.register.repository.bos;

import com.bos.register.entity.bos.OTPDim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface OTPRepo extends JpaRepository<OTPDim, String> {
    @Query("SELECT username FROM OTPDim WHERE username = :username")
    String findUsername(@Param("username") String username);

    @Transactional
    @Modifying
    @Query("DELETE FROM OTPDim WHERE username = :username")
    void deleteByUsername(@Param("username") String username);

    @Query("SELECT COUNT(username) FROM OTPDim WHERE username = :username")
    int getCountByUsername(@Param("username") String username);

    @Transactional
    @Modifying
    @Query("UPDATE OTPDim SET flag = :flag WHERE username = :username")
    void updateFlag(@Param("flag") Integer flag, @Param("username") String username);

    @Query(value = "SELECT * FROM OTPDim WHERE username = :username AND flag = :flag", nativeQuery = true)
    OTPDim findOTPByUsername(@Param("username") String username, @Param("flag") Integer flag);
}

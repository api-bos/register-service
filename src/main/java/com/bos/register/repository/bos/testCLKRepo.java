package com.bos.register.repository.bos;

import com.bos.register.entity.testCLK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface testCLKRepo extends JpaRepository<testCLK,Integer> {
    List<testCLK> findAll();
}

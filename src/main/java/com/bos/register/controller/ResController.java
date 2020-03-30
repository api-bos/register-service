package com.bos.register.controller;

import bca.bit.proj.library.base.ResultEntity;
import com.bos.register.dto.RegisterField;
import com.bos.register.entity.bca.NasabahDim;
import com.bos.register.entity.bos.SellerDim;
import com.bos.register.service.RegiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/regis")
public class ResController {
    @Autowired
    RegiService service;

    @PostMapping("/sOTP")
    public ResultEntity sendOTP(@RequestBody RegisterField registerField) throws Exception {
        System.out.println("Trying Send OTP ServiceS");
        return service.sendOTP(registerField);
    }

    @GetMapping(value = "/nasabah/{cardNo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultEntity<NasabahDim> getNasByCardNo(@PathVariable("cardNo") String cardNo){
        System.out.println("CARD NO Request: " + cardNo);
        System.out.println("Trying Request");

        return service.getNasabahByCardNo(cardNo);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultEntity<List<NasabahDim>> getAllNas(){
        System.out.println("Trying Request");
        return service.getAllNasabah();
    }

    @GetMapping(value = "/seller", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultEntity<List<SellerDim>> getAllSeller(){
        System.out.println("SELLER");
        return service.getAllSeller();
    }
}

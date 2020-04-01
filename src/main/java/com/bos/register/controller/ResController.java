package com.bos.register.controller;

import bca.bit.proj.library.base.ResultEntity;
import com.bos.register.dto.RegisterField;
import com.bos.register.dto.RegisterVerif;
import com.bos.register.entity.SellerDim;
import com.bos.register.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/bos/regis")
public class ResController {
    @Autowired
    RegisterService service;

    @PostMapping("/sOTP")
    public ResultEntity sendOTP(@RequestBody RegisterField registerField) {
        System.out.println("Trying Send OTP Service");
        return service.sendOTP(registerField);
    }

    @PostMapping("/sOTP/{phone}")
    public ResultEntity resendOTP(@PathVariable("phone") String phone){
        System.out.println("Trying reSend OTP Service");
        return service.resendOTP(phone);
    }

    @PostMapping("/vOTP")
    public ResultEntity verifOTP(@RequestBody RegisterVerif registerVerif){
        System.out.println("Trying Verification OTP Service");
        return service.verifOTP(registerVerif);
    }

    @GetMapping(value = "/seller", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultEntity<List<SellerDim>> getAllSeller(){
        System.out.println("SELLER");
        return service.getAllSeller();
    }
}

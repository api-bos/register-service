package com.bos.register.service;

import bca.bit.proj.library.base.ResultEntity;
import bca.bit.proj.library.enums.ErrorCode;
import com.bos.register.dto.RegisterField;
import com.bos.register.entity.NasabahDim;
import com.bos.register.entity.OTPDim;
import com.bos.register.entity.SellerDim;
import com.bos.register.entity.testCLK;
import com.bos.register.repository.NasabahRepo;
import com.bos.register.repository.OTPRepo;
import com.bos.register.repository.SellerRepo;
import com.bos.register.repository.testCLKRepo;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class RegiService {
    @Autowired
    NasabahRepo nasRepo;
    @Autowired
    OTPRepo otpRepo;
    @Autowired
    SellerRepo sellRepo;

    @Autowired
    testCLKRepo test;

    private void initMessageSender(){
        String l_ACCOUNT_SID = "AC17cb2fb0f7fd9bfe9b4b619d19b79031";
        String l_AUTH_TOKEN = "d2236be02b13d3fb5ad1b5f7bcfa148f";
        Twilio.init(l_ACCOUNT_SID, l_AUTH_TOKEN);
        System.out.println("Done init twilio");
    }

    private int createRandomNumber(){
        Random random = new Random();
        StringBuilder tmp_randomNumber = new StringBuilder();

        for (int i=0; i<4; i++){
            tmp_randomNumber.append(random.nextInt(10));
        }
        System.out.println("OTP: " + tmp_randomNumber);
        return Integer.parseInt(tmp_randomNumber.toString());
    }

    private void sendOTP(String p_username, String p_phoneNumber){
        String l_message;
        String l_otpCode = "0";
        boolean l_checkUniqueOTP = false;

        //Create random number yg tidak terdaftar di db
        while (!l_checkUniqueOTP){
            l_otpCode = Integer.toString(createRandomNumber());

            if (otpRepo.findById(l_otpCode).equals(Optional.empty())){
                OTPDim otp = new OTPDim();
                otp.setId(l_otpCode);
                otp.setUsername(p_username);
                otp.setFlag(0);
                otpRepo.save(otp);

                l_checkUniqueOTP = true;
            }
        }

        l_message = "Your OTP code is " + l_otpCode + ", only valid for 3 minutes";
        initMessageSender();

        try{
            Message.creator(new PhoneNumber(p_phoneNumber), new PhoneNumber("+18175063556"), l_message).create();
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }

    public ResultEntity<NasabahDim> getNasabahByCardNo(String cardNo){
        NasabahDim nasdim = nasRepo.getNasabahByCardNo(cardNo);

        if(nasdim != null){
            System.out.println("Data is not NULL");
            return new ResultEntity<>(nasdim,ErrorCode.B000);
        }
        else {
            System.out.println("Data is NULL");
            return new ResultEntity<>(null,ErrorCode.B999);
        }
    }

    public ResultEntity<List<NasabahDim>> getAllNasabah(){
        List<NasabahDim> data = nasRepo.findAll();

        if (data.size() > 0){
            return new ResultEntity<>(data, ErrorCode.B000);
        }
        else {
            return new ResultEntity<>(data, ErrorCode.B999);
        }
    }

    public ResultEntity<List<testCLK>> getAlltest(){
        List<testCLK> data = test.findAll();

        if (data.size() > 0){
            return new ResultEntity<>(data, ErrorCode.B000);
        }
        else {
            return new ResultEntity<>(data, ErrorCode.B999);
        }
    }

    public ResultEntity sendOTP(RegisterField registerField){
        String msg;
        boolean l_sendOTPFlag;
        String l_phoneNumber;

        //Cek nasabah harus terdaftar di db
        if (registerField.getCard_no().equals(nasRepo.findCardNo(registerField.getCard_no()))){
            //Get phone number dari BCA db
            l_phoneNumber = nasRepo.getPhoneByCardNo(registerField.getCard_no());

            //Check nasabah if exist as seller
            if (registerField.getCard_no().equals(nasRepo.findCardNo(registerField.getCard_no())) &&
                    sellRepo.getFlagByCardNo(registerField.getCard_no()) < 3){

                //Check OTP sudah ter-generate berapa kali (maks 3) berdasarkan username?
                if (otpRepo.getCountByUsername(registerField.getUsername()) == 0){
                    l_sendOTPFlag = true;

                }else if (otpRepo.getCountByUsername(registerField.getUsername()) < 3) {
                    otpRepo.updateFlag(1, registerField.getUsername());
                    l_sendOTPFlag = true;

                }else{
                    msg = "OTP dgn username " + registerField.getUsername() + ", sudah generate 3x";
                    return new ResultEntity(msg,ErrorCode.B999);
                }

                //Check nasabah sudah terdaftar as seller?
            }else if (registerField.getCard_no().equals(sellRepo.findCardNo(registerField.getCard_no())) &&
                    sellRepo.getFlagByCardNo(registerField.getCard_no()) == 4) {
                msg = "Nasabah dengan no kartu ATM: " + registerField.getCard_no() + " sudah terdaftar sebagai seller";
                return new ResultEntity(msg, ErrorCode.B000);

            }else {
                try{
                    //Get nasabah from db by kartuID
                    NasabahDim tmp_nasabah = nasRepo.getNasabahByCardNo(registerField.getCard_no());

                    //Save data to db (table seller)
                    SellerDim tmp_seller = new SellerDim();
                    tmp_seller.setCardNo(tmp_nasabah.getCardNo());
                    tmp_seller.setNama(tmp_nasabah.getNama());
                    tmp_seller.setPhone(tmp_nasabah.getPhone());
                    tmp_seller.setUsername(registerField.getUsername());
                    tmp_seller.setPassword(registerField.getPassword());
                    tmp_seller.setFlag(0);
                    sellRepo.save(tmp_seller);

                    l_sendOTPFlag = true;

                }catch (Exception e){
                    msg = "Username sudah digunakan";
                    return new ResultEntity(msg, ErrorCode.B999);
                }
            }

        }else{
            msg = "No Kartu ATM tidak terdaftar";
            return new ResultEntity(msg,ErrorCode.B999);
        }

        //Send OTP
        if (l_sendOTPFlag){
            sendOTP(registerField.getUsername(), l_phoneNumber);
            msg = "Berhasil send OTP";
            return new ResultEntity(msg, ErrorCode.B000);

        }else{
            msg = "Tidak berhasil send OTP";
            return new ResultEntity<>(msg, ErrorCode.B999);
        }
    }
}

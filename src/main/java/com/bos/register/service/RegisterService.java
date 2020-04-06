package com.bos.register.service;

import bca.bit.proj.library.base.ResultEntity;
import bca.bit.proj.library.enums.ErrorCode;
import com.bos.register.config.twillio.CustomNetworkClient;
import com.bos.register.config.twillio.TwilioUtil;
import com.bos.register.dto.RegisterField;
import com.bos.register.dto.RegisterVerif;
import com.bos.register.dto.Verif;
import com.bos.register.entity.OTPDim;
import com.bos.register.entity.SellerDim;
import com.bos.register.repository.OTPRepo;
import com.bos.register.repository.SellerRepo;
import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class RegisterService {
    @Autowired
    OTPRepo otpRepo;
    @Autowired
    SellerRepo sellRepo;

    private void initMessageSender() {
        CustomNetworkClient newHttpClient = new CustomNetworkClient();
        Twilio.init(TwilioUtil.ACCOUNT_SID, TwilioUtil.AUTH_TOKEN);
        TwilioRestClient client = new TwilioRestClient
                .Builder(TwilioUtil.ACCOUNT_SID, TwilioUtil.AUTH_TOKEN)
                .httpClient(newHttpClient)
                .build();
        Twilio.setRestClient(client);

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

    private boolean sendOTP(String p_username, String p_phoneNumber) {
        String l_message;
        String l_otpCode = "0";
        boolean l_checkUniqueOTP = false;

        //Create random number yg tidak terdaftar di db
        while (!l_checkUniqueOTP){
            l_otpCode = Integer.toString(createRandomNumber());

            if (otpRepo.findById(l_otpCode).equals(Optional.empty())){
                OTPDim otp = new OTPDim();
                otp.setOtpCode(l_otpCode);
                otp.setUsername(p_username);
                otp.setFlag(0);
                otpRepo.save(otp);

                l_checkUniqueOTP = true;
            }
        }

        l_message = "Your OTP code is " + l_otpCode + ", only valid for 3 minutes";
        initMessageSender();

        try{
            System.out.println("Trying to send OTP");
            Message.creator(new PhoneNumber(p_phoneNumber), new PhoneNumber("+18175063556"), l_message).create();
            System.out.println("Send OTP Success");
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private Integer isSeller(String username, String acctNo, String phone){
        Integer status;

        String vUsername = sellRepo.findUsername(username);
        String vAcctNo = sellRepo.findAcctNo(acctNo);
        String vPhone = sellRepo.findPhone(phone);

        System.out.println("\nusename: "+vUsername);
        System.out.println("account_no: "+vAcctNo);
        System.out.println("phone: "+vPhone);
        System.out.println();

        if(vUsername!=null && !vUsername.isEmpty()) status = 1;
        else if(vAcctNo!=null && !vAcctNo.isEmpty()) status = 2;
        else if(vPhone!=null && !vPhone.isEmpty()) status = 3;
        else status = 0;

        return status;
    }

    private Integer isNasabah(String acctNo, String phone){
        String result;
//        final String uri = "http://localhost:4422/bos/verif";
        final String uri = "http://dummybca.apps.pcf.dti.co.id/bos/verif";
        RestTemplate restTemplate = new RestTemplate();

        //setting up the request headers
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        //setting up the request body
        Verif verif = new Verif();
        verif.setAcctNo(acctNo);
        verif.setMobileNum(phone);

        //request entity is created with request body and headers
        HttpEntity<Verif> requestEntity = new HttpEntity<>(verif,requestHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if(responseEntity.getStatusCode() == HttpStatus.OK){
            System.out.println("CONNECTED TO BCA API");
            result = responseEntity.getBody();

        }
        else  {
            System.out.println("NOT CONNECTED TO BCA API");
            result = "4";
        }

        assert result != null;
        return Integer.valueOf(result);
    }

    public ResultEntity<String> sendOTP(RegisterField registerField) {
        System.out.println("rek no inserted: " + registerField.getNoRek());
        boolean otpStatus;

        String username = registerField.getBosId();
        String nama = registerField.getNama();
        String acctNo = registerField.getNoRek();
        String phone = registerField.getNoHp();
        String pw = new BCryptPasswordEncoder().encode(registerField.getPassword());

        Integer isSeller = isSeller(username,acctNo,phone);
        Integer isNasabah = isNasabah(acctNo,phone);
        System.out.println("isSeller: "+isSeller);
        System.out.println("isNasabah: "+isNasabah);

        if(isSeller == 1)
            return new ResultEntity<>("Username telah digunakan", ErrorCode.BIT_999);
        else if(isSeller == 2)
            return new ResultEntity<>("Account_no telah digunakan",ErrorCode.BIT_999);
        else if(isSeller == 3)
            return new ResultEntity<>("No_hp telah digunakan",ErrorCode.BIT_999);
        else if(isNasabah != 0){
            if(isNasabah == 1)
                return new ResultEntity<>("BCA -- INVALID MOBILE NUMBER",ErrorCode.BIT_999);
            else if(isNasabah == 2)
                return new ResultEntity<>("BCA -- INVALID ACCOUNT NO",ErrorCode.BIT_999);
            else return new ResultEntity<>("SYSTEM UNDER MAINTENANCE",ErrorCode.BIT_999);
        }
        else if(username == null || nama == null || acctNo == null || phone == null || pw == null){
            System.out.println("\nNULL FIELD");
            return new ResultEntity<>("Field tidak boleh kosong",ErrorCode.BIT_999);
        }
        else{
            SellerDim tmp_seller = new SellerDim();
            tmp_seller.setAccountNo(acctNo);
            tmp_seller.setName(nama);
            tmp_seller.setPhone(phone);
            tmp_seller.setUsername(username);
            tmp_seller.setPassword(pw);
            tmp_seller.setFlag(0);
            sellRepo.save(tmp_seller);

            //Send OTP
            otpStatus = sendOTP(username, phone);
            if(otpStatus){
                return new ResultEntity<>("Berhasil mengirim OTP", ErrorCode.BIT_000);
            }
            else return new ResultEntity<>("Gagal mengirim OTP", ErrorCode.BIT_999);
        }
    }

    public ResultEntity<String> resendOTP(String noHp){
        boolean otpStatus;
        String idBos = sellRepo.findUsernamebyPhone(noHp);
        Integer flagSeller = sellRepo.getFlagByUsername(idBos);
        System.out.println("username: "+idBos);
        System.out.println("flag: "+flagSeller);

        if(idBos == null) return new ResultEntity<>("Invalid no_hp", ErrorCode.BIT_999);
        if(flagSeller == 4) return new ResultEntity<>("User telah terverifikasi", ErrorCode.BIT_999);
        if(flagSeller == 3) return new ResultEntity<>("User terblockir selama 1 hari", ErrorCode.BIT_999);

        // Check flag & jumlah otp yg terkirim
        Integer countOTPByUsername = otpRepo.getCountByUsername(idBos);
        if (countOTPByUsername < 3){
            if(countOTPByUsername != 0){
                otpRepo.updateFlag(1, idBos);
            }

            //Send OTP
            otpStatus = sendOTP(idBos, noHp);
            if(otpStatus){
                return new ResultEntity<>("Berhasil mengirim OTP", ErrorCode.BIT_000);
            }
            else return new ResultEntity<>("Gagal mengirim OTP", ErrorCode.BIT_999);
        }
        else{
            String msg = "OTP dgn username " + idBos + ", sudah generate 3x";
            return new ResultEntity<>(msg,ErrorCode.BIT_999);
        }
    }

    public ResultEntity<String> verifOTP(RegisterVerif registerVerif){
        Integer otp = registerVerif.getOtp();
        String username = registerVerif.getBosId();
        System.out.println("otp: "+otp);
        System.out.println("username: "+username);

        Integer votp = otpRepo.findOTPByUsername(username,0);
        System.out.println("vOTP: "+votp);
        if(votp == null) return new ResultEntity<>("Invalid bos_id", ErrorCode.BIT_999);

        if(votp.equals(otp)){
            //Update flag OTP disable
            otpRepo.updateFlag(1, username);
            //update flag seller
            sellRepo.updateFlagByUsername(4, username);

            Integer idSeller = sellRepo.findIdSellerbyUsername(username);
            return new ResultEntity<>("id_seller: "+idSeller, ErrorCode.BIT_000);
        }
        else {
            Integer flagSeller = sellRepo.getFlagByUsername(username);

            if(flagSeller < 3){
                flagSeller = flagSeller +1;

                sellRepo.updateFlagByUsername(flagSeller, username);
                return new ResultEntity<>("Kode OTP salah, sisa coba: "+(3-flagSeller), ErrorCode.BIT_999);
            }
            else{
                otpRepo.updateFlag(1,username);
                return new ResultEntity<>("User terblockir selama 1 hari", ErrorCode.BIT_999);
            }
        }
    }

    public ResultEntity<List<SellerDim>> getAllSeller(){
        List<SellerDim> data = sellRepo.findAll();

        if (data.size() > 0){
            return new ResultEntity<>(data, ErrorCode.BIT_000);
        }
        else {
            return new ResultEntity<>(data, ErrorCode.BIT_999);
        }
    }
}

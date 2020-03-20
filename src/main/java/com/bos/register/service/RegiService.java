package com.bos.register.service;

import bca.bit.proj.library.base.ResultEntity;
import bca.bit.proj.library.enums.ErrorCode;
import com.bos.register.config.twillio.CustomNetworkClient;
import com.bos.register.config.twillio.TwilioUtil;
import com.bos.register.dto.RegisterField;
import com.bos.register.entity.bca.NasabahDim;
import com.bos.register.entity.bos.OTPDim;
import com.bos.register.entity.bos.SellerDim;
import com.bos.register.repository.bca.NasabahRepo;
import com.bos.register.repository.bos.OTPRepo;
import com.bos.register.repository.bos.SellerRepo;
import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
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

    /*
    public RestTemplate getProxyRestTemplate(){
        System.out.println("Coba Proxy");
        String proxyServerIpAddr = "10.1.10.47";
        int proxyServerPort = Integer.parseInt("8080");
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        String connectTO = "5000";
        String readTO = "5000";

        Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyServerIpAddr, proxyServerPort));

        System.out.println("Connect TO : " + connectTO);
        System.out.println("Read TO : " + readTO);

        httpRequestFactory.setConnectTimeout(Integer.parseInt(connectTO));
        httpRequestFactory.setReadTimeout(Integer.parseInt(readTO));
        httpRequestFactory.setProxy(proxy);
        return new RestTemplate(httpRequestFactory);
    }

     */

    private void initMessageSender() throws Exception {
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

    private boolean sendOTP(String p_username, String p_phoneNumber) throws Exception {
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
            return true;
        }catch (Exception e){
            System.out.println(e.toString());
            return false;
        }
    }

    public ResultEntity<NasabahDim> getNasabahByCardNo(String cardNo){
        NasabahDim nasdim = nasRepo.getNasabahByCardNo(cardNo);

        if(nasdim != null){
            System.out.println("Data is not NULL");
            return new ResultEntity<>(nasdim, ErrorCode.BIT_000);
        }
        else {
            System.out.println("Data is NULL");
            return new ResultEntity<>(null, ErrorCode.BIT_999);
        }
    }

    private boolean isNasabah(String cardNo){
        if(nasRepo.findCardNo(cardNo) == null){
            System.out.println("Not registered at BCA");
            return false;
        }else{
            System.out.println("Registered at BCA");
            return true;
        }
    }

    private boolean isSeller(String cardNo){
        if(sellRepo.findCardNum(cardNo) == null){
            System.out.println("Not registered as Seller");
            return false;
        }
        else {
            System.out.println("Registered as Seller");
            return true;
        }
    }



    public ResultEntity sendOTP(RegisterField registerField) throws Exception {
        System.out.println("card no inserted: " + registerField.getCard_no());
        String msg;
        String cardNo = registerField.getCard_no();
        String l_phoneNumber;
        boolean otpStatus;

        if(isSeller(cardNo) && (sellRepo.getFlagByCardNo(cardNo) < 3)){
            // Check flag & jumlah otp yg terkirim
            Integer countOTPByUsername = otpRepo.getCountByUsername(registerField.getUsername());
            if (countOTPByUsername < 3){
                if(countOTPByUsername != 0){
                    otpRepo.updateFlag(1, registerField.getUsername());
                }

                //Send OTP
                l_phoneNumber = nasRepo.getPhoneByCardNo(registerField.getCard_no());
                otpStatus = sendOTP(registerField.getUsername(), l_phoneNumber);
                if(otpStatus){
                    return new ResultEntity("Berhasil mengirim OTP", ErrorCode.BIT_000);
                }
                else return new ResultEntity("Gagal mengirim OTP", ErrorCode.BIT_999);
            }
            else{
                msg = "OTP dgn username " + registerField.getUsername() + ", sudah generate 3x";
                return new ResultEntity(msg,ErrorCode.BIT_999);
            }
        }
        else if(isSeller(cardNo) && (sellRepo.getFlagByCardNo(cardNo) == 4)){
            msg = "Nasabah dengan no kartu ATM: " + registerField.getCard_no() + " sudah terverifikasi sebagai seller";
            return new ResultEntity(msg, ErrorCode.BIT_000);
        }
        else{
            if(isNasabah(cardNo)){
                try{
                    //Get nasabah from db by kartuID
                    NasabahDim tmp_nasabah = nasRepo.getNasabahByCardNo(registerField.getCard_no());
                    System.out.println("masuk ke temp");

                    //Save data to db (table seller)
                    SellerDim tmp_seller = new SellerDim();
                    tmp_seller.setCardNum(tmp_nasabah.getAtmCardNo());
                    tmp_seller.setName(tmp_nasabah.getAcctName());
                    tmp_seller.setPhone(tmp_nasabah.getMobileNum());
                    tmp_seller.setUsername(registerField.getUsername());
                    tmp_seller.setPassword(registerField.getPassword());
                    tmp_seller.setAccountNo(tmp_nasabah.getAcctCardNo());
                    tmp_seller.setFlag(0);
                    sellRepo.save(tmp_seller);

                    //Send OTP
                    l_phoneNumber = nasRepo.getPhoneByCardNo(registerField.getCard_no());
                    otpStatus = sendOTP(registerField.getUsername(), l_phoneNumber);
                    if(otpStatus){
                        return new ResultEntity("Berhasil mengirim OTP", ErrorCode.BIT_000);
                    }
                    else return new ResultEntity("Gagal mengirim OTP", ErrorCode.BIT_999);
                }catch (Exception e){
                    e.printStackTrace();
                    msg = "System under maintenance";
                    return new ResultEntity(msg, ErrorCode.BIT_999);
                }
            }
            else{
                return new ResultEntity<>("Not registered at BCA", ErrorCode.BIT_999);
            }
        }

        /*
        //Cek nasabah harus terdaftar di db
        if (registerField.getCard_no().equals(nasRepo.findCardNo(registerField.getCard_no()))){
            System.out.println("Nasabah registered");

            //Get phone number dari BCA db
            l_phoneNumber = nasRepo.getPhoneByCardNo(registerField.getCard_no());
            System.out.println("phone: " + l_phoneNumber);

            //Check nasabah if exist as seller
            if (registerField.getCard_no().equals(
                    sellRepo.getFlagByCardNo(registerField.getCard_no())) &&
                    sellRepo.getFlagByCardNo(registerField.getCard_no()) < 3){
                System.out.println("exist as seller");

                //Check OTP sudah ter-generate berapa kali (maks 3) berdasarkan username?
                if (otpRepo.getCountByUsername(registerField.getUsername()) == 0){
                    l_sendOTPFlag = true;

                }else if (otpRepo.getCountByUsername(registerField.getUsername()) < 3) {
                    otpRepo.updateFlag(1, registerField.getUsername());
                    l_sendOTPFlag = true;

                }else{
                    l_sendOTPFlag = false;
                    msg = "OTP dgn username " + registerField.getUsername() + ", sudah generate 3x";
                    return new ResultEntity(msg,ErrorCode.BIT_999);
                }

                //Check nasabah sudah terdaftar as seller?
            }else if (registerField.getCard_no().equals(sellRepo.findCardNum(registerField.getCard_no())) &&
                    sellRepo.getFlagByCardNo(registerField.getCard_no()) == 4) {
                msg = "Nasabah dengan no kartu ATM: " + registerField.getCard_no() + " sudah terdaftar sebagai seller";
                return new ResultEntity(msg, ErrorCode.BIT_000);

            }else {

                try{
                    //Get nasabah from db by kartuID
                    NasabahDim tmp_nasabah = nasRepo.getNasabahByCardNo(registerField.getCard_no());
                    System.out.println("masuk ke temp");

                    //Save data to db (table seller)
                    SellerDim tmp_seller = new SellerDim();
                    tmp_seller.setCardNum(tmp_nasabah.getAtmCardNo());
                    tmp_seller.setName(tmp_nasabah.getAcctName());
                    tmp_seller.setPhone(tmp_nasabah.getMobileNum());
                    tmp_seller.setUsername(registerField.getUsername());
                    tmp_seller.setPassword(registerField.getPassword());
                    tmp_seller.setFlag(0);
                    sellRepo.save(tmp_seller);

                    l_sendOTPFlag = true;

                }catch (Exception e){
                    e.printStackTrace();
                    msg = "System under maintenance";
                    return new ResultEntity(msg, ErrorCode.BIT_999);
                }
            }

        }else{
            msg = "No Kartu ATM tidak terdaftar";
            return new ResultEntity(msg, ErrorCode.BIT_999);
        }

         */
    }

    public ResultEntity<List<NasabahDim>> getAllNasabah(){
        List<NasabahDim> data = nasRepo.findAll();

        if (data.size() > 0){
            return new ResultEntity<>(data, ErrorCode.BIT_000);
        }
        else {
            return new ResultEntity<>(data, ErrorCode.BIT_999);
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

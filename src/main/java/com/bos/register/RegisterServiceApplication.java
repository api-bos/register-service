package com.bos.register;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RegisterServiceApplication {
	public static void main(String[] args) {
		System.setProperty("http.proxyHost", "10.17.10.42");
		System.setProperty("http.proxyPort", "8080");
//		System.setProperty("https.proxyHost", "10.17.10.42");
//		System.setProperty("https.proxyPort", "8080");

		SpringApplication.run(RegisterServiceApplication.class, args);
	}

}

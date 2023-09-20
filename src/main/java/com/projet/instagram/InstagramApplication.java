package com.projet.instagram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
public class InstagramApplication {

	public static void main(String[] args) {
		SpringApplication.run(InstagramApplication.class, args);

		openSwagger();
	}

	private static void openSwagger() {
		System.setProperty("java.awt.headless", "false");
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.browse(new URI("http://localhost:8080/swagger-ui/index.html"));
		}
		catch(Exception e) {
			System.out.println("ERROR LOADING PAGE");
		}
	}

}

package com.example.momskitchen;

import org.springframework.boot.SpringApplication;

public class TestMomskitchenApplication {

	public static void main(String[] args) {
		SpringApplication.from(MomskitchenApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

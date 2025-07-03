package com.mmw.metal_micro_wire_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MetalMicroWireBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetalMicroWireBackendApplication.class, args);
	}

}

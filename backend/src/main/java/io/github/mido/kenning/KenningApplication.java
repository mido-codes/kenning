package io.github.mido.kenning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KenningApplication {

	public static void main(String[] args) {
		SpringApplication.run(KenningApplication.class, args);
	}

}

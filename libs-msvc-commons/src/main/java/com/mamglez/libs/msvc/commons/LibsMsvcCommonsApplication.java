package com.mamglez.libs.msvc.commons;

// import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class LibsMsvcCommonsApplication {

	// no se va a ejecutar, es una libreria
	// public static void main(String[] args) {
	// 	SpringApplication.run(LibsMsvcCommonsApplication.class, args);
	// }

}

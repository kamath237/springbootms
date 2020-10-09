package io.javabrains.zuulservice;

import io.javabrains.zuulservice.filters.pre.JWTValidationPreFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableZuulProxy
public class ZuulServiceApplication {

	@Bean
	public JWTValidationPreFilter preFilter() {
		return new JWTValidationPreFilter();
	}
	public static void main(String[] args) {
		SpringApplication.run(ZuulServiceApplication.class, args);
	}

}

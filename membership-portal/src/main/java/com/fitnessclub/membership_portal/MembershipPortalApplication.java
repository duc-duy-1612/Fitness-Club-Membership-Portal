package com.fitnessclub.membership_portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.fitnessclub.membership_portal", "com.fitnessclub.membershipportal"})
@EnableJpaRepositories(basePackages = "com.fitnessclub.membershipportal.repository")
@EntityScan(basePackages = "com.fitnessclub.membershipportal.entity")
public class MembershipPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(MembershipPortalApplication.class, args);
	}

}

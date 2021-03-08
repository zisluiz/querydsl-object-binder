package com.zisluiz.querydslbinder.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.zisluiz.querydslbinder.model.test.entity"})
public class QuerydslBinderTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuerydslBinderTestApplication.class, args);
	}

}
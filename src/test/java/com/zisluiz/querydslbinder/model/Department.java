package com.zisluiz.querydslbinder.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Department {
	private Integer id;
	private String name;
	private Warehouse warehouse;
	private List<Product> products;
	private List<Product> lockedProducts;
}

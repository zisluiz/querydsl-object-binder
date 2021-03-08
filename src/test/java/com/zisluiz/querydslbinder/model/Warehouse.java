package com.zisluiz.querydslbinder.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Warehouse {
	private Integer id;
	private String name;
	private Integer quantity;
	
	private Boolean active;
	private List<Department> departments;
		
	private String owner;
	private Manager manager;
	
	private List<Manager> managers;
	private List<Person> owners;
	private List<WarehouseNoConstructor> otherWarehouses;
}

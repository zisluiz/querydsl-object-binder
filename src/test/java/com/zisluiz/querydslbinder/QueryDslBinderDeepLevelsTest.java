package com.zisluiz.querydslbinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.querydsl.core.Tuple;
import com.zisluiz.querydslbinder.infra.util.QueryDslUtil;
import com.zisluiz.querydslbinder.model.Department;
import com.zisluiz.querydslbinder.model.Warehouse;

public class QueryDslBinderDeepLevelsTest {
	
	@Test
	public void deepLevelBasicTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		List<String> expressions = Arrays.asList("id", "name", "active", "d.id", "d.name", "p.id", "p.name");
		
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 10, "Dep1", 100, "Prod1"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 10, "Dep1", 101, "Prod2"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 11, "Dep2", 102, "Prod3"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(2, "Warehouse2", true, 12, "Dep3", 103, "Prod4"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(2, "Warehouse2", true, 13, "Dep4", 104, "Prod5"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 14, "Dep5", 105, "Prod6"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 15, "Dep6", null, null), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(4, "Warehouse4", false, null, null, null, null), expressions));

		List<Warehouse> parsed = QueryDslBinder.to(tuples, Warehouse.class, 
				new GroupByBinder()
				.key("id", QueryDslUtil.makeExpression("id"))
				.field("name", QueryDslUtil.makeExpression("name"))
				.field("active", QueryDslUtil.makeExpression("active"))
				.collection("departments", new GroupByBinder()
						.key("id", QueryDslUtil.makeExpression("d.id"))
						.field("name", QueryDslUtil.makeExpression("d.name"))
						.collection("products", new GroupByBinder()
								.key("id", QueryDslUtil.makeExpression("p.id"))
								.field("name", QueryDslUtil.makeExpression("p.name")))));
		
		Assertions.assertEquals(parsed.size(), 4);
		
		Warehouse warehouse1 = parsed.get(0);
		Assertions.assertEquals((Integer) 1, warehouse1.getId());
		Assertions.assertEquals("Warehouse1", warehouse1.getName());
		Assertions.assertTrue(warehouse1.getActive());
		Assertions.assertEquals(2, warehouse1.getDepartments().size());
		
		Department department1Warehouse1 = warehouse1.getDepartments().get(0);
		Assertions.assertEquals((Integer) 10, department1Warehouse1.getId());
		Assertions.assertEquals("Dep1", department1Warehouse1.getName());
		Assertions.assertEquals(2, department1Warehouse1.getProducts().size());
		
		Assertions.assertEquals((Integer) 100, department1Warehouse1.getProducts().get(0).getId());
		Assertions.assertEquals("Prod1", department1Warehouse1.getProducts().get(0).getName());
		Assertions.assertEquals((Integer) 101, department1Warehouse1.getProducts().get(1).getId());
		Assertions.assertEquals("Prod2", department1Warehouse1.getProducts().get(1).getName());
		
		Department department2Warehouse1 = warehouse1.getDepartments().get(1);
		Assertions.assertEquals((Integer) 11, department2Warehouse1.getId());
		Assertions.assertEquals("Dep2", department2Warehouse1.getName());
		Assertions.assertEquals(1, department2Warehouse1.getProducts().size());
		Assertions.assertEquals((Integer) 102, department2Warehouse1.getProducts().get(0).getId());
		Assertions.assertEquals("Prod3", department2Warehouse1.getProducts().get(0).getName());		
		
		Warehouse warehouse2 = parsed.get(1);
		Assertions.assertEquals((Integer) 2, warehouse2.getId());
		Assertions.assertEquals("Warehouse2", warehouse2.getName());
		Assertions.assertEquals(2, warehouse2.getDepartments().size());
		
		Department department3Warehouse2 = warehouse2.getDepartments().get(0);
		Assertions.assertEquals((Integer) 12, department3Warehouse2.getId());
		Assertions.assertEquals("Dep3", department3Warehouse2.getName());
		Assertions.assertEquals(1, department3Warehouse2.getProducts().size());
		Assertions.assertEquals((Integer) 103, department3Warehouse2.getProducts().get(0).getId());
		Assertions.assertEquals("Prod4", department3Warehouse2.getProducts().get(0).getName());
		
		Department department4Warehouse2 = warehouse2.getDepartments().get(1);
		Assertions.assertEquals((Integer) 13, department4Warehouse2.getId());
		Assertions.assertEquals("Dep4", department4Warehouse2.getName());
		Assertions.assertEquals(1, department4Warehouse2.getProducts().size());
		Assertions.assertEquals((Integer) 104, department4Warehouse2.getProducts().get(0).getId());
		Assertions.assertEquals("Prod5", department4Warehouse2.getProducts().get(0).getName());		
		
		Warehouse warehouse3 = parsed.get(2);
		Assertions.assertEquals((Integer) 3, warehouse3.getId());
		Assertions.assertEquals("Warehouse3", warehouse3.getName());
		Assertions.assertEquals(2, warehouse3.getDepartments().size());
		Department department5Warehouse3 = warehouse3.getDepartments().get(0);
		Assertions.assertEquals((Integer) 14, department5Warehouse3.getId());
		Assertions.assertEquals("Dep5", department5Warehouse3.getName());	
		Assertions.assertEquals(1, department5Warehouse3.getProducts().size());
		Assertions.assertEquals((Integer) 105, department5Warehouse3.getProducts().get(0).getId());
		Assertions.assertEquals("Prod6", department5Warehouse3.getProducts().get(0).getName());
		Department department6Warehouse3 = warehouse3.getDepartments().get(1);
		Assertions.assertEquals((Integer) 15, department6Warehouse3.getId());
		Assertions.assertEquals("Dep6", department6Warehouse3.getName());
		Assertions.assertNull(department6Warehouse3.getProducts());
		
		Warehouse warehouse4 = parsed.get(3);
		Assertions.assertEquals((Integer) 4, warehouse4.getId());
		Assertions.assertEquals("Warehouse4", warehouse4.getName());
		Assertions.assertNull(warehouse4.getDepartments());
	}
	
	@Test
	public void inconsistentDataTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		List<String> expressions = Arrays.asList("id", "name", "active", "d.id", "d.name", "p.id", "p.name");
		
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 10, "Dep1", 100, "Prod1"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 10, "Dep1", 101, "Prod2"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 11, "Dep2", 102, "Prod3"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(2, "Warehouse2", true, 12, "Dep3", 103, "Prod4"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(2, "Warehouse2", true, 13, "Dep4", 104, "Prod5"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 14, "Dep5", 105, "Prod6"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 15, "Dep6", null, null), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(4, "Warehouse4", false, null, null, null, null), expressions));		
		List<Warehouse> parsed = QueryDslBinder.to(tuples, Warehouse.class, 
				new GroupByBinder()
				.key("id", QueryDslUtil.makeExpression("id"))
				.field("name", QueryDslUtil.makeExpression("name"))
				.field("active", QueryDslUtil.makeExpression("active"))
				.collection("departments", new GroupByBinder()
						.key("id", QueryDslUtil.makeExpression("d.id"))
						.field("name", QueryDslUtil.makeExpression("d.name"))
						.collection("products", new GroupByBinder()
								.key("id", QueryDslUtil.makeExpression("p.id"))
								.field("name", QueryDslUtil.makeExpression("p.name")))));
		
		Assertions.assertEquals(parsed.size(), 4);
		
		Warehouse warehouse1 = parsed.get(0);
		Assertions.assertEquals((Integer) 1, warehouse1.getId());
		Assertions.assertEquals("Warehouse1", warehouse1.getName());
		Assertions.assertTrue(warehouse1.getActive());
		Assertions.assertEquals(2, warehouse1.getDepartments().size());
		
		Department department1Warehouse1 = warehouse1.getDepartments().get(0);
		Assertions.assertEquals((Integer) 10, department1Warehouse1.getId());
		Assertions.assertEquals("Dep1", department1Warehouse1.getName());
		Assertions.assertEquals(2, department1Warehouse1.getProducts().size());
		
		Assertions.assertEquals((Integer) 100, department1Warehouse1.getProducts().get(0).getId());
		Assertions.assertEquals("Prod1", department1Warehouse1.getProducts().get(0).getName());
		Assertions.assertEquals((Integer) 101, department1Warehouse1.getProducts().get(1).getId());
		Assertions.assertEquals("Prod2", department1Warehouse1.getProducts().get(1).getName());
		
		Department department2Warehouse1 = warehouse1.getDepartments().get(1);
		Assertions.assertEquals((Integer) 11, department2Warehouse1.getId());
		Assertions.assertEquals("Dep2", department2Warehouse1.getName());
		Assertions.assertEquals(1, department2Warehouse1.getProducts().size());
		Assertions.assertEquals((Integer) 102, department2Warehouse1.getProducts().get(0).getId());
		Assertions.assertEquals("Prod3", department2Warehouse1.getProducts().get(0).getName());		
		
		Warehouse warehouse2 = parsed.get(1);
		Assertions.assertEquals((Integer) 2, warehouse2.getId());
		Assertions.assertEquals("Warehouse2", warehouse2.getName());
		Assertions.assertEquals(2, warehouse2.getDepartments().size());
		
		Department department3Warehouse2 = warehouse2.getDepartments().get(0);
		Assertions.assertEquals((Integer) 12, department3Warehouse2.getId());
		Assertions.assertEquals("Dep3", department3Warehouse2.getName());
		Assertions.assertEquals(1, department3Warehouse2.getProducts().size());
		Assertions.assertEquals((Integer) 103, department3Warehouse2.getProducts().get(0).getId());
		Assertions.assertEquals("Prod4", department3Warehouse2.getProducts().get(0).getName());
		
		Department department4Warehouse2 = warehouse2.getDepartments().get(1);
		Assertions.assertEquals((Integer) 13, department4Warehouse2.getId());
		Assertions.assertEquals("Dep4", department4Warehouse2.getName());
		Assertions.assertEquals(1, department4Warehouse2.getProducts().size());
		Assertions.assertEquals((Integer) 104, department4Warehouse2.getProducts().get(0).getId());
		Assertions.assertEquals("Prod5", department4Warehouse2.getProducts().get(0).getName());		
		
		Warehouse warehouse3 = parsed.get(2);
		Assertions.assertEquals((Integer) 3, warehouse3.getId());
		Assertions.assertEquals("Warehouse3", warehouse3.getName());
		Assertions.assertEquals(2, warehouse3.getDepartments().size());
		Department department5Warehouse3 = warehouse3.getDepartments().get(0);
		Assertions.assertEquals((Integer) 14, department5Warehouse3.getId());
		Assertions.assertEquals("Dep5", department5Warehouse3.getName());	
		Assertions.assertEquals(1, department5Warehouse3.getProducts().size());
		Assertions.assertEquals((Integer) 105, department5Warehouse3.getProducts().get(0).getId());
		Assertions.assertEquals("Prod6", department5Warehouse3.getProducts().get(0).getName());
		Department department6Warehouse3 = warehouse3.getDepartments().get(1);
		Assertions.assertEquals((Integer) 15, department6Warehouse3.getId());
		Assertions.assertEquals("Dep6", department6Warehouse3.getName());
		Assertions.assertNull(department6Warehouse3.getProducts());
		
		Warehouse warehouse4 = parsed.get(3);
		Assertions.assertEquals((Integer) 4, warehouse4.getId());
		Assertions.assertEquals("Warehouse4", warehouse4.getName());
		Assertions.assertNull(warehouse4.getDepartments());
	}
	
	@Test
	public void twoCollectionsTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		List<String> expressions = Arrays.asList("id", "name", "active", "pe.id", "pe.name", "d.id", "d.name", "p.id", "p.name", "pb.id", "pb.name");
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 1, "Luiz", 10, "Dep1", 100, "Prod1", 200, "Prod1 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 1, "Luiz", 10, "Dep1", 101, "Prod2", 200, "Prod1 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 1, "Luiz", 11, "Dep2", 102, "Prod3", 200, "Prod1 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 2, "Eduardo", 10, "Dep1", 100, "Prod1", 200, "Prod1 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 2, "Eduardo", 10, "Dep1", 101, "Prod2", 200, "Prod1 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 2, "Eduardo", 11, "Dep2", 102, "Prod3", 200, "Prod1 Bloq"), expressions));		
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(2, "Warehouse2", true, null, null, 12, "Dep3", 103, "Prod4", 201, "Prod2 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(2, "Warehouse2", true, null, null, 13, "Dep4", 104, "Prod5", null, null), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 3, "Rodrigo", 14, "Dep5", 105, "Prod6", 202, "Prod3 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 4, "Gabriel", 14, "Dep5", 105, "Prod6", 202, "Prod3 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 5, "Carlos", 14, "Dep5", 105, "Prod6", 202, "Prod3 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 3, "Rodrigo", 14, "Dep5", 105, "Prod6", 203, "Prod4 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 4, "Gabriel", 14, "Dep5", 105, "Prod6", 203, "Prod4 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 5, "Carlos", 14, "Dep5", 105, "Prod6", 203, "Prod4 Bloq"), expressions));		
		
		List<Warehouse> parsed = QueryDslBinder.to(tuples, Warehouse.class, 
				new GroupByBinder()
				.key("id", QueryDslUtil.makeExpression("id"))
				.field("name", QueryDslUtil.makeExpression("name"))
				.field("active", QueryDslUtil.makeExpression("active"))
				.collection("departments", new GroupByBinder()
						.key("id", QueryDslUtil.makeExpression("d.id"))
						.field("name", QueryDslUtil.makeExpression("d.name"))
						.collection("products", new GroupByBinder()
								.key("id", QueryDslUtil.makeExpression("p.id"))
								.field("name", QueryDslUtil.makeExpression("p.name")))
						.collection("lockedProducts", new GroupByBinder()
								.key("id", QueryDslUtil.makeExpression("pb.id"))
								.field("name", QueryDslUtil.makeExpression("pb.name"))))
				.collection("managers", new GroupByBinder()
						.key("id", QueryDslUtil.makeExpression("pe.id"))
						.field("name", QueryDslUtil.makeExpression("pe.name"))));
		
		Assertions.assertEquals(parsed.size(), 3);
		
		//almx1
		Warehouse warehouse1 = parsed.get(0);
		Assertions.assertEquals((Integer) 1, warehouse1.getId());
		Assertions.assertEquals("Warehouse1", warehouse1.getName());
		Assertions.assertTrue(warehouse1.getActive());
		Assertions.assertEquals(2, warehouse1.getDepartments().size());
		
		Assertions.assertEquals(2, warehouse1.getManagers().size());
		Assertions.assertEquals((Integer) 1, warehouse1.getManagers().get(0).getId());
		Assertions.assertEquals("Luiz", warehouse1.getManagers().get(0).getName());
		Assertions.assertEquals((Integer) 2, warehouse1.getManagers().get(1).getId());
		Assertions.assertEquals("Eduardo", warehouse1.getManagers().get(1).getName());		
		
		Assertions.assertEquals((Integer) 10, warehouse1.getDepartments().get(0).getId());
		Assertions.assertEquals("Dep1", warehouse1.getDepartments().get(0).getName());
		
		Assertions.assertEquals(2, warehouse1.getDepartments().get(0).getProducts().size());
		Assertions.assertEquals((Integer) 100, warehouse1.getDepartments().get(0).getProducts().get(0).getId());
		Assertions.assertEquals("Prod1", warehouse1.getDepartments().get(0).getProducts().get(0).getName());
		Assertions.assertEquals((Integer) 101, warehouse1.getDepartments().get(0).getProducts().get(1).getId());
		Assertions.assertEquals("Prod2", warehouse1.getDepartments().get(0).getProducts().get(1).getName());
		
		Assertions.assertEquals(1, warehouse1.getDepartments().get(0).getLockedProducts().size());
		Assertions.assertEquals((Integer) 200, warehouse1.getDepartments().get(0).getLockedProducts().get(0).getId());
		Assertions.assertEquals("Prod1 Bloq", warehouse1.getDepartments().get(0).getLockedProducts().get(0).getName());		
		
		Assertions.assertEquals((Integer) 11, warehouse1.getDepartments().get(1).getId());
		Assertions.assertEquals("Dep2", warehouse1.getDepartments().get(1).getName());
		Assertions.assertEquals(1, warehouse1.getDepartments().get(1).getProducts().size());
		Assertions.assertEquals((Integer) 102, warehouse1.getDepartments().get(1).getProducts().get(0).getId());
		Assertions.assertEquals("Prod3", warehouse1.getDepartments().get(1).getProducts().get(0).getName());		
		
		Assertions.assertEquals(1, warehouse1.getDepartments().get(1).getLockedProducts().size());
		Assertions.assertEquals((Integer) 200, warehouse1.getDepartments().get(1).getLockedProducts().get(0).getId());
		Assertions.assertEquals("Prod1 Bloq", warehouse1.getDepartments().get(1).getLockedProducts().get(0).getName());
		
		//almx2
		Warehouse warehouse2 = parsed.get(1);
		Assertions.assertEquals((Integer) 2, warehouse2.getId());
		Assertions.assertEquals("Warehouse2", warehouse2.getName());
		Assertions.assertEquals(2, warehouse2.getDepartments().size());
		
		Assertions.assertEquals((Integer) 12, warehouse2.getDepartments().get(0).getId());
		Assertions.assertEquals("Dep3", warehouse2.getDepartments().get(0).getName());
		Assertions.assertEquals(1, warehouse2.getDepartments().get(0).getProducts().size());
		Assertions.assertEquals((Integer) 103, warehouse2.getDepartments().get(0).getProducts().get(0).getId());
		Assertions.assertEquals("Prod4", warehouse2.getDepartments().get(0).getProducts().get(0).getName());
		
		Assertions.assertEquals(1, warehouse2.getDepartments().get(0).getLockedProducts().size());
		Assertions.assertEquals((Integer) 201, warehouse2.getDepartments().get(0).getLockedProducts().get(0).getId());
		Assertions.assertEquals("Prod2 Bloq", warehouse2.getDepartments().get(0).getLockedProducts().get(0).getName());		
		
		Assertions.assertEquals((Integer) 13, warehouse2.getDepartments().get(1).getId());
		Assertions.assertEquals("Dep4", warehouse2.getDepartments().get(1).getName());
		Assertions.assertEquals(1, warehouse2.getDepartments().get(1).getProducts().size());
		Assertions.assertEquals((Integer) 104, warehouse2.getDepartments().get(1).getProducts().get(0).getId());
		Assertions.assertEquals("Prod5", warehouse2.getDepartments().get(1).getProducts().get(0).getName());		
		
		Assertions.assertNull(warehouse2.getDepartments().get(1).getLockedProducts());
		Assertions.assertNull(warehouse2.getManagers());
		//almx3
		Warehouse warehouse3 = parsed.get(2);
		Assertions.assertEquals((Integer) 3, warehouse3.getId());
		Assertions.assertEquals("Warehouse3", warehouse3.getName());
		Assertions.assertEquals(1, warehouse3.getDepartments().size());
		Assertions.assertEquals((Integer) 14, warehouse3.getDepartments().get(0).getId());
		Assertions.assertEquals("Dep5", warehouse3.getDepartments().get(0).getName());	
		Assertions.assertEquals(1, warehouse3.getDepartments().get(0).getProducts().size());
		Assertions.assertEquals((Integer) 105, warehouse3.getDepartments().get(0).getProducts().get(0).getId());
		Assertions.assertEquals("Prod6", warehouse3.getDepartments().get(0).getProducts().get(0).getName());
		
		Assertions.assertEquals(3, warehouse3.getManagers().size());
		Assertions.assertEquals((Integer) 3, warehouse3.getManagers().get(0).getId());
		Assertions.assertEquals("Rodrigo", warehouse3.getManagers().get(0).getName());
		Assertions.assertEquals((Integer) 4, warehouse3.getManagers().get(1).getId());
		Assertions.assertEquals("Gabriel", warehouse3.getManagers().get(1).getName());
		Assertions.assertEquals((Integer) 5, warehouse3.getManagers().get(2).getId());
		Assertions.assertEquals("Carlos", warehouse3.getManagers().get(2).getName());	
		
		Assertions.assertEquals(2, warehouse3.getDepartments().get(0).getLockedProducts().size());
		Assertions.assertEquals((Integer) 202, warehouse3.getDepartments().get(0).getLockedProducts().get(0).getId());
		Assertions.assertEquals("Prod3 Bloq", warehouse3.getDepartments().get(0).getLockedProducts().get(0).getName());
		
		Assertions.assertEquals((Integer) 203, warehouse3.getDepartments().get(0).getLockedProducts().get(1).getId());
		Assertions.assertEquals("Prod4 Bloq", warehouse3.getDepartments().get(0).getLockedProducts().get(1).getName());		
	}	
	
	@Test
	public void sameReferenceForEqualKeysTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		List<String> expressions = Arrays.asList("id", "name", "active", "pe.id", "pe.name", "d.id", "d.name", "p.id", "p.name", "pb.id", "pb.name");
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 1, "Luiz", 10, "Dep1", 100, "Prod1", 200, "Prod1 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 1, "Luiz", 10, "Dep1", 101, "Prod2", 200, "Prod1 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 1, "Luiz", 11, "Dep2", 102, "Prod3", 200, "Prod1 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 2, "Eduardo", 10, "Dep1", 100, "Prod1", 200, "Prod1 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 2, "Eduardo", 10, "Dep1", 101, "Prod2", 200, "Prod1 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "Warehouse1", true, 2, "Eduardo", 11, "Dep2", 102, "Prod3", 200, "Prod1 Bloq"), expressions));		
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(2, "Warehouse2", true, null, null, 12, "Dep3", 103, "Prod4", 201, "Prod2 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(2, "Warehouse2", true, null, null, 13, "Dep4", 104, "Prod5", null, null), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 3, "Rodrigo", 14, "Dep5", 105, "Prod6", 202, "Prod3 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 4, "Gabriel", 14, "Dep5", 105, "Prod6", 202, "Prod3 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 5, "Carlos", 14, "Dep5", 105, "Prod6", 202, "Prod3 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 3, "Rodrigo", 14, "Dep5", 105, "Prod6", 203, "Prod4 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 4, "Gabriel", 14, "Dep5", 105, "Prod6", 203, "Prod4 Bloq"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(3, "Warehouse3", false, 5, "Carlos", 14, "Dep5", 105, "Prod6", 203, "Prod4 Bloq"), expressions));		
		
		List<Warehouse> parsed = QueryDslBinder.to(tuples, Warehouse.class, 
				new GroupByBinder()
				.key("id", QueryDslUtil.makeExpression("id"))
				.field("name", QueryDslUtil.makeExpression("name"))
				.field("active", QueryDslUtil.makeExpression("active"))
				.collection("departments", new GroupByBinder()
						.key("id", QueryDslUtil.makeExpression("d.id"))
						.single("warehouse", new GroupByBinder()
								.key("id",  QueryDslUtil.makeExpression("id")))
						.field("name", QueryDslUtil.makeExpression("d.name"))
						.collection("products", new GroupByBinder()
								.key("id", QueryDslUtil.makeExpression("p.id"))
								.field("name", QueryDslUtil.makeExpression("p.name")))
						.collection("lockedProducts", new GroupByBinder()
								.key("id", QueryDslUtil.makeExpression("pb.id"))
								.field("name", QueryDslUtil.makeExpression("pb.name"))))
				.collection("managers", new GroupByBinder()
						.key("id", QueryDslUtil.makeExpression("pe.id"))
						.field("name", QueryDslUtil.makeExpression("pe.name"))));
		
		Assertions.assertEquals(parsed.size(), 3);
		
		//references must be the same for objects with same id key
		
		//almx1
		Warehouse warehouse1 = parsed.get(0);
		Assertions.assertTrue(warehouse1.getDepartments().get(0).getWarehouse().equals(warehouse1));
		Assertions.assertEquals(warehouse1.getDepartments().size(), warehouse1.getDepartments().get(0).getWarehouse().getDepartments().size());
		
		Assertions.assertTrue(warehouse1.getDepartments().get(1).getWarehouse().equals(warehouse1));
		Assertions.assertEquals(warehouse1.getDepartments().size(), warehouse1.getDepartments().get(1).getWarehouse().getDepartments().size());		
		
		//almx2
		Warehouse warehouse2 = parsed.get(1);
		Assertions.assertTrue(warehouse2.getDepartments().get(0).getWarehouse().equals(warehouse2));
		Assertions.assertEquals(warehouse2.getDepartments().size(), warehouse2.getDepartments().get(0).getWarehouse().getDepartments().size());		
		
		Assertions.assertTrue(warehouse2.getDepartments().get(1).getWarehouse().equals(warehouse2));
		Assertions.assertEquals(warehouse2.getDepartments().size(), warehouse2.getDepartments().get(1).getWarehouse().getDepartments().size());		
		
		//almx3
		Warehouse warehouse3 = parsed.get(2);
		Assertions.assertTrue(warehouse3.getDepartments().get(0).getWarehouse().equals(warehouse3));
		Assertions.assertEquals(warehouse3.getDepartments().size(), warehouse3.getDepartments().get(0).getWarehouse().getDepartments().size());
	}		
}
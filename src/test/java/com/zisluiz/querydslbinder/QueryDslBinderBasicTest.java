package com.zisluiz.querydslbinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.querydsl.core.Tuple;
import com.zisluiz.querydslbinder.infra.util.QueryDslUtil;
import com.zisluiz.querydslbinder.model.Manager;
import com.zisluiz.querydslbinder.model.Warehouse;
import com.zisluiz.querydslbinder.model.WarehouseNoConstructor;

public class QueryDslBinderBasicTest {

	@Test
	public void noArgumentsTest() {
		Assertions.assertTrue(QueryDslBinder.to(null, null, null).isEmpty());
	}

	@Test
	public void argumentOnlyDtoClassTest() {
		Assertions.assertTrue(QueryDslBinder.to(null, Warehouse.class, null).isEmpty());
	}

	@Test
	public void noGroupByTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		
		createSampleRow(tuples);

		Assertions.assertThrows(Exception.class, () -> QueryDslBinder.to(tuples, Warehouse.class, null));
	}

	@Test
	public void collectionWithoutKeyTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		
		createSampleRow(tuples);

		Assertions.assertThrows(Exception.class, () -> QueryDslBinder.to(tuples, Warehouse.class,
				new GroupByBinder().field("id", QueryDslUtil.makeExpression("id")).collection("otherWarehouses",
						new GroupByBinder())));
	}

	@Test
	public void classWithoutPublicConstructorTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		
		createSampleRow(tuples);
		
		Assertions.assertThrows(Exception.class, () -> QueryDslBinder.to(tuples, WarehouseNoConstructor.class,
				new GroupByBinder().field("id", QueryDslUtil.makeExpression("id"))));
	}

	@Test
	public void onlyFieldsClassTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		
		createSampleRow(tuples);
		
		List<Warehouse> parsed = QueryDslBinder.to(tuples, Warehouse.class,
				new GroupByBinder().field("id", QueryDslUtil.makeExpression("id"))
						.field("name", QueryDslUtil.makeExpression("name"))
						.field("quantity", QueryDslUtil.makeExpression("quantity")));

		Assertions.assertEquals(parsed.size(), 1);
		Assertions.assertEquals((Integer) 1, parsed.get(0).getId());
		Assertions.assertEquals("test", parsed.get(0).getName());
		Assertions.assertEquals((Integer) 10, parsed.get(0).getQuantity());
		Assertions.assertNull(parsed.get(0).getManager());
	}

	@Test
	public void onlyKeysClassTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		
		createSampleRow(tuples);
		
		List<Warehouse> parsed = QueryDslBinder.to(tuples, Warehouse.class, new GroupByBinder()
				.key("id", QueryDslUtil.makeExpression("id")).key("name", QueryDslUtil.makeExpression("name")));

		Assertions.assertEquals(parsed.size(), 1);
		Assertions.assertEquals((Integer) 1, parsed.get(0).getId());
		Assertions.assertEquals("test", parsed.get(0).getName());
		Assertions.assertNull(parsed.get(0).getQuantity());
	}

	@Test
	public void wrongFieldNameTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		
		createSampleRow(tuples);

		Assertions.assertThrows(Exception.class, () -> QueryDslBinder.to(tuples, Warehouse.class, new GroupByBinder()
				.key("id", QueryDslUtil.makeExpression("id")).key("nombre", QueryDslUtil.makeExpression("name"))));
	}

	@Test
	public void inconsistentKeysClassTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		List<String> expressions = Arrays.asList("id", "name", "quantity");
		
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "test", 10), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(2, null, 5), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(null, "test2", 3), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(null, null, 4), expressions));
		
		List<Warehouse> parsed = QueryDslBinder.to(tuples, Warehouse.class,
				new GroupByBinder().key("id", QueryDslUtil.makeExpression("id"))
						.key("name", QueryDslUtil.makeExpression("name"))
						.field("quantity", QueryDslUtil.makeExpression("quantity")));

		Assertions.assertEquals(3, parsed.size());

		Assertions.assertEquals((Integer) 1, parsed.get(0).getId());
		Assertions.assertEquals("test", parsed.get(0).getName());
		Assertions.assertEquals((Integer) 10, parsed.get(0).getQuantity());

		Assertions.assertEquals((Integer) 2, parsed.get(1).getId());
		Assertions.assertNull(parsed.get(1).getName());
		Assertions.assertEquals((Integer) 5, parsed.get(1).getQuantity());

		Assertions.assertNull(parsed.get(2).getId());
		Assertions.assertEquals("test2", parsed.get(2).getName());
		Assertions.assertEquals((Integer) 3, parsed.get(2).getQuantity());
	}

	@Test
	public void inconsistentDataTest() {
		List<Tuple> tuples = new ArrayList<Tuple>();
		List<String> expressions = Arrays.asList("id", "name", "quantity");
		
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "test", 10), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(2, null, 5), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(null, null, 3), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(null, null, null), expressions));
		
		List<Warehouse> parsed = QueryDslBinder.to(tuples, Warehouse.class,
				new GroupByBinder().field("id", QueryDslUtil.makeExpression("id"))
						.field("name", QueryDslUtil.makeExpression("name"))
						.field("quantity", QueryDslUtil.makeExpression("quantity")));

		Assertions.assertEquals(3, parsed.size());

		Assertions.assertEquals((Integer) 1, parsed.get(0).getId());
		Assertions.assertEquals("test", parsed.get(0).getName());
		Assertions.assertEquals((Integer) 10, parsed.get(0).getQuantity());

		Assertions.assertEquals((Integer) 2, parsed.get(1).getId());
		Assertions.assertNull(parsed.get(1).getName());
		Assertions.assertEquals((Integer) 5, parsed.get(1).getQuantity());

		Assertions.assertNull(parsed.get(2).getId());
		Assertions.assertNull(parsed.get(2).getName());
		Assertions.assertEquals((Integer) 3, parsed.get(2).getQuantity());
	}

	@Test
	public void singleRelationBasicTest() {
		List<Object> row = Arrays.asList(1, "test", 10, 1, "Luiz");
		List<String> expressions = Arrays.asList("id", "name", "quantity", "g.id", "g.name");
		
		Tuple tuple = QueryDslUtil.createTuple(row, expressions);

		Warehouse parsed = QueryDslBinder.one(tuple, Warehouse.class,
				new GroupByBinder().key("id", QueryDslUtil.makeExpression("id"))
						.field("name", QueryDslUtil.makeExpression("name"))
						.field("quantity", QueryDslUtil.makeExpression("quantity"))
						.single("manager", new GroupByBinder().key("id", QueryDslUtil.makeExpression("g.id"))
								.field("name", QueryDslUtil.makeExpression("g.name"))));

		Assertions.assertNotNull(parsed);

		Assertions.assertEquals((Integer) 1, parsed.getId());
		Assertions.assertEquals("test", parsed.getName());
		Assertions.assertEquals((Integer) 10, parsed.getQuantity());

		Assertions.assertEquals((Integer) 1, parsed.getManager().getId());
		Assertions.assertEquals("Luiz", parsed.getManager().getName());
	}
	
	@Test
	public void singleRelationNullKeySingleTest() {
		Tuple tuple = QueryDslUtil.createTuple(Arrays.asList(1, "test", 10, null, "Luiz"),
				Arrays.asList("id", "name", "quantity", "g.id", "g.name"));

		Warehouse parsed = QueryDslBinder.one(tuple, Warehouse.class,
				new GroupByBinder().key("id", QueryDslUtil.makeExpression("id"))
						.field("name", QueryDslUtil.makeExpression("name"))
						.field("quantity", QueryDslUtil.makeExpression("quantity"))
						.single("manager", new GroupByBinder()
								.key("id", QueryDslUtil.makeExpression("g.id"))
								.field("name", QueryDslUtil.makeExpression("g.name"))));

		Assertions.assertNotNull(parsed);

		Assertions.assertEquals((Integer) 1, parsed.getId());
		Assertions.assertEquals("test", parsed.getName());
		Assertions.assertEquals((Integer) 10, parsed.getQuantity());

		Assertions.assertNull(parsed.getManager());
	}
	
	@Test
	public void singleRelationWithoutKeyTest() {
		Tuple tuple = QueryDslUtil.createTuple(Arrays.asList(1, "test", 10, null, "Luiz"),
				Arrays.asList("id", "name", "quantity", "g.id", "g.name"));

		Warehouse parsed = QueryDslBinder.one(tuple, Warehouse.class,
				new GroupByBinder().key("id", QueryDslUtil.makeExpression("id"))
						.field("name", QueryDslUtil.makeExpression("name"))
						.field("quantity", QueryDslUtil.makeExpression("quantity"))
						.single("manager", new GroupByBinder()
								.field("id", QueryDslUtil.makeExpression("g.id"))
								.field("name", QueryDslUtil.makeExpression("g.name"))));

		Assertions.assertNotNull(parsed);

		Assertions.assertEquals((Integer) 1, parsed.getId());
		Assertions.assertEquals("test", parsed.getName());
		Assertions.assertEquals((Integer) 10, parsed.getQuantity());

		Assertions.assertNull(parsed.getManager().getId());
		Assertions.assertEquals("Luiz", parsed.getManager().getName());
	}	

	@Test
	public void singleRelationRequiredKeyTest() {
		List<Object> row = Arrays.asList(1, "test", 10, 1, "Luiz");
		List<String> expressions = Arrays.asList("id", "name", "quantity", "g.id", "g.name");
		
		Tuple tuple = QueryDslUtil.createTuple(row, expressions);

		Assertions.assertThrows(Exception.class, () -> QueryDslBinder.one(tuple, Warehouse.class,
				new GroupByBinder().field("id", QueryDslUtil.makeExpression("id"))
						.field("name", QueryDslUtil.makeExpression("name"))
						.field("quantity", QueryDslUtil.makeExpression("quantity"))
						.single("manager", new GroupByBinder().key("id", QueryDslUtil.makeExpression("g.id"))
								.field("name", QueryDslUtil.makeExpression("g.name")))));
	}

	@Test
	public void singleRelationRepeatedValuesTest() {
		List<String> expressions = Arrays.asList("id", "name", "quantity", "g.id", "g.name");
		
		List<Tuple> tuples = new ArrayList<Tuple>();
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "test", 10, 1, "Luiz"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "test", 10, 1, "Rafael"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "test", 10, 2, "Paulo"), expressions));
		tuples.add(QueryDslUtil.createTuple(Arrays.asList(1, "test", 10, 3, "Rodrigo"), expressions));

		List<Warehouse> parsed = QueryDslBinder.to(tuples, Warehouse.class,
				new GroupByBinder().key("id", QueryDslUtil.makeExpression("id"))
						.field("name", QueryDslUtil.makeExpression("name"))
						.field("quantity", QueryDslUtil.makeExpression("quantity"))
						.collection("managers", new GroupByBinder()
								.field("id", QueryDslUtil.makeExpression("g.id"))
								.field("name", QueryDslUtil.makeExpression("g.name"))));
		
		Assertions.assertEquals(1, parsed.size());

		Warehouse warehouseTest = parsed.get(0);
		
		Assertions.assertEquals((Integer) 1, warehouseTest.getId());
		Assertions.assertEquals("test", warehouseTest.getName());
		Assertions.assertEquals((Integer) 10, warehouseTest.getQuantity());
		
		Manager managerLuiz = warehouseTest.getManagers().get(0);
		Assertions.assertEquals((Integer) 1, managerLuiz.getId());
		Assertions.assertEquals("Luiz", managerLuiz.getName());
		
		Manager managerRafael = warehouseTest.getManagers().get(1);
		Assertions.assertEquals((Integer) 1, managerRafael.getId());
		Assertions.assertEquals("Rafael", managerRafael.getName());
		
		Manager managerPaulo = warehouseTest.getManagers().get(2);
		Assertions.assertEquals((Integer) 2, managerPaulo.getId());
		Assertions.assertEquals("Paulo", managerPaulo.getName());
		
		Manager managerRodrigo = warehouseTest.getManagers().get(3);
		Assertions.assertEquals((Integer) 3, managerRodrigo.getId());
		Assertions.assertEquals("Rodrigo", managerRodrigo.getName());		
	}
	
	private void createSampleRow(List<Tuple> tuples) {
		List<Object> row = Arrays.asList(1, 10, "test");
		List<String> expressions = Arrays.asList("id", "quantity", "name");
		
		tuples.add(QueryDslUtil.createTuple(row, expressions));
	}
}
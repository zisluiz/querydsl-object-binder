# QueryDsl Object Binder

## What is QueryDsl
[QueryDsl](http://www.querydsl.com/) is a framework for JPA/Java to write queries with generated QType objects. A plugin read your JPA entities and generates these files. 

## Problem to solve
A frequently task in development queries is aggregate a query result into related objects, that may be into entity objects or other objects, like DTO and object values. 

QueryDsl has a functionality to aggregate a result into a parent and children objects, documented in [http://www.querydsl.com/static/querydsl/latest/reference/html/ch03s02.html](http://www.querydsl.com/static/querydsl/latest/reference/html/ch03s02.html).

In "3.2.4. Result aggregation" section, a query example with posts and comments is presented. But QueryDsl not provide a way to aggregate objects with more deep children levels, like explaned in [https://stackoverflow.com/questions/59655149/querydsl-multilevel-result-aggregation](https://stackoverflow.com/questions/59655149/querydsl-multilevel-result-aggregation).

There is no way to deal with a query like:
```java
query.from(_city)
		.join(_city.state, _state)
		.join(_state.country, _country);
```

Sometimes we need this result in a object-structure like:

City:
* id
* name
* State 

State:
* id
* name
* Country

Country
* id
* name

But no exist a way to do tis with only Querydsl/Spring JPA.

## Solution
This lib offers a way to convert/bind/aggregate QueryDsl results into objects with n children levels. See a example below:
```java
JPAQuery<Tuple> query = new JPAQuery<Tuple>(em);

query.from(_city)
		.join(_city.state, _state)
		.join(_state.country, _country);

List<Tuple> tupleResult = query.select(_city.id, _city.name,
		_state.id, _state.name,
		_country.id, _country.name).orderBy(_city.id.asc()).fetch();

List<City> cities = QueryDslBinder.to(tupleResult, City.class,
		new GroupByBinder()
			.key("id", _city.id).
			field("name", _city.name)
			.single("state", new GroupByBinder()
					.key("id", _state.id)
					.field("name", _state.name)
					.single("country", new GroupByBinder()
							.key("id", _country.id)
							.field("name", _country.name)					
							.collection("states", new GroupByBinder()
									.key("id", _state.id)))));
```

The method QueryDslBinder.to convert a list of querydsl tuple into desired list of City objects. The third parameter is the bind specification, where "key" and "field" properties inside class City is associated with respective querydsl expression used in select statement. Can be specified more than one "keys", "fields", "single" relations and "collection" relations on any level.

When we want a grouping, like State on class City, we specify a "single" object with the field name on class City, and specify your fields. 

When we want a grouping list, like a object Country with a list of States, we specify a "collection" with their fields. In example case, a state and your fields are already specified, so "states" field will contain a list of States with fields id and name filled, objects will have the same reference memory. Objects are instanciated only one time, according with specified "key" and type class that belongs.

With this lib, a result can be aggregated with any children level, in different manners. Children with backward parent reference, with each object and your unique key will have only one and same reference.

A complete working test code can be found in file QuerySqlBinderIntegrationTest.java running with Spring Boot, JPA, QueryDsl and H2 database.

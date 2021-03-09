package com.zisluiz.querydslbinder;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.zisluiz.querydslbinder.infra.H2JpaConfig;
import com.zisluiz.querydslbinder.infra.QuerydslBinderTestApplication;
import com.zisluiz.querydslbinder.test.entity.City;
import com.zisluiz.querydslbinder.test.entity.Country;
import com.zisluiz.querydslbinder.test.entity.QCity;
import com.zisluiz.querydslbinder.test.entity.QCountry;
import com.zisluiz.querydslbinder.test.entity.QState;
import com.zisluiz.querydslbinder.test.entity.State;

@SpringBootTest(classes = { QuerydslBinderTestApplication.class, H2JpaConfig.class }, webEnvironment = WebEnvironment.MOCK)
public class QuerySqlBinderIntegrationTest  {
	private static final QCountry _country = QCountry.country;
	private static final QState _state = QState.state;
	private static final QCity _city = QCity.city;
	
	@PersistenceContext
	private EntityManager em;
	
	@Test
	@Transactional
	public void basicQueryTest() {
		Country brazil = new Country("Brazil");
		em.persist(brazil);
		
		State santaCatarina = new State("Santa Catarina", brazil); 
		em.persist(santaCatarina);
		
		State saoPauloState = new State("São Paulo", brazil); 
		em.persist(saoPauloState);
		
		City balnearioCamboriu = new City("Balneário Camboriú", santaCatarina);
		em.persist(balnearioCamboriu);
		
		City saoPaulo = new City("São Paulo", saoPauloState);
		em.persist(saoPaulo);
		
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

		Assertions.assertTrue(cities.size() == 2);
		
		City resultBalnearioCamboriu = cities.get(0);
		
		Assertions.assertEquals(balnearioCamboriu.getName(), resultBalnearioCamboriu.getName());
		Assertions.assertEquals(santaCatarina.getName(), resultBalnearioCamboriu.getState().getName());
		Assertions.assertEquals(brazil.getName(), resultBalnearioCamboriu.getState().getCountry().getName());
		
		City resultSaoPaulo = cities.get(1);
		Assertions.assertEquals(saoPaulo.getName(), resultSaoPaulo.getName());
		Assertions.assertEquals(saoPauloState.getName(), resultSaoPaulo.getState().getName());
		Assertions.assertEquals(brazil.getName(), resultSaoPaulo.getState().getCountry().getName());
		
		//brazil must have two states
		Assertions.assertTrue(resultBalnearioCamboriu.getState().getCountry().getStates().size() == 2);
		
		//must be same objects memory
		Assertions.assertEquals(resultBalnearioCamboriu.getState().getCountry(), resultSaoPaulo.getState().getCountry());
		
		//must be different states
		Assertions.assertNotEquals(resultBalnearioCamboriu.getState().getCountry().getStates().get(0), 
				resultBalnearioCamboriu.getState().getCountry().getStates().get(1));
		
		//although name is not specified, states collections has the entire loaded object state, binded by id, with id and name filled.
		Assertions.assertEquals(resultBalnearioCamboriu.getState().getCountry().getStates().get(0).getName(), santaCatarina.getName());
		Assertions.assertEquals(resultBalnearioCamboriu.getState().getCountry().getStates().get(1).getName(), saoPauloState.getName());
	}
}

package com.zisluiz.querydslbinder.infra.util;

import java.util.List;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;

public class QueryDslUtil {

	public static Tuple createTuple(List<Object> values, List<String>  expressions) {
		return new MockTuple(values.toArray(), expressions.toArray(new String[expressions.size()]));
	}

	public static Expression<?> makeExpression(String field) {
		return Expressions.asString(field);
	}

}

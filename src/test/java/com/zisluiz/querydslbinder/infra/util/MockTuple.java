package com.zisluiz.querydslbinder.infra.util;

import java.util.Arrays;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;

public class MockTuple implements Tuple {
    private final Object[] a;
    private final Expression<String>[] expressions;

    @SuppressWarnings("unchecked")
	public MockTuple(Object[] a, String[] expressions) {
        this.a = a;
        this.expressions = new Expression[expressions.length];
        
        for (int i = 0; i < expressions.length; i++)        
            this.expressions[i] = (Expression<String>) QueryDslUtil.makeExpression(expressions[i]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(int index, Class<T> type) {
        return (T) a[index];
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T get(Expression<T> expr) {
    	for (int i = 0; i < expressions.length; i++) {
    		if (expressions[i].toString().equals(expr.toString()))
    			return (T) a[i];
    			
    	}
        return null;
    }

    @Override
    public int size() {
        return a.length;
    }

    @Override
    public Object[] toArray() {
        return a;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Tuple) {
            return Arrays.equals(a, ((Tuple) obj).toArray());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(a);
    }

    @Override
    public String toString() {
        return Arrays.toString(a);
    }

}
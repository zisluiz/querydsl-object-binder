package com.zisluiz.querydslbinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.querydsl.core.types.Expression;

public class GroupByBinder {
	private Map<FieldTypeBinder, List<FieldBinder>> paths = new HashMap<FieldTypeBinder, List<FieldBinder>>();
	private List<FieldBinder> orderedFields = new ArrayList<FieldBinder>();

	public GroupByBinder() {
	}

	/**
	 * Specify the "key" field to identify a object instance.
	 * @param attributeName name of attribute in class
	 * @param fieldExpressionPath related querydsl expression
	 * @return GroupByBinder reference
	 */
	public GroupByBinder key(String attributeName, Expression<?> fieldExpressionPath) {
		FieldBinder fieldBinder = new FieldBinder(attributeName, fieldExpressionPath, FieldTypeBinder.KEY);
		addPath(fieldBinder.getFieldType(), fieldBinder);
		orderedFields.add(fieldBinder);
		return this;
	}
	
	/**
	 * Specify the "field" (attribute) of a object instance.
	 * @param attributeName name of attribute in class
	 * @param fieldExpressionPath related querydsl expression
	 * @return GroupByBinder reference
	 */
	public GroupByBinder field(String fieldName, Expression<?> fieldExpressionPath) {
		FieldBinder fieldBinder = new FieldBinder(fieldName, fieldExpressionPath, FieldTypeBinder.FIELD);
		addPath(fieldBinder.getFieldType(), fieldBinder);
		orderedFields.add(fieldBinder);
		return this;
	}
	
	/**
	 * Specify a attribute "field" that has a own field specification, and represent a single object.
	 * @param attributeName name of attribute in class
	 * @param singleSpecfication related "fields" specification for a single object
	 * @return GroupByBinder reference
	 */
	public GroupByBinder single(String attributeName, GroupByBinder singleSpecfication) {
		FieldBinder fieldBinder = new FieldBinder(attributeName, singleSpecfication, FieldTypeBinder.SINGLE);
		addPath(fieldBinder.getFieldType(), fieldBinder);
		orderedFields.add(fieldBinder);
		return this;
	}

	/**
	 * Specify a attribute "field" that has a own field specification, and represent a collection of objects.
	 * @param attributeName name of attribute in class
	 * @param collectionSpecfication related "fields" specification for a collection of objects
	 * @return GroupByBinder reference
	 */
	public GroupByBinder collection(String attributeName, GroupByBinder collectionSpecfication) {
		FieldBinder fieldBinder = new FieldBinder(attributeName, collectionSpecfication, FieldTypeBinder.COLLECTION);
		addPath(fieldBinder.getFieldType(), fieldBinder);
		orderedFields.add(fieldBinder);
		return this;
	}

	protected boolean containsKeyOrField() {
		return containsKeys() || !getPath(FieldTypeBinder.FIELD).isEmpty();
	}

	protected boolean containsKeys() {
		return !getPath(FieldTypeBinder.KEY).isEmpty();
	}

	protected boolean containsGroupBy() {
		return !getPath(FieldTypeBinder.COLLECTION).isEmpty() || !getPath(FieldTypeBinder.SINGLE).isEmpty();
	}

	protected List<FieldBinder> getAllFields() {
		return orderedFields;
	}

	protected List<FieldBinder> getKeysPath() {
		return getPath(FieldTypeBinder.KEY);
	}

	protected List<FieldBinder> getGroupByPaths() {
		List<FieldBinder> groupBys = new ArrayList<FieldBinder>(getPath(FieldTypeBinder.COLLECTION));
		groupBys.addAll(getPath(FieldTypeBinder.SINGLE));
		return groupBys;
	}

	public List<FieldBinder> getFieldsPath() {
		return getPath(FieldTypeBinder.FIELD);
	}

	public List<FieldBinder> getSinglesPath() {
		return getPath(FieldTypeBinder.SINGLE);
	}

	private void addPath(FieldTypeBinder fieldType, FieldBinder fieldBinder) {
		List<FieldBinder> fields = paths.computeIfAbsent(fieldType, k -> new ArrayList<FieldBinder>());

		fields.add(fieldBinder);
	}

	private List<FieldBinder> getPath(FieldTypeBinder fieldType) {
		List<FieldBinder> fields = paths.computeIfAbsent(fieldType, k -> new ArrayList<FieldBinder>());

		return fields;
	}
}

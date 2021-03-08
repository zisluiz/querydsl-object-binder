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

	public GroupByBinder field(String fieldNameDto, Expression<?> selectFieldPath) {
		FieldBinder fieldBinder = new FieldBinder(fieldNameDto, selectFieldPath, FieldTypeBinder.FIELD);
		addPath(fieldBinder.getFieldType(), fieldBinder);
		orderedFields.add(fieldBinder);
		return this;
	}

	public GroupByBinder collection(String fieldNameDto, GroupByBinder collectionSpecfication) {
		FieldBinder fieldBinder = new FieldBinder(fieldNameDto, collectionSpecfication, FieldTypeBinder.COLLECTION);
		addPath(fieldBinder.getFieldType(), fieldBinder);
		orderedFields.add(fieldBinder);
		return this;
	}

	public GroupByBinder single(String fieldNameDto, GroupByBinder singleSpecfication) {
		FieldBinder fieldBinder = new FieldBinder(fieldNameDto, singleSpecfication, FieldTypeBinder.SINGLE);
		addPath(fieldBinder.getFieldType(), fieldBinder);
		orderedFields.add(fieldBinder);
		return this;
	}

	public GroupByBinder key(String fieldNameDto, Expression<?> selectKeyFieldPath) {
		FieldBinder fieldBinder = new FieldBinder(fieldNameDto, selectKeyFieldPath, FieldTypeBinder.KEY);
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

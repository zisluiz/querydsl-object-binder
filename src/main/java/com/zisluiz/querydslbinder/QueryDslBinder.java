package com.zisluiz.querydslbinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.querydsl.core.Tuple;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class QueryDslBinder {
	private Map<GroupByBinder, ClassConfiguration<?>> constructors = new HashMap<GroupByBinder, ClassConfiguration<?>>();
	private Map<RowKey, Object> mapKeys = new HashMap<RowKey, Object>();
	
	private QueryDslBinder() {
	}

	@Getter
	@Setter
	private static class ClassConfiguration<T> {
		private Class<T> dtoClass;
		private Constructor<T> noArgumentConstructor;
		private GroupByBinder groupByBinder;		
		private List<T> orderedRows = new ArrayList<T>();

		public ClassConfiguration(Class<T> dtoClass, GroupByBinder groupByBinder) {
			this.dtoClass = dtoClass;
			this.groupByBinder = groupByBinder;
		}

		public void addRow(T instancedDto) {
			orderedRows.add(instancedDto);
		}
	}

	@Getter
	@Setter
	@AllArgsConstructor
	private class InstancedRow<T> {
		private RowKey rowKey;
		private T instance;
		private boolean cached = false;
	}

	@Getter
	@Setter
	private static class RowKey {
		private List<Object> keys;
		private List<String> expressionKey;
		private String hashKey;		

		public RowKey(List<String> expressionKey, String className, List<Object> keys) {
			this.expressionKey = expressionKey;
			this.keys = keys;
		
			this.hashKey = StringUtils.join(this.expressionKey, ",");
			this.hashKey += "[" + className + "]";
			this.hashKey += "[";
			
			for (Object key : this.keys)
				if (key != null)
					this.hashKey += key.toString() + ".";
			
			this.hashKey += "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + this.hashKey.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RowKey other = (RowKey) obj;

			if (hashKey == null) {
				return other.hashKey == null;
			} else return hashKey.equals(other.hashKey);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> to(List<Tuple> tupleList, Class<T> dtoClass, GroupByBinder groupByBinder) {
		if (CollectionUtils.isEmpty(tupleList))
			return new ArrayList<>();
		try {
			dtoClass = (Class<T>) Class.forName(dtoClass.getTypeName());

			validate(dtoClass, groupByBinder);

			QueryDslBinder instance = new QueryDslBinder();
			ClassConfiguration<T> classConfiguration = instance.getClassConfiguration(dtoClass, groupByBinder);

			for (Tuple tuple : tupleList) {
				instance.bindTuple(null, tuple, classConfiguration);
			}

			return (List<T>) classConfiguration.getOrderedRows();
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | NoSuchMethodException
				| InstantiationException | IllegalAccessException | InvocationTargetException
				| IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T one(Tuple tuple, Class<T> dtoClass, GroupByBinder groupByBinder) {
		if (tuple == null)
			return null;

		try {
			dtoClass = (Class<T>) Class.forName(dtoClass.getTypeName());
			validate(dtoClass, groupByBinder);

			QueryDslBinder instance = new QueryDslBinder();
			ClassConfiguration<T> classConfiguration = instance.getClassConfiguration(dtoClass, groupByBinder);
			return (T) instance.bindTuple(null, tuple, classConfiguration).getInstance();
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | NoSuchMethodException
				| InstantiationException | IllegalAccessException | InvocationTargetException
				| IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T one(List<Tuple> tupleList, Class<T> dtoClass, GroupByBinder groupByBinder) {
		List<T> resultList = to(tupleList, dtoClass, groupByBinder);
		if (resultList.size() > 1) {
			throw new RuntimeException("The grouping rule generated more than one item.");
		} else if (resultList.size() == 1) {
			return resultList.get(0);
		}
		return null;
	}

	private <T> InstancedRow<T> bindTuple(RowKey parent, Tuple tuple, ClassConfiguration<T> classConfiguration)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, IllegalArgumentException,
			NoSuchMethodException, SecurityException, ClassNotFoundException, NoSuchFieldException {
		List<Pair<FieldBinder, Object>> keys = new ArrayList<>();
		List<Pair<FieldBinder, Object>> groupBys = new ArrayList<>();

		extractKeys(tuple, classConfiguration.getGroupByBinder().getKeysPath(), keys);
		InstancedRow<T> row = getInstanceDto(parent, classConfiguration, keys);

		if (row == null)
			return null;

		if (!row.isCached()) {
			List<Pair<FieldBinder, Object>> values = new ArrayList<>();
			extractValues(tuple, classConfiguration.getGroupByBinder().getFieldsPath(), values);

			boolean hasAnyValue = false;

			for (Pair<FieldBinder, Object> key : keys) {
				if (key.getValue() != null) {
					hasAnyValue = true;
					FieldUtils.writeField(key.getKey().getAttribute(), row.getInstance(), key.getValue(), true);
				}
			}

			for (Pair<FieldBinder, Object> value : values) {
				if (value.getValue() != null) {
					hasAnyValue = true;
					FieldUtils.writeField(value.getKey().getAttribute(), row.getInstance(), value.getValue(), true);
				}
			}

			if (hasAnyValue) {
				if (parent == null)
					classConfiguration.addRow(row.getInstance());
				
				if (row.getRowKey() != null)
					getMapKeys().put(row.getRowKey(), row.getInstance());				
			}
		}

		if (row.getRowKey() != null) {
			extractGroupBy(row.getRowKey(), tuple, classConfiguration.getGroupByBinder().getGroupByPaths(), groupBys);
		}

		setGroupByValues(row.getInstance(), groupBys);
		return row;
	}

	@SuppressWarnings("unchecked")
	private <T> InstancedRow<T> getInstanceDto(RowKey parentKey, ClassConfiguration<T> classConfiguration,
			List<Pair<FieldBinder, Object>> keys)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ClassNotFoundException {
		T instancedDto = null;
		RowKey rowKey = null;
		InstancedRow<T> instance = null;

		if (!keys.isEmpty()) {
			List<Object> identifiers = new ArrayList<Object>();
			List<String> expressions = new ArrayList<String>();

			for (Pair<FieldBinder, Object> key : keys) {
				if (key.getValue() != null)
					identifiers.add(key.getValue());
				if (key.getKey() != null)
					expressions.add(key.getKey().getExpression().toString());
			}

			if (identifiers.isEmpty())
				return null;

			rowKey = new RowKey(expressions, classConfiguration.getDtoClass().getSimpleName(), identifiers);
			instancedDto = (T) getMapKeys().get(rowKey);
		}

		if (instancedDto == null) {
			instancedDto = classConfiguration.noArgumentConstructor.newInstance();
			instance = new InstancedRow<T>(rowKey, instancedDto, false);
		} else
			instance = new InstancedRow<T>(rowKey, instancedDto, true);

		return instance;
	}

	private <T> void setGroupByValues(T parsedRow, List<Pair<FieldBinder, Object>> groupBys)
			throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException,
			IllegalArgumentException, SecurityException, ClassNotFoundException {
		for (Pair<FieldBinder, Object> groupBy : groupBys) {
			if (groupBy.getKey().isCollection()) { // its a collection DTO property
				@SuppressWarnings("unchecked")
				Collection<Object> children = (Collection<Object>) FieldUtils.readField(groupBy.getKey().getAttribute(),
						parsedRow, true);
				if (children == null) {
					Class<?> collectionClass = groupBy.getKey().getAttribute().getType();
					if (collectionClass.equals(Set.class))
						children = new HashSet<>();
					else if (collectionClass.equals(List.class))
						children = new ArrayList<Object>();

					FieldUtils.writeField(groupBy.getKey().getAttribute(), parsedRow, children, true);
				}

				if (groupBy.getValue() != null && !children.contains(groupBy.getValue())) {
					children.add(groupBy.getValue());
				}
			} else { // its a single DTO property
				FieldUtils.writeField(groupBy.getKey().getAttribute(), parsedRow, groupBy.getValue(), true);
			}
		}
	}

	private void extractKeys(Tuple tuple, List<FieldBinder> bindFields, List<Pair<FieldBinder, Object>> keys)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		for (FieldBinder keyFieldconfig : bindFields) {
			Object keyValue = keyFieldconfig.getTupleValue(tuple);
			keys.add(Pair.of(keyFieldconfig, keyValue));
		}
	}

	private void extractValues(Tuple tuple, List<FieldBinder> bindFields, List<Pair<FieldBinder, Object>> values)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		for (FieldBinder fieldConfig : bindFields) {
			Object fieldValue = fieldConfig.getTupleValue(tuple);
			values.add(Pair.of(fieldConfig, fieldValue));
		}
	}

	private void extractGroupBy(RowKey parent, Tuple tuple, List<FieldBinder> bindFields,
			List<Pair<FieldBinder, Object>> collections)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, IllegalArgumentException,
			NoSuchMethodException, SecurityException, ClassNotFoundException, NoSuchFieldException {
		for (FieldBinder groupByFieldConfig : bindFields) {
			Class<?> groupByType = groupByFieldConfig.getGroupByTypeClass();
			InstancedRow<?> child = bindTuple(parent, tuple,
					getClassConfiguration(groupByType, groupByFieldConfig.getGroupBy()));
			if (child != null)
				collections.add(Pair.of(groupByFieldConfig, child.getInstance()));
		}
	}

	private <T> ClassConfiguration<T> getClassConfiguration(Class<T> dtoClass, GroupByBinder groupBy)
			throws NoSuchFieldException, SecurityException, NoSuchMethodException {
		@SuppressWarnings("unchecked")
		ClassConfiguration<T> configuration = (ClassConfiguration<T>) constructors.get(groupBy);
		if (configuration == null) {
			configuration = loadClassConfigurations(dtoClass, groupBy);
			constructors.put(groupBy, configuration);
		}

		return configuration;
	}

	private <T> ClassConfiguration<T> loadClassConfigurations(Class<T> dtoClass, GroupByBinder groupBy)
			throws NoSuchFieldException, SecurityException, NoSuchMethodException {
		ClassConfiguration<T> configuration = new ClassConfiguration<T>(dtoClass, groupBy);

		configuration.setNoArgumentConstructor(dtoClass.getConstructor());

		for (FieldBinder key : groupBy.getAllFields()) {
			Field field = getFieldClass(dtoClass, key);

			key.setAttribute(field);
		}

		return configuration;
	}

	private static <T> Field getFieldClass(Class<T> dtoClass, FieldBinder key) {
		Field field = FieldUtils.getField(dtoClass, key.getAttributeName(), true);

		if (field == null)
			throw new IllegalArgumentException(
					"Field " + key.getAttributeName() + " not found on class " + dtoClass.getName() + "!");
		return field;
	}

	private static <T> void validate(Class<T> dtoClass, GroupByBinder groupBy) throws ClassNotFoundException {
		if (!groupBy.containsKeyOrField())
			throw new IllegalArgumentException("No field specified on GroupByBinder!");

		if (!groupBy.containsKeys() && groupBy.containsGroupBy())
			throw new IllegalArgumentException(
					"At least one key field must be specified when collection grouping is used!");

		try {
			dtoClass.getConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Must exist a public constructor with no args in class " + dtoClass.getName() + "!");
		}

		for (FieldBinder fieldBinder : groupBy.getAllFields()) {
			if (StringUtils.isEmpty(fieldBinder.getAttributeName()))
				if (fieldBinder.isKey())
					throw new IllegalArgumentException(
							"Must be set the field name to be used as key in class " + dtoClass.getName() + "!");
				else if (fieldBinder.isCollection())
					throw new IllegalArgumentException(
							"Must be set the field name of added collection in class "
									+ dtoClass.getName() + "!");
				else
					throw new IllegalArgumentException(
							"Must be set the field name from class " + dtoClass.getName() + "!");

			if (fieldBinder.isGroupBy()) {
				if (fieldBinder.getClass() == null)
					throw new IllegalArgumentException(
							"Must be set the class of objects inside collection in class " + dtoClass.getName() + "!");

				fieldBinder.setAttribute(getFieldClass(dtoClass, fieldBinder));

				validate(fieldBinder.getGroupByTypeClass(), fieldBinder.getGroupBy());
			} else if (fieldBinder.getExpression() == null)
				throw new IllegalArgumentException(
						"Must be set the a QueryDsl expression used on select query!");
		}
	}
	
	public Map<RowKey, Object> getMapKeys() {
		return mapKeys;
	}
}

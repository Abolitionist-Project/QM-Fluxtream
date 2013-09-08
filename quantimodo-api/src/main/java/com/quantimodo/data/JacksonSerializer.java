package com.quantimodo.data;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParseException;

public class JacksonSerializer extends ObjectMapper {
	private static final SimpleModule module;
	static {
		module = new SimpleModule("Quantimodo");
		module.<Success>addSerializer(Success.class, new SuccessSerializer());
		
		module.<Measurement>addSerializer(Measurement.class, new MeasurementSerializer());
		module.<Measurement>addDeserializer(Measurement.class, new MeasurementDeserializer());
		
		module.<MeasurementSource>addSerializer(MeasurementSource.class, new MeasurementSourceSerializer());
		module.<MeasurementSource>addDeserializer(MeasurementSource.class, new MeasurementSourceDeserializer());
		
		module.<Unit>addSerializer(Unit.class, new UnitSerializer());
		module.<Unit>addDeserializer(Unit.class, new UnitDeserializer());
		
		module.<UnitCategory>addSerializer(UnitCategory.class, new UnitCategorySerializer());
		module.<UnitCategory>addDeserializer(UnitCategory.class, new UnitCategoryDeserializer());
		
		module.<Variable>addSerializer(Variable.class, new VariableSerializer());
		module.<Variable>addDeserializer(Variable.class, new VariableDeserializer());
		
		module.<VariableCategory>addSerializer(VariableCategory.class, new VariableCategorySerializer());
		module.<VariableCategory>addDeserializer(VariableCategory.class, new VariableCategoryDeserializer());
		
		module.<VariableUserSettings>addSerializer(VariableUserSettings.class, new VariableUserSettingsSerializer());
		module.<VariableUserSettings>addDeserializer(VariableUserSettings.class, new VariableUserSettingsDeserializer());
	}
	
	public JacksonSerializer() {
		super();
		this.registerModule(module);
	}
	
	public static class SuccessSerializer extends JsonSerializer<Success> {
		@Override
		public void serialize(final Success value, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
			if (value == null) { generator.writeNull(); return; }
			generator.writeStartObject();
			generator.writeBooleanField("success", true);
			generator.writeEndObject();
		}
	}
	
	public static class MeasurementSerializer extends JsonSerializer<Measurement> {
		@Override
		public void serialize(final Measurement value, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
			if (value == null) { generator.writeNull(); return; }
			generator.writeStartObject();
			generator.writeStringField("source", value.getMeasurementSource());
			generator.writeStringField("variable", value.getVariableName());
			generator.writeStringField("combinationOperation", value.getCombinationOperation().toString());
			generator.writeNumberField("timestamp", value.getTimestamp() / 1000L);
			generator.writeNumberField("value", value.getValue());
			generator.writeStringField("unit", value.getAbbreviatedUnitName());
			generator.writeEndObject();
		}
	}
	
	public static class MeasurementDeserializer extends JsonDeserializer<Measurement> {
		@Override
		public Measurement deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
			final ObjectCodec codec = parser.getCodec();
			final JsonNode object = codec.<JsonNode>readTree(parser);
			if (object.isNull()) return null;
			final String measurementSource = getString(codec, object, "source");
			final String variableName = getString(codec, object, "variable");
			final CombinationOperation combinationOperation = CombinationOperation.valueOf(getString(codec, object, "combinationOperation"));
			final long timestamp = getLong(codec, object, "timestamp")*1000L;
			final double value = getDouble(codec, object, "value");
			final String abbreviatedUnitName = getString(codec, object, "unit");
			return new Measurement(measurementSource, variableName, combinationOperation, timestamp, value, abbreviatedUnitName);
		}
	}
	
	public static class MeasurementSourceSerializer extends JsonSerializer<MeasurementSource> {
		@Override
		public void serialize(final MeasurementSource value, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
			if (value == null) { generator.writeNull(); return; }
			generator.writeStartObject();
			generator.writeStringField("name", value.getName());
			generator.writeEndObject();
		}
	}
	
	public static class MeasurementSourceDeserializer extends JsonDeserializer<MeasurementSource> {
		@Override
		public MeasurementSource deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
			final ObjectCodec codec = parser.getCodec();
			final JsonNode object = codec.<JsonNode>readTree(parser);
			if (object.isNull()) return null;
			final String name = getString(codec, object, "name");
			return new MeasurementSource(name);
		}
	}
	
	public static class UnitSerializer extends JsonSerializer<Unit> {
		@Override
		public void serialize(final Unit value, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
			if (value == null) { generator.writeNull(); return; }
			generator.writeStartObject();
			generator.writeStringField("name", value.getName());
			generator.writeStringField("abbreviatedName", value.getAbbreviatedName());
			generator.writeStringField("category", value.getCategoryName());
			generator.writeArrayFieldStart("conversionSteps");
			for (final Unit.ConversionStep operation : value.getConversionSteps()) {
				generator.writeStartObject();
				generator.writeStringField("operation", operation.operation.toString());
				generator.writeNumberField("value", operation.value);
				generator.writeEndObject();
			}
			generator.writeEndArray();
			generator.writeEndObject();
		}
	}
	
	public static class UnitDeserializer extends JsonDeserializer<Unit> {
		@Override
		public Unit deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
			final ObjectCodec codec = parser.getCodec();
			final JsonNode object = codec.<JsonNode>readTree(parser);
			if (object.isNull()) return null;
			final String name = getString(codec, object, "name");
			final String abbreviatedName = getString(codec, object, "abbreviatedName");
			final String categoryName = getString(codec, object, "category");
			final List<Unit.ConversionStep> conversionSteps = new ArrayList<Unit.ConversionStep>();
			for (final JsonNode conversionStep : getArray(codec, object, "conversionSteps")) {
				final String operation = getString(codec, conversionStep, "operation");
				final double value = getDouble(codec, conversionStep, "value");
				conversionSteps.add(new Unit.ConversionStep(operation, value));
			}
			return new Unit(name, abbreviatedName, categoryName, conversionSteps.toArray(new Unit.ConversionStep[0]));
		}
	}
	
	public static class UnitCategorySerializer extends JsonSerializer<UnitCategory> {
		@Override
		public void serialize(final UnitCategory value, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
			if (value == null) { generator.writeNull(); return; }
			generator.writeStartObject();
			generator.writeStringField("name", value.getName());
			generator.writeEndObject();
		}
	}
	
	public static class UnitCategoryDeserializer extends JsonDeserializer<UnitCategory> {
		@Override
		public UnitCategory deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
			final ObjectCodec codec = parser.getCodec();
			final JsonNode object = codec.<JsonNode>readTree(parser);
			if (object.isNull()) return null;
			final String name = getString(codec, object, "name");
			return new UnitCategory(name);
		}
	}
	
	public static class VariableSerializer extends JsonSerializer<Variable> {
		@Override
		public void serialize(final Variable value, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
			if (value == null) { generator.writeNull(); return; }
			generator.writeStartObject();
			generator.writeStringField("name", value.getName());
			generator.writeStringField("category", value.getCategoryName());
			generator.writeStringField("unit", value.getAbbreviatedDefaultUnitName());
			generator.writeStringField("combinationOperation", value.getCombinationOperation().toString());
			generator.writeEndObject();
		}
	}
	
	public static class VariableDeserializer extends JsonDeserializer<Variable> {
		@Override
		public Variable deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
			final ObjectCodec codec = parser.getCodec();
			final JsonNode object = codec.<JsonNode>readTree(parser);
			if (object.isNull()) return null;
			final String name = getString(codec, object, "name");
			final String categoryName = getString(codec, object, "category");
			final String abbreviatedDefaultUnitName = getString(codec, object, "unit");
			final CombinationOperation combinationOperation = CombinationOperation.valueOf(getString(codec, object, "combinationOperation"));
			return new Variable(name, categoryName, abbreviatedDefaultUnitName, combinationOperation);
		}
	}
	
	public static class VariableCategorySerializer extends JsonSerializer<VariableCategory> {
		@Override
		public void serialize(final VariableCategory value, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
			if (value == null) { generator.writeNull(); return; }
			generator.writeStartObject();
			generator.writeStringField("name", value.getName());
			generator.writeEndObject();
		}
	}
	
	public static class VariableCategoryDeserializer extends JsonDeserializer<VariableCategory> {
		@Override
		public VariableCategory deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
			final ObjectCodec codec = parser.getCodec();
			final JsonNode object = codec.<JsonNode>readTree(parser);
			if (object.isNull()) return null;
			final String name = getString(codec, object, "name");
			return new VariableCategory(name);
		}
	}
	
	public static class VariableUserSettingsSerializer extends JsonSerializer<VariableUserSettings> {
		@Override
		public void serialize(final VariableUserSettings value, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
			if (value == null) { generator.writeNull(); return; }
			generator.writeStartObject();
			generator.writeStringField("variable", value.getVariableName());
			generator.writeStringField("unit", value.getAbbreviatedUnitName());
			generator.writeEndObject();
		}
	}
	
	public static class VariableUserSettingsDeserializer extends JsonDeserializer<VariableUserSettings> {
		@Override
		public VariableUserSettings deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
			final ObjectCodec codec = parser.getCodec();
			final JsonNode object = codec.<JsonNode>readTree(parser);
			if (object.isNull()) return null;
			final String variableName = getString(codec, object, "variable");
			final String abbreviatedUnitName = getString(codec, object, "unit");
			return new VariableUserSettings(variableName, abbreviatedUnitName);
		}
	}
	
	private static final long getLong(final ObjectCodec codec, final JsonNode node, final String fieldName) throws JsonParseException {
		if (!node.has(fieldName)) throw new JsonParseException("Cannot find field named " + fieldName + ".", codec.treeAsTokens(node).getCurrentLocation());
		final JsonNode field = node.get(fieldName);
		if (!field.canConvertToLong()) throw new JsonParseException("Field named " + fieldName + " is not a valid long.", codec.treeAsTokens(node).getCurrentLocation());
		return field.asLong();
	}
	
	private static final double getDouble(final ObjectCodec codec, final JsonNode node, final String fieldName) throws JsonParseException {
		if (!node.has(fieldName)) throw new JsonParseException("Cannot find field named " + fieldName + ".", codec.treeAsTokens(node).getCurrentLocation());
		final JsonNode field = node.get(fieldName);
		if (!field.isNumber()) throw new JsonParseException("Field named " + fieldName + " is not a valid double.", codec.treeAsTokens(node).getCurrentLocation());
		return field.asDouble();
	}
	
	private static final String getString(final ObjectCodec codec, final JsonNode node, final String fieldName) throws JsonParseException {
		if (!node.has(fieldName)) throw new JsonParseException("Cannot find field named " + fieldName + ".", codec.treeAsTokens(node).getCurrentLocation());
		final JsonNode field = node.get(fieldName);
		if (!field.isTextual()) throw new JsonParseException("Field named " + fieldName + " is not a valid String.", codec.treeAsTokens(node).getCurrentLocation());
		return field.textValue();
	}
	
	private static final JsonNode getArray(final ObjectCodec codec, final JsonNode node, final String fieldName) throws JsonParseException {
		if (!node.has(fieldName)) throw new JsonParseException("Cannot find field named " + fieldName + ".", codec.treeAsTokens(node).getCurrentLocation());
		final JsonNode field = node.get(fieldName);
		if (!field.isArray()) throw new JsonParseException("Field named " + fieldName + " is not a valid array.", codec.treeAsTokens(node).getCurrentLocation());
		return field;
	}
}

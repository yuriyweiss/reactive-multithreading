package yuriy.weiss.common.utils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.commons.lang3.StringUtils;

import yuriy.weiss.common.utils.json.adapter.LocalDateSerializer;
import yuriy.weiss.common.utils.json.adapter.LocalDateTimeSerializer;

import static java.lang.Long.parseLong;
import static yuriy.weiss.common.utils.DateUtils.DEFAULT_TIME_ZONE;
import static yuriy.weiss.common.utils.DateUtils.parseDate;

public class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Создать ObjectMapper, дополнив его десериализаторами типов LocalDateTime и LocalDate.<br>
     * Задать дополнительные параметры маппинга.
     *
     * @return дополненный экземпляр ObjectMapper
     */
    public static ObjectMapper createObjectMapper() {
        final SimpleModule simpleModule = new SimpleModule();

        simpleModule.addDeserializer( LocalDateTime.class,
                new StdScalarDeserializer<LocalDateTime>( LocalDateTime.class ) {
                    @Override
                    public LocalDateTime deserialize( JsonParser jsonParser,
                            DeserializationContext deserializationContext ) throws IOException {
                        final String valueAsString = jsonParser.getValueAsString();
                        if ( valueAsString == null ) {
                            return null;
                        }
                        try {
                            long timestamp = parseLong( valueAsString );
                            return LocalDateTime.ofInstant( Instant.ofEpochMilli( timestamp ), DEFAULT_TIME_ZONE );
                        } catch ( NumberFormatException e ) {
                            // do nothing
                        }
                        DateTimeParseException parseException = null;
                        List<String> dateTimeFormats = Arrays.asList( "yyyy-MM-dd'T'HH:mm:ss.SSS",
                                "yyyy-MM-dd'T'HH:mm:ss.SS",
                                "yyyy-MM-dd'T'HH:mm:ss.S", "yyyy-MM-dd'T'HH:mm:ss" );
                        for ( String dateTimeFormat : dateTimeFormats ) {
                            try {
                                return DateUtils.formatDateTime( valueAsString, dateTimeFormat );
                            } catch ( DateTimeParseException e ) {
                                parseException = e;
                            }
                        }
                        throw parseException; //NOSONAR
                    }
                } );

        simpleModule.addDeserializer( LocalDate.class, new StdScalarDeserializer<LocalDate>( LocalDate.class ) {
            @Override
            public LocalDate deserialize( JsonParser jsonParser, DeserializationContext deserializationContext ) throws IOException {
                final String valueAsString = jsonParser.getValueAsString();
                if ( valueAsString == null ) {
                    return null;
                }
                try {
                    long timestamp = parseLong( valueAsString );
                    return LocalDateTime.ofInstant( Instant.ofEpochMilli( timestamp ),
                            DEFAULT_TIME_ZONE ).toLocalDate();
                } catch ( NumberFormatException e ) {
                    // do nothing
                }
                return parseDate( valueAsString );
            }
        } );

        simpleModule.addSerializer( LocalDateTime.class, new LocalDateTimeSerializer() );
        simpleModule.addSerializer( LocalDate.class, new LocalDateSerializer() );
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule( simpleModule );
        mapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );
        mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );
        return mapper;
    }

    /**
     * Преобразовать любой объект в строку в формате JSON.
     *
     * @param object исходный объект
     * @return json-представление объекта
     * @throws JsonProcessingException
     */
    public static String objectToJsonString( Object object ) throws JsonProcessingException {
        return objectToJsonString( null, object );
    }

    /**
     * Преобразовать любой объект в строку в формате JSON.
     *
     * @param objectMapper существующий objectMapper
     * @param object       исходный объект
     * @return json-представление объекта
     * @throws JsonProcessingException
     */
    public static String objectToJsonString( final ObjectMapper objectMapper, final Object object ) throws JsonProcessingException {
        if ( object != null ) {
            return Objects.requireNonNullElseGet( objectMapper, JsonUtils::createObjectMapper ).writeValueAsString(
                    object );
        } else {
            return null;
        }
    }

    /**
     * Дженерик-метод, преобразующий json-строку в объект.
     *
     * @param jsonString  json-строка с содержимым объекта
     * @param objectClass класс объекта
     * @param <T>         тип объекта, определяется по контексту вызова и по заданному классу
     * @return объект заданного класса или null, если строка пустая
     * @throws IOException ошибка парсинга json или приведения к заданному классу
     */
    public static <T> T jsonStringToObject( String jsonString, Class<T> objectClass ) throws IOException {
        return jsonStringToObject( null, jsonString, objectClass );
    }

    /**
     * Дженерик-метод, преобразующий json-строку в объект.
     *
     * @param objectMapper существующий objectMapper
     * @param jsonString   json-строка с содержимым объекта
     * @param objectClass  класс объекта
     * @param <T>          тип объекта, определяется по контексту вызова и по заданному классу
     * @return объект заданного класса или null, если строка пустая
     * @throws IOException ошибка парсинга json или приведения к заданному классу
     */
    public static <T> T jsonStringToObject( final ObjectMapper objectMapper, final String jsonString,
            final Class<T> objectClass ) throws IOException {
        if ( StringUtils.isNotBlank( jsonString ) ) {
            return Objects.requireNonNullElseGet( objectMapper, JsonUtils::createObjectMapper ).readValue( jsonString,
                    objectClass );
        } else {
            return null;
        }
    }

    /**
     * Дженерик-метод, преобразующий json-строку в массив объектов.
     *
     * @param jsonString  json-строка с содержимым
     * @param objectClass класс объекта в массиве
     * @return массив объектов заданного класса или null, если строка пустая
     * @throws IOException ошибка парсинга json или приведения к заданному классу
     */
    public static <T> List<T> jsonStringToArray( String jsonString, Class<T[]> objectClass ) throws IOException {
        return jsonStringToArray( null, jsonString, objectClass );
    }

    /**
     * Дженерик-метод, преобразующий json-строку в массив объектов.
     *
     * @param objectMapper существующий objectMapper
     * @param jsonString   json-строка с содержимым
     * @param objectClass  класс объекта в массиве
     * @return массив объектов заданного класса или null, если строка пустая
     * @throws IOException ошибка парсинга json или приведения к заданному классу
     */
    public static <T> List<T> jsonStringToArray( final ObjectMapper objectMapper, final String jsonString,
            final Class<T[]> objectClass ) throws IOException {
        if ( StringUtils.isNotBlank( jsonString ) ) {
            return Arrays.asList(
                    Objects.requireNonNullElseGet( objectMapper, JsonUtils::createObjectMapper ).readValue( jsonString,
                            objectClass ) );
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Распарсить json-строку в дерево элементов JsonNode.
     *
     * @param objectMapper существующий objectMapper
     * @param jsonString   json-строка с содержимым
     * @return распарсенное дерево json-нод с содержимым
     * @throws IOException ошибка парсинга json
     */
    public static JsonNode jsonStringToNode( final ObjectMapper objectMapper, final String jsonString ) throws IOException {
        if ( StringUtils.isNotBlank( jsonString ) ) {
            return Objects.requireNonNullElseGet( objectMapper, JsonUtils::createObjectMapper ).readTree( jsonString );
        } else {
            return null;
        }
    }
}

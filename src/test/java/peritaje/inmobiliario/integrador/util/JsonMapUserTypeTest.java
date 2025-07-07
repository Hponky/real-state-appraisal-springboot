package peritaje.inmobiliario.integrador.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonMapUserTypeTest {

    private JsonMapUserType jsonMapUserType;
    private ObjectMapper mockObjectMapper;

    @BeforeEach
    void setUp() {
        mockObjectMapper = mock(ObjectMapper.class);
        jsonMapUserType = new JsonMapUserType(mockObjectMapper);
    }

    @Test
    @DisplayName("Debe convertir un mapa a una columna de base de datos JSON")
    void convertToDatabaseColumn_shouldConvertMapToJsonString() throws JsonProcessingException {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key1", "value1");
        testMap.put("key2", 123);
        String expectedJson = "{\"key1\":\"value1\",\"key2\":123}";

        when(mockObjectMapper.writeValueAsString(testMap)).thenReturn(expectedJson);

        String result = jsonMapUserType.convertToDatabaseColumn(testMap);

        assertNotNull(result);
        assertEquals(expectedJson, result);
        verify(mockObjectMapper, times(1)).writeValueAsString(testMap);
    }

    @Test
    @DisplayName("Debe devolver nulo cuando el mapa de entrada es nulo para la conversión a columna de base de datos")
    void convertToDatabaseColumn_shouldReturnNullWhenAttributeIsNull() {
        String result = jsonMapUserType.convertToDatabaseColumn(null);
        assertNull(result);
        verifyNoInteractions(mockObjectMapper);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando falla la serialización a JSON")
    void convertToDatabaseColumn_shouldThrowIllegalArgumentExceptionOnSerializationError() throws JsonProcessingException {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key1", "value1");

        when(mockObjectMapper.writeValueAsString(testMap)).thenThrow(JsonProcessingException.class);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            jsonMapUserType.convertToDatabaseColumn(testMap);
        });

        assertTrue(thrown.getMessage().contains("Error serializing Map to JSON string"));
        assertNotNull(thrown.getCause());
        assertTrue(thrown.getCause() instanceof JsonProcessingException);
        verify(mockObjectMapper, times(1)).writeValueAsString(testMap);
    }

    @Test
    @DisplayName("Debe convertir una cadena JSON de base de datos a un mapa de entidad")
    void convertToEntityAttribute_shouldConvertJsonStringToMap() throws IOException {
        String jsonString = "{\"key1\":\"value1\",\"key2\":123}";
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("key1", "value1");
        expectedMap.put("key2", 123);

        when(mockObjectMapper.readValue(jsonString, Map.class)).thenReturn(expectedMap);

        Map<String, Object> result = jsonMapUserType.convertToEntityAttribute(jsonString);

        assertNotNull(result);
        assertEquals(expectedMap, result);
        verify(mockObjectMapper, times(1)).readValue(jsonString, Map.class);
    }

    @Test
    @DisplayName("Debe devolver nulo cuando la cadena de base de datos es nula para la conversión a entidad")
    void convertToEntityAttribute_shouldReturnNullWhenDbDataIsNull() {
        Map<String, Object> result = jsonMapUserType.convertToEntityAttribute(null);
        assertNull(result);
        verifyNoInteractions(mockObjectMapper);
    }

    @Test
    @DisplayName("Debe devolver nulo cuando la cadena de base de datos está vacía para la conversión a entidad")
    void convertToEntityAttribute_shouldReturnNullWhenDbDataIsEmpty() {
        Map<String, Object> result = jsonMapUserType.convertToEntityAttribute("");
        assertNull(result);
        verifyNoInteractions(mockObjectMapper);
    }

    @Test
    @DisplayName("Debe devolver nulo cuando la cadena de base de datos está en blanco para la conversión a entidad")
    void convertToEntityAttribute_shouldReturnNullWhenDbDataIsBlank() {
        Map<String, Object> result = jsonMapUserType.convertToEntityAttribute("   ");
        assertNull(result);
        verifyNoInteractions(mockObjectMapper);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando falla la deserialización de JSON")
    void convertToEntityAttribute_shouldThrowIllegalArgumentExceptionOnDeserializationError() throws IOException {
        String jsonString = "{invalid json}";

        when(mockObjectMapper.readValue(jsonString, Map.class)).thenThrow(JsonProcessingException.class);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            jsonMapUserType.convertToEntityAttribute(jsonString);
        });

        assertTrue(thrown.getMessage().contains("Error deserializing JSON string to Map"));
        assertNotNull(thrown.getCause());
        assertTrue(thrown.getCause() instanceof IOException);
        verify(mockObjectMapper, times(1)).readValue(jsonString, Map.class);
    }
}
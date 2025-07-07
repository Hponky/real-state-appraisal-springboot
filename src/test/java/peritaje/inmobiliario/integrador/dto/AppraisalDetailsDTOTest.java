package peritaje.inmobiliario.integrador.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppraisalDetailsDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Debe crear una instancia de AppraisalDetailsDTO con el constructor AllArgsConstructor")
    void shouldCreateInstanceWithAllArgsConstructor() {
        AppraisalDetailsDTO dto = new AppraisalDetailsDTO(
                "Casa", "Calle Falsa 123", "Springfield", "Central", "3", 150.5);

        assertNotNull(dto);
        assertEquals("Casa", dto.getPropertyType());
        assertEquals("Calle Falsa 123", dto.getAddress());
        assertEquals("Springfield", dto.getCity());
        assertEquals("Central", dto.getDepartment());
        assertEquals("3", dto.getEstrato());
        assertEquals(150.5, dto.getBuiltArea());
    }

    @Test
    @DisplayName("Debe permitir establecer y obtener valores usando getters y setters")
    void shouldAllowGettersAndSetters() {
        AppraisalDetailsDTO dto = new AppraisalDetailsDTO();

        dto.setPropertyType("Apartamento");
        dto.setAddress("Avenida Siempre Viva 742");
        dto.setCity("Capital City");
        dto.setDepartment("Norte");
        dto.setEstrato("4");
        dto.setBuiltArea(80.0);

        assertEquals("Apartamento", dto.getPropertyType());
        assertEquals("Avenida Siempre Viva 742", dto.getAddress());
        assertEquals("Capital City", dto.getCity());
        assertEquals("Norte", dto.getDepartment());
        assertEquals("4", dto.getEstrato());
        assertEquals(80.0, dto.getBuiltArea());
    }

    @Test
    @DisplayName("Debe serializar AppraisalDetailsDTO a JSON correctamente")
    void shouldSerializeToJsonCorrectly() throws Exception {
        AppraisalDetailsDTO dto = new AppraisalDetailsDTO(
                "Casa", "Calle Falsa 123", "Springfield", "Central", "3", 150.5);

        String json = objectMapper.writeValueAsString(dto);

        // Usamos contains para ser más flexibles con el orden de las propiedades si
        // Jackson lo cambia
        assertTrue(json.contains("\"propertyType\":\"Casa\""));
        assertTrue(json.contains("\"address\":\"Calle Falsa 123\""));
        assertTrue(json.contains("\"city\":\"Springfield\""));
        assertTrue(json.contains("\"department\":\"Central\""));
        assertTrue(json.contains("\"estrato\":\"3\""));
        assertTrue(json.contains("\"builtArea\":150.5"));
    }

    @Test
    @DisplayName("Debe deserializar JSON a AppraisalDetailsDTO correctamente")
    void shouldDeserializeFromJsonCorrectly() throws Exception {
        String json = "{\"propertyType\":\"Apartamento\",\"address\":\"Avenida Siempre Viva 742\",\"city\":\"Capital City\",\"department\":\"Norte\",\"estrato\":\"4\",\"builtArea\":80.0}";

        AppraisalDetailsDTO dto = objectMapper.readValue(json, AppraisalDetailsDTO.class);

        assertNotNull(dto);
        assertEquals("Apartamento", dto.getPropertyType());
        assertEquals("Avenida Siempre Viva 742", dto.getAddress());
        assertEquals("Capital City", dto.getCity());
        assertEquals("Norte", dto.getDepartment());
        assertEquals("4", dto.getEstrato());
        assertEquals(80.0, dto.getBuiltArea());
    }

    @Test
    @DisplayName("Debe manejar valores nulos durante la serialización y deserialización")
    void shouldHandleNullValues() throws Exception {
        AppraisalDetailsDTO dto = new AppraisalDetailsDTO(
                null, null, "Ciudad Nula", null, "1", null);

        String json = objectMapper.writeValueAsString(dto);
        assertTrue(json.contains("\"propertyType\":null"));
        assertTrue(json.contains("\"address\":null"));
        assertTrue(json.contains("\"city\":\"Ciudad Nula\""));
        assertTrue(json.contains("\"department\":null"));
        assertTrue(json.contains("\"estrato\":\"1\""));
        assertTrue(json.contains("\"builtArea\":null"));

        AppraisalDetailsDTO deserializedDto = objectMapper.readValue(json, AppraisalDetailsDTO.class);
        assertNull(deserializedDto.getPropertyType());
        assertNull(deserializedDto.getAddress());
        assertEquals("Ciudad Nula", deserializedDto.getCity());
        assertNull(deserializedDto.getDepartment());
        assertEquals("1", deserializedDto.getEstrato());
        assertNull(deserializedDto.getBuiltArea());
    }

    @Test
    @DisplayName("Debe tener un comportamiento correcto para equals y hashCode")
    void shouldHaveCorrectEqualsAndHashCode() {
        AppraisalDetailsDTO dto1 = new AppraisalDetailsDTO(
                "Casa", "Calle Falsa 123", "Springfield", "Central", "3", 150.5);
        AppraisalDetailsDTO dto2 = new AppraisalDetailsDTO(
                "Casa", "Calle Falsa 123", "Springfield", "Central", "3", 150.5);
        AppraisalDetailsDTO dto3 = new AppraisalDetailsDTO(
                "Apartamento", "Calle Falsa 123", "Springfield", "Central", "3", 150.5);

        // Test equals
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1, null);
        assertNotEquals(dto1, new Object());

        // Test hashCode
        assertEquals(dto1.hashCode(), dto2.hashCode());
        // No se puede asegurar que los hash codes sean diferentes para objetos
        // diferentes,
        // pero es una buena práctica verificar que sean iguales para objetos iguales.
    }
}
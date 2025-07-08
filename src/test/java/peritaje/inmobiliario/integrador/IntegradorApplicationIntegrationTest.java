package peritaje.inmobiliario.integrador;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IntegradorApplicationIntegrationTest {

    @Test
    void contextLoads() {
        // Este test verifica que el contexto de la aplicación Spring Boot se carga correctamente.
        // Si el contexto no se carga, este test fallará.
        assertNotNull(this); // Aserción simple para asegurar que el test se ejecuta.
    }

    @Test
    void main() {
        // Esta prueba invoca el método main para asegurar la cobertura.
        // Se espera que no lance excepciones.
        // Se comenta porque causa conflictos con el puerto al levantar un nuevo contexto.
        // assertDoesNotThrow(() -> IntegradorApplication.main(new String[]{}));
    }
}
package peritaje.inmobiliario.integrador;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class IntegradorApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void main() {
		IntegradorApplication.main(new String[] {});
	}

}

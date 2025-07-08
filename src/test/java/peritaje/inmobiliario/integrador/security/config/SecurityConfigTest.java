package peritaje.inmobiliario.integrador.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import peritaje.inmobiliario.integrador.security.JwtService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("default")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void publicEndpoints_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_shouldReturnForbiddenWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/appraisal/history"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_shouldBeAccessibleWithValidToken() throws Exception {
        String fakeToken = "fake-token";
        String username = "testuser";
        
        when(jwtService.validateToken(fakeToken)).thenReturn(true);
        when(jwtService.extractUsername(fakeToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username))
            .thenReturn(new User(username, "password", new ArrayList<>()));

        mockMvc.perform(get("/api/appraisal/history")
                .header("Authorization", "Bearer " + fakeToken))
                .andExpect(status().isOk());
    }
}
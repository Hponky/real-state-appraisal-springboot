package peritaje.inmobiliario.integrador.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SupabaseClientConfig {

    @Bean
    public WebClient supabaseWebClient(@Value("${supabase.url}") String supabaseUrl,
                                     @Value("${supabase.service-key}") String supabaseServiceKey) {
        return WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", supabaseServiceKey)
                .defaultHeader("Authorization", "Bearer " + supabaseServiceKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
package peritaje.inmobiliario.integrador.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoClaveDTO {
    @JsonAlias("nombre")
    private String documento;
}
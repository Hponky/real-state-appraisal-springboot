package peritaje.inmobiliario.integrador.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    private String message;
    @JsonProperty("msg")
    private String msg;
    private String error;
    private Integer statusCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
}
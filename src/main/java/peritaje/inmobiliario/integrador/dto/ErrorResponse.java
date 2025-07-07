package peritaje.inmobiliario.integrador.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    private String message;
    @JsonProperty("msg")
    private String msg;
    private String error;
    private Integer statusCode;

    public ErrorResponse() {
        // Constructor por defecto
    }

    public ErrorResponse(String message, String error) {
        this.message = message;
        this.error = error;
    }

    public ErrorResponse(String message, String msg, String error, Integer statusCode) {
        this.message = message;
        this.msg = msg;
        this.error = error;
        this.statusCode = statusCode;
    }

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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorResponse that = (ErrorResponse) o;
        return java.util.Objects.equals(message, that.message) &&
                java.util.Objects.equals(msg, that.msg) &&
                java.util.Objects.equals(error, that.error) &&
                java.util.Objects.equals(statusCode, that.statusCode);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(message, msg, error, statusCode);
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "message='" + message + '\'' +
                ", msg='" + msg + '\'' +
                ", error='" + error + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }
}
package ru.yandex.practicum.commerce.interactionapi.common.dto;

import java.util.List;

public class ApiErrorDto {

    private Object cause;
    private List<Object> stackTrace;
    private String httpStatus;
    private String userMessage;
    private String message;
    private List<Object> suppressed;
    private String localizedMessage;

    public ApiErrorDto() {
    }

    public Object getCause() {
        return cause;
    }

    public void setCause(Object cause) {
        this.cause = cause;
    }

    public List<Object> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<Object> stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Object> getSuppressed() {
        return suppressed;
    }

    public void setSuppressed(List<Object> suppressed) {
        this.suppressed = suppressed;
    }

    public String getLocalizedMessage() {
        return localizedMessage;
    }

    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
    }
}
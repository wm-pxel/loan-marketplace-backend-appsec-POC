package com.westmonroe.loansyndication.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorInfo {

    private String title;
    private Integer status;
    private String timestamp;
    private String message;

    private List<ErrorDetail> errors = new ArrayList<>();

    public ErrorInfo() {}

    public ErrorInfo(String title, Integer status, String timestamp, String message) {
        this.title = title;
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
    }

    public ErrorInfo(String title, Integer status, String timestamp, String message, List<ErrorDetail> errors) {
        this.title = title;
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
        this.errors = errors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorDetail> errors) {
        this.errors = errors;
    }

}
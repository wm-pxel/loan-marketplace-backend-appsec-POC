package com.westmonroe.loansyndication.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorDetail {

    private String fieldName;
    private Object rejectedValue;
    private String message;

    public ErrorDetail() {}

    public ErrorDetail(String fieldName, Object rejectedValue, String message) {
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
        this.message = message;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
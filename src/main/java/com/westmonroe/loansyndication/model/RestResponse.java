package com.westmonroe.loansyndication.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(name = "Response", description = "Model for a Lamina RestResponse.")
public class RestResponse {

    private String title;
    private Integer status;
    private String timestamp;
    private String message;

    public RestResponse() {}

    public RestResponse(String title, Integer status, String timestamp, String message) {
        this.title = title;
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
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

}
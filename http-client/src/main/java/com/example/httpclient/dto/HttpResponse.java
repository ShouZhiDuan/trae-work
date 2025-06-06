package com.example.httpclient.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP响应数据传输对象
 */
public class HttpResponse<T> {

    private int statusCode;

    private String statusMessage;

    private Map<String, String> headers = new HashMap<>();

    private T body;

    private String rawBody;

    private long responseTime; // 响应时间（毫秒）

    private LocalDateTime timestamp;

    private boolean success;

    private String errorMessage;

    private String contentType;

    private long contentLength;

    // 构造函数
    public HttpResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public HttpResponse(int statusCode, T body) {
        this();
        this.statusCode = statusCode;
        this.body = body;
        this.success = statusCode >= 200 && statusCode < 300;
    }

    // 便捷方法
    public boolean isSuccessful() {
        return success;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }

    public HttpResponse<T> addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    // 静态工厂方法
    public static <T> HttpResponse<T> success(T body) {
        HttpResponse<T> response = new HttpResponse<>(200, body);
        response.setStatusMessage("OK");
        return response;
    }

    public static <T> HttpResponse<T> error(int statusCode, String errorMessage) {
        HttpResponse<T> response = new HttpResponse<>();
        response.setStatusCode(statusCode);
        response.setErrorMessage(errorMessage);
        response.setSuccess(false);
        return response;
    }

    // Getters and Setters
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        this.success = statusCode >= 200 && statusCode < 300;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public String getRawBody() {
        return rawBody;
    }

    public void setRawBody(String rawBody) {
        this.rawBody = rawBody;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", success=" + success +
                ", responseTime=" + responseTime +
                ", timestamp=" + timestamp +
                ", contentType='" + contentType + '\'' +
                ", contentLength=" + contentLength +
                (errorMessage != null ? ", errorMessage='" + errorMessage + '\'' : "") +
                '}';
    }
}
package com.example.httpclient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求数据传输对象
 */
public class HttpRequest {

    @NotBlank(message = "URL不能为空")
    private String url;

    @NotNull(message = "HTTP方法不能为空")
    private HttpMethod method = HttpMethod.GET;

    private Map<String, String> headers = new HashMap<>();

    private Map<String, Object> queryParams = new HashMap<>();

    private Object body;

    private String contentType = "application/json";

    private int timeout = 30; // 超时时间（秒）

    private boolean followRedirects = true;

    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
    }

    // 构造函数
    public HttpRequest() {}

    public HttpRequest(String url, HttpMethod method) {
        this.url = url;
        this.method = method;
    }

    // 便捷方法
    public HttpRequest addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpRequest addQueryParam(String key, Object value) {
        this.queryParams.put(key, value);
        return this;
    }

    public HttpRequest setJsonBody(Object body) {
        this.body = body;
        this.contentType = "application/json";
        return this;
    }

    public HttpRequest setFormBody(Map<String, Object> formData) {
        this.body = formData;
        this.contentType = "application/x-www-form-urlencoded";
        return this;
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "url='" + url + '\'' +
                ", method=" + method +
                ", headers=" + headers +
                ", queryParams=" + queryParams +
                ", contentType='" + contentType + '\'' +
                ", timeout=" + timeout +
                ", followRedirects=" + followRedirects +
                '}';
    }
}
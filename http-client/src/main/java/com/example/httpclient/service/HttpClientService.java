package com.example.httpclient.service;

import com.example.httpclient.dto.HttpRequest;
import com.example.httpclient.dto.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * HTTP客户端服务类
 * 提供高性能的HTTP请求功能
 */
@Service
public class HttpClientService {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientService.class);

    private final OkHttpClient okHttpClient;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public HttpClientService(OkHttpClient okHttpClient, WebClient webClient) {
        this.okHttpClient = okHttpClient;
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 执行同步HTTP请求
     */
    public <T> HttpResponse<T> execute(HttpRequest request, Class<T> responseType) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 构建OkHttp请求
            Request okHttpRequest = buildOkHttpRequest(request);
            
            // 创建自定义超时的客户端
            OkHttpClient clientWithTimeout = okHttpClient.newBuilder()
                .connectTimeout(request.getTimeout(), TimeUnit.SECONDS)
                .readTimeout(request.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(request.getTimeout(), TimeUnit.SECONDS)
                .build();

            // 执行请求
            try (Response response = clientWithTimeout.newCall(okHttpRequest).execute()) {
                return buildHttpResponse(response, responseType, startTime);
            }

        } catch (Exception e) {
            logger.error("HTTP请求执行失败: {}", e.getMessage(), e);
            HttpResponse<T> errorResponse = new HttpResponse<>();
            errorResponse.setStatusCode(500);
            errorResponse.setErrorMessage(e.getMessage());
            errorResponse.setSuccess(false);
            errorResponse.setResponseTime(System.currentTimeMillis() - startTime);
            return errorResponse;
        }
    }

    /**
     * 执行异步HTTP请求
     */
    public <T> CompletableFuture<HttpResponse<T>> executeAsync(HttpRequest request, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> execute(request, responseType));
    }

    /**
     * 执行响应式HTTP请求（使用WebClient）
     */
    public <T> Mono<HttpResponse<T>> executeReactive(HttpRequest request, Class<T> responseType) {
        long startTime = System.currentTimeMillis();
        
        try {
            WebClient.RequestHeadersSpec<?> spec = buildWebClientRequest(request);
            
            return spec.retrieve()
                .toEntity(responseType)
                .map(entity -> {
                    HttpResponse<T> httpResponse = new HttpResponse<>();
                    httpResponse.setStatusCode(entity.getStatusCode().value());
                    httpResponse.setBody(entity.getBody());
                    httpResponse.setSuccess(entity.getStatusCode().is2xxSuccessful());
                    httpResponse.setResponseTime(System.currentTimeMillis() - startTime);
                    
                    // 设置响应头
                    entity.getHeaders().forEach((key, values) -> {
                        if (!values.isEmpty()) {
                            httpResponse.addHeader(key, values.get(0));
                        }
                    });
                    
                    return httpResponse;
                })
                .onErrorResume(throwable -> {
                    logger.error("响应式HTTP请求执行失败: {}", throwable.getMessage(), throwable);
                    HttpResponse<T> errorResponse = new HttpResponse<>();
                    errorResponse.setStatusCode(500);
                    errorResponse.setErrorMessage(throwable.getMessage());
                    errorResponse.setSuccess(false);
                    errorResponse.setResponseTime(System.currentTimeMillis() - startTime);
                    return Mono.just(errorResponse);
                });
                
        } catch (Exception e) {
            logger.error("响应式HTTP请求构建失败: {}", e.getMessage(), e);
            return Mono.just(HttpResponse.error(500, e.getMessage()));
        }
    }

    /**
     * GET请求便捷方法
     */
    public <T> HttpResponse<T> get(String url, Class<T> responseType) {
        HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.GET);
        return execute(request, responseType);
    }

    /**
     * POST请求便捷方法
     */
    public <T> HttpResponse<T> post(String url, Object body, Class<T> responseType) {
        HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.POST)
            .setJsonBody(body);
        return execute(request, responseType);
    }

    /**
     * PUT请求便捷方法
     */
    public <T> HttpResponse<T> put(String url, Object body, Class<T> responseType) {
        HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.PUT)
            .setJsonBody(body);
        return execute(request, responseType);
    }

    /**
     * DELETE请求便捷方法
     */
    public <T> HttpResponse<T> delete(String url, Class<T> responseType) {
        HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.DELETE);
        return execute(request, responseType);
    }

    /**
     * 构建OkHttp请求
     */
    private Request buildOkHttpRequest(HttpRequest request) throws JsonProcessingException {
        // 构建URL（包含查询参数）
        String url = buildUrlWithQueryParams(request.getUrl(), request.getQueryParams());
        
        Request.Builder builder = new Request.Builder().url(url);
        
        // 添加请求头
        request.getHeaders().forEach(builder::addHeader);
        
        // 设置Content-Type
        if (StringUtils.hasText(request.getContentType())) {
            builder.addHeader("Content-Type", request.getContentType());
        }
        
        // 构建请求体
        RequestBody requestBody = buildRequestBody(request);
        
        // 设置HTTP方法
        switch (request.getMethod()) {
            case GET:
                builder.get();
                break;
            case POST:
                builder.post(requestBody != null ? requestBody : RequestBody.create("", null));
                break;
            case PUT:
                builder.put(requestBody != null ? requestBody : RequestBody.create("", null));
                break;
            case DELETE:
                if (requestBody != null) {
                    builder.delete(requestBody);
                } else {
                    builder.delete();
                }
                break;
            case PATCH:
                builder.patch(requestBody != null ? requestBody : RequestBody.create("", null));
                break;
            case HEAD:
                builder.head();
                break;
            case OPTIONS:
                builder.method("OPTIONS", requestBody);
                break;
        }
        
        return builder.build();
    }

    /**
     * 构建WebClient请求
     */
    private WebClient.RequestHeadersSpec<?> buildWebClientRequest(HttpRequest request) {
        String url = buildUrlWithQueryParams(request.getUrl(), request.getQueryParams());
        
        WebClient.RequestBodySpec requestSpec = webClient
            .method(org.springframework.http.HttpMethod.valueOf(request.getMethod().name()))
            .uri(url);
        
        // 添加请求头
        request.getHeaders().forEach(requestSpec::header);
        
        // 设置请求体
        if (request.getBody() != null) {
            return requestSpec.bodyValue(request.getBody());
        } else {
            return requestSpec;
        }
    }

    /**
     * 构建请求体
     */
    private RequestBody buildRequestBody(HttpRequest request) throws JsonProcessingException {
        if (request.getBody() == null) {
            return null;
        }
        
        MediaType mediaType = MediaType.parse(request.getContentType());
        
        if (request.getContentType().contains("application/json")) {
            String json = objectMapper.writeValueAsString(request.getBody());
            return RequestBody.create(json, mediaType);
        } else if (request.getContentType().contains("application/x-www-form-urlencoded")) {
            FormBody.Builder formBuilder = new FormBody.Builder();
            if (request.getBody() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> formData = (Map<String, Object>) request.getBody();
                formData.forEach((key, value) -> formBuilder.add(key, String.valueOf(value)));
            }
            return formBuilder.build();
        } else {
            // 其他类型，转换为字符串
            String content = request.getBody() instanceof String ? 
                (String) request.getBody() : 
                objectMapper.writeValueAsString(request.getBody());
            return RequestBody.create(content, mediaType);
        }
    }

    /**
     * 构建包含查询参数的URL
     */
    private String buildUrlWithQueryParams(String baseUrl, Map<String, Object> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return baseUrl;
        }
        
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        boolean hasQuery = baseUrl.contains("?");
        
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            urlBuilder.append(hasQuery ? "&" : "?");
            urlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                     .append("=")
                     .append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
            hasQuery = true;
        }
        
        return urlBuilder.toString();
    }

    /**
     * 构建HTTP响应对象
     */
    private <T> HttpResponse<T> buildHttpResponse(Response response, Class<T> responseType, long startTime) throws IOException {
        HttpResponse<T> httpResponse = new HttpResponse<>();
        httpResponse.setStatusCode(response.code());
        httpResponse.setStatusMessage(response.message());
        httpResponse.setSuccess(response.isSuccessful());
        httpResponse.setResponseTime(System.currentTimeMillis() - startTime);
        
        // 设置响应头
        response.headers().forEach(pair -> httpResponse.addHeader(pair.getFirst(), pair.getSecond()));
        
        // 设置内容类型和长度
        String contentType = response.header("Content-Type");
        if (contentType != null) {
            httpResponse.setContentType(contentType);
        }
        
        String contentLength = response.header("Content-Length");
        if (contentLength != null) {
            try {
                httpResponse.setContentLength(Long.parseLong(contentLength));
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        // 处理响应体
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String rawBody = responseBody.string();
            httpResponse.setRawBody(rawBody);
            
            if (StringUtils.hasText(rawBody)) {
                try {
                    if (responseType == String.class) {
                        @SuppressWarnings("unchecked")
                        T body = (T) rawBody;
                        httpResponse.setBody(body);
                    } else {
                        T body = objectMapper.readValue(rawBody, responseType);
                        httpResponse.setBody(body);
                    }
                } catch (JsonProcessingException e) {
                    logger.warn("响应体JSON解析失败，返回原始字符串: {}", e.getMessage());
                    if (responseType == String.class) {
                        @SuppressWarnings("unchecked")
                        T body = (T) rawBody;
                        httpResponse.setBody(body);
                    }
                }
            }
        }
        
        return httpResponse;
    }
}
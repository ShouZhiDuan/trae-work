package com.example.httpclient.service;

import com.example.httpclient.dto.HttpRequest;
import com.example.httpclient.dto.HttpResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP客户端服务测试类
 */
@SpringBootTest
@TestPropertySource(properties = {
    "http.client.connect-timeout=10",
    "http.client.read-timeout=10"
})
class HttpClientServiceTest {

    @Autowired
    private HttpClientService httpClientService;

    private WireMockServer wireMockServer;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        // 启动WireMock服务器
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
        baseUrl = "http://localhost:8089";
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testGetRequest() {
        // 模拟GET请求
        stubFor(get(urlEqualTo("/test"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Hello World\"}")));

        // 执行GET请求
        HttpRequest request = new HttpRequest(baseUrl + "/test", HttpRequest.HttpMethod.GET);
        HttpResponse<Object> response = httpClientService.execute(request, Object.class);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getResponseTime() > 0);
    }

    @Test
    void testPostRequest() {
        // 模拟POST请求
        stubFor(post(urlEqualTo("/test"))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 1, \"status\": \"created\"}")));

        // 准备请求数据
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "Test");
        requestBody.put("value", "123");

        // 执行POST请求
        HttpRequest request = new HttpRequest(baseUrl + "/test", HttpRequest.HttpMethod.POST)
            .setJsonBody(requestBody);
        HttpResponse<Object> response = httpClientService.execute(request, Object.class);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testPutRequest() {
        // 模拟PUT请求
        stubFor(put(urlEqualTo("/test/1"))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\": 1, \"status\": \"updated\"}")));

        // 准备请求数据
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "Updated Test");
        requestBody.put("value", "456");

        // 执行PUT请求
        HttpRequest request = new HttpRequest(baseUrl + "/test/1", HttpRequest.HttpMethod.PUT)
            .setJsonBody(requestBody);
        HttpResponse<Object> response = httpClientService.execute(request, Object.class);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteRequest() {
        // 模拟DELETE请求
        stubFor(delete(urlEqualTo("/test/1"))
            .willReturn(aResponse()
                .withStatus(204)));

        // 执行DELETE请求
        HttpRequest request = new HttpRequest(baseUrl + "/test/1", HttpRequest.HttpMethod.DELETE);
        HttpResponse<Object> response = httpClientService.execute(request, Object.class);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(204, response.getStatusCode());
    }

    @Test
    void testRequestWithHeaders() {
        // 模拟带请求头的请求
        stubFor(get(urlEqualTo("/test-headers"))
            .withHeader("Authorization", equalTo("Bearer token123"))
            .withHeader("X-Custom-Header", equalTo("custom-value"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Headers received\"}")));

        // 执行带请求头的请求
        HttpRequest request = new HttpRequest(baseUrl + "/test-headers", HttpRequest.HttpMethod.GET)
            .addHeader("Authorization", "Bearer token123")
            .addHeader("X-Custom-Header", "custom-value");
        HttpResponse<Object> response = httpClientService.execute(request, Object.class);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testRequestWithQueryParams() {
        // 模拟带查询参数的请求
        stubFor(get(urlPathEqualTo("/test-query"))
            .withQueryParam("page", equalTo("1"))
            .withQueryParam("size", equalTo("10"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"page\": 1, \"size\": 10}")));

        // 执行带查询参数的请求
        HttpRequest request = new HttpRequest(baseUrl + "/test-query", HttpRequest.HttpMethod.GET)
            .addQueryParam("page", 1)
            .addQueryParam("size", 10);
        HttpResponse<Object> response = httpClientService.execute(request, Object.class);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testAsyncRequest() throws Exception {
        // 模拟异步请求
        stubFor(get(urlEqualTo("/test-async"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\": \"Async response\"}")
                .withFixedDelay(100))); // 模拟延迟

        // 执行异步请求
        HttpRequest request = new HttpRequest(baseUrl + "/test-async", HttpRequest.HttpMethod.GET);
        CompletableFuture<HttpResponse<Object>> futureResponse = 
            httpClientService.executeAsync(request, Object.class);

        // 验证结果
        HttpResponse<Object> response = futureResponse.get();
        assertNotNull(response);
        assertTrue(response.isSuccessful());
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testErrorResponse() {
        // 模拟错误响应
        stubFor(get(urlEqualTo("/test-error"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Not Found\"}")));

        // 执行请求
        HttpRequest request = new HttpRequest(baseUrl + "/test-error", HttpRequest.HttpMethod.GET);
        HttpResponse<Object> response = httpClientService.execute(request, Object.class);

        // 验证结果
        assertNotNull(response);
        assertFalse(response.isSuccessful());
        assertEquals(404, response.getStatusCode());
        assertTrue(response.isClientError());
    }

    @Test
    void testTimeout() {
        // 模拟超时
        stubFor(get(urlEqualTo("/test-timeout"))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(15000))); // 15秒延迟，超过默认超时时间

        // 执行请求（设置较短的超时时间）
        HttpRequest request = new HttpRequest(baseUrl + "/test-timeout", HttpRequest.HttpMethod.GET);
        request.setTimeout(2); // 2秒超时
        
        HttpResponse<Object> response = httpClientService.execute(request, Object.class);

        // 验证结果（应该超时失败）
        assertNotNull(response);
        assertFalse(response.isSuccessful());
        assertEquals(500, response.getStatusCode());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    void testConvenienceMethods() {
        // 测试便捷方法
        
        // GET
        stubFor(get(urlEqualTo("/convenience/get"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("GET response")));
        
        HttpResponse<String> getResponse = httpClientService.get(baseUrl + "/convenience/get", String.class);
        assertTrue(getResponse.isSuccessful());
        
        // POST
        stubFor(post(urlEqualTo("/convenience/post"))
            .willReturn(aResponse()
                .withStatus(201)
                .withBody("POST response")));
        
        HttpResponse<String> postResponse = httpClientService.post(
            baseUrl + "/convenience/post", 
            Map.of("key", "value"), 
            String.class
        );
        assertTrue(postResponse.isSuccessful());
        
        // PUT
        stubFor(put(urlEqualTo("/convenience/put"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("PUT response")));
        
        HttpResponse<String> putResponse = httpClientService.put(
            baseUrl + "/convenience/put", 
            Map.of("key", "updated"), 
            String.class
        );
        assertTrue(putResponse.isSuccessful());
        
        // DELETE
        stubFor(delete(urlEqualTo("/convenience/delete"))
            .willReturn(aResponse()
                .withStatus(204)));
        
        HttpResponse<String> deleteResponse = httpClientService.delete(
            baseUrl + "/convenience/delete", 
            String.class
        );
        assertTrue(deleteResponse.isSuccessful());
    }
}
package com.example.httpclient.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * HTTP客户端配置类
 * 配置高性能的HTTP客户端连接池和超时参数
 */
@Configuration
@ConfigurationProperties(prefix = "http.client")
public class HttpClientConfig {

    // 连接超时时间（秒）
    private int connectTimeout = 30;
    
    // 读取超时时间（秒）
    private int readTimeout = 60;
    
    // 写入超时时间（秒）
    private int writeTimeout = 60;
    
    // 连接池最大空闲连接数
    private int maxIdleConnections = 50;
    
    // 连接保持活跃时间（分钟）
    private int keepAliveDuration = 5;
    
    // 最大请求数
    private int maxRequests = 200;
    
    // 每个主机最大请求数
    private int maxRequestsPerHost = 20;
    
    // 是否跳过SSL证书验证（仅用于开发和测试环境）
    private boolean skipSslVerification = false;
    
    // SSL协议版本
    private String sslProtocol = "TLS";
    
    // 是否启用主机名验证
    private boolean enableHostnameVerification = true;

    /**
     * 配置OkHttpClient
     * 高性能HTTP客户端，支持连接池和HTTP/2
     */
    @Bean
    public OkHttpClient okHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(
            maxIdleConnections, 
            keepAliveDuration, 
            TimeUnit.MINUTES
        );

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .connectionPool(connectionPool)
            .retryOnConnectionFailure(true);
            
        // 配置SSL验证
        if (skipSslVerification) {
            configureSslBypass(builder);
        }
        
        return builder.build();
    }

    /**
     * 配置RestTemplate
     * 基于OkHttp的同步HTTP客户端
     */
    @Bean
    public RestTemplate restTemplate(OkHttpClient okHttpClient) {
        OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
        return new RestTemplate(factory);
    }

    /**
     * 配置WebClient
     * 响应式HTTP客户端，支持异步和流式处理
     */
    @Bean
    public WebClient webClient() {
        WebClient.Builder builder = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)); // 10MB
            
        // 配置SSL验证
        if (skipSslVerification) {
            HttpClient httpClient = HttpClient.create()
                .secure(sslSpec -> {
                    try {
                        sslSpec.sslContext(io.netty.handler.ssl.SslContextBuilder
                            .forClient()
                            .trustManager(io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE)
                            .build())
                            .handshakeTimeout(Duration.ofSeconds(30))
                            .closeNotifyFlushTimeout(Duration.ofSeconds(10));
                    } catch (Exception e) {
                        throw new RuntimeException("配置WebClient SSL失败", e);
                    }
                });
            builder.clientConnector(new ReactorClientHttpConnector(httpClient));
        }
        
        return builder.build();
    }

    // Getters and Setters
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public void setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
    }

    public int getKeepAliveDuration() {
        return keepAliveDuration;
    }

    public void setKeepAliveDuration(int keepAliveDuration) {
        this.keepAliveDuration = keepAliveDuration;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }

    public void setMaxRequestsPerHost(int maxRequestsPerHost) {
        this.maxRequestsPerHost = maxRequestsPerHost;
    }
    
    public boolean isSkipSslVerification() {
        return skipSslVerification;
    }
    
    public void setSkipSslVerification(boolean skipSslVerification) {
        this.skipSslVerification = skipSslVerification;
    }
    
    public String getSslProtocol() {
        return sslProtocol;
    }
    
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }
    
    public boolean isEnableHostnameVerification() {
        return enableHostnameVerification;
    }
    
    public void setEnableHostnameVerification(boolean enableHostnameVerification) {
        this.enableHostnameVerification = enableHostnameVerification;
    }
    
    /**
     * 配置OkHttp客户端跳过SSL验证
     * 警告：仅用于开发和测试环境，生产环境请谨慎使用
     */
    private void configureSslBypass(OkHttpClient.Builder builder) {
        try {
            // 创建信任所有证书的TrustManager
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        // 不进行客户端证书验证
                    }
                    
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // 不进行服务器证书验证
                    }
                    
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };
            
            // 创建SSLContext
            SSLContext sslContext = SSLContext.getInstance(sslProtocol);
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // 配置SSL套接字工厂
            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            
            // 配置主机名验证
            if (!enableHostnameVerification) {
                builder.hostnameVerifier((hostname, session) -> true);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("配置SSL绕过失败", e);
        }
    }
    
    /**
     * 为WebClient创建不安全的SSL上下文
     * 警告：仅用于开发和测试环境，生产环境请谨慎使用
     */
    private SSLContext createUnsafeSslContext() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        // 不进行客户端证书验证
                    }
                    
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // 不进行服务器证书验证
                    }
                    
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };
            
            SSLContext sslContext = SSLContext.getInstance(sslProtocol);
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("创建不安全SSL上下文失败", e);
        }
    }
}
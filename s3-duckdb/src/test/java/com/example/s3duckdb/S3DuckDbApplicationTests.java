package com.example.s3duckdb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "seaweedfs.endpoint=http://localhost:8333",
    "seaweedfs.access-key=test-key",
    "seaweedfs.secret-key=test-secret",
    "seaweedfs.bucket=test-bucket"
})
class S3DuckDbApplicationTests {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否能正常加载
    }
}
package com.example.aq2.controller;

import com.example.aq2.dto.*;
import com.example.aq2.service.QueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 查询控制器测试类
 */
@WebMvcTest(QueryController.class)
class QueryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private QueryService queryService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data").value("服务运行正常"));
    }
    
    @Test
    void testInfoEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("AQ2.0 Post Task API"));
    }
    
    @Test
    void testProcessQuerySuccess() throws Exception {
        // 准备测试数据
        QueryRequest request = createTestQueryRequest();
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("status", "success");
        mockResult.put("processTime", System.currentTimeMillis());
        
        // Mock服务方法
        when(queryService.processQuery(any(QueryRequest.class))).thenReturn(mockResult);
        
        // 执行测试
        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("success"));
    }
    
    @Test
    void testProcessQueryValidationError() throws Exception {
        // 准备无效的测试数据（缺少必填字段）
        QueryRequest request = new QueryRequest();
        
        // 执行测试
        mockMvc.perform(post("/api/v1/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
    
    /**
     * 创建测试用的查询请求
     */
    private QueryRequest createTestQueryRequest() {
        QueryRequest request = new QueryRequest();
        
        // 数据库配置
        JdbcConf jdbcConf = new JdbcConf();
        jdbcConf.setHost("localhost");
        jdbcConf.setPort(3306);
        jdbcConf.setUser("testuser");
        jdbcConf.setPass("testpass");
        jdbcConf.setDbName("testdb");
        request.setJdbcConf(jdbcConf);
        
        // FTP配置
        FtpConf ftpConf = new FtpConf();
        ftpConf.setHost("ftp.test.com");
        ftpConf.setPort(21);
        ftpConf.setUser("ftpuser");
        ftpConf.setPass("ftppass");
        request.setFtpConf(ftpConf);
        
        // 查询文件路径和ID名称
        request.setQueryFilePath("/test/path/query.csv");
        request.setQueryIdName("testIdName");
        
        // 查询信息
        QueryInfo queryInfo = new QueryInfo();
        
        // 公安场景
        GaInfo gaInfo = new GaInfo();
        GaTask gaTask = new GaTask();
        gaTask.setId(123L);
        gaTask.setName("测试任务");
        gaTask.setModelName("测试模型");
        gaTask.setUser("测试用户");
        gaTask.setNodeName("测试机构");
        gaInfo.setTask(gaTask);
        
        RunListItem gaRunItem = new RunListItem();
        gaRunItem.setTbName("test_table");
        gaRunItem.setIdName("test_id");
        gaRunItem.setPublicKey("test_public_key");
        gaRunItem.setResponseFilePath("/test/response/path.csv");
        gaInfo.setRunList(Arrays.asList(gaRunItem));
        
        queryInfo.setGa(gaInfo);
        
        // 医院场景
        YyInfo yyInfo = new YyInfo();
        RunListItem yyRunItem = new RunListItem();
        yyRunItem.setTbName("hospital_table");
        yyRunItem.setIdName("hospital_id");
        yyRunItem.setPublicKey("hospital_public_key");
        yyRunItem.setResponseFilePath("/hospital/response/path.csv");
        yyInfo.setRunList(Arrays.asList(yyRunItem));
        
        queryInfo.setYy(yyInfo);
        
        request.setQueryInfo(queryInfo);
        
        return request;
    }
}
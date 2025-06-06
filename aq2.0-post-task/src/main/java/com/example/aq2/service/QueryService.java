package com.example.aq2.service;

import com.example.aq2.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 查询处理服务
 */
@Slf4j
@Service
public class QueryService {
    
    /**
     * 处理查询请求
     */
    public Map<String, Object> processQuery(QueryRequest request) {
        log.info("开始处理查询请求，查询文件路径: {}", request.getQueryFilePath());
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 验证数据库连接
            validateDatabaseConnection(request.getJdbcConf());
            log.info("数据库连接验证成功");
            
            // 2. 验证FTP连接
            validateFtpConnection(request.getFtpConf());
            log.info("FTP连接验证成功");
            
            // 3. 处理查询信息
            processQueryInfo(request.getQueryInfo(), result);
            
            // 4. 记录处理结果
            result.put("queryFilePath", request.getQueryFilePath());
            result.put("queryIdName", request.getQueryIdName());
            result.put("processTime", System.currentTimeMillis());
            result.put("status", "success");
            
            log.info("查询请求处理完成");
            
        } catch (Exception e) {
            log.error("处理查询请求时发生错误: {}", e.getMessage(), e);
            result.put("status", "error");
            result.put("errorMessage", e.getMessage());
            throw new RuntimeException("查询处理失败: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 验证数据库连接
     */
    private void validateDatabaseConnection(JdbcConf jdbcConf) throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", 
                                 jdbcConf.getHost(), jdbcConf.getPort(), jdbcConf.getDbName());
        
        try (Connection connection = DriverManager.getConnection(url, jdbcConf.getUser(), jdbcConf.getPass())) {
            if (connection == null || !connection.isValid(5)) {
                throw new SQLException("数据库连接无效");
            }
        }
    }
    
    /**
     * 验证FTP连接
     */
    private void validateFtpConnection(FtpConf ftpConf) throws IOException {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ftpConf.getHost(), ftpConf.getPort());
            boolean loginSuccess = ftpClient.login(ftpConf.getUser(), ftpConf.getPass());
            
            if (!loginSuccess) {
                throw new IOException("FTP登录失败");
            }
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        }
    }
    
    /**
     * 处理查询信息
     */
    private void processQueryInfo(QueryInfo queryInfo, Map<String, Object> result) {
        // 处理公安场景
        if (queryInfo.getGa() != null) {
            log.info("处理公安场景查询");
            processGaInfo(queryInfo.getGa(), result);
        }
        
        // 处理医院场景
        if (queryInfo.getYy() != null) {
            log.info("处理医院场景查询");
            processYyInfo(queryInfo.getYy(), result);
        }
    }
    
    /**
     * 处理公安场景信息
     */
    private void processGaInfo(GaInfo gaInfo, Map<String, Object> result) {
        Map<String, Object> gaResult = new HashMap<>();
        
        // 处理任务信息
        GaTask task = gaInfo.getTask();
        Map<String, Object> taskInfo = new HashMap<>();
        taskInfo.put("taskId", task.getId());
        taskInfo.put("taskName", task.getName());
        taskInfo.put("modelName", task.getModelName());
        taskInfo.put("user", task.getUser());
        taskInfo.put("nodeName", task.getNodeName());
        gaResult.put("taskInfo", taskInfo);
        
        // 处理运行列表
        gaResult.put("runListCount", gaInfo.getRunList().size());
        gaResult.put("runList", gaInfo.getRunList());
        
        result.put("gaProcessResult", gaResult);
        log.info("公安场景处理完成，任务ID: {}, 运行列表数量: {}", task.getId(), gaInfo.getRunList().size());
    }
    
    /**
     * 处理医院场景信息
     */
    private void processYyInfo(YyInfo yyInfo, Map<String, Object> result) {
        Map<String, Object> yyResult = new HashMap<>();
        
        // 处理运行列表
        yyResult.put("runListCount", yyInfo.getRunList().size());
        yyResult.put("runList", yyInfo.getRunList());
        
        result.put("yyProcessResult", yyResult);
        log.info("医院场景处理完成，运行列表数量: {}", yyInfo.getRunList().size());
    }
}
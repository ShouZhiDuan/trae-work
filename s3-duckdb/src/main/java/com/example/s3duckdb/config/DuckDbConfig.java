package com.example.s3duckdb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DuckDbConfig {

    @Value("${duckdb.database-path::memory:}")
    private String databasePath;

    @Bean
    public DataSource duckDbDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.duckdb.DuckDBDriver");
        dataSource.setUrl("jdbc:duckdb:" + databasePath);
        return dataSource;
    }

    @Bean
    public JdbcTemplate duckDbJdbcTemplate(DataSource duckDbDataSource) {
        return new JdbcTemplate(duckDbDataSource);
    }
}
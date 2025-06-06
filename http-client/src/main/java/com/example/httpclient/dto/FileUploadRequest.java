package com.example.httpclient.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传请求数据传输对象
 */
public class FileUploadRequest {

    @NotBlank(message = "上传URL不能为空")
    private String url;

    private MultipartFile file;

    private File localFile;

    private byte[] fileBytes;

    private String fileName;

    private String fieldName = "file"; // 表单字段名

    private String contentType;

    private Map<String, String> headers = new HashMap<>();

    private Map<String, Object> formFields = new HashMap<>(); // 额外的表单字段

    private int timeout = 300; // 文件上传超时时间（秒），默认5分钟

    private boolean showProgress = false; // 是否显示上传进度

    // 构造函数
    public FileUploadRequest() {}

    public FileUploadRequest(String url, MultipartFile file) {
        this.url = url;
        this.file = file;
        this.fileName = file.getOriginalFilename();
        this.contentType = file.getContentType();
    }

    public FileUploadRequest(String url, File localFile) {
        this.url = url;
        this.localFile = localFile;
        this.fileName = localFile.getName();
    }

    public FileUploadRequest(String url, byte[] fileBytes, String fileName) {
        this.url = url;
        this.fileBytes = fileBytes;
        this.fileName = fileName;
    }

    // 便捷方法
    public FileUploadRequest addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public FileUploadRequest addFormField(String key, Object value) {
        this.formFields.put(key, value);
        return this;
    }

    public FileUploadRequest withProgress() {
        this.showProgress = true;
        return this;
    }

    // 验证方法
    public boolean hasFile() {
        return file != null || localFile != null || fileBytes != null;
    }

    public long getFileSize() {
        if (file != null) {
            return file.getSize();
        } else if (localFile != null) {
            return localFile.length();
        } else if (fileBytes != null) {
            return fileBytes.length;
        }
        return 0;
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
        if (file != null) {
            this.fileName = file.getOriginalFilename();
            this.contentType = file.getContentType();
        }
    }

    public File getLocalFile() {
        return localFile;
    }

    public void setLocalFile(File localFile) {
        this.localFile = localFile;
        if (localFile != null) {
            this.fileName = localFile.getName();
        }
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getFormFields() {
        return formFields;
    }

    public void setFormFields(Map<String, Object> formFields) {
        this.formFields = formFields;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }

    @Override
    public String toString() {
        return "FileUploadRequest{" +
                "url='" + url + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", fileSize=" + getFileSize() +
                ", timeout=" + timeout +
                ", showProgress=" + showProgress +
                '}';
    }
}
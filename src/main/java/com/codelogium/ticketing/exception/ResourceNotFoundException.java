package com.codelogium.ticketing.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(Long id, Class<?> entityClass) {
        super("资源不存在: " + entityClass.getSimpleName() + " id=" + id);
    }
}

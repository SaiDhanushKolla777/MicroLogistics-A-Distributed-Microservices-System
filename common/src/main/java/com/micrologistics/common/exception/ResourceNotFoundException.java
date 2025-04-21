package com.micrologistics.common.exception;

import lombok.Getter;

/**
 * Exception that is thrown when a requested resource is not found.
 * Used by repository or service layers when entities cannot be located.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;
    
    /**
     * Create a new resource not found exception with the specified message.
     * 
     * @param message The error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }
    
    /**
     * Create a new resource not found exception with details about the resource.
     * 
     * @param resourceName The name of the resource type
     * @param fieldName The name of the field used to look up the resource
     * @param fieldValue The value of the field used to look up the resource
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}

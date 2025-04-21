package com.micrologistics.common.exception;

import lombok.Getter;

/**
 * Exception that represents a business logic error.
 * Used to indicate problems with business rules or constraints.
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    
    /**
     * Create a new business exception with the specified message.
     * 
     * @param message The error message
     */
    public BusinessException(String message) {
        this(message, "BUSINESS_ERROR");
    }
    
    /**
     * Create a new business exception with the specified message and error code.
     * 
     * @param message The error message
     * @param errorCode The error code
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Create a new business exception with the specified message, error code, and cause.
     * 
     * @param message The error message
     * @param errorCode The error code
     * @param cause The cause of the exception
     */
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Common error codes
     */
    public static final String ERROR_ITEM_ALREADY_EXISTS = "ITEM_ALREADY_EXISTS";
    public static final String ERROR_CONTAINER_FULL = "CONTAINER_FULL";
    public static final String ERROR_INVALID_STATUS_TRANSITION = "INVALID_STATUS_TRANSITION";
    public static final String ERROR_CONTAINER_CLOSED = "CONTAINER_CLOSED";
    public static final String ERROR_ROUTE_INVALID = "ROUTE_INVALID";
}

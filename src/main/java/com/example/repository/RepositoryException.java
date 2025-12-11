package com.example.repository;

/**
 * Custom unchecked exception for repository/database errors.
 */
public class RepositoryException extends RuntimeException {

    /**
     * Creates a new RepositoryException with a message and a cause.
     *
     * @param message descriptive error message
     * @param cause the underlying exception that caused this error
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
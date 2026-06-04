package com.klaye.monolith.common.exception;

/**
 * Exception lancée lorsqu'une ressource existe déjà.
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}

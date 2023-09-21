package com.inovus.testtask.xmltojson.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author nafis
 * @since 21.09.2023
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StorageException extends RuntimeException{

    public StorageException(String message) {
        super(message);
    }
}

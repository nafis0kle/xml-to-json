package com.inovus.testtask.xmltojson.service;

import java.nio.file.Path;

/**
 * @author nafis
 * @since 19.09.2023
 */
public interface FileStorageService {
    Path getStoragePath();

    void deleteAll();

    void init();
}

package com.inovus.testtask.xmltojson.service;

import java.nio.file.Path;

/**
 * Класс управляет хранилищем для сохранения файлов
 *
 * @author nafis
 * @since 19.09.2023
 */
public interface FileStorageService {

    /**
     * Относительный путь к директории сохранения файлов
     * @return Path
     */
    Path getStoragePath();

    /**
     * Удаляет директорию хранения файлов со всем содержимым
     */
    void deleteAll();

    /**
     * Создает директорию хранения файлов
     */
    void init();
}

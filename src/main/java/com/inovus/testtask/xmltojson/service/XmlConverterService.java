package com.inovus.testtask.xmltojson.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Класс конвертирует XML-файлы в другие форматы
 *
 * @author nafis
 * @since 19.09.2023
 */
public interface XmlConverterService {

    /**
     * Загружает искомый файл на сервер
     * @param file загружаемый файл
     */
    void upload(MultipartFile file);

    /**
     * Конвертирует XML-файл в JSON-файл
     * @param fileName название XML-файла
     * @return Полученный JSON-файл
     */
    String convertToJson(String fileName);
}

package com.inovus.testtask.xmltojson.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author nafis
 * @since 19.09.2023
 */
public interface XmlConverterService {

    void upload(MultipartFile file);
}

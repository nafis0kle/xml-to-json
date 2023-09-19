package com.inovus.testtask.xmltojson.service.Impl;

import com.inovus.testtask.xmltojson.service.FileStorageService;
import com.inovus.testtask.xmltojson.service.XmlConverterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author nafis
 * @since 19.09.2023
 */

@Service
@RequiredArgsConstructor
public class XmlConverterServiceImpl implements XmlConverterService {

    private final FileStorageService fileStorageService;

    @Override
    public void upload(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }
            Path destinationFile = fileStorageService.getStoragePath().resolve(
                            Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(fileStorageService.getStoragePath().toAbsolutePath())) {
                // This is a security check
                throw new RuntimeException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }
}

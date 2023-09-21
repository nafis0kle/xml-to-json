package com.inovus.testtask.xmltojson.service.Impl;

import com.inovus.testtask.xmltojson.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author nafis
 * @since 19.09.2023
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.service.file-storage-directory-name}")
    private String rootLocation;
    private Path storagePath;

    @PostConstruct
    public void initStorageDirectory() {
        storagePath = Paths.get(rootLocation);
    }

    @Override
    public Path getStoragePath() {
        return storagePath;
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(storagePath.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(storagePath);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }
}

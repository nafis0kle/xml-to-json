package com.inovus.testtask.xmltojson.service.Impl;

import com.inovus.testtask.xmltojson.XmlNode;
import com.inovus.testtask.xmltojson.service.FileStorageService;
import com.inovus.testtask.xmltojson.service.XmlConverterService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

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

    @Override
    public void convertToJson() {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DefaultHandler handler = new DefaultHandler()
            {
                Map<String, XmlNode> nodeNameToParam = new HashMap<>();
                String currentNode = null;
                Integer currentLevel = 0;

                //parser starts parsing a specific element inside the document
                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    ++currentLevel;
                    currentNode = qName;
                    nodeNameToParam.put(qName, new XmlNode(currentLevel, 0.0));

                    System.out.println("Start element: " + qName + "Current level: " + currentLevel);
                }

                //parser ends parsing the specific element inside the document
                public void endElement(String uri, String localName, String qName) {
                    --currentLevel;

                    System.out.println("End element: " + qName);
                }

                //reads the text value of the currently parsed element
                public void characters(char ch[], int start, int length) {
                    String val = new String(ch, start, length);
                    String[] arr = val.split(" ");
                    for (String el : arr) {
                        if (NumberUtils.isCreatable(el)) {
                            Double number = Double.parseDouble(el);
                            for (Map.Entry<String, XmlNode> entry : nodeNameToParam.entrySet()) {
                                if (entry.getValue().getLevel() > currentLevel) {
                                    entry.getValue().plusValue(number);
                                }
                            }
                        }
                    }
                }
            };

            saxParser.parse("/xml-files/test-xml.xml", handler);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

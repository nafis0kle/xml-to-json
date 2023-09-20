package com.inovus.testtask.xmltojson.service.Impl;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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
import java.io.FileOutputStream;
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
        try(FileOutputStream fos = new FileOutputStream("./xml-files/output.json")) {
            JsonFactory jFactory = new JsonFactory();
            JsonGenerator jGenerator = jFactory
                    .createGenerator(fos, JsonEncoding.UTF8);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DefaultHandler handler = new DefaultHandler() {
                Map<String, XmlNode> nodeNameToParam = new HashMap<>();
                String currentNode = null;
                Integer currentLevel = 0;

                public void startDocument() {
                    try {
                        jGenerator.writeStartObject();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    ++currentLevel;
                    currentNode = qName;
                    nodeNameToParam.put(qName, new XmlNode(currentLevel, 0.0));

                    try {
                        jGenerator.writeObjectFieldStart(qName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println("Start element: " + qName + ". Current level: " + currentLevel + ". "+nodeNameToParam);
                }

                public void characters(char ch[], int start, int length) {
                    String str = new String(ch, start, length);
                    findNumberAndPlusValue(str);
                }

                public void endElement(String uri, String localName, String qName) {
                    --currentLevel;

                    try {
                        jGenerator.writeNumberField("value", nodeNameToParam.get(qName).getValue());
                        jGenerator.writeEndObject();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    nodeNameToParam.remove(qName);
                    System.out.println("End element: " + qName + ". " + nodeNameToParam);
                }

                public void endDocument() {
                    try {
                        jGenerator.writeEndObject();
                        jGenerator.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                private void findNumberAndPlusValue(String str) {
                    String[] arr = str.split(" ");
                    for (String el : arr) {
                        if (NumberUtils.isCreatable(el)) {
                            Double number = Double.parseDouble(el);
                            for (Map.Entry<String, XmlNode> entry : nodeNameToParam.entrySet()) {
                                // Увеличивается value для всех элементов уровнем выше
                                if (entry.getValue().getLevel() <= currentLevel) {
                                    entry.getValue().plusValue(number);
                                }
                            }
                        }
                    }
                }
            };

            saxParser.parse("./xml-files/test-xml.xml", handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

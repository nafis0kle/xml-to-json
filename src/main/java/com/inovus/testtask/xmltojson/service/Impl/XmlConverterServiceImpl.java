package com.inovus.testtask.xmltojson.service.Impl;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.inovus.testtask.xmltojson.domain.entity.XmlNode;
import com.inovus.testtask.xmltojson.domain.exception.JsonConvertException;
import com.inovus.testtask.xmltojson.domain.exception.StorageException;
import com.inovus.testtask.xmltojson.service.FileStorageService;
import com.inovus.testtask.xmltojson.service.XmlConverterService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author nafis
 * @since 19.09.2023
 */
@Service
@RequiredArgsConstructor
public class XmlConverterServiceImpl implements XmlConverterService {

    private final FileStorageService fileStorageService;

    @Value("${app.service.output-json-file-name}")
    private String outputJsonFileName;

    @Override
    public void upload(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Невозможно загрузить пустой файл");
            }
            Path destinationFile = fileStorageService.getStoragePath().resolve(
                            Paths.get(Objects.requireNonNull(file.getOriginalFilename())))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(fileStorageService.getStoragePath().toAbsolutePath())) {
                // This is a security check
                throw new StorageException("Ошибка при сохранении файла. Выход за рамки разрешенной директории");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Ошибка при сохранении файла");
        }
    }

    private String getConvertedJson(String jsonFile) {
        try {
            return new String(Files.readAllBytes(Paths.get(jsonFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String convertToJson(String fileName) {
        String xmlFilePath = "./" + fileStorageService.getStoragePath().toString() + "/" + fileName;
        String jsonFilePath = "./" + fileStorageService.getStoragePath().toString() + "/" + outputJsonFileName;

        try(FileOutputStream fos = new FileOutputStream(jsonFilePath)) {
            //Init JsonGenerator
            JsonFactory jFactory = new JsonFactory();
            JsonGenerator jGenerator = jFactory
                    .createGenerator(fos, JsonEncoding.UTF8);

            //Init XPath
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document doc = builder.parse(xmlFilePath);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            //Init SAX
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                // Служит для хранения и сложения "value"
                Map<Integer, XmlNode> tagLevelToXmlNode = new HashMap<>();
                // Название тега, для которой необходимо создать json массив значений
                String arrayTagName = "";
                // Количество элементов внутри json массива
                Integer arrayTagElementCount = 0;
                // Уровень вложенности тега
                Integer tagLevel = 0;

                public void startDocument() {
                    try {
                        jGenerator.writeStartObject();
                    } catch (IOException e) {
                        throw new JsonConvertException(e.getMessage());
                    }
                }

                public void startElement(String uri, String localName, String qName, Attributes attributes) {
                    try {
                        ++tagLevel;
                        tagLevelToXmlNode.put(tagLevel, new XmlNode(qName, 0.0));

                        if (Objects.equals(arrayTagName, qName)) {
                            jGenerator.writeStartObject();
                            return;
                        }

                        if (tagLevel != 1) {
                            XPathExpression xPathExpression = xPath.compile(
                                    generateIsTagRepeatedExp(qName)
                            );
                            Boolean isTagRepeated = (Boolean) xPathExpression.evaluate(doc, XPathConstants.BOOLEAN);

                            if (isTagRepeated) {
                                XPathExpression xPathExpressionCount = xPath.compile(
                                        generateTagCountExp()
                                );
                                arrayTagElementCount = ((Double) xPathExpressionCount.evaluate(doc, XPathConstants.NUMBER)).intValue();
                                arrayTagName = qName;
                                jGenerator.writeArrayFieldStart(qName);
                                jGenerator.writeStartObject();
                                return;
                            }
                        }

                        jGenerator.writeObjectFieldStart(qName);

                    } catch (IOException | XPathExpressionException e) {
                        throw new JsonConvertException(e.getMessage());
                    }
                }

                public void characters(char[] ch, int start, int length) {
                    String str = new String(ch, start, length);
                    findNumberAndPlusValue(str);
                }

                public void endElement(String uri, String localName, String qName) {
                    try {
                        jGenerator.writeNumberField("value", tagLevelToXmlNode.get(tagLevel).getValue());
                        jGenerator.writeEndObject();

                        if (Objects.equals(arrayTagName, qName)) {
                            --arrayTagElementCount;
                            if (arrayTagElementCount == 0) {
                                jGenerator.writeEndArray();
                                arrayTagName = "";
                            }
                        }

                        tagLevelToXmlNode.remove(tagLevel);
                        --tagLevel;
                    } catch (IOException e) {
                        throw new JsonConvertException(e.getMessage());
                    }
                }

                public void endDocument() {
                    try {
                        jGenerator.writeEndObject();
                        jGenerator.close();
                    } catch (IOException e) {
                        throw new JsonConvertException(e.getMessage());
                    }
                }

                /**
                 * Ищет число внутри значения тега и при успешном поиске
                 * увеличивает "value" для всех родительских тегов
                 * @param str - значение тега
                 */
                private void findNumberAndPlusValue(String str) {
                    String[] arr = str.split(" ");
                    for (String el : arr) {
                        if (NumberUtils.isCreatable(el)) {
                            Double number = Double.parseDouble(el);
                            for (Map.Entry<Integer, XmlNode> entry : tagLevelToXmlNode.entrySet()) {
                                // Увеличивается value для всех элементов уровнем выше
                                if (entry.getKey() <= tagLevel) {
                                    entry.getValue().plusValue(number);
                                }
                            }
                        }
                    }
                }

                /**
                 * XPath выражение для проверки, повторяется ли одинаковый тег.
                 * Пример: //parentTag[count(childTag)=1=false()]
                 * @param qName - название тега
                 * @return - XPath выражение
                 */
                private String generateIsTagRepeatedExp(String qName) {
                    StringBuilder expression = new StringBuilder("/");
                    for (int i = 1; i < tagLevel; i++) {
                        expression
                                .append("/")
                                .append(tagLevelToXmlNode.get(i).getName());
                    }
                    expression
                            .append("[count(")
                            .append(qName)
                            .append(")=1=false()]");

                    return expression.toString();
                }

                /**
                 * XPath выражение для получения кол-ва повторяющихся тегов.
                 * Пример: count(//parentTag/childTag)
                 * @return - XPath выражение
                 */
                private String generateTagCountExp() {
                    StringBuilder expression = new StringBuilder("count(/");
                    for (int i = 1; i <= tagLevel; i++) {
                        expression
                                .append("/")
                                .append(tagLevelToXmlNode.get(i).getName());
                    }
                    expression.append(")");

                    return expression.toString();
                }
            };

            saxParser.parse(xmlFilePath, handler);
        } catch (Exception e) {
            throw new JsonConvertException(e.getMessage());
        }

        return getConvertedJson(jsonFilePath);
    }

}

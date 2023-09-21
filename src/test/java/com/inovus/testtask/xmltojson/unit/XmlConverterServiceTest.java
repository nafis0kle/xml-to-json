package com.inovus.testtask.xmltojson.unit;

import com.inovus.testtask.xmltojson.service.FileStorageService;
import com.inovus.testtask.xmltojson.service.Impl.XmlConverterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author nafis
 * @since 21.09.2023
 */

@ExtendWith(MockitoExtension.class)
public class XmlConverterServiceTest {
    @InjectMocks
    private XmlConverterServiceImpl xmlConverterService;
    @Mock
    private FileStorageService fileStorageService;

    @Test
    public void testConvertToJson() {
        when(fileStorageService.getStoragePath()).thenReturn(Paths.get("src/test/resources"));
        ReflectionTestUtils.setField(xmlConverterService, "outputJsonFileName", "output.json");

        String expectedJson;
        String actualJson = xmlConverterService.convertToJson("testXml.xml");
        try {
            expectedJson = new String(Files.readAllBytes(Paths.get("src/test/resources/expectedOutput.json")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(expectedJson, actualJson);
    }
}

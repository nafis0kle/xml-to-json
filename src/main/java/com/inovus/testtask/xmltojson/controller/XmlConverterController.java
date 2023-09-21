package com.inovus.testtask.xmltojson.controller;

import com.inovus.testtask.xmltojson.domain.exception.JsonConvertException;
import com.inovus.testtask.xmltojson.domain.exception.StorageException;
import com.inovus.testtask.xmltojson.service.XmlConverterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author nafis
 * @since 19.09.2023
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/xml/converters")
public class XmlConverterController {

    private final XmlConverterService xmlConverterService;

    @GetMapping()
    public String showUploadXmlFilePage() {
        return "uploadForm";
    }

    @GetMapping("/json")
    public String showConvertedJson(@RequestParam("fileName") String fileName) {
        xmlConverterService.convertToJson(fileName);
        return "showJson";
    }

    @PostMapping()
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        xmlConverterService.upload(file);

        redirectAttributes.addFlashAttribute(
                "message",
                "Файл " + file.getOriginalFilename() + " успешно загружен на сервер!"
        );
        redirectAttributes.addFlashAttribute("fileName", file.getOriginalFilename());

        return "redirect:/xml/converters";
    }

    @ExceptionHandler({StorageException.class, JsonConvertException.class})
    public ModelAndView handleStorageException(Exception ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("uploadForm");
        modelAndView.addObject("exception", ex.getMessage());
        return modelAndView;
    }
}

package com.inovus.testtask.xmltojson.controller;

import com.inovus.testtask.xmltojson.service.XmlConverterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author nafis
 * @since 19.09.2023
 */

@Controller
@RequiredArgsConstructor
@RequestMapping("/xml")
public class XmlConverterController {

    private final XmlConverterService xmlConverterService;

    @GetMapping()
    public String loadXmlFilePage() {
        return "uploadForm";
    }

    @GetMapping("/convert/json")
    public String convertXmlToJson() {

        return "uploadForm";
    }

    @PostMapping()
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {

        xmlConverterService.upload(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/xml";
    }
}

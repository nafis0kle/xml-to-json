package com.inovus.testtask.xmltojson;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author nafis
 * @since 20.09.2023
 */
@Getter
@Setter
@AllArgsConstructor
public class XmlNode {

    private Integer level;
    private Double value;

    public void plusValue(Double addedValue) {
        value = value + addedValue;
    }
}

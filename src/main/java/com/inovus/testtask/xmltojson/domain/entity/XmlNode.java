package com.inovus.testtask.xmltojson.domain.entity;

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

    private String name;
    private Double value;

    public void plusValue(Double addedValue) {
        value = value + addedValue;
    }

    @Override
    public String toString() {
        return "XmlNode{" +
                "name=" + name +
                ", value=" + value +
                '}';
    }

}

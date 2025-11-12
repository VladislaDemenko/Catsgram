package ru.yandex.practicum.catsgram.model;

import lombok.Data;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
public class ImageData {
    private final byte[] data;
    private final String name;
}

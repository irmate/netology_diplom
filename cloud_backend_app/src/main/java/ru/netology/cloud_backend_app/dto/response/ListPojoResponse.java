package ru.netology.cloud_backend_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListPojoResponse {
    private String filename;
    private Integer size;
}
package ru.netology.cloud_backend_app.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class EditRequest {
    @NotNull
    @NotBlank
    private String filename;
}
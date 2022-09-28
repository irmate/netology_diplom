package ru.netology.cloud_backend_app.rest;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloud_backend_app.dto.request.EditRequest;
import ru.netology.cloud_backend_app.dto.response.ListPojoResponse;
import ru.netology.cloud_backend_app.security.validation.ValidFile;
import ru.netology.cloud_backend_app.service.ContentManagerService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.websocket.server.PathParam;
import java.util.List;

@RestController
@RequestMapping("/")
@Validated
public class AppRestController {

    private final ContentManagerService contentManagerService;

    public AppRestController(ContentManagerService contentManagerService) {
        this.contentManagerService = contentManagerService;
    }

    @PostMapping("/file")
    public ResponseEntity upload(
            @PathParam("filename") @NotBlank @NotNull String filename,
            @ValidFile @RequestPart("file") MultipartFile multipartFile)
    {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        contentManagerService.upload(filename, multipartFile, auth);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/file")
    public ResponseEntity delete(@PathParam("filename") @NotBlank @NotNull String filename) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        contentManagerService.delete(filename, auth);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> download(@PathParam("filename") @NotBlank @NotNull String filename) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        var file = contentManagerService.getFile(filename, auth);
        var response = file.getResource();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/file")
    public ResponseEntity edit(
            @PathParam("filename") @NotBlank @NotNull String filename,
            @Valid @RequestBody EditRequest editRequest)
    {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        contentManagerService.edit(editRequest.getFilename(), filename, auth);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ListPojoResponse>> getFilesByLimit(@PathParam("limit") @Positive int limit) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        var result = contentManagerService.getListByLimit(limit, auth);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
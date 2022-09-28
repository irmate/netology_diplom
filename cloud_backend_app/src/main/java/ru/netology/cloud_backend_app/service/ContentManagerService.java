package ru.netology.cloud_backend_app.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloud_backend_app.dto.response.ListPojoResponse;

import java.util.List;

public interface ContentManagerService {
    void upload(String filename, MultipartFile multipartFile, Authentication auth);
    void delete(String name, Authentication auth);
    MultipartFile getFile(String name, Authentication auth);
    void edit(String name, String filename, Authentication auth);
    List<ListPojoResponse> getListByLimit(int limit, Authentication auth);
}
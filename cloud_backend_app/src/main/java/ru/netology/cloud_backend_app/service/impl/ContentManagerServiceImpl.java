package ru.netology.cloud_backend_app.service.impl;

import lombok.SneakyThrows;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloud_backend_app.dto.response.ListPojoResponse;
import ru.netology.cloud_backend_app.exception.ContentNotFoundException;
import ru.netology.cloud_backend_app.model.Content;
import ru.netology.cloud_backend_app.dto.response.CustomMultipartFile;
import ru.netology.cloud_backend_app.model.Status;
import ru.netology.cloud_backend_app.repository.ContentRepository;
import ru.netology.cloud_backend_app.service.ContentManagerService;
import ru.netology.cloud_backend_app.service.UserService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ContentManagerServiceImpl implements ContentManagerService {

    private final ContentRepository contentRepository;
    private final UserService userService;

    public ContentManagerServiceImpl(ContentRepository contentRepository, UserService userService) {
        this.contentRepository = contentRepository;
        this.userService = userService;
    }

    @SneakyThrows(IOException.class)
    @Override
    public void upload(String filename, MultipartFile multipartFile, Authentication auth) {
        var user = userService.findByLogin(auth.getName());

        var date = new Date();
        var content = new Content();
        content.setName(filename);
        content.setData(multipartFile.getBytes());
        content.setCreated(new Date(date.getTime()));
        content.setUpdated(new Date(date.getTime()));
        content.setStatus(Status.ACTIVE);
        content.setUser(user);
        contentRepository.saveAndFlush(content);
    }

    @SneakyThrows(ContentNotFoundException.class)
    @Override
    public void delete(String name, Authentication auth) {
        var user = userService.findByLogin(auth.getName());

        if (contentRepository.findByName(name, user.getId()).isPresent()) {
            contentRepository.delete(name, user.getId());
        } else {
            throw new ContentNotFoundException("Error delete file");
        }
    }

    @SneakyThrows(ContentNotFoundException.class)
    @Override
    public MultipartFile getFile(String name, Authentication auth) {
        var user = userService.findByLogin(auth.getName());

        var content = contentRepository.findByName(name, user.getId()).orElseThrow(
                () -> new ContentNotFoundException("Error download file")
        );
        return new CustomMultipartFile(content.getData(), content.getName());
    }

    @SneakyThrows(ContentNotFoundException.class)
    @Override
    public void edit(String name, String filename, Authentication auth) {
        var user = userService.findByLogin(auth.getName());

        if (contentRepository.findByName(filename, user.getId()).isPresent()) {
            contentRepository.update(name, filename, user.getId());
        } else {
            throw new ContentNotFoundException("Error edit filename");
        }
    }

    @Override
    public List<ListPojoResponse> getListByLimit(int limit, Authentication auth) {
        var user = userService.findByLogin(auth.getName());

        var list = contentRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updated")),
                user.getId());

        var result = new CopyOnWriteArrayList<ListPojoResponse>();
        for (Content content : list) {
            result.add(new ListPojoResponse(content.getName(), content.getData().length));
        }
        return result;
    }
}
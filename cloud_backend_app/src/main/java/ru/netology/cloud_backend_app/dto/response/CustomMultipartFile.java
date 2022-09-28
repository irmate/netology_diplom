package ru.netology.cloud_backend_app.dto.response;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class CustomMultipartFile implements MultipartFile {
    private final byte[] bytes;
    private final String name;

    public CustomMultipartFile(byte[] bytes, String name) {
        this.bytes = bytes;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return name;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return bytes == null || bytes.length == 0;
    }

    @Override
    public long getSize() {
        return bytes.length;
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void transferTo(File dest) throws IllegalStateException, IOException {
        try (FileOutputStream f = new FileOutputStream(dest)) {
            f.write(bytes);
        }
    }
}
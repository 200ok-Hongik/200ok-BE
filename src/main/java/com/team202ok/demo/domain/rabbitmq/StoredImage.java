package com.team202ok.demo.domain.rabbitmq;

import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Files;

public record StoredImage(String filename, String contentType, byte[] bytes) implements MultipartFile {
    public String getName() { return "image"; }
    public String getOriginalFilename() { return filename; }
    public String getContentType() { return contentType; }
    public boolean isEmpty() { return bytes.length == 0; }
    public long getSize() { return bytes.length; }
    public byte[] getBytes() { return bytes; }
    public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }
    public void transferTo(File file) throws IOException { Files.write(file.toPath(), bytes); }
}

package com.example.controller;

import com.example.model.FileMetadata;
import com.example.service.FileService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileController {
    private final FileService fileService;
    private final Gson gson;

    public FileController() {
        this.fileService = new FileService();

        // Configure Gson with LocalDateTime support
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    context.serialize(src.format(formatter)))
            .registerTypeAdapter(LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    LocalDateTime.parse(json.getAsString(), formatter))
            .create();
    }

    public String listFiles(Request req, Response res) {
        String userId = req.session().attribute("userId");

        if (userId == null) {
            res.status(401);
            return gson.toJson(Map.of("error", "Not authenticated"));
        }

        List<FileMetadata> files = fileService.getUserFiles(userId);
        res.type("application/json");
        return gson.toJson(files);
    }

    public String uploadFile(Request req, Response res) {
        String userId = req.session().attribute("userId");

        if (userId == null) {
            res.status(401);
            return gson.toJson(Map.of("error", "Not authenticated"));
        }

        try {
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            Part filePart = req.raw().getPart("file");
            String fileName = filePart.getSubmittedFileName();
            long fileSize = filePart.getSize();
            String contentType = filePart.getContentType();

            try (InputStream fileContent = filePart.getInputStream()) {
                FileMetadata metadata = fileService
                        .saveFile(userId, fileName, fileContent, fileSize, contentType);

                if (metadata != null) {
                    res.type("application/json");
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("file", metadata);
                    return gson.toJson(response);
                }
            }
        } catch (Exception e) {
            System.err.println("=== Upload File Error ===");
            System.err.println("User ID: " + userId);
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================");
            res.status(500);
            return gson.toJson(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }

        res.status(500);
        return gson.toJson(Map.of("error", "Failed to upload file"));
    }

    public Object viewFile(Request req, Response res) {
        String userId = req.session().attribute("userId");

        if (userId == null) {
            res.status(401);
            return "Not authenticated";
        }

        try {
            Long fileId = Long.parseLong(req.params(":id"));
            FileMetadata metadata = fileService.getFileMetadata(fileId, userId);

            if (metadata != null) {
                byte[] fileContent = fileService.getFileContent(fileId, userId);
                if (fileContent != null) {
                    res.type(metadata.getContentType() != null ? metadata.getContentType() : "application/octet-stream");
                    res.raw().getOutputStream().write(fileContent);
                    res.raw().getOutputStream().flush();
                    return res.raw();
                }
            }
        } catch (Exception e) {
            System.err.println("=== View File Error ===");
            System.err.println("User ID: " + userId);
            System.err.println("File ID: " + req.params(":id"));
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("======================");
        }

        res.status(404);
        return "File not found";
    }

    public Object downloadFile(Request req, Response res) {
        String userId = req.session().attribute("userId");

        if (userId == null) {
            res.status(401);
            return "Not authenticated";
        }

        try {
            Long fileId = Long.parseLong(req.params(":id"));
            FileMetadata metadata = fileService.getFileMetadata(fileId, userId);

            if (metadata != null) {
                byte[] fileContent = fileService.getFileContent(fileId, userId);
                if (fileContent != null) {
                    res.type(metadata.getContentType() != null ? metadata.getContentType() : "application/octet-stream");
                    res.header("Content-Disposition", "attachment; filename=\"" + metadata.getFileName() + "\"");
                    res.raw().getOutputStream().write(fileContent);
                    res.raw().getOutputStream().flush();
                    return res.raw();
                }
            }
        } catch (Exception e) {
            System.err.println("=== Download File Error ===");
            System.err.println("User ID: " + userId);
            System.err.println("File ID: " + req.params(":id"));
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("==========================");
        }

        res.status(404);
        return "File not found";
    }
}

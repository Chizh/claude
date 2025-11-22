package com.example.controller;

import com.example.model.FileMetadata;
import com.example.service.FileService;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileController {
    private final FileService fileService;
    private final Gson gson;

    public FileController() {
        this.fileService = new FileService();
        this.gson = new Gson();
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

            try (InputStream fileContent = filePart.getInputStream()) {
                FileMetadata metadata = fileService.saveFile(userId, fileName, fileContent, fileSize);

                if (metadata != null) {
                    res.type("application/json");
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("file", metadata);
                    return gson.toJson(response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            return gson.toJson(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }

        res.status(500);
        return gson.toJson(Map.of("error", "Failed to upload file"));
    }

    public Object downloadFile(Request req, Response res) {
        String userId = req.session().attribute("userId");

        if (userId == null) {
            res.status(401);
            return "Not authenticated";
        }

        try {
            Long fileId = Long.parseLong(req.params(":id"));
            byte[] fileContent = fileService.getFileContent(fileId, userId);

            if (fileContent != null) {
                res.type("application/octet-stream");
                res.header("Content-Disposition", "attachment");
                res.raw().getOutputStream().write(fileContent);
                res.raw().getOutputStream().flush();
                return res.raw();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        res.status(404);
        return "File not found";
    }
}

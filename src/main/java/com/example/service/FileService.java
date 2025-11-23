package com.example.service;

import com.example.db.Database;
import com.example.model.FileMetadata;
import org.sql2o.Connection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class FileService {
    private static final String UPLOAD_DIR = "uploads/";

    public FileService() {
        new File(UPLOAD_DIR).mkdirs();
    }

    public FileMetadata saveFile(String userId, String fileName, InputStream fileContent, long fileSize, String contentType) {
        try {
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            String filePath = UPLOAD_DIR + uniqueFileName;

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fileContent.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            FileMetadata metadata = new FileMetadata(userId, fileName, filePath, fileSize, contentType);

            String sql = "INSERT INTO files (user_id, file_name, file_path, file_size, content_type, uploaded_at) " +
                    "VALUES (:userId, :fileName, :filePath, :fileSize, :contentType, :uploadedAt)";

            try (Connection conn = Database.getSql2o().open()) {
                long id = (long) conn.createQuery(sql, true)
                        .addParameter("userId", metadata.getUserId())
                        .addParameter("fileName", metadata.getFileName())
                        .addParameter("filePath", metadata.getFilePath())
                        .addParameter("fileSize", metadata.getFileSize())
                        .addParameter("contentType", metadata.getContentType())
                        .addParameter("uploadedAt", metadata.getUploadedAt())
                        .executeUpdate()
                        .getKey();
                metadata.setId(id);
            }

            return metadata;
        } catch (Exception e) {
            System.err.println("=== File Service Save Error ===");
            System.err.println("User ID: " + userId);
            System.err.println("File Name: " + fileName);
            System.err.println("File Size: " + fileSize);
            System.err.println("Content Type: " + contentType);
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("==============================");
            return null;
        }
    }

    public List<FileMetadata> getUserFiles(String userId) {
        String sql = "SELECT id, user_id AS userId, file_name AS fileName, " +
                "file_path AS filePath, file_size AS fileSize, content_type AS contentType, " +
                "uploaded_at AS uploadedAt " +
                "FROM files WHERE user_id = :userId ORDER BY uploaded_at DESC";

        try (Connection conn = Database.getSql2o().open()) {
            return conn.createQuery(sql)
                    .addParameter("userId", userId)
                    .executeAndFetch(FileMetadata.class);
        }
    }

    public FileMetadata getFileMetadata(Long fileId, String userId) {
        String sql = "SELECT id, user_id AS userId, file_name AS fileName, " +
                "file_path AS filePath, file_size AS fileSize, content_type AS contentType, " +
                "uploaded_at AS uploadedAt " +
                "FROM files WHERE id = :id AND user_id = :userId";

        try (Connection conn = Database.getSql2o().open()) {
            return conn.createQuery(sql)
                    .addParameter("id", fileId)
                    .addParameter("userId", userId)
                    .executeAndFetchFirst(FileMetadata.class);
        }
    }

    public byte[] getFileContent(Long fileId, String userId) {
        String sql = "SELECT file_path FROM files WHERE id = :id AND user_id = :userId";

        try (Connection conn = Database.getSql2o().open()) {
            String filePath = conn.createQuery(sql)
                    .addParameter("id", fileId)
                    .addParameter("userId", userId)
                    .executeScalar(String.class);

            if (filePath != null) {
                return Files.readAllBytes(Paths.get(filePath));
            }
        } catch (Exception e) {
            System.err.println("=== File Service Get Content Error ===");
            System.err.println("File ID: " + fileId);
            System.err.println("User ID: " + userId);
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=====================================");
        }
        return null;
    }
}

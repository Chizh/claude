package com.example.db;

import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.quirks.NoQuirks;

import java.time.LocalDateTime;
import java.sql.Timestamp;

public class Database {
    private static Sql2o sql2o;

    public static void init() {
        try {
            System.out.println("Initializing database...");

            // Create custom quirks to handle LocalDateTime conversion
            NoQuirks quirks = new NoQuirks() {{
                // Register converter for LocalDateTime
                converters.put(LocalDateTime.class, new Converter<LocalDateTime>() {
                    @Override
                    public LocalDateTime convert(Object val) throws org.sql2o.converters.ConverterException {
                        if (val == null) {
                            return null;
                        }
                        if (val instanceof Timestamp) {
                            return ((Timestamp) val).toLocalDateTime();
                        }
                        if (val instanceof java.sql.Date) {
                            return ((java.sql.Date) val).toLocalDate().atStartOfDay();
                        }
                        if (val instanceof LocalDateTime) {
                            return (LocalDateTime) val;
                        }
                        throw new org.sql2o.converters.ConverterException("Cannot convert " + val.getClass().getName() + " to LocalDateTime");
                    }

                    @Override
                    public Object toDatabaseParam(LocalDateTime val) {
                        return val == null ? null : Timestamp.valueOf(val);
                    }
                });
            }};

            sql2o = new Sql2o("jdbc:h2:./filedb", "sa", "", quirks);
            createTables();

            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            System.err.println("=== Database Initialization Error ===");
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("====================================");
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void createTables() {
        String createFilesTable = "CREATE TABLE IF NOT EXISTS files (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id VARCHAR(255) NOT NULL, " +
                "file_name VARCHAR(512) NOT NULL, " +
                "file_path VARCHAR(1024) NOT NULL, " +
                "file_size BIGINT NOT NULL, " +
                "content_type VARCHAR(255), " +
                "uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";

        try (org.sql2o.Connection conn = sql2o.open()) {
            System.out.println("Creating tables...");
            conn.createQuery(createFilesTable).executeUpdate();

            // Add content_type column if it doesn't exist (for existing databases)
            try {
                conn.createQuery("ALTER TABLE files ADD COLUMN IF NOT EXISTS content_type VARCHAR(255)").executeUpdate();
            } catch (Exception e) {
                // Column might already exist, ignore
            }

            System.out.println("Tables created successfully");
        } catch (Exception e) {
            System.err.println("=== Create Tables Error ===");
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("==========================");
            throw new RuntimeException("Failed to create tables", e);
        }
    }

    public static Sql2o getSql2o() {
        return sql2o;
    }
}

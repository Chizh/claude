package com.example.db;

import org.sql2o.Sql2o;

public class Database {
    private static Sql2o sql2o;

    public static void init() {
        sql2o = new Sql2o("jdbc:h2:./filedb", "sa", "");
        createTables();
    }

    private static void createTables() {
        String createFilesTable = "CREATE TABLE IF NOT EXISTS files (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id VARCHAR(255) NOT NULL, " +
                "file_name VARCHAR(512) NOT NULL, " +
                "file_path VARCHAR(1024) NOT NULL, " +
                "file_size BIGINT NOT NULL, " +
                "uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";

        try (org.sql2o.Connection conn = sql2o.open()) {
            conn.createQuery(createFilesTable).executeUpdate();
        }
    }

    public static Sql2o getSql2o() {
        return sql2o;
    }
}

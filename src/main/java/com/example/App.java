package com.example;

import com.example.auth.GoogleAuth;
import com.example.controller.AuthController;
import com.example.controller.FileController;
import com.example.db.Database;
import com.example.viewmodel.FilesViewModel;
import com.example.viewmodel.LoginViewModel;
import freemarker.template.Configuration;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import static spark.Spark.*;

public class App {
    public static void main(String[] args) {
        port(80);

        Database.init();

        staticFiles.location("/public");

        // Configure FreeMarker
        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
        Configuration freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_26);
        freeMarkerConfiguration.setClassForTemplateLoading(App.class, "/templates");
        freeMarkerEngine.setConfiguration(freeMarkerConfiguration);

        AuthController authController = new AuthController();
        FileController fileController = new FileController();

        // Global exception handler
        exception(Exception.class, (exception, request, response) -> {
            System.err.println("=== EXCEPTION OCCURRED ===");
            System.err.println("Request: " + request.requestMethod() + " " + request.pathInfo());
            System.err.println("Exception: " + exception.getClass().getName());
            System.err.println("Message: " + exception.getMessage());
            System.err.println("Stack trace:");
            exception.printStackTrace();
            System.err.println("=========================");

            response.status(500);
            response.body("Internal Server Error: " + exception.getMessage());
        });

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        get("/", (req, res) -> {
            String userId = req.session().attribute("userId");
            if (userId != null) {
                res.redirect("/files");
                return "";
            }
            LoginViewModel viewModel = new LoginViewModel(GoogleAuth.getClientId());
            return freeMarkerEngine.render(new ModelAndView(viewModel, "login.ftl"));
        });

        get(
            "/files",
                    (req, res) -> {
                        String userId = req.session().attribute("userId");
                        if (userId == null) {
                            res.redirect("/");
                            return "";
                        }
                        FilesViewModel viewModel = new FilesViewModel(
                            req.session().attribute("name"),
                            req.session().attribute("email")
                        );
                        return freeMarkerEngine.render(new ModelAndView(viewModel, "files.ftl"));
            }
        );

        post("/api/auth/google", authController::verifyToken);
        get("/api/auth/logout", authController::logout);
        get("/api/auth/client-id", authController::getClientId);

        get("/api/files", fileController::listFiles);
        post("/api/files/upload", fileController::uploadFile);
        get("/api/files/:id/view", fileController::viewFile);
        get("/api/files/:id/download", fileController::downloadFile);

        System.out.println("Server started on http://localhost:4567");
    }
}

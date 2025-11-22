package com.example;

import com.example.auth.GoogleAuth;
import com.example.controller.AuthController;
import com.example.controller.FileController;
import com.example.db.Database;
import com.example.viewmodel.FilesViewModel;
import com.example.viewmodel.LoginViewModel;
import freemarker.template.Configuration;
import spark.ModelAndView;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;

public class App {
    public static void main(String[] args) {
        Spark.port(80);

        Database.init();

        Spark.staticFiles.location("/public");

        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
        Configuration freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_26);
        freeMarkerConfiguration.setClassForTemplateLoading(App.class, "/templates");
        freeMarkerEngine.setConfiguration(freeMarkerConfiguration);

        AuthController authController = new AuthController();
        FileController fileController = new FileController();

        Spark.before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        Spark.get("/", (req, res) -> {
            String userId = req.session().attribute("userId");
            if (userId != null) {
                res.redirect("/files");
                return "";
            }
            LoginViewModel viewModel = new LoginViewModel(GoogleAuth.getClientId());
            return freeMarkerEngine.render(new ModelAndView(viewModel, "login.ftl"));
        });

        Spark.get("/files", (req, res) -> {
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
        });

        Spark.post("/api/auth/google", authController::verifyToken);
        Spark.get("/api/auth/logout", authController::logout);
        Spark.get("/api/auth/client-id", authController::getClientId);

        Spark.get("/api/files", fileController::listFiles);
        Spark.post("/api/files/upload", fileController::uploadFile);
        Spark.get("/api/files/:id/download", fileController::downloadFile);

        System.out.println("Server started on http://localhost:4567");
    }
}

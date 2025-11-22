package com.example.controller;

import com.example.auth.GoogleAuth;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class AuthController {
    private final GoogleAuth googleAuth;
    private final Gson gson;

    public AuthController() {
        this.googleAuth = new GoogleAuth();
        this.gson = new Gson();
    }

    public String verifyToken(Request req, Response res) {
        String idTokenString = req.body();
        Map<String, String> tokenData = gson.fromJson(idTokenString, Map.class);
        String token = tokenData.get("credential");

        GoogleIdToken.Payload payload = googleAuth.verify(token);

        if (payload != null) {
            String userId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            req.session(true);
            req.session().attribute("userId", userId);
            req.session().attribute("email", email);
            req.session().attribute("name", name);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("email", email);
            response.put("name", name);

            res.type("application/json");
            return gson.toJson(response);
        }

        res.status(401);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Invalid token");
        return gson.toJson(response);
    }

    public String logout(Request req, Response res) {
        req.session().invalidate();
        res.redirect("/");
        return "";
    }

    public String getClientId(Request req, Response res) {
        Map<String, String> response = new HashMap<>();
        response.put("clientId", GoogleAuth.getClientId());
        res.type("application/json");
        return gson.toJson(response);
    }
}

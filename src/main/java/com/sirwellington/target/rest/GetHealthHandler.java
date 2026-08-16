package com.sirwellington.target.rest;

import io.javalin.http.Context;
import java.util.Map;

public class GetHealthHandler {

    public void handle(Context ctx) {
        ctx.json(Map.of("status", "ok"));
    }
}

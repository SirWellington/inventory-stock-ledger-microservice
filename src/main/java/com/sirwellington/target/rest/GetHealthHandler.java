package com.sirwellington.target.rest;

import io.javalin.http.Context;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class GetHealthHandler {

    public void handle(Context ctx) {
        ctx.json(Map.of("status", "ok"));
    }
}

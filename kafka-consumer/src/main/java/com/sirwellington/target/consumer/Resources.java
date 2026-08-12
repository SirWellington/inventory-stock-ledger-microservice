package com.sirwellington.target.consumer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Utility for reading classpath resources. */
public final class Resources {

    private Resources() {}

    /** Reads a classpath resource into a single string. */
    public static String load(String path) {
        try (var stream = Resources.class.getResourceAsStream(path)) {
            if (stream == null) return null;
            var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return reader.lines().reduce("", (a, b) -> a + System.lineSeparator() + b);
        } catch (Exception e) {
            return null;
        }
    }
}

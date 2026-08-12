package com.sirwellington.target.common;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility for reading classpath resources. */
public final class Resources {

    private static final Logger LOG = LoggerFactory.getLogger(Resources.class);

    private Resources() {}

    /** Reads a classpath resource into a single string. */
    public static String load(String path) {
        try (var stream = Resources.class.getResourceAsStream(path)) {
            if (stream == null) return null;
            return new String(
                stream.readAllBytes(),
                StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            LOG.error("Failed to load resource at {}", path, e);
            return null;
        }
    }
}

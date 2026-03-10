package net.deckserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DataPaths {

    private static final Logger logger = LoggerFactory.getLogger(DataPaths.class);
    private static final String DATA_PROPERTY = "jol.data";
    private static final String DATA_ENV = "JOL_DATA";

    private static final Path BASE_DIR = resolveBaseDir();

    private DataPaths() {
    }

    public static Path baseDir() {
        return BASE_DIR;
    }

    public static Path path(String first, String... more) {
        return BASE_DIR.resolve(Path.of(first, more));
    }

    public static Path ensureDirectory(String first, String... more) {
        Path directory = path(first, more);
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create data directory: " + directory.toAbsolutePath(), e);
        }
        return directory;
    }

    private static Path resolveBaseDir() {
        String configuredPath = System.getProperty(DATA_PROPERTY);
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = System.getenv(DATA_ENV);
        }

        Path baseDir;
        if (configuredPath != null && !configuredPath.isBlank()) {
            baseDir = Path.of(configuredPath);
        } else {
            Path testResourcesDir = Path.of("src", "test", "resources", "data");
            baseDir = Files.exists(testResourcesDir) ? testResourcesDir : Path.of("data");
            logger.warn(
                    "Neither system property '{}' nor environment variable '{}' is set. Falling back to {}",
                    DATA_PROPERTY,
                    DATA_ENV,
                    baseDir.toAbsolutePath()
            );
        }

        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize data directory: " + baseDir.toAbsolutePath(), e);
        }

        return baseDir;
    }
}
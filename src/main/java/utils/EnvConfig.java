package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Robust .env and environment variable configuration loader.
 * Priority: System Properties > System Environment Variables > .env file > Default Values.
 */
public class EnvConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);
    private static final Map<String, String> envMap = new HashMap<>();
    private static boolean loaded = false;

    static {
        loadEnv();
    }

    public static synchronized void loadEnv() {
        envMap.clear();

        // Check common locations for .env
        File envFile = new File(".env");
        if (!envFile.exists()) {
            envFile = new File(System.getProperty("user.dir"), ".env");
        }

        if (envFile.exists() && envFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    int equalsIdx = line.indexOf('=');
                    if (equalsIdx > 0) {
                        String key = line.substring(0, equalsIdx).trim();
                        String val = line.substring(equalsIdx + 1).trim();

                        // Strip optional surrounding quotes
                        if ((val.startsWith("\"") && val.endsWith("\"")) ||
                            (val.startsWith("'") && val.endsWith("'"))) {
                            val = val.substring(1, val.length() - 1);
                        }

                        envMap.put(key, val);
                        count++;
                    }
                }
                logger.info("Loaded {} environment variables from {}", count, envFile.getAbsolutePath());
            } catch (IOException e) {
                logger.warn("Failed to read .env file: {}", e.getMessage());
            }
        } else {
            logger.info("No .env file found at {}; falling back to system environment and defaults.", envFile.getAbsolutePath());
        }

        loaded = true;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        if (!loaded) loadEnv();

        // 1. System property (-Dkey=value)
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }

        // 2. OS Environment variable
        String sysEnv = System.getenv(key);
        if (sysEnv != null && !sysEnv.trim().isEmpty()) {
            return sysEnv.trim();
        }

        // 3. .env map
        String val = envMap.get(key);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }

        // 4. Default fallback
        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, null);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, null);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val) || "1".equals(val) || "yes".equalsIgnoreCase(val);
    }
}

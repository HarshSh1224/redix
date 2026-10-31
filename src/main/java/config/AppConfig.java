package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = AppConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if(input == null) {
                throw new RuntimeException("application.properties not found");
            }

            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new RuntimeException("IO Exception while loading resources: " + e.getMessage());
        }
    }

    private static int getInt(String key) {
        return Integer.parseInt(PROPERTIES.getProperty(key));
    }

    private static String getString(String key) {
        return PROPERTIES.getProperty(key);
    }

    private AppConfig() {} // Prevent initialization of the class

    public static int getServerPort() {
        return getInt("server.port");
    }

    public static int getMaxConnections() {
        return getInt("server.maxConnections");
    }
}

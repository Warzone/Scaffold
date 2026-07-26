package network.warzone.scaffold.utils.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// New file transfer service GoFile.io does not require API credentials, so this class is currently unused.
@Deprecated
public class FtpManager {
    private Properties properties;
    private String configPath = "plugins/Scaffold/config.properties";

    public FtpManager() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (FileInputStream in = new FileInputStream(configPath)) {
            properties.load(in);
        } catch (IOException e) {
            System.out.println("Error loading the config properties.");
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
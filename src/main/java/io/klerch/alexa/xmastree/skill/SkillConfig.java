package io.klerch.alexa.xmastree.skill;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Encapsulates access to application-wide property values
 */
public class SkillConfig {
    private static Properties properties = new Properties();
    private static final String defaultPropertiesFile = "app.properties";
    private static final String customPropertiesFile = "my.app.properties";

    /**
     * Static block does the bootstrapping of all configuration properties with
     * reading out values from different resource files
     */
    static {
        final String propertiesFile =
                SkillConfig.class.getClassLoader().getResource(customPropertiesFile) != null ?
                        customPropertiesFile : defaultPropertiesFile;
        final InputStream propertiesStream = SkillConfig.class.getClassLoader().getResourceAsStream(propertiesFile);
        try {
            properties.load(propertiesStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (propertiesStream != null) {
                try {
                    propertiesStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getAlexaAppId() {
        return properties.getProperty("AlexaAppId");
    }

    public static String getS3BucketUrl() {
        return properties.getProperty("S3BucketUrl");
    }

    public static String getMp3IntroUrl() {
        return getS3BucketUrl() + properties.getProperty("Mp3FileIntro");
    }

    public static String getMp3ColorUrl() {
        return getS3BucketUrl() + properties.getProperty("Mp3FileColor");
    }

    public static String getMp3ShowUrl() {
        return getS3BucketUrl() + properties.getProperty("Mp3FileShow");
    }
}

package portsim.io;

import java.util.Properties;

public final class AppProperties {
    private static final AppProperties INSTANCE = new AppProperties();

    private int numTerminals;

    public static AppProperties getInstance() {
        return INSTANCE;
    }

    private AppProperties() {
        try(var iStream = AppProperties.class.getResourceAsStream("/application.properties")) {
            var properties = new Properties();
            properties.load(iStream);

            numTerminals = Integer.parseInt(properties.getProperty("application.terminals"));
        } catch (Exception e) {
            AppLogger.getInstance().severe("Failed to read the properties file", e);
        }
    }

    public int getNumTerminals() {
        return numTerminals;
    }
}

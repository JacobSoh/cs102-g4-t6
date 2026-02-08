package edu.cs102.g04t06;

/**
 * Provides access to runtime version metadata for the application.
 * Part of the {@code edu.cs102.g04t06} module.
 */
public class SystemInfo {

    /**
     * Creates a SystemInfo helper.
     */
    public SystemInfo() {
        // Utility class; instances are allowed but not required.
    }

    /**
     * Returns the current Java runtime version string.
     *
     * @return the {@code java.version} system property
     */
    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Returns the current JavaFX runtime version string.
     *
     * @return the {@code javafx.version} system property
     */
    public static String javafxVersion() {
        return System.getProperty("javafx.version");
    }

}

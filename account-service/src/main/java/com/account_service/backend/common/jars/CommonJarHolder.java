package com.account_service.backend.common.jars;

/**
 * Placeholder and loader registry for custom/shared JAR dependencies.
 *
 * External enterprise or CBS-proprietary JARs (e.g. CBS connectors, HSM cryptographic
 * libraries, ISO-8583 parsers) can be placed in the project `jars/` folder or imported
 * via custom Maven coordinates and registered here if programmatic initialization is required.
 */
public final class CommonJarHolder {

    private CommonJarHolder() {
        // Prevent instantiation
    }

    public static final String JARS_DIRECTORY = "jars";

    public static String getJarDirectoryPath() {
        return JARS_DIRECTORY;
    }
}

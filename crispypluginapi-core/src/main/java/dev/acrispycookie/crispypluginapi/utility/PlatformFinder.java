package dev.acrispycookie.crispypluginapi.utility;

public class PlatformFinder {

    private static Platform platform = null;

    public static Platform find() {
        if (platform != null)
            return platform;
        try {
            Class<?> spigotClass = Platform.SPIGOT.getClazz();
            platform = Platform.SPIGOT;
        } catch (ClassNotFoundException e) {
            return platform;
        }
        return platform;
    }

    public enum Platform {
        SPIGOT("dev.acrispycookie.crispypluginapi.spigot", "SpigotPluginAPI");

        private final String packageName;
        private final String className;
        Platform(String packageName, String className) {
            this.packageName = packageName;
            this.className = className;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getClassName() {
            return className;
        }

        public Class<?> getClazz() throws ClassNotFoundException {
            return Class.forName(packageName + "." + className);
        }

        public Class<?> getClazz(String packageToAppend, String classNameToAppend) {
            try {
                return Class.forName(packageName + "." + packageToAppend + "." + getName() + classNameToAppend);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        public String getName() {
            return this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase();
        }
    }
}

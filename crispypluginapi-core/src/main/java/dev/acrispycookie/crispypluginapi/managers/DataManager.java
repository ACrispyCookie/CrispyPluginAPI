package dev.acrispycookie.crispypluginapi.managers;

import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;
import dev.acrispycookie.crispypluginapi.features.CrispyFeature;
import dev.acrispycookie.crispypluginapi.features.options.PersistentOption;
import org.hibernate.HibernateError;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

import java.util.HashSet;
import java.util.Set;

public class DataManager extends BaseManager {

    private boolean enabled = false;
    private SessionFactory factory;
    private String tablePrefix;
    private final Set<Class<?>> annotatedClasses = new HashSet<>();

    public DataManager(CrispyPluginAPI api) {
        super(api);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void load() throws ManagerLoadException {
        if (!enabled)
            return;
        if(!api.getManager(ConfigManager.class).hasDefault() || !api.getManager(ConfigManager.class).hasPath(
                api.getManager(ConfigManager.class).getDefault(), "database"))
            return;
        if(isConfigurationDefault())
            throw new ManagerLoadException("Please configure the database settings in the config.yml file and restart the server.");
    }

    public void unload() {
        if (!enabled)
            return;
        factory.close();
        tablePrefix = null;
    }

    @Override
    public void reload() throws ManagerReloadException {
        if (!enabled)
            return;
        try {
            initHibernate();
        } catch (HibernateError e) {
            throw new ManagerReloadException(e, true, true);
        }
    }

    public void registerAnnotated(Class<?> clazz) {
        annotatedClasses.add(clazz);
    }

    public void onAnnotatedRegister() throws ManagerLoadException {
        try {
            initHibernate();
        } catch (Exception e) {
            throw new ManagerLoadException(e);
        }
    }

    public Session newSession() {
        if (!enabled)
            return null;
        return factory.openSession();
    }

    private void initHibernate() throws HibernateError {
        Configuration configuration = new Configuration();
        annotatedClasses.forEach(configuration::addAnnotatedClass);
        String type = getOptionValue(DatabaseOption.TYPE);
        String database = getOptionValue(DatabaseOption.DATABASE);
        configuration.setProperty(AvailableSettings.DIALECT, getHibernateDialect(type));
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_DRIVER, getHibernateDriver(type));
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_URL, getUrl(getOptionValue(DatabaseOption.HOST), getOptionValue(DatabaseOption.PORT), database));
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_USER, getOptionValue(DatabaseOption.USERNAME));
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, getOptionValue(DatabaseOption.PASSWORD));
        configuration.setProperty(AvailableSettings.JAKARTA_HBM2DDL_DB_NAME, database);
        configuration.setProperty(AvailableSettings.HBM2DDL_AUTO, "update");

        FeatureManager featureManager = api.getManager(FeatureManager.class);
        for (CrispyFeature<?, ?, ?, ? extends PersistentOption> feature : featureManager.getEnabledFeatures()) {
            feature.getData().forEach((d) -> configuration.addAnnotatedClass(d.clazz()));
        }

        factory = configuration.buildSessionFactory();
        tablePrefix = getOptionValue(DatabaseOption.TABLE_PREFIX);
    }

    private String getOptionValue(DatabaseOption option) {
        return api.getManager(ConfigManager.class).getString(
                api.getManager(ConfigManager.class).getDefault(),
                "database." + option.getPath()
        );
    }

    private String getHibernateDialect(String option) {
        String packageName = "org.hibernate.dialect.";
        switch (option) {
            case "h2":
                packageName += "H2Dialect";
                break;
            case "mysql":
                packageName += "MySQLDialect";
                break;
            case "postgresql":
                packageName += "PostgreSQLDialect";
                break;
        }
        return packageName;
    }

    private String getHibernateDriver(String option) {
        String packageName = "";
        switch (option) {
            case "h2":
                packageName = "org.h2.Driver";
                break;
            case "mysql":
                packageName = "com.mysql.jdbc.Driver";
                break;
            case "postgresql":
                packageName = "org.postgresql.Driver";
                break;
        }
        return packageName;
    }

    private String getUrl(String host, String port, String database) {
        return "jdbc:mysql://" + host + ":" + port + "/" + database + "?createDatabaseIfNotExist=true&characterEncoding=utf8";
    }

    private boolean isConfigurationDefault() {
        for (DatabaseOption option : DatabaseOption.values()) {
            if (!getOptionValue(option).equals(option.getDefaultValue()))
                return false;
        }
        return true;
    }

    private enum DatabaseOption {
        TYPE("type", "mysql"),
        HOST("host", "test"),
        PORT("port", "3306"),
        DATABASE("database", "test"),
        USERNAME("username", "test"),
        PASSWORD("password", "test"),
        TABLE_PREFIX("table-prefix", "test");

        private final String path;
        private final String defaultValue;

        DatabaseOption(String path, String defaultValue) {
            this.path = path;
            this.defaultValue = defaultValue;
        }

        public String getPath() {
            return path;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }
}

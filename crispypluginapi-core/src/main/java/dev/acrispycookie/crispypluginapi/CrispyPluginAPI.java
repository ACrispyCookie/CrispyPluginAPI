package dev.acrispycookie.crispypluginapi;

import dev.acrispycookie.crispycommons.CommonsSettings;
import dev.acrispycookie.crispycommons.CrispyCommons;
import dev.acrispycookie.crispycommons.logging.CrispyLogger;
import dev.acrispycookie.crispycommons.platform.CrispyPlugin;
import dev.acrispycookie.crispycommons.platform.commands.PlatformCommand;
import dev.acrispycookie.crispycommons.platform.commands.PlatformListener;
import dev.acrispycookie.crispypluginapi.features.CrispyFeature;
import dev.acrispycookie.crispypluginapi.files.YamlFileManager;
import dev.acrispycookie.crispypluginapi.managers.*;
import dev.dejvokep.boostedyaml.settings.Settings;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

public abstract class CrispyPluginAPI {

    protected final CrispyPlugin plugin;
    protected final CrispyCommons commons;
    private long beforeLoading;
    private final SortedMap<ManagerType, List<Class<? extends BaseManager>>> managers;
    private final Map<Class<? extends BaseManager>, BaseManager> managerInstances;
    protected abstract CrispyCommons setupCrispyCommons(CommonsSettings settings);
    public abstract void registerListener(CrispyPlugin plugin, PlatformListener listener);
    public abstract void unregisterListener(PlatformListener listener);
    public abstract boolean isPluginEnabled(String name);

    public CrispyPluginAPI(CrispyPlugin plugin, CommonsSettings settings) {
        this.plugin = plugin;
        this.managers = new TreeMap<>(Enum::compareTo);
        this.managerInstances = new HashMap<>();
        this.commons = setupCrispyCommons(settings);
        initManagers();
    }

    public CrispyPluginAPI setYamlSettings(Settings... settings) {
        YamlFileManager.setSettings(settings);
        return this;
    }

    public CrispyPluginAPI disableConfig() {
        getManager(ConfigManager.class).disableDefault();
        return this;
    }

    public CrispyPluginAPI disableLanguage() {
        getManager(LanguageManager.class).disableDefault();
        return this;
    }

    public CrispyPluginAPI enableDatabase() {
        getManager(SimpleDataManager.class).setEnabled(true);
        return this;
    }

    public CrispyPluginAPI addConfig(ConfigManager.ConfigInfo info) {
        getManager(ConfigManager.class).addConfig(info);
        return this;
    }

    public CrispyPluginAPI addFeature(Class<? extends CrispyFeature<?, ?, ?, ?>> feature) {
        getManager(FeatureManager.class).registerFeature(feature);
        return this;
    }

    public CrispyPluginAPI addManager(ManagerType type, Class<? extends BaseManager> managerClass) {
        try {
            BaseManager instance = managerClass.getConstructor(CrispyPluginAPI.class).newInstance(this);
            managerInstances.put(managerClass, instance);
            managers.get(type).add(managerClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add manager " + managerClass.getSimpleName(), e);
        }
        return this;
    }

    public void start() {
        this.beforeLoading = System.currentTimeMillis();
        getManager(FeatureManager.class).initializeFeatures();

        for (ManagerType type : managers.keySet()) {
            for (Class<? extends BaseManager> manager : managers.get(type)) {
                try {
                    managerInstances.get(manager).load();
                } catch (BaseManager.ManagerLoadException e) {
                    CrispyLogger.printException(plugin, e, "Couldn't load because this manager failed to load: " + manager.getSimpleName());
                    CrispyLogger.log(plugin, Level.SEVERE, "Configure your plugin correctly and restart or contact the developer!");
                    return;
                } catch (Exception e) {
                    CrispyLogger.printException(plugin, e, "A critical error occurred while loading the manager: " + manager.getSimpleName());
                    CrispyLogger.log(plugin, Level.SEVERE, "Please contact your developer!");
                    return;
                }
            }
        }

        CrispyLogger.log(plugin, Level.INFO, "Loaded plugin with " + getManager(FeatureManager.class).getEnabledFeatures().size() + " features enabled! (" + (System.currentTimeMillis() - beforeLoading) + "ms)");
    }

    public void stop() {
        CrispyLogger.log(plugin, Level.INFO, "Goodbye!");
    }

    public <T extends BaseManager> T getManager(Class<T> type) {
        return type.cast(managerInstances.get(type));
    }

    public boolean reload() {
        beforeLoading = System.currentTimeMillis();
        boolean success = true;

        for (ManagerType type : managers.keySet()) {
            for (Class<? extends BaseManager> manager : managers.get(type)) {
                try {
                    managerInstances.get(manager).reload();
                } catch (BaseManager.ManagerReloadException e) {
                    if (e.stopLoading()) {
                        CrispyLogger.printException(plugin, e, "Couldn't reload because this manager failed to reload: " + manager.getSimpleName() + ".");
                        CrispyLogger.log(plugin, Level.SEVERE, "Fix it and restart the server.");
                        return false;
                    }
                    if (e.requiresRestart()) {
                        CrispyLogger.log(plugin, Level.WARNING, "This manager requires restarting: " + manager.getSimpleName());
                        if (success)
                            success = !e.requiresRestart();
                    }
                }
            }
        }

        if(!success)
            CrispyLogger.log(plugin, Level.WARNING, "RESTART REQUIRED! One or more features need restarting after reloading!");
        CrispyLogger.log(plugin, Level.INFO, "Finished reloading plugin with " + getManager(FeatureManager.class).getEnabledFeatures().size() + " features enabled! (" + (System.currentTimeMillis() - beforeLoading) + "ms)");
        return success;
    }

    public CrispyPlugin getPlugin() {
        return plugin;
    }

    public CrispyCommons getCommons() {
        return commons;
    }

    public void registerCommand(CrispyPlugin plugin, String prefix, PlatformCommand command) {
        getCommons().registerCommand(plugin, prefix, command);
    }

    public void unregisterCommand(CrispyPlugin plugin, PlatformCommand command) {
        getCommons().unregisterCommand(plugin, command);
    }

    private void initManagers() {
        Arrays.stream(ManagerType.values()).forEach(m -> {
            try {
                managers.put(m, new ArrayList<>());
                managers.get(m).add(m.getDefaultManager());
                managerInstances.put(m.getDefaultManager(), m.getDefaultManager().getConstructor(CrispyPluginAPI.class).newInstance(this));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // From the highest priority on reloading to the lowest
    public enum ManagerType {
        CONFIG(ConfigManager.class),
        LANGUAGE(LanguageManager.class),
        DATABASE(SimpleDataManager.class),
        FEATURE(FeatureManager.class);

        private Class<? extends BaseManager> defaultManager;
        ManagerType(Class<? extends BaseManager> defaultManager) {
            this.defaultManager = defaultManager;
        }

        public Class<? extends BaseManager> getDefaultManager() {
            return defaultManager;
        }
        private void setDefaultManager(Class<? extends BaseManager> defaultManager) { this.defaultManager = defaultManager; }
    }
}

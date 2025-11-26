package dev.acrispycookie.crispypluginapi.managers;

import dev.acrispycookie.crispycommons.logging.CrispyLogger;
import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;
import dev.acrispycookie.crispypluginapi.features.CrispyFeature;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class FeatureManager extends BaseManager {

    private final Map<String, CrispyFeature<?, ?, ?, ?>> features;
    private final Set<Class<? extends CrispyFeature<?, ?, ?, ?>>> registeredFeatures;

    public FeatureManager(CrispyPluginAPI api) {
        super(api);
        this.features = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.registeredFeatures = new HashSet<>();
    }

    public void load() throws ManagerLoadException {
        for (Class<? extends CrispyFeature<?, ?, ?, ?>> fClass : registeredFeatures) {
            try {
                CrispyFeature<?, ?, ?, ?> feature = (CrispyFeature<?, ?, ?, ?>) fClass.getConstructors()[0].newInstance(api);
                features.put(feature.getName(), feature);
            } catch (Exception e) {
                CrispyLogger.printException(api.getPlugin(), e, "Couldn't load the feature " + fClass.getSimpleName());
            }
        }

        ConfigManager configManager = api.getManager(ConfigManager.class);
        DataManager dataManager = api.getManager(DataManager.class);
        configManager.onSerializableRegister();
        if (dataManager.isEnabled())
            dataManager.onAnnotatedRegister();

        for (CrispyFeature<?, ?, ?, ?> feature : features.values()) {
            if (!feature.load())
                throw new ManagerLoadException("Couldn't load the feature " + feature.getName());
        }
    }

    public void unload() {
        for (CrispyFeature<?, ?, ?, ?> f : features.values()) {
            f.unload();
        }
    }

    @Override
    public void reload() throws ManagerReloadException {
        boolean success = true;
        Set<CrispyFeature<?, ?, ?, ?>> enabledFeatures = features.values().stream().filter(CrispyFeature::isEnabled).collect(Collectors.toSet());
        for (CrispyFeature<?, ?, ?, ?> f : enabledFeatures) {
            try {
                if(!f.reload() && success) {
                    success = false;
                }
            } catch (Exception e) {
                throw new ManagerReloadException(e, true, true);
            }
        }

        if(!success) {
            throw new ManagerReloadException("", true, false);
        }
    }

    public void registerFeature(Class<? extends CrispyFeature<?, ?, ?, ?>> fClass) {
        registeredFeatures.add(fClass);
    }

    public CrispyFeature<?, ?, ?, ?> getFeature(String name) {
        return features.get(name);
    }

    public <T extends CrispyFeature<?, ?, ?, ?>> T getFeature(Class<T> tClass) {
        for (CrispyFeature<?, ?, ?, ?> f : features.values()) {
            if(tClass.equals(f.getClass())) {
                return tClass.cast(f);
            }
        }
        return null;
    }

    public Set<CrispyFeature<?, ?, ?, ?>> getFeatures() {
        return new HashSet<>(features.values());
    }

    public Set<CrispyFeature<?, ?, ?, ?>> getEnabledFeatures() {
        return features.values().stream().filter(CrispyFeature::isEnabled).collect(Collectors.toSet());
    }
}

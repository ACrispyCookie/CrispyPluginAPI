package dev.acrispycookie.crispypluginapi.features;

import dev.acrispycookie.crispycommons.logging.CrispyLogger;
import dev.acrispycookie.crispycommons.platform.player.PlatformCommandSender;
import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;
import dev.acrispycookie.crispypluginapi.features.options.ConfigurationOption;
import dev.acrispycookie.crispypluginapi.features.options.PersistentOption;
import dev.acrispycookie.crispypluginapi.features.options.StringOption;
import dev.acrispycookie.crispypluginapi.managers.ConfigManager;
import dev.acrispycookie.crispypluginapi.managers.DataManager;
import dev.acrispycookie.crispypluginapi.managers.LanguageManager;
import dev.acrispycookie.crispypluginapi.utility.AdapterPair;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

public abstract class CrispyFeature<C extends ConfigurationOption, M extends StringOption, P extends StringOption, D extends PersistentOption> {

    private boolean enabled;
    private boolean loaded = false;
    private final Set<FeatureCommand<?>> commands;
    private final Set<FeatureListener<?>> listeners;
    private static final Set<String> loadedDependencies = new HashSet<>();
    private final CrispyPluginAPI api;
    public abstract String getName();
    protected abstract void onLoad();
    protected abstract boolean onReload();
    protected abstract void onUnload();
    public abstract Set<D> getData();
    public abstract FeatureMessage getMessage(M option);
    protected abstract Set<C> getOptions();
    protected abstract Set<M> getMessages();
    protected abstract Set<P> getPermissions();
    protected abstract Set<String> getDependencies();
    protected abstract Set<? extends FeatureCommand<?>> commandsToLoad();
    protected abstract Set<? extends FeatureListener<?>> listenersToLoad();
    protected abstract Set<AdapterPair<?>> serializableToRegister();

    public CrispyFeature(CrispyPluginAPI api) {
        this.api = api;
        this.enabled = getEnabledOption();
        this.commands = new HashSet<>();
        this.listeners = new HashSet<>();
        serializableToRegister().forEach(AdapterPair::register);
        getData().stream().map(D::clazz).forEach(api.getManager(DataManager.class)::registerAnnotated);
    }

    public boolean load() {
        if (!enabled)
            return true;
        if (loaded)
            return true;
        Set<String> missingDeps = checkForDependencies();
        if(!missingDeps.isEmpty()) {
            CrispyLogger.log(api.getPlugin(), Level.WARNING,
                    "Couldn't load the feature \"" + getName() + "\" because the following dependencies are missing: " + String.join(", ", missingDeps));
            return false;
        }
        onLoad();
        loadedDependencies.addAll(getDependencies());
        loadCommands();
        loadListeners();
        CrispyLogger.log(api.getPlugin(), Level.INFO,
                "Loaded feature \"" + getName() + "\"!");
        loaded = true;
        return true;
    }

    public void unload() {
        if (!loaded)
            return;
        commands.forEach(FeatureCommand::unregister);
        listeners.forEach(FeatureListener::unregister);
        commands.clear();
        listeners.clear();
        onUnload();
        CrispyLogger.log(api.getPlugin(), Level.INFO,
                "Unloaded feature \"" + getName() + "\"!");
        loaded = false;
    }

    public boolean reload() {
        boolean newEnabled = getEnabledOption();
        if (!newEnabled) {
            if (enabled)
                unload();
            setEnabled(false);
            return true;
        }

        if (!enabled) {
            setEnabled(true);
            return load();
        }

        unload();
        if (onReload()) {
            loadCommands();
            loadListeners();
            CrispyLogger.log(api.getPlugin(), Level.INFO, "Feature \"" + getName() + "\" was reloaded successfully");
            loaded = true;
            return true;
        } else {
            unload();
            CrispyLogger.log(api.getPlugin(), Level.INFO, "Feature \"" + getName() + "\" needs a restart to be enabled again!");
            return false;
        }
    }

    public boolean isEnabled() {
        return enabled && loaded;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setEnabledOption(enabled);
    }

    public FeatureOption getOption(C option) {
        return new FeatureOption(option);
    }

    public String getPermission(P option) {
        return api.getManager(ConfigManager.class).getString(getConfig(), "features." + getName() + ".permissions." + option.path());
    }

    public <T> T getData(D option, Class<T> clazz, Object id) {
        return commitDataTransaction(session -> {
            return session.get(clazz, id);
        });
    }

    public boolean commitDataTransaction(Consumer<Session> consumer) {
        return commitDataTransaction(session -> {
            consumer.accept(session);
            return true;
        }) != null;
    }

    public <T> T commitDataTransaction(Function<Session, T> consumer) {
        DataManager manager = api.getManager(DataManager.class);
        Session session = null;
        Transaction transaction = null;
        try {
            session = manager.newSession();
            transaction = session.beginTransaction();
            T toReturn = consumer.apply(session);
            transaction.commit();
            session.close();
            return toReturn;
        } catch (Exception e) {
            CrispyLogger.printException(api.getPlugin(), e, "Couldn't complete a data transaction from the feature: " + getName());
            if (session != null) {
                if (transaction != null)
                    transaction.rollback();
                session.close();
            }
            return null;
        }
    }

    public Set<? extends FeatureCommand<?>> getCommands() {
        return commands;
    }

    public Set<? extends FeatureListener<?>> getListeners() {
        return listeners;
    }

    public CrispyPluginAPI getApi() {
        return api;
    }

    public static Set<String> getLoadedDependencies() {
        return loadedDependencies;
    }

    private void loadCommands() {
        commandsToLoad().forEach(c -> {
            c.register();
            commands.add(c);
        });
    }

    private void loadListeners() {
        listenersToLoad().forEach(c -> {
            c.register();
            listeners.add(c);
        });
    }

    private Set<String> checkForDependencies() {
        Set<String> missing = new HashSet<>();
        for(String dep : getDependencies()) {
            if(!api.isPluginEnabled(dep)) {
                missing.add(dep);
            }
        }
        return missing;
    }

    private boolean getEnabledOption() {
        return api.getManager(ConfigManager.class).getBoolean(
                getConfig(),
                getPath("enabled")
        );
    }

    private void setEnabledOption(boolean enabled) {
        api.getManager(ConfigManager.class).save(
                getConfig(),
                getPath("enabled"),
                enabled
        );
    }

    private <T> T getOptionByString(String path, Class<T> tClass, T def) {
        return api.getManager(ConfigManager.class).getAs(
                getConfig(),
                getPath(path),
                tClass
        );
    }

    private ConfigManager.ConfigInfo getConfig() {
        return api.getManager(ConfigManager.class).getDefault();
    }

    private String getPath(String path) {
        return "features." + getName() + "." + path;
    }

    public abstract class FeatureMessage {
        private final String path;
        public FeatureMessage(String path) {
            this.path = path;
        }

        public Component get() {
            return api.getManager(LanguageManager.class).get(getName() + "." + path);
        }

        public Component get(Map<String, String> placeholders) {
            return api.getManager(LanguageManager.class).get(getName() + "." + path, placeholders);
        }

        public abstract void send(PlatformCommandSender audience);

        public abstract void send(PlatformCommandSender audience, Map<String, String> placeholders);

    }

    public class FeatureOption {
        private final C option;
        public FeatureOption(C option) {
            this.option = option;
        }

        public <T> T as(Class<T> clazz) {
            return as(clazz, null);
        }

        public <T> T as(Class<T> clazz, T def) {
            return getOptionByString(option.path(), clazz, def);
        }

        public Section asSection() {
            return api.getManager(ConfigManager.class).getSection(getConfig(), getPath(option.path()));
        }

        public String asString() {
            return api.getManager(ConfigManager.class).getString(getConfig(), getPath(option.path()));
        }

        public boolean asBoolean() {
            return api.getManager(ConfigManager.class).getBoolean(getConfig(), getPath(option.path()));
        }

        public char asChar() {
            return api.getManager(ConfigManager.class).getChar(getConfig(), getPath(option.path()));
        }

        public byte asByte() {
            return api.getManager(ConfigManager.class).getByte(getConfig(), getPath(option.path()));
        }

        public int asInt() {
            return api.getManager(ConfigManager.class).getInt(getConfig(), getPath(option.path()));
        }

        public short asShort() {
            return api.getManager(ConfigManager.class).getShort(getConfig(), getPath(option.path()));
        }

        public long asLong() {
            return api.getManager(ConfigManager.class).getLong(getConfig(), getPath(option.path()));
        }

        public float asFloat() {
            return api.getManager(ConfigManager.class).getFloat(getConfig(), getPath(option.path()));
        }

        public double asDouble() {
            return api.getManager(ConfigManager.class).getDouble(getConfig(), getPath(option.path()));
        }

        public List<?> asList() {
            return api.getManager(ConfigManager.class).getList(getConfig(), getPath(option.path()));
        }

        public List<String> asStringList() {
            return api.getManager(ConfigManager.class).getStringList(getConfig(), getPath(option.path()));
        }

        public List<Byte> asByteList() {
            return api.getManager(ConfigManager.class).getByteList(getConfig(), getPath(option.path()));
        }

        public List<Integer> asIntList() {
            return api.getManager(ConfigManager.class).getIntList(getConfig(), getPath(option.path()));
        }

        public List<Short> asShortList() {
            return api.getManager(ConfigManager.class).getShortList(getConfig(), getPath(option.path()));
        }

        public List<Long> asLongList() {
            return api.getManager(ConfigManager.class).getLongList(getConfig(), getPath(option.path()));
        }

        public List<Float> asFloatList() {
            return api.getManager(ConfigManager.class).getFloatList(getConfig(), getPath(option.path()));
        }

        public List<Double> asDoubleList() {
            return api.getManager(ConfigManager.class).getDoubleList(getConfig(), getPath(option.path()));
        }

        public List<Map<?, ?>> asMapList() {
            return api.getManager(ConfigManager.class).getMapList(getConfig(), getPath(option.path()));
        }

        public <T> boolean is(Class<T> clazz) {
            return api.getManager(ConfigManager.class).is(getConfig(), getPath(option.path()), clazz);
        }

        public boolean isSection() {
            return api.getManager(ConfigManager.class).isSection(getConfig(), getPath(option.path()));
        }

        public boolean isString() {
            return api.getManager(ConfigManager.class).isString(getConfig(), getPath(option.path()));
        }

        public boolean isBoolean() {
            return api.getManager(ConfigManager.class).isBoolean(getConfig(), getPath(option.path()));
        }

        public boolean isChar() {
            return api.getManager(ConfigManager.class).isChar(getConfig(), getPath(option.path()));
        }

        public boolean isByte() {
            return api.getManager(ConfigManager.class).isByte(getConfig(), getPath(option.path()));
        }

        public boolean isInt() {
            return api.getManager(ConfigManager.class).isInt(getConfig(), getPath(option.path()));
        }

        public boolean isShort() {
            return api.getManager(ConfigManager.class).isShort(getConfig(), getPath(option.path()));
        }

        public boolean isLong() {
            return api.getManager(ConfigManager.class).isLong(getConfig(), getPath(option.path()));
        }

        public boolean isFloat() {
            return api.getManager(ConfigManager.class).isFloat(getConfig(), getPath(option.path()));
        }

        public boolean isDouble() {
            return api.getManager(ConfigManager.class).isDouble(getConfig(), getPath(option.path()));
        }

        public boolean isList() {
            return api.getManager(ConfigManager.class).isList(getConfig(), getPath(option.path()));
        }

        public void save(Object object) {
            api.getManager(ConfigManager.class).save(getConfig(), getPath(option.path()), object);
        }
    }
}

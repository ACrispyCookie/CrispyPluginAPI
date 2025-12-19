package dev.acrispycookie.crispypluginapi.features;

import dev.acrispycookie.crispypluginapi.features.options.ConfigurationOption;
import dev.acrispycookie.crispypluginapi.features.options.PersistentOption;
import dev.acrispycookie.crispypluginapi.features.options.StringOption;
import dev.acrispycookie.crispypluginapi.spigot.SpigotPluginAPI;
import dev.acrispycookie.crispypluginapi.spigot.features.SpigotFeature;

public abstract class SpigotDatabaseFeature<C extends ConfigurationOption, M extends StringOption, P extends StringOption, D extends PersistentOption> extends SpigotFeature<C, M, P, D> implements DatabaseFeature {

    public SpigotDatabaseFeature(SpigotPluginAPI api) {
        super(api);
        initDatabase(getData());
    }

    public <T> T getData(D option, Class<T> clazz, Object id) {
        return commitDataTransaction(session -> {
            return session.get(clazz, id);
        });
    }
}

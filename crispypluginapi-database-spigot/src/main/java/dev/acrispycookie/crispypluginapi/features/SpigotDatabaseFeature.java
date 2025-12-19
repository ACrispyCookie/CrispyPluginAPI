package dev.acrispycookie.crispypluginapi.features;

import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;
import dev.acrispycookie.crispypluginapi.features.options.ConfigurationOption;
import dev.acrispycookie.crispypluginapi.features.options.PersistentOption;
import dev.acrispycookie.crispypluginapi.features.options.StringOption;

public abstract class SpigotDatabaseFeature<C extends ConfigurationOption, M extends StringOption, P extends StringOption, D extends PersistentOption> extends DatabaseFeature<C, M, P, D> {
    public SpigotDatabaseFeature(CrispyPluginAPI api) {
        super(api);
    }
}

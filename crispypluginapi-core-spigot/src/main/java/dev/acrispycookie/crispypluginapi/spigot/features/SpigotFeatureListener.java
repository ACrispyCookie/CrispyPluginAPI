package dev.acrispycookie.crispypluginapi.spigot.features;

import dev.acrispycookie.crispycommons.platform.commands.SpigotListener;
import dev.acrispycookie.crispypluginapi.features.CrispyFeature;
import dev.acrispycookie.crispypluginapi.features.FeatureListener;
import dev.acrispycookie.crispypluginapi.spigot.SpigotPluginAPI;
import org.bukkit.event.Listener;

public class SpigotFeatureListener<T extends CrispyFeature<?, ?, ?, ?>> extends FeatureListener<T> implements Listener {

    public SpigotFeatureListener(T feature, SpigotPluginAPI api) {
        super(feature, api);
    }

    @Override
    protected void register() {
        getApi().registerListener(getApi().getPlugin(), (SpigotListener) () -> this);
    }

    @Override
    protected void unregister() {
        getApi().unregisterListener((SpigotListener) () -> this);
    }

    public SpigotPluginAPI getApi() {
        return (SpigotPluginAPI) super.getApi();
    }
}

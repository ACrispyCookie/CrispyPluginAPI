package dev.acrispycookie.crispypluginapi.spigot;

import dev.acrispycookie.crispycommons.CommonsSettings;
import dev.acrispycookie.crispycommons.CrispyCommons;
import dev.acrispycookie.crispycommons.SpigotCommonsSettings;
import dev.acrispycookie.crispycommons.SpigotCrispyCommons;
import dev.acrispycookie.crispycommons.platform.CrispyPlugin;
import dev.acrispycookie.crispycommons.platform.SpigotCrispyPlugin;
import dev.acrispycookie.crispycommons.platform.commands.PlatformListener;
import dev.acrispycookie.crispycommons.platform.commands.SpigotListener;
import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;
import dev.acrispycookie.crispypluginapi.features.CrispyFeature;
import dev.acrispycookie.crispypluginapi.managers.BaseManager;
import dev.acrispycookie.crispypluginapi.managers.ConfigManager;
import dev.acrispycookie.crispypluginapi.spigot.features.base.BaseFeature;
import dev.dejvokep.boostedyaml.settings.Settings;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotPluginAPI extends CrispyPluginAPI {

    public SpigotPluginAPI(JavaPlugin plugin, SpigotCommonsSettings settings) {
        super((SpigotCrispyPlugin) () -> plugin, settings);
        addFeature(BaseFeature.class);
    }

    public SpigotPluginAPI disableConfig() {
        super.disableConfig();
        return this;
    }

    public SpigotPluginAPI setYamlSettings(Settings... settings) {
        super.setYamlSettings(settings);
        return this;
    }

    public SpigotPluginAPI disableLanguage() {
        super.disableLanguage();
        return this;
    }

    public SpigotPluginAPI enableDatabase() {
        super.enableDatabase();
        return this;
    }

    public SpigotPluginAPI addConfig(ConfigManager.ConfigInfo info) {
        super.addConfig(info);
        return this;
    }

    public SpigotPluginAPI addFeature(Class<? extends CrispyFeature<?, ?, ?, ?>> feature) {
        super.addFeature(feature);
        return this;
    }

    public SpigotPluginAPI addManager(ManagerType manager, Class<? extends BaseManager> type) {
        super.addManager(manager, type);
        return this;
    }

    public SpigotCrispyCommons getCommons() {
        return (SpigotCrispyCommons) commons;
    }

    public SpigotCrispyPlugin getPlugin() {
        return (SpigotCrispyPlugin) plugin;
    }

    @Override
    protected CrispyCommons setupCrispyCommons(CommonsSettings settings) {
        return new SpigotCrispyCommons(getPlugin().getSpigot(), BukkitAudiences.create(((SpigotCrispyPlugin) plugin).getSpigot()), (SpigotCommonsSettings) settings);
    }

    @Override
    public void registerListener(CrispyPlugin plugin, PlatformListener listener) {
        Bukkit.getPluginManager().registerEvents(((SpigotListener) listener).getSpigot(), ((SpigotCrispyPlugin) plugin).getSpigot());
    }

    @Override
    public void unregisterListener(PlatformListener listener) {
        HandlerList.unregisterAll(((SpigotListener) listener).getSpigot());
    }

    @Override
    public boolean isPluginEnabled(String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }
}

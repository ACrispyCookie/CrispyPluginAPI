package dev.acrispycookie.crispypluginapi.spigot.features;

import dev.acrispycookie.crispycommons.platform.player.PlatformCommandSender;
import dev.acrispycookie.crispycommons.platform.player.SpigotCommandSender;
import dev.acrispycookie.crispypluginapi.features.CrispyFeature;
import dev.acrispycookie.crispypluginapi.features.options.ConfigurationOption;
import dev.acrispycookie.crispypluginapi.features.options.PersistentOption;
import dev.acrispycookie.crispypluginapi.features.options.StringOption;
import dev.acrispycookie.crispypluginapi.spigot.SpigotPluginAPI;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SpigotFeature<C extends ConfigurationOption, M extends StringOption, P extends StringOption, D extends PersistentOption> extends CrispyFeature<C, M, P, D> {

    @Override
    protected abstract Set<SpigotFeatureCommand<?>> commandsToLoad();
    @Override
    protected abstract Set<SpigotFeatureListener<?>> listenersToLoad();

    public SpigotFeature(SpigotPluginAPI api) {
        super(api);
    }

    @Override
    public SpigotPluginAPI getApi() {
        return (SpigotPluginAPI) super.getApi();
    }

    @Override
    public Set<SpigotFeatureCommand<?>> getCommands() {
        return super.getCommands()
                .stream()
                .map(c -> (SpigotFeatureCommand<?>) c)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<SpigotFeatureListener<?>> getListeners() {
        return super.getListeners()
                .stream()
                .map(c -> (SpigotFeatureListener<?>) c)
                .collect(Collectors.toSet());
    }

    @Override
    public SpigotFeatureMessage getMessage(M option) {
        return new SpigotFeatureMessage(option.path());
    }

    public class SpigotFeatureMessage extends FeatureMessage {

        public SpigotFeatureMessage(String path) {
            super(path);
        }

        public void send(CommandSender audience) {
            send((SpigotCommandSender) () -> audience);
        }

        public void send(CommandSender audience, Map<String, String> placeholders) {
            send((SpigotCommandSender) () -> audience, placeholders);
        }

        @Override
        public void send(PlatformCommandSender audience) {
            audience.sendMessage(get());
        }

        @Override
        public void send(PlatformCommandSender audience, Map<String, String> placeholders) {
            audience.sendMessage(get(placeholders));
        }
    }
}

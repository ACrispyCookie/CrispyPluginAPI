package dev.acrispycookie.crispypluginapi.spigot.features;

import dev.acrispycookie.crispycommons.platform.commands.SpigotCommand;
import dev.acrispycookie.crispycommons.platform.player.PlatformCommandSender;
import dev.acrispycookie.crispycommons.platform.player.PlatformPlayer;
import dev.acrispycookie.crispycommons.platform.player.SpigotCommandSender;
import dev.acrispycookie.crispycommons.platform.player.SpigotPlayer;
import dev.acrispycookie.crispypluginapi.features.CrispyFeature;
import dev.acrispycookie.crispypluginapi.features.FeatureCommand;
import dev.acrispycookie.crispypluginapi.spigot.SpigotPluginAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class SpigotFeatureCommand<T extends CrispyFeature<?, ?, ?, ?>> extends FeatureCommand<T> {

    private final BukkitCommand command;
    protected abstract boolean runPlayer(Player player, String[] args);
    protected abstract boolean runConsole(CommandSender sender, String[] args);
    protected abstract List<String> getTabOptions(Player sender, String[] args);

    public SpigotFeatureCommand(T feature, SpigotPluginAPI api, String name, String description) {
        super(feature, api, name, description);
        command = new BukkitCommand(name) {
            @Override
            public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
                return SpigotFeatureCommand.this.execute(commandSender instanceof Player ?
                        (SpigotPlayer) () -> (Player) commandSender :
                        (SpigotCommandSender) () -> commandSender, s, strings);
            }

            @Override
            public @NotNull List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String alias, String[] args) {
                return SpigotFeatureCommand.this.tabComplete(commandSender instanceof Player ?
                        (SpigotPlayer) () -> (Player) commandSender :
                        (SpigotCommandSender) () -> commandSender, alias, args);
            }
        };
        command.setDescription(description);
    }

    @Override
    protected void register() {
        getApi().registerCommand(getApi().getPlugin(), getApi().getPlugin().getName(), (SpigotCommand) () -> command);
    }

    @Override
    protected void unregister() {
        getApi().unregisterCommand(getApi().getPlugin(), (SpigotCommand) () -> command);
    }

    @Override
    protected boolean runPlayer(PlatformPlayer player, String[] args) {
        return runPlayer(((SpigotPlayer) player).getSpigot(), args);
    }

    @Override
    protected boolean runConsole(PlatformCommandSender sender, String[] args) {
        return runConsole(((SpigotCommandSender) sender).getSpigot(), args);
    }

    @Override
    protected List<String> getTabOptions(PlatformPlayer sender, String[] args) {
        return getTabOptions(((SpigotPlayer) sender).getSpigot(), args);
    }

    public SpigotPluginAPI getApi() {
        return (SpigotPluginAPI) super.getApi();
    }
}

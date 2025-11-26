package dev.acrispycookie.crispypluginapi.managers;

import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;
import dev.acrispycookie.crispypluginapi.files.YamlFileManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.IOException;
import java.util.Map;

public class LanguageManager extends BaseManager {

    private static final boolean HEX_SUPPORT = false;
    private boolean enabled = true;
    private YamlFileManager yamlManager;

    public LanguageManager(CrispyPluginAPI api) {
        super(api);
    }

    public void load() throws ManagerLoadException {
        if(enabled) {
            try {
                this.yamlManager = new YamlFileManager(api, "lang.yml", "");
            } catch (IOException e) {
                throw new ManagerLoadException(e);
            }
        }
    }

    public void unload() {
        yamlManager = null;
    }

    @Override
    public void reload() throws ManagerReloadException {
        try {
            yamlManager.reload();
        } catch (IOException e) {
            throw new ManagerReloadException(e, true, true);
        }
    }

    public void disableDefault() {
        enabled = false;
    }

    public Component get(String path) {
        if(!yamlManager.get().isSection(path))
            return LegacyComponentSerializer.legacySection().deserialize(translate(buildListString(path)));
        return buildComponent(path);
    }

    public Component get(String path, Map<String, String> placeholders) {
        if(!yamlManager.get().isSection(path))
            return LegacyComponentSerializer.legacySection().deserialize(parsePlaceholders(translate(buildListString(path)), placeholders));
        return buildComponent(path);
    }

    private Component buildComponent(String path) {
        path = addRootPath(path);
        Component component = LegacyComponentSerializer.legacySection().deserialize(translate(buildListString(path + ".text")));
        if (yamlManager.get().contains(path + ".hover-text")) {
            component = component.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(translate(buildListString(path + ".hover-text")))));
        }
        if(yamlManager.get().contains(path + ".click-action") && yamlManager.get().contains(path + ".click-data")) {
            component = component.clickEvent(ClickEvent.clickEvent(
                    ClickEvent.Action.valueOf(yamlManager.get().getString(path + ".click-action")),
                    yamlManager.get().getString(path + ".click-data"))
            );
        }
        return component;
    }

    private String buildListString(String path) {
        path = addRootPath(path);
        if(!yamlManager.get().getStringList(path).isEmpty()) {
            StringBuilder s = new StringBuilder();
            for(String ss : yamlManager.get().getStringList(path)){
                s.append(ss).append("\n");
            }
            s = new StringBuilder(s.substring(0, Math.max(s.length() - 1, s.length())));
            return s.toString();
        } else {
            return yamlManager.get().getString(path);
        }
    }

    private String parsePlaceholders(String msg, Map<String, String> placeholders) {
        for (String placeholder : placeholders.keySet()) {
            msg = msg.replace(placeholder, placeholders.get(placeholder));
        }
        return msg;
    }

    private String translate(String msg) {
        if(!HEX_SUPPORT) {
            return msg.replaceAll("&", "§");
        }
        return msg;
    }

    private String addRootPath(String path) {
        return "features." + path;
    }
}

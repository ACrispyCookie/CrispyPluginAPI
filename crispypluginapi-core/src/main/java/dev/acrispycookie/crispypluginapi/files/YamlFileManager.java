package dev.acrispycookie.crispypluginapi.files;

import dev.acrispycookie.crispycommons.logging.CrispyLogger;
import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;
import dev.acrispycookie.crispypluginapi.managers.DataManager;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class YamlFileManager extends DataFileManager {

    private final YamlDocument yaml;
    private int missingFields;
    private static Settings[] configSettings;

    public YamlFileManager(CrispyPluginAPI api, String name, String directory) throws IOException {
        super(api, name, directory);
        if (configSettings == null)
            yaml = YamlDocument.create(getFile());
        else
            yaml = YamlDocument.create(getFile(), configSettings);
        missingFields = loadDefaultFields() + loadMissingFields();
        if (missingFields != 0)
            CrispyLogger.log(api.getPlugin(), Level.WARNING, "Configuration \"" + name + "\" was missing " + missingFields + " field" + (missingFields != 1 ? "s!" : "!"));
        CrispyLogger.log(api.getPlugin(), Level.INFO, "Loaded configuration \"" + name + "\"!");
    }

    public YamlDocument get() {
        return yaml;
    }

    public void set(Route path, Object value) {
        yaml.set(path, value);
        try {
            yaml.save();
        } catch (IOException e) {
            CrispyLogger.printException(api.getPlugin(), e, "Failed to save the config: " + getName());
        }
    }

    public void set(String path, Object value) {
        yaml.set(path, value);
        try {
            yaml.save();
        } catch (IOException e) {
            CrispyLogger.printException(api.getPlugin(), e, "Failed to save the config: " + getName());
        }
    }

    public void reload() throws IOException {
        super.reload();
        yaml.reload();
        missingFields = loadDefaultFields() + loadMissingFields();
    }

    public int getMissingFields() {
        return missingFields;
    }

    public static void setSettings(Settings... settings) {
        configSettings = settings;
    }

    private int loadMissingFields() {
        try {
            YamlDocument original;
            if (configSettings == null)
                original = YamlDocument.create(getOriginalContent());
            else
                original = YamlDocument.create(getOriginalContent(), configSettings);
            int count = 0;
            for (Route field : original.getRoutes(true)) {
                if (yaml.contains(field))
                    continue;
                set(field, original.get(field));
                ++count;
            }
            return count;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int loadDefaultFields() {
        try {
            InputStream stream = getDefaultContent();
            if (stream == null)
                return 0;
            YamlDocument original;
            if (configSettings == null)
                original = YamlDocument.create(getDefaultContent());
            else
                original = YamlDocument.create(getDefaultContent(), configSettings);
            int count = 0;
            for (String field : original.getRoutesAsStrings(true)) {
                if (yaml.contains(field) || shouldNotLoadDatabase(field))
                    continue;
                set(field, original.get(field));
                ++count;
            }
            return count;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldNotLoadDatabase(String field) {
        return (!api.getManager(DataManager.class).isEnabled() && (field.startsWith("database.") || field.equals("database")));
    }
}

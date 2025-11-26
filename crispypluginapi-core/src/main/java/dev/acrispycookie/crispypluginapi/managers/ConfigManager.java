package dev.acrispycookie.crispypluginapi.managers;

import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;
import dev.acrispycookie.crispypluginapi.files.YamlFileManager;
import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager extends BaseManager {

    private ConfigInfo defaultConfig;
    private final HashMap<ConfigInfo, YamlFileManager> configs;
    private final ArrayList<ConfigInfo> toLoad;

    public ConfigManager(CrispyPluginAPI api) {
        super(api);
        this.configs = new HashMap<>();
        this.toLoad = new ArrayList<>();
        this.defaultConfig = new ConfigInfo("config.yml", "");
        this.toLoad.add(defaultConfig);
    }

    public void load() throws ManagerLoadException {
        for(ConfigInfo info : toLoad) {
            try {
                YamlFileManager manager = new YamlFileManager(api, info.getFile(), info.getDirectory());
                configs.put(info, manager);
            } catch (IOException e) {
                throw new ManagerLoadException(e);
            }
        }
    }

    public void unload() {
        configs.clear();
        toLoad.clear();
        defaultConfig = null;
    }

    @Override
    public void reload() throws ManagerReloadException {
        for (ConfigInfo i : configs.keySet()) {
            try {
                configs.get(i).reload();
            } catch (IOException e) {
                throw new ManagerReloadException(e, true, true);
            }
        }
    }

    public <T> T getAs(ConfigInfo config, String path, Class<T> tClass) throws InvalidTypeException {
        if(!configs.get(config).get().is(path, tClass))
            throw new InvalidTypeException("Value at " + path + " is not of " + tClass.getSimpleName() + ".");
        return configs.get(config).get().getAs(path, tClass);
    }

    public Section getSection(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isSection(path))
            throw new InvalidTypeException("Value at " + path + " is not a section.");
        return configs.get(config).get().getSection(path);
    }

    public String getString(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isString(path))
            throw new InvalidTypeException("Value at " + path + " is not a string.");
        return configs.get(config).get().getString(path);
    }

    public boolean getBoolean(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isBoolean(path))
            throw new InvalidTypeException("Value at " + path + " is not a boolean.");
        return configs.get(config).get().getBoolean(path);
    }

    public char getChar(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isChar(path))
            throw new InvalidTypeException("Value at " + path + " is not a character.");
        return configs.get(config).get().getChar(path);
    }

    public byte getByte(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isByte(path))
            throw new InvalidTypeException("Value at " + path + " is not a byte.");
        return configs.get(config).get().getByte(path);
    }

    public int getInt(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isInt(path))
            throw new InvalidTypeException("Value at " + path + " is not an integer.");
        return configs.get(config).get().getInt(path);
    }

    public short getShort(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isShort(path))
            throw new InvalidTypeException("Value at " + path + " is not a short.");
        return configs.get(config).get().getShort(path);
    }

    public long getLong(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isLong(path))
            throw new InvalidTypeException("Value at " + path + " is not a long integer.");
        return configs.get(config).get().getLong(path);
    }

    public float getFloat(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isFloat(path))
            throw new InvalidTypeException("Value at " + path + " is not a float.");
        return configs.get(config).get().getFloat(path);
    }

    public double getDouble(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isDouble(path))
            throw new InvalidTypeException("Value at " + path + " is not a double.");
        return configs.get(config).get().getDouble(path);
    }

    public List<?> getList(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isList(path))
            throw new InvalidTypeException("Value at " + path + " is not a list.");
        return configs.get(config).get().getList(path);
    }

    public List<String> getStringList(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isList(path))
            throw new InvalidTypeException("Value at " + path + " is not a list.");
        return configs.get(config).get().getStringList(path);
    }

    public List<Byte> getByteList(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isList(path))
            throw new InvalidTypeException("Value at " + path + " is not a list.");
        return configs.get(config).get().getByteList(path);
    }

    public List<Integer> getIntList(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isList(path))
            throw new InvalidTypeException("Value at " + path + " is not a list.");
        return configs.get(config).get().getIntList(path);
    }

    public List<Short> getShortList(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isList(path))
            throw new InvalidTypeException("Value at " + path + " is not a list.");
        return configs.get(config).get().getShortList(path);
    }

    public List<Long> getLongList(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isLong(path))
            throw new InvalidTypeException("Value at " + path + " is not a long integer.");
        return configs.get(config).get().getLongList(path);
    }

    public List<Float> getFloatList(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isList(path))
            throw new InvalidTypeException("Value at " + path + " is not a list.");
        return configs.get(config).get().getFloatList(path);
    }

    public List<Double> getDoubleList(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isList(path))
            throw new InvalidTypeException("Value at " + path + " is not a list.");
        return configs.get(config).get().getDoubleList(path);
    }

    public List<Map<?, ?>> getMapList(ConfigInfo config, String path) throws InvalidTypeException {
        if(!configs.get(config).get().isList(path))
            throw new InvalidTypeException("Value at " + path + " is not a list.");
        return configs.get(config).get().getMapList(path);
    }

    public <T> boolean is(ConfigInfo config, String path, Class<T> tClass) {
        return configs.get(config).get().is(path, tClass);
    }

    public boolean isSection(ConfigInfo config, String path) {
        return configs.get(config).get().isSection(path);
    }

    public boolean isString(ConfigInfo config, String path) {
        return configs.get(config).get().isString(path);
    }

    public boolean isBoolean(ConfigInfo config, String path) {
        return configs.get(config).get().isBoolean(path);
    }

    public boolean isChar(ConfigInfo config, String path) {
        return configs.get(config).get().isChar(path);
    }

    public boolean isByte(ConfigInfo config, String path) {
        return configs.get(config).get().isByte(path);
    }

    public boolean isInt(ConfigInfo config, String path) {
        return configs.get(config).get().isInt(path);
    }

    public boolean isShort(ConfigInfo config, String path) {
        return configs.get(config).get().isShort(path);
    }

    public boolean isLong(ConfigInfo config, String path) {
        return configs.get(config).get().isLong(path);
    }

    public boolean isFloat(ConfigInfo config, String path) {
        return configs.get(config).get().isFloat(path);
    }

    public boolean isDouble(ConfigInfo config, String path) {
        return configs.get(config).get().isDouble(path);
    }

    public boolean isList(ConfigInfo config, String path) {
        return configs.get(config).get().isList(path);
    }

    public void save(ConfigInfo config, String path, Object value) {
        YamlFileManager fileManager = configs.get(config);
        fileManager.set(path, value);
    }

    public boolean hasPath(ConfigInfo config, String path) {
        return configs.get(config).get().contains(path);
    }

    public void addConfig(ConfigInfo info) {
        toLoad.add(info);
    }

    public void onSerializableRegister() {
        try {
            for (YamlFileManager yaml : configs.values()) {
                yaml.get().reload();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disableDefault() {
        this.toLoad.remove(defaultConfig);
        defaultConfig = null;
    }

    public boolean hasDefault() {
        return defaultConfig != null;
    }

    public ConfigInfo getDefault() {
        return defaultConfig;
    }


    public static class ConfigInfo {
        private final String file;
        private final String directory;

        public ConfigInfo(String file, String directory) {
            this.file = file;
            this.directory = directory;
        }

        public String getFile() {
            return file;
        }

        public String getDirectory() {
            return directory;
        }
    }

    public static class InvalidTypeException extends RuntimeException {
        public InvalidTypeException(String errorMessage) {
            super(errorMessage);
        }
    }
}

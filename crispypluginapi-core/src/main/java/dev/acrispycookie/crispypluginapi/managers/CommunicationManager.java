package dev.acrispycookie.crispypluginapi.managers;

import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;

import java.util.HashMap;
import java.util.Map;

public class CommunicationManager extends BaseManager {

    private final Map<String, ReceiverType> receivers = new HashMap<>();

    public CommunicationManager(CrispyPluginAPI api) {
        super(api);
    }

    @Override
    public void load() throws ManagerLoadException {

    }

    @Override
    public void unload() {

    }

    @Override
    public void reload() throws ManagerReloadException {

    }

    enum ReceiverType {
        PROXY,

    }

    enum MessengerType {
        PLUGIN_MESSAGE,
        RABBITMQ;
    }
}

package dev.acrispycookie.crispypluginapi.managers;

import dev.acrispycookie.crispypluginapi.CrispyPluginAPI;

public class SimpleDataManager extends BaseManager {

    private boolean enabled = false;

    public SimpleDataManager(CrispyPluginAPI api) {
        super(api);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void load() throws ManagerLoadException {
        if (!enabled)
            return;
    }

    public void unload() {
        if (!enabled)
            return;
    }

    @Override
    public void reload() throws ManagerReloadException {
        if (!enabled)
            return;
    }
}

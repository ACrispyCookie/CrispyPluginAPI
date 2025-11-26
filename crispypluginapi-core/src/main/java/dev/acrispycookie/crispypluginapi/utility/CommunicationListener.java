package dev.acrispycookie.crispypluginapi.utility;

import com.google.gson.JsonObject;

public abstract class CommunicationListener {

    private final String channel;
    public abstract void onReceive(JsonObject message);

    public CommunicationListener(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }
}

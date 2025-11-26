package dev.acrispycookie.crispypluginapi.utility;

import dev.dejvokep.boostedyaml.serialization.standard.StandardSerializer;
import dev.dejvokep.boostedyaml.serialization.standard.TypeAdapter;

public class AdapterPair<T> {

    private final Class<T> clazz;
    private final TypeAdapter<T> adapter;

    public AdapterPair(Class<T> clazz, TypeAdapter<T> adapter) {
        this.clazz = clazz;
        this.adapter = adapter;
    }

    public void register() {
        StandardSerializer.getDefault().register(clazz, adapter);
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public TypeAdapter<T> getAdapter() {
        return adapter;
    }
}

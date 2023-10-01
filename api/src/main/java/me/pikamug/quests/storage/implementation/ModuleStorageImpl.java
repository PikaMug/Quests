package me.pikamug.quests.storage.implementation;

import me.pikamug.quests.Quests;

import java.io.File;

public interface ModuleStorageImpl {
    Quests getPlugin();

    String getImplementationName();

    void init();

    void close();

    void loadModule(final File jar);
}

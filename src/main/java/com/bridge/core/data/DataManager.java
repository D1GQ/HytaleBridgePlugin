package com.bridge.core.data;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.core.Constants;

@Deprecated
public final class DataManager {
    public static class SaveData {
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static SaveData currentData;
    private static File dataFolder;

    public static void init(@Nonnull Path path) {
        dataFolder = path.toFile();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        if (getFilePath().toFile().exists())
        {
            load();
        }
        else
        {
            currentData = new SaveData();
            save();
        }
    }

    private static void load() {
        try {
            String json = Files.readString(getFilePath());
            var data = gson.fromJson(json, SaveData.class);
            currentData = data;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Path filePath = getFilePath();
            Files.createDirectories(filePath.getParent());
            String json = gson.toJson(currentData);
            Files.writeString(filePath, json);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path getFilePath() {
        return dataFolder.toPath().resolve("data.json");
    }

    public static Path getWorldsPath(@Nonnull String worldName) {
        return Constants.UNIVERSE_PATH.resolve("worlds/" + worldName + "/");
    }
}

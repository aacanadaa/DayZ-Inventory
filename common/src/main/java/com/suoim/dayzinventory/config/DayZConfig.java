/*
 * DayZ Inventory
 * Copyright 2026 suoim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.suoim.dayzinventory.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * The mod's settings, read from {@code config/dayz_inventory.properties}.
 * <p>
 * A plain properties file on purpose: it needs no config library on any of the
 * three loaders, and players can edit it by hand. The file is rewritten on every
 * load so that new options show up with their comments, while every value the
 * player set is kept.
 * <p>
 * Gameplay options are read on whichever side runs the game logic: the server
 * for a dedicated server, the player's own file in singleplayer. Interface
 * options are only ever read on the client.
 */
public final class DayZConfig {
    public static final String FILE_NAME = "dayz_inventory.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger("dayz_inventory");
    private static volatile DayZConfig current = new DayZConfig();

    // Gameplay (server side)
    /** Walk-over item pickup. Off by default: ground items are taken through the Vicinity panel. */
    public boolean autoPickup = false;
    /** Right-clicking a chest or barrel opens it in the DayZ screen instead of the vanilla one. */
    public boolean openContainersInDayZScreen = true;
    /** How far, in blocks, the Vicinity panel reaches for ground items and containers. */
    public int vicinityRadius = 3;

    // Interface (client side)
    /** Pressing the inventory key opens the DayZ screen instead of the vanilla inventory. */
    public boolean replaceInventoryScreen = true;
    /** Active status effects are shown on the Survivor panel. */
    public boolean showStatusEffects = true;
    /** Health, food and (with a supported mod) thirst bars are shown on the Survivor panel. */
    public boolean showVitals = true;
    /** Press on an item and release over another slot to move it there. */
    public boolean dragAndDrop = true;

    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 8;

    /** The active settings. Never null: defaults apply until {@link #load} runs. */
    public static DayZConfig get() {
        return current;
    }

    /**
     * Reads the config file from {@code configDirectory}, then writes it back so
     * the file always lists every option. A missing or unreadable file leaves
     * the defaults in place; this never throws.
     */
    public static void load(Path configDirectory) {
        DayZConfig config = new DayZConfig();
        Path file = configDirectory.resolve(FILE_NAME);
        Properties properties = new Properties();
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.warn("[dayz_inventory] Could not read {}, using defaults", file, e);
            }
        }

        config.autoPickup = bool(properties, "autoPickup", config.autoPickup);
        config.openContainersInDayZScreen = bool(properties, "openContainersInDayZScreen", config.openContainersInDayZScreen);
        config.vicinityRadius = clamp(integer(properties, "vicinityRadius", config.vicinityRadius), MIN_RADIUS, MAX_RADIUS);
        config.replaceInventoryScreen = bool(properties, "replaceInventoryScreen", config.replaceInventoryScreen);
        config.showStatusEffects = bool(properties, "showStatusEffects", config.showStatusEffects);
        config.showVitals = bool(properties, "showVitals", config.showVitals);
        config.dragAndDrop = bool(properties, "dragAndDrop", config.dragAndDrop);

        current = config;
        config.write(file);
    }

    private void write(Path file) {
        String text = String.join("\n",
            "# DayZ Inventory configuration",
            "# Changes take effect the next time the game (or server) starts.",
            "",
            "# --- Gameplay (read by the server; in singleplayer, by this file) ---",
            "",
            "# Pick up items by walking over them, as in vanilla. When false, ground items",
            "# are picked up through the Vicinity panel. Creative players always keep vanilla",
            "# pickup.",
            "autoPickup=" + this.autoPickup,
            "",
            "# Right-clicking a chest or barrel opens it in the DayZ screen. When false,",
            "# the vanilla container screen opens instead.",
            "openContainersInDayZScreen=" + this.openContainersInDayZScreen,
            "",
            "# How far, in blocks, the Vicinity panel reaches for ground items and",
            "# containers (" + MIN_RADIUS + " to " + MAX_RADIUS + "). On a server, set the same value on the server",
            "# and on every client.",
            "vicinityRadius=" + this.vicinityRadius,
            "",
            "# --- Interface (client only) ---",
            "",
            "# Open the DayZ screen with the inventory key. When false, the vanilla",
            "# inventory opens and the DayZ screen is only used for containers.",
            "replaceInventoryScreen=" + this.replaceInventoryScreen,
            "",
            "# Show active status effects on the Survivor panel.",
            "showStatusEffects=" + this.showStatusEffects,
            "",
            "# Show health, food and thirst bars on the Survivor panel. Thirst needs Tough",
            "# As Nails, Thirst Was Taken or Dehydration.",
            "showVitals=" + this.showVitals,
            "",
            "# Press on an item and release it over another slot to move it there.",
            "# Turned off automatically while Mouse Tweaks is installed, which has its own",
            "# drag gestures.",
            "dragAndDrop=" + this.dragAndDrop,
            "");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write(text);
            }
        } catch (IOException e) {
            LOGGER.warn("[dayz_inventory] Could not write {}", file, e);
        }
    }

    private static boolean bool(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        if (value == null) return fallback;
        value = value.trim();
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        LOGGER.warn("[dayz_inventory] {}={} is not true or false, using {}", key, value, fallback);
        return fallback;
    }

    private static int integer(Properties properties, String key, int fallback) {
        String value = properties.getProperty(key);
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            LOGGER.warn("[dayz_inventory] {}={} is not a whole number, using {}", key, value, fallback);
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

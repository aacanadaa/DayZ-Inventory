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
package com.suoim.dayzinventory.client.compat;

import com.suoim.dayzinventory.client.mixin.compat.KeyMappingAccessor;
import com.suoim.dayzinventory.platform.Platform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Optional integrations, all reached without a compile-time dependency.
 * <p>
 * Every call into another mod goes through reflection or a key mapping, never
 * a direct class reference, so the DayZ screen loads and works with none of
 * these mods installed. Any failure is logged once and reported as "did not
 * work", which lets the caller fall back to something that does.
 */
public final class ModCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("dayz_inventory");

    public static final String JEI = "jei";
    public static final String REI = "roughlyenoughitems";
    public static final String EMI = "emi";
    public static final String CURIOS = "curios";
    public static final String TRINKETS = "trinkets";
    public static final String ACCESSORIES = "accessories";
    public static final String MOUSE_TWEAKS = "mousetweaks";

    private static boolean probed;
    private static boolean jei, rei, emi, curios, trinkets, accessories, mouseTweaks;

    private ModCompat() {
    }

    /** Mod presence never changes while the game runs, so it is probed once. */
    private static void probe() {
        if (probed || !Platform.isReady()) {
            return;
        }
        jei = Platform.isModLoaded(JEI);
        rei = Platform.isModLoaded(REI);
        emi = Platform.isModLoaded(EMI);
        curios = Platform.isModLoaded(CURIOS);
        trinkets = Platform.isModLoaded(TRINKETS);
        accessories = Platform.isModLoaded(ACCESSORIES);
        mouseTweaks = Platform.isModLoaded(MOUSE_TWEAKS);
        probed = true;
    }

    public static boolean hasCurios() { probe(); return curios; }
    public static boolean hasTrinkets() { probe(); return trinkets; }
    public static boolean hasAccessories() { probe(); return accessories; }
    public static boolean hasMouseTweaks() { probe(); return mouseTweaks; }

    /** The installed recipe viewer's short name, or null when there is none. */
    public static @Nullable String recipeViewerName() {
        probe();
        if (jei) return "JEI";
        if (rei) return "REI";
        if (emi) return "EMI";
        return null;
    }

    // ------------------------------------------------------------------
    // Recipe viewers
    // ------------------------------------------------------------------

    /**
     * Shows or hides the recipe viewer's item list.
     * <p>
     * Each viewer is asked directly. Simulating its keybind does not work: JEI,
     * REI and EMI read their keys from key events rather than polling a key
     * mapping, and JEI's default binding needs Ctrl held as well.
     */
    public static void toggleRecipeViewer() {
        probe();
        if (jei && invokeOn("mezz.jei.common.Internal", "getClientToggleState",
                "mezz.jei.common.config.IClientToggleState", "toggleOverlayEnabled")) {
            return;
        }
        if (rei && invokeOn("me.shedaniel.rei.api.client.REIRuntime", "getInstance",
                "me.shedaniel.rei.api.client.REIRuntime", "toggleOverlayVisible")) {
            return;
        }
        if (emi) {
            try {
                Class.forName("dev.emi.emi.screen.EmiScreenManager")
                    .getMethod("toggleVisibility", boolean.class)
                    .invoke(null, true);
            } catch (Throwable t) {
                warnOnce("EMI", t);
            }
        }
    }

    /**
     * Calls {@code ownerClass.getter()} and then {@code type.method()} on the
     * result. The second call goes through the public interface type, because
     * the object returned is often a non-public implementation class.
     */
    private static boolean invokeOn(String ownerClass, String getter, String type, String method) {
        try {
            Object target = Class.forName(ownerClass).getMethod(getter).invoke(null);
            if (target == null) return false;
            Method toggle = Class.forName(type).getMethod(method);
            toggle.invoke(target);
            return true;
        } catch (Throwable t) {
            warnOnce(ownerClass, t);
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Key mappings
    // ------------------------------------------------------------------

    /**
     * Registers one click on the first key mapping whose translation key equals
     * one of {@code names}, or failing that contains every word of
     * {@code fallbackWords}. Returns false when no such mapping exists or the
     * click could not be registered.
     */
    public static boolean clickKeyMapping(Minecraft minecraft, String[] names, String... fallbackWords) {
        KeyMapping mapping = findKeyMapping(minecraft, names, fallbackWords);
        if (mapping == null) {
            return false;
        }
        try {
            KeyMappingAccessor accessor = (KeyMappingAccessor) mapping;
            accessor.dayz$setClickCount(accessor.dayz$getClickCount() + 1);
            return true;
        } catch (Throwable t) {
            // The accessor lives in the optional compat mixin config; if that did
            // not apply, fall back rather than fail.
            warnOnce("KeyMappingAccessor", t);
            return false;
        }
    }

    private static @Nullable KeyMapping findKeyMapping(Minecraft minecraft, String[] names, String... fallbackWords) {
        if (minecraft == null || minecraft.options == null) return null;
        for (KeyMapping mapping : minecraft.options.keyMappings) {
            for (String name : names) {
                if (mapping.getName().equals(name)) {
                    return mapping;
                }
            }
        }
        if (fallbackWords.length == 0) return null;
        for (KeyMapping mapping : minecraft.options.keyMappings) {
            String name = mapping.getName().toLowerCase(Locale.ROOT);
            boolean all = true;
            for (String word : fallbackWords) {
                if (!name.contains(word)) {
                    all = false;
                    break;
                }
            }
            if (all) {
                return mapping;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------

    private static final java.util.Set<String> warned = new java.util.HashSet<>();

    static void warnOnce(String what, Throwable t) {
        if (warned.add(what)) {
            LOGGER.warn("[dayz_inventory] Integration with {} failed; using the fallback", what, t);
        }
    }
}

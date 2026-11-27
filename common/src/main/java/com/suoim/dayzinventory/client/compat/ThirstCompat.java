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

import com.suoim.dayzinventory.platform.Platform;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reads the player's thirst from whichever supported thirst mod is installed,
 * so the Survivor panel can show it next to health and food.
 * <p>
 * Supported, each through its own public entry point and by reflection only:
 * <ul>
 *   <li><b>Tough As Nails</b> - {@code toughasnails.api.thirst.ThirstHelper.getThirst(Player).getThirst()}</li>
 *   <li><b>Thirst Was Taken</b> (Forge) - the {@code PLAYER_THIRST} capability</li>
 *   <li><b>Dehydration</b> (Fabric) - {@code ThirstManagerAccess.getThirstManager().getThirstLevel()}</li>
 * </ul>
 * Every value is on vanilla's food scale, 0 to 20. If a mod changes its API the
 * lookup fails closed: the thirst bar is simply not shown.
 */
public final class ThirstCompat {
    public static final int MAX = 20;

    private interface Source {
        int read(Player player) throws Exception;
    }

    private static boolean resolved;
    private static Source source;

    private ThirstCompat() {
    }

    /** The player's thirst from 0 to {@link #MAX}, or -1 when no supported mod provides one. */
    public static int thirst(Player player) {
        if (!resolved) {
            source = resolve();
            resolved = true;
        }
        if (source == null || player == null) {
            return -1;
        }
        try {
            int value = source.read(player);
            return value < 0 ? -1 : Math.min(MAX, value);
        } catch (Throwable t) {
            ModCompat.warnOnce("thirst", t);
            source = null;
            return -1;
        }
    }

    private static Source resolve() {
        if (Platform.isModLoaded("toughasnails")) {
            Source tan = toughAsNails();
            if (tan != null) return tan;
        }
        if (Platform.isModLoaded("thirst")) {
            Source twt = thirstWasTaken();
            if (twt != null) return twt;
        }
        if (Platform.isModLoaded("dehydration")) {
            Source dehydration = dehydration();
            if (dehydration != null) return dehydration;
        }
        return null;
    }

    private static Source toughAsNails() {
        try {
            Class<?> helper = Class.forName("toughasnails.api.thirst.ThirstHelper");
            Method getThirst = helper.getMethod("getThirst", Player.class);
            Method level = Class.forName("toughasnails.api.thirst.IThirst").getMethod("getThirst");
            Method enabled = findMethod(helper, "isThirstEnabled");
            return player -> {
                if (enabled != null && !(Boolean) enabled.invoke(null)) return -1;
                Object thirst = getThirst.invoke(null, player);
                return thirst == null ? -1 : (Integer) level.invoke(thirst);
            };
        } catch (Throwable t) {
            ModCompat.warnOnce("Tough As Nails", t);
            return null;
        }
    }

    private static Source thirstWasTaken() {
        try {
            Field capabilityField = Class.forName("dev.ghen.thirst.foundation.common.capability.ModCapabilities")
                .getField("PLAYER_THIRST");
            Object capability = capabilityField.get(null);
            Class<?> capabilityType = Class.forName("net.minecraftforge.common.capabilities.Capability");
            Method level = Class.forName("dev.ghen.thirst.foundation.common.capability.IThirst").getMethod("getThirst");
            return player -> {
                Method getCapability = player.getClass().getMethod("getCapability", capabilityType);
                Object lazy = getCapability.invoke(player, capability);
                Optional<?> present = (Optional<?>) lazy.getClass().getMethod("resolve").invoke(lazy);
                return present.isPresent() ? (Integer) level.invoke(present.get()) : -1;
            };
        } catch (Throwable t) {
            ModCompat.warnOnce("Thirst Was Taken", t);
            return null;
        }
    }

    private static Source dehydration() {
        try {
            Class<?> access = Class.forName("net.dehydration.access.ThirstManagerAccess");
            Method manager = access.getMethod("getThirstManager");
            Class<?> managerType = Class.forName("net.dehydration.thirst.ThirstManager");
            Method level = managerType.getMethod("getThirstLevel");
            Method hasThirst = findMethod(managerType, "hasThirst");
            return player -> {
                if (!access.isInstance(player)) return -1;
                Object thirstManager = manager.invoke(player);
                if (thirstManager == null) return -1;
                if (hasThirst != null && !(Boolean) hasThirst.invoke(thirstManager)) return -1;
                return (Integer) level.invoke(thirstManager);
            };
        } catch (Throwable t) {
            ModCompat.warnOnce("Dehydration", t);
            return null;
        }
    }

    private static Method findMethod(Class<?> owner, String name) {
        try {
            return owner.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}

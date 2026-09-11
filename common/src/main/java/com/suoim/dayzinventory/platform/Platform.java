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
package com.suoim.dayzinventory.platform;

public class Platform {
    /**
     * The active loader implementation, installed by the loader entrypoint:
     * Fabric's {@code ModInitializer} or Forge's mod constructor.
     * <p>
     * It is {@code null} until that runs, so never dereference it directly from
     * code that could execute earlier (static initialisers, mixins that fire
     * during bootstrap). Use {@link #isModLoaded(String)} for capability probes.
     */
    public static IPlatformHelper HELPER = null;

    /**
     * Set while the Curios/Trinkets buttons deliberately let the vanilla
     * inventory screen open instead of the DayZ one.
     */
    public static boolean allowVanillaInventory = false;

    /**
     * Null-safe mod-presence probe.
     * <p>
     * JEI, REI, EMI, Trinkets and Curios are all optional. They are probed from
     * render and click handlers, and a missing optional dependency must never be
     * able to crash the screen. If a probe somehow runs before the loader has
     * installed its helper, this reports "not loaded" rather than throwing.
     */
    public static boolean isModLoaded(String modId) {
        IPlatformHelper helper = HELPER;
        return helper != null && helper.isModLoaded(modId);
    }

    /** True once the loader has installed its {@link IPlatformHelper}. */
    public static boolean isReady() {
        return HELPER != null;
    }
}

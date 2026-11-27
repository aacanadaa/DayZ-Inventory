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
package com.suoim.dayzinventory.client.mixin.compat;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * "Presses" one specific key mapping.
 * <p>
 * Curios and Accessories open their screens when their key mapping reports a
 * click ({@code consumeClick()} on the next client tick). Raising that mapping's
 * own click count is the one way to trigger exactly that mapping:
 * {@code KeyMapping.click(key)} goes through the key lookup instead, which skips
 * mappings whose conflict context is inactive while a screen is open and only
 * ever reaches one of several mappings sharing a key - which is why the Curios
 * button often did nothing.
 * <p>
 * This replaces reading the bound key by reflection, which broke in production:
 * field names are obfuscated there, so the lookup either failed or returned the
 * default binding instead of the player's.
 */
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    @Accessor("clickCount")
    int dayz$getClickCount();

    @Accessor("clickCount")
    void dayz$setClickCount(int clickCount);
}

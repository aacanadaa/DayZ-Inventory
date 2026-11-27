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

import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Everything the shared code needs from a mod loader. Each loader module
 * installs its implementation into {@link Platform#HELPER} first thing during
 * mod construction.
 * <p>
 * Keep this interface small: game logic belongs in the shared code, and only
 * the calls that genuinely differ per loader belong here.
 */
public interface IPlatformHelper {
    boolean isModLoaded(String modId);

    /** The loader's config directory, where {@code dayz_inventory.properties} lives. */
    Path getConfigDirectory();

    void sendPacketToServer(Identifier packetId, FriendlyByteBuf buf);

    /**
     * Client side only: whether the connected server has this mod. When it does
     * not, the DayZ screen cannot be opened (the server would never answer), so
     * the vanilla inventory is left alone.
     */
    boolean isServerModPresent();

    /**
     * Opens the DayZ menu for {@code player}: their own inventory when
     * {@code container} is null, otherwise that container from {@code pos}.
     */
    void openMenu(ServerPlayer player, @Nullable Container container, @Nullable BlockPos pos, Component title);

    MenuType<DayZInventoryScreenHandler> getScreenHandlerType();
}

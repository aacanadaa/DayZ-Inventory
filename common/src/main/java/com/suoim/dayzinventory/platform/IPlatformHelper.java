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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;

public interface IPlatformHelper {
    boolean isModLoaded(String modId);
    
    void sendPacketToServer(Identifier packetId, FriendlyByteBuf buf);
    
    void openPlayerInventory(ServerPlayer player);
    
    void openContainerInventory(ServerPlayer player, BlockPos pos);
    
    MenuType<DayZInventoryScreenHandler> getScreenHandlerType();
}

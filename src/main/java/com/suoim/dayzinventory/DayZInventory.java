package com.suoim.dayzinventory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DayZInventory implements ModInitializer {
	public static final String MOD_ID = "dayz-inventory";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static MenuType<DayZInventoryScreenHandler> DAYZ_INVENTORY_SCREEN_HANDLER;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing DayZ Inventory Mod...");

		// Register the custom screen handler MenuType
		DAYZ_INVENTORY_SCREEN_HANDLER = Registry.register(
				BuiltInRegistries.MENU,
				id("dayz_inventory"),
				new ExtendedScreenHandlerType<>(DayZInventoryScreenHandler::new)
		);

		// Register Server-Side Packet Receivers
		DayZInventoryPackets.registerServerReceivers();
	}

	public static void openPlayerInventory(ServerPlayer player) {
		player.openMenu(new ExtendedScreenHandlerFactory() {
			@Override
			public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
				buf.writeBoolean(false); // No container
				buf.writeBoolean(false); // No container pos
			}

			@Override
			public Component getDisplayName() {
				return Component.translatable("container.inventory");
			}

			@Override
			public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
				return new DayZInventoryScreenHandler(syncId, playerInventory, null, null);
			}
		});
	}

	public static void openContainerInventory(ServerPlayer player, BlockPos pos) {
		Level level = player.level();
		BlockState state = level.getBlockState(pos);
		Container container = null;
		if (state.getBlock() instanceof ChestBlock chestBlock) {
			container = ChestBlock.getContainer(chestBlock, state, level, pos, true);
		} else if (level.getBlockEntity(pos) instanceof Container c) {
			container = c;
		}

		if (container != null) {
			final Container finalContainer = container;
			player.openMenu(new ExtendedScreenHandlerFactory() {
				@Override
				public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
					buf.writeBoolean(true); // Has container
					buf.writeInt(finalContainer.getContainerSize());
					buf.writeBoolean(true); // Has container pos
					buf.writeBlockPos(pos);
				}

				@Override
				public Component getDisplayName() {
					return state.getBlock().getName();
				}

				@Override
				public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
					return new DayZInventoryScreenHandler(syncId, playerInventory, finalContainer, pos);
				}
			});
		}
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}


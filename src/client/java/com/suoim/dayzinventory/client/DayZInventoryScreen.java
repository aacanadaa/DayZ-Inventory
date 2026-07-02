package com.suoim.dayzinventory.client;

import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.DayZInventoryPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DayZInventoryScreen extends AbstractContainerScreen<DayZInventoryScreenHandler> {
    private int scanTicks = 0;
    private final List<ItemEntity> vicinityItems = new ArrayList<>();
    private double scrollAmount = 0.0;

    // Custom dragging state for vicinity items
    private ItemEntity draggedEntity = null;
    private ItemStack draggedStack = null;

    public DayZInventoryScreen(DayZInventoryScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 540;
        this.imageHeight = 230;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.updateVicinityList();
        this.updateSlotPositions();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.minecraft != null && this.minecraft.level != null && this.minecraft.player != null) {
            scanTicks++;
            if (scanTicks % 10 == 0) {
                scanTicks = 0;
                updateVicinityList();
            }
        }
    }

    private void updateVicinityList() {
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) return;
        
        double radius = 3.0;
        List<ItemEntity> entities = this.minecraft.level.getEntitiesOfClass(
                ItemEntity.class,
                this.minecraft.player.getBoundingBox().inflate(radius),
                entity -> entity.isAlive()
        );
        
        this.vicinityItems.clear();
        this.vicinityItems.addAll(entities);
    }

    private int getScrollContentHeight() {
        int groundSectionHeight = 5 + 15 + (vicinityItems.isEmpty() ? 15 : vicinityItems.size() * 22);
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        if (containerSize > 0) {
            int rows = (int) Math.ceil(containerSize / 9.0);
            return groundSectionHeight + 25 + rows * 18;
        }
        return groundSectionHeight;
    }

    private void updateSlotPositions() {
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        int groundSectionHeight = 5 + 15 + (vicinityItems.isEmpty() ? 15 : vicinityItems.size() * 22);
        
        // Clamp scroll amount
        int contentHeight = getScrollContentHeight();
        double maxScroll = Math.max(0, contentHeight - 190);
        if (this.scrollAmount > maxScroll) {
            this.scrollAmount = maxScroll;
        }
        if (this.scrollAmount < 0.0) {
            this.scrollAmount = 0.0;
        }

        // 1. Position Container Slots
        for (int i = 0; i < containerSize; i++) {
            Slot slot = this.menu.slots.get(i);
            int relY = groundSectionHeight + 25 + (i / 9) * 18;
            int relX = 9 + (i % 9) * 18;
            
            int slotX = leftPos + relX;
            int slotY = topPos + 25 + relY - (int) this.scrollAmount;

            // Viewport is Y range [topPos + 25, topPos + 215]
            if (slotY >= topPos + 25 && slotY + 18 <= topPos + 215) {
                slot.x = relX;
                slot.y = 25 + relY - (int) this.scrollAmount;
            } else {
                slot.x = -2000;
                slot.y = -2000;
            }
        }

        // 2. Position Player Inventory Slots (27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = containerSize + col + row * 9;
                Slot slot = this.menu.slots.get(index);
                slot.x = 369 + col * 18;
                slot.y = 30 + row * 18;
            }
        }

        // 3. Position Player Hotbar Slots (9 slots)
        for (int col = 0; col < 9; col++) {
            int index = containerSize + 27 + col;
            Slot slot = this.menu.slots.get(index);
            slot.x = 369 + col * 18;
            slot.y = 180;
        }

        // 4. Position Armor Slots (Helmet, Chestplate, Leggings, Boots)
        Slot helmetSlot = this.menu.slots.get(containerSize + 36);
        helmetSlot.x = 195;
        helmetSlot.y = 35;

        Slot chestSlot = this.menu.slots.get(containerSize + 37);
        chestSlot.x = 195;
        chestSlot.y = 60;

        Slot legsSlot = this.menu.slots.get(containerSize + 38);
        legsSlot.x = 195;
        legsSlot.y = 85;

        Slot bootsSlot = this.menu.slots.get(containerSize + 39);
        bootsSlot.x = 195;
        bootsSlot.y = 110;

        // 5. Position Offhand Slot
        Slot offhandSlot = this.menu.slots.get(containerSize + 40);
        offhandSlot.x = 327;
        offhandSlot.y = 35;
    }

    private boolean isMouseOverVicinity(double mouseX, double mouseY) {
        return mouseX >= leftPos + 5 && mouseX <= leftPos + 180 && mouseY >= topPos + 25 && mouseY <= topPos + 215;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (isMouseOverVicinity(mouseX, mouseY)) {
            this.scrollAmount = Math.max(0.0, this.scrollAmount - amount * 12.0);
            this.updateSlotPositions();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverVicinity(mouseX, mouseY)) {
            int startY = topPos + 25;
            double clickRelY = mouseY - startY + scrollAmount;
            int relY = 5 + 15; // Ground Items title height offset
            
            if (!vicinityItems.isEmpty()) {
                for (ItemEntity itemEntity : vicinityItems) {
                    int rowY = startY + relY - (int) scrollAmount;
                    if (mouseY >= rowY && mouseY <= rowY + 20 && rowY >= startY && rowY + 20 <= topPos + 215) {
                        if (mouseX >= leftPos + 9 && mouseX <= leftPos + 171) {
                            if (Screen.hasShiftDown()) {
                                // Quick pickup
                                FriendlyByteBuf buf = PacketByteBufs.create();
                                buf.writeInt(itemEntity.getId());
                                ClientPlayNetworking.send(DayZInventoryPackets.QUICK_PICKUP_ITEM_PACKET, buf);
                            } else {
                                // Start dragging
                                this.draggedEntity = itemEntity;
                                this.draggedStack = itemEntity.getItem().copy();
                            }
                            return true;
                        }
                    }
                    relY += 22;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.draggedStack != null) {
            Slot hoveredSlot = this.getSlotAt(mouseX, mouseY);
            if (hoveredSlot != null) {
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(this.draggedEntity.getId());
                buf.writeInt(hoveredSlot.index);
                buf.writeInt(this.draggedStack.getCount());
                ClientPlayNetworking.send(DayZInventoryPackets.PICKUP_ITEM_PACKET, buf);
            } else {
                // Check if released over "Hands" slot
                int handsSlotX = leftPos + 261;
                int handsSlotY = topPos + 175;
                if (mouseX >= handsSlotX && mouseX <= handsSlotX + 18 && mouseY >= handsSlotY && mouseY <= handsSlotY + 18) {
                    if (this.minecraft != null && this.minecraft.player != null) {
                        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
                        int activeSlotIdx = containerSize + 27 + this.minecraft.player.getInventory().selected;
                        FriendlyByteBuf buf = PacketByteBufs.create();
                        buf.writeInt(this.draggedEntity.getId());
                        buf.writeInt(activeSlotIdx);
                        buf.writeInt(this.draggedStack.getCount());
                        ClientPlayNetworking.send(DayZInventoryPackets.PICKUP_ITEM_PACKET, buf);
                    }
                }
            }
            this.draggedEntity = null;
            this.draggedStack = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private Slot getSlotAt(double mouseX, double mouseY) {
        for (Slot slot : this.menu.slots) {
            if (slot.x >= 0 && mouseX >= (leftPos + slot.x) && mouseX < (leftPos + slot.x + 18) 
                    && mouseY >= (topPos + slot.y) && mouseY < (topPos + slot.y + 18)) {
                return slot;
            }
        }
        return null;
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        if (isMouseOverVicinity(mouseX, mouseY)) {
            return true;
        }
        return mouseX < left || mouseY < top || mouseX > left + imageWidth || mouseY > top + imageHeight;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.updateSlotPositions();
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Render custom dragged stack on cursor
        if (this.draggedStack != null && !this.draggedStack.isEmpty()) {
            guiGraphics.renderFakeItem(this.draggedStack, mouseX - 8, mouseY - 8);
            guiGraphics.renderItemDecorations(this.font, this.draggedStack, mouseX - 8, mouseY - 8);
        }

        // Render tooltip for vicinity items
        if (this.draggedStack == null && isMouseOverVicinity(mouseX, mouseY)) {
            int startY = topPos + 25;
            int relY = 5 + 15;
            if (!vicinityItems.isEmpty()) {
                for (ItemEntity itemEntity : vicinityItems) {
                    int rowY = startY + relY - (int) scrollAmount;
                    if (mouseY >= rowY && mouseY <= rowY + 20 && rowY >= startY && rowY + 20 <= topPos + 215) {
                        if (mouseX >= leftPos + 9 && mouseX <= leftPos + 171) {
                            guiGraphics.renderTooltip(this.font, itemEntity.getItem(), mouseX, mouseY);
                            break;
                        }
                    }
                    relY += 22;
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Disable default label rendering (we render custom headers in renderBg)
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 1. Dark screen background overlay
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xD8050505);

        // 2. Sleek column dividing lines
        guiGraphics.fill(leftPos + 180, topPos + 5, leftPos + 181, topPos + imageHeight - 5, 0x40FFFFFF);
        guiGraphics.fill(leftPos + 360, topPos + 5, leftPos + 361, topPos + imageHeight - 5, 0x40FFFFFF);

        // 3. Column Headers
        guiGraphics.fill(leftPos + 5, topPos + 5, leftPos + 175, topPos + 22, 0xFF1C1C1C);
        guiGraphics.drawString(this.font, "VICINITY", leftPos + 10, topPos + 9, 0xFFFFFFFF, false);

        guiGraphics.fill(leftPos + 185, topPos + 5, leftPos + 355, topPos + 22, 0xFF1C1C1C);
        guiGraphics.drawString(this.font, "SURVIVOR", leftPos + 190, topPos + 9, 0xFFFFFFFF, false);

        guiGraphics.fill(leftPos + 365, topPos + 5, leftPos + 535, topPos + 22, 0xFF9E0B0B);
        guiGraphics.drawString(this.font, "INVENTORY", leftPos + 370, topPos + 9, 0xFFFFFFFF, false);

        // 4. Hands Mirror Slot
        guiGraphics.drawString(this.font, "HANDS", leftPos + 258, topPos + 162, 0xAAAAAAFF, false);
        guiGraphics.fill(leftPos + 261, topPos + 175, leftPos + 279, topPos + 193, 0x80101010);
        drawSlotBorder(guiGraphics, leftPos + 261, topPos + 175);

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack handsStack = this.minecraft.player.getMainHandItem();
            if (!handsStack.isEmpty()) {
                guiGraphics.renderFakeItem(handsStack, leftPos + 262, topPos + 176);
                guiGraphics.renderItemDecorations(this.font, handsStack, leftPos + 262, topPos + 176);
            }
            
            // Draw 3D Player entity
            InventoryScreen.renderEntityInInventory(
                guiGraphics,
                leftPos + 270,
                topPos + 150,
                45,
                (float) (leftPos + 270 - mouseX),
                (float) (topPos + 80 - mouseY),
                this.minecraft.player
            );
        }

        // 5. Draw Vicinity List (Ground Items + Container slots titles)
        renderVicinityList(guiGraphics, mouseX, mouseY);

        // 6. Draw Scrollbar
        int contentHeight = getScrollContentHeight();
        if (contentHeight > 190) {
            int maxScroll = contentHeight - 190;
            int barHeight = Math.max(20, (190 * 190) / contentHeight);
            int barTop = topPos + 25 + (int) ((scrollAmount * (190 - barHeight)) / maxScroll);
            guiGraphics.fill(leftPos + 176, topPos + 25, leftPos + 178, topPos + 215, 0xFF151515); // background
            guiGraphics.fill(leftPos + 176, barTop, leftPos + 178, barTop + barHeight, 0xFF555555); // thumb
        }
    }

    private void renderVicinityList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startY = topPos + 25;
        int endY = topPos + 215;

        guiGraphics.enableScissor(leftPos + 5, startY, leftPos + 175, endY);

        int relY = 5;

        // Draw Ground Items header
        guiGraphics.drawString(this.font, "Ground Items", leftPos + 9, startY + relY - (int) scrollAmount, 0x88FFFFFF, false);
        relY += 15;

        if (vicinityItems.isEmpty()) {
            guiGraphics.drawString(this.font, "No items nearby", leftPos + 9, startY + relY - (int) scrollAmount, 0x44FFFFFF, false);
            relY += 15;
        } else {
            for (ItemEntity itemEntity : vicinityItems) {
                int rowY = startY + relY - (int) scrollAmount;

                if (rowY + 20 >= startY && rowY <= endY) {
                    ItemStack stack = itemEntity.getItem();

                    boolean hovering = mouseX >= leftPos + 9 && mouseX <= leftPos + 171 && mouseY >= rowY && mouseY <= rowY + 20;
                    int bgColor = hovering ? 0xFF2C2C2C : 0xFF1C1C1C;
                    guiGraphics.fill(leftPos + 9, rowY, leftPos + 171, rowY + 20, bgColor);

                    // Draw border manually
                    guiGraphics.fill(leftPos + 9, rowY, leftPos + 171, rowY + 1, 0xFF333333); // top
                    guiGraphics.fill(leftPos + 9, rowY + 19, leftPos + 171, rowY + 20, 0xFF333333); // bottom
                    guiGraphics.fill(leftPos + 9, rowY, leftPos + 10, rowY + 20, 0xFF333333); // left
                    guiGraphics.fill(leftPos + 170, rowY, leftPos + 171, rowY + 20, 0xFF333333); // right

                    guiGraphics.renderFakeItem(stack, leftPos + 11, rowY + 2);
                    guiGraphics.renderItemDecorations(this.font, stack, leftPos + 11, rowY + 2);

                    String name = stack.getHoverName().getString();
                    if (name.length() > 18) {
                        name = name.substring(0, 16) + "...";
                    }
                    guiGraphics.drawString(this.font, name, leftPos + 32, rowY + 6, 0xFFFFFFFF, false);
                }
                relY += 22;
            }
        }

        // Draw Container header
        if (this.menu.getContainerInventory() != null) {
            relY += 10;
            guiGraphics.drawString(this.font, "Container", leftPos + 9, startY + relY - (int) scrollAmount, 0x88FFFFFF, false);
        }

        guiGraphics.disableScissor();
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot.x < 0) return; // Hidden slot

        // Draw custom slot background and border
        guiGraphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, 0x80101010);
        drawSlotBorder(guiGraphics, leftPos + slot.x - 1, topPos + slot.y - 1);

        super.renderSlot(guiGraphics, slot);
    }

    private void drawSlotBorder(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 1, 0xFF333333); // top
        guiGraphics.fill(x, y + 17, x + 18, y + 18, 0xFF333333); // bottom
        guiGraphics.fill(x, y, x + 1, y + 18, 0xFF333333); // left
        guiGraphics.fill(x + 17, y, x + 18, y + 18, 0xFF333333); // right
    }
}

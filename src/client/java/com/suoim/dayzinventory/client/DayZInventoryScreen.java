package com.suoim.dayzinventory.client;

import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.DayZInventoryPackets;
import com.suoim.dayzinventory.mixin.SlotAccessor;
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
import org.joml.Quaternionf;

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
        // Dynamically scale based on window size
        this.imageWidth = Math.max(500, Math.min(560, this.width - 20));
        this.imageHeight = Math.max(220, Math.min(230, this.height - 20));
        
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

    private int getColumnX(int colIndex) {
        int remaining = this.imageWidth - 486; // 486 is 3 * 162 (standard column widths)
        int gap = remaining / 4;
        return leftPos + gap + colIndex * (162 + gap);
    }

    private int getScrollContentHeight() {
        int groundSectionHeight = 5 + 15 + (vicinityItems.isEmpty() ? 15 : (int) Math.ceil(vicinityItems.size() / 9.0) * 18);
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        if (containerSize > 0) {
            int rows = (int) Math.ceil(containerSize / 9.0);
            return groundSectionHeight + 25 + rows * 18;
        }
        return groundSectionHeight;
    }

    private void updateSlotPositions() {
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        int groundSectionHeight = 5 + 15 + (vicinityItems.isEmpty() ? 15 : (int) Math.ceil(vicinityItems.size() / 9.0) * 18);
        
        int viewportHeight = this.imageHeight - 40;
        int contentHeight = getScrollContentHeight();
        double maxScroll = Math.max(0, contentHeight - viewportHeight);
        if (this.scrollAmount > maxScroll) {
            this.scrollAmount = maxScroll;
        }
        if (this.scrollAmount < 0.0) {
            this.scrollAmount = 0.0;
        }

        int leftColumnX = getColumnX(0);
        int middleColumnX = getColumnX(1);
        int rightColumnX = getColumnX(2);

        // 1. Position Container Slots
        for (int i = 0; i < containerSize; i++) {
            Slot slot = this.menu.slots.get(i);
            int relY = groundSectionHeight + 25 + (i / 9) * 18;
            int relX = (i % 9) * 18;
            
            int slotX = leftColumnX + relX;
            int slotY = topPos + 25 + relY - (int) this.scrollAmount;

            SlotAccessor accessor = (SlotAccessor) slot;
            // Viewport is Y range [topPos + 25, topPos + 25 + viewportHeight]
            if (slotY >= topPos + 25 && slotY + 18 <= topPos + 25 + viewportHeight) {
                accessor.setX(slotX - leftPos);
                accessor.setY(25 + relY - (int) this.scrollAmount);
            } else {
                accessor.setX(-2000);
                accessor.setY(-2000);
            }
        }

        // 2. Position Player Inventory Slots (27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = containerSize + col + row * 9;
                Slot slot = this.menu.slots.get(index);
                SlotAccessor accessor = (SlotAccessor) slot;
                accessor.setX(rightColumnX + col * 18 - leftPos);
                accessor.setY(30 + row * 18);
            }
        }

        // 3. Position Player Hotbar Slots (9 slots)
        for (int col = 0; col < 9; col++) {
            int index = containerSize + 27 + col;
            Slot slot = this.menu.slots.get(index);
            SlotAccessor accessor = (SlotAccessor) slot;
            accessor.setX(rightColumnX + col * 18 - leftPos);
            accessor.setY(imageHeight - 30);
        }

        // 4. Position Armor Slots (Helmet, Chestplate, Leggings, Boots)
        Slot helmetSlot = this.menu.slots.get(containerSize + 36);
        ((SlotAccessor) helmetSlot).setX(middleColumnX + 15 - leftPos);
        ((SlotAccessor) helmetSlot).setY(35);

        Slot chestSlot = this.menu.slots.get(containerSize + 37);
        ((SlotAccessor) chestSlot).setX(middleColumnX + 15 - leftPos);
        ((SlotAccessor) chestSlot).setY(60);

        Slot legsSlot = this.menu.slots.get(containerSize + 38);
        ((SlotAccessor) legsSlot).setX(middleColumnX + 15 - leftPos);
        ((SlotAccessor) legsSlot).setY(85);

        Slot bootsSlot = this.menu.slots.get(containerSize + 39);
        ((SlotAccessor) bootsSlot).setX(middleColumnX + 15 - leftPos);
        ((SlotAccessor) bootsSlot).setY(110);

        // 5. Position Offhand Slot
        Slot offhandSlot = this.menu.slots.get(containerSize + 40);
        ((SlotAccessor) offhandSlot).setX(middleColumnX + 129 - leftPos);
        ((SlotAccessor) offhandSlot).setY(35);
    }

    private boolean isMouseOverVicinity(double mouseX, double mouseY) {
        int leftColumnX = getColumnX(0);
        int viewportHeight = this.imageHeight - 40;
        return mouseX >= leftColumnX && mouseX <= leftColumnX + 162 && mouseY >= topPos + 25 && mouseY <= topPos + 25 + viewportHeight;
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
            int leftColumnX = getColumnX(0);
            int startY = topPos + 25;
            double clickX = mouseX - leftColumnX;
            double clickY = mouseY - startY + scrollAmount;
            int relY = 5 + 15; // Ground Items title height offset
            
            if (!vicinityItems.isEmpty()) {
                int rows = (int) Math.ceil(vicinityItems.size() / 9.0);
                int groundGridHeight = rows * 18;
                if (clickY >= relY && clickY < relY + groundGridHeight) {
                    int col = (int) (clickX / 18);
                    int row = (int) ((clickY - relY) / 18);
                    int index = row * 9 + col;
                    if (index >= 0 && index < vicinityItems.size()) {
                        ItemEntity itemEntity = vicinityItems.get(index);
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
                int middleColumnX = getColumnX(1);
                int handsSlotX = middleColumnX + 72;
                int handsSlotY = topPos + imageHeight - 50;
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
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
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
            int leftColumnX = getColumnX(0);
            int startY = topPos + 25;
            double clickX = mouseX - leftColumnX;
            double clickY = mouseY - startY + scrollAmount;
            int relY = 5 + 15;
            if (!vicinityItems.isEmpty()) {
                int rows = (int) Math.ceil(vicinityItems.size() / 9.0);
                int groundGridHeight = rows * 18;
                if (clickY >= relY && clickY < relY + groundGridHeight) {
                    int col = (int) (clickX / 18);
                    int row = (int) ((clickY - relY) / 18);
                    int index = row * 9 + col;
                    if (index >= 0 && index < vicinityItems.size()) {
                        guiGraphics.renderTooltip(this.font, vicinityItems.get(index).getItem(), mouseX, mouseY);
                    }
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
        int leftColumnX = getColumnX(0);
        int middleColumnX = getColumnX(1);
        int rightColumnX = getColumnX(2);

        // 1. Dark screen background overlay
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xD8050505);

        // 2. Sleek column dividing lines
        int line1X = (leftColumnX + 162 + middleColumnX) / 2;
        int line2X = (middleColumnX + 162 + rightColumnX) / 2;
        guiGraphics.fill(line1X, topPos + 5, line1X + 1, topPos + imageHeight - 5, 0x40FFFFFF);
        guiGraphics.fill(line2X, topPos + 5, line2X + 1, topPos + imageHeight - 5, 0x40FFFFFF);

        // 3. Column Headers
        guiGraphics.fill(leftColumnX - 4, topPos + 5, leftColumnX + 162 + 4, topPos + 22, 0xFF1C1C1C);
        guiGraphics.drawString(this.font, "VICINITY", leftColumnX, topPos + 9, 0xFFFFFFFF, false);

        guiGraphics.fill(middleColumnX - 4, topPos + 5, middleColumnX + 162 + 4, topPos + 22, 0xFF1C1C1C);
        guiGraphics.drawString(this.font, "SURVIVOR", middleColumnX, topPos + 9, 0xFFFFFFFF, false);

        guiGraphics.fill(rightColumnX - 4, topPos + 5, rightColumnX + 162 + 4, topPos + 22, 0xFF9E0B0B);
        guiGraphics.drawString(this.font, "INVENTORY", rightColumnX, topPos + 9, 0xFFFFFFFF, false);

        // 4. Hands Mirror Slot
        int handsSlotX = middleColumnX + 72;
        int handsSlotY = topPos + imageHeight - 50;

        guiGraphics.drawString(this.font, "HANDS", middleColumnX + 66, handsSlotY - 12, 0xAAAAAAFF, false);
        guiGraphics.fill(handsSlotX, handsSlotY, handsSlotX + 18, handsSlotY + 18, 0x80101010);
        drawSlotBorder(guiGraphics, handsSlotX, handsSlotY);

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack handsStack = this.minecraft.player.getMainHandItem();
            if (!handsStack.isEmpty()) {
                guiGraphics.renderFakeItem(handsStack, handsSlotX + 1, handsSlotY + 1);
                guiGraphics.renderItemDecorations(this.font, handsStack, handsSlotX + 1, handsSlotY + 1);
            }
            
            // Draw 3D Player entity
            int renderX = middleColumnX + 81;
            int renderY = topPos + imageHeight - 75;
            float f = (float) Math.atan((double) ((renderX - mouseX) / 40.0F));
            float g = (float) Math.atan((double) ((topPos + 80 - mouseY) / 40.0F));
            Quaternionf pose = (new Quaternionf()).rotateZ((float) Math.PI);
            Quaternionf cameraPose = (new Quaternionf()).rotateX(g * 20.0F * ((float) Math.PI / 180.0F));
            pose.mul(cameraPose);

            float backupBodyRot = this.minecraft.player.yBodyRot;
            float backupYRot = this.minecraft.player.getYRot();
            float backupXRot = this.minecraft.player.getXRot();
            float backupHeadRotO = this.minecraft.player.yHeadRotO;
            float backupHeadRot = this.minecraft.player.yHeadRot;

            this.minecraft.player.yBodyRot = 180.0F + f * 20.0F;
            this.minecraft.player.setYRot(180.0F + f * 40.0F);
            this.minecraft.player.setXRot(-g * 20.0F);
            this.minecraft.player.yHeadRot = this.minecraft.player.getYRot();
            this.minecraft.player.yHeadRotO = this.minecraft.player.getYRot();

            InventoryScreen.renderEntityInInventory(
                guiGraphics,
                renderX,
                renderY,
                (int) (imageHeight * 0.2), // Auto scales scale factor
                pose,
                cameraPose,
                this.minecraft.player
            );

            this.minecraft.player.yBodyRot = backupBodyRot;
            this.minecraft.player.setYRot(backupYRot);
            this.minecraft.player.setXRot(backupXRot);
            this.minecraft.player.yHeadRotO = backupHeadRotO;
            this.minecraft.player.yHeadRot = backupHeadRot;
        }

        // 5. Draw Slot Backgrounds & Borders behind visible slots
        for (Slot slot : this.menu.slots) {
            if (slot.x >= 0) {
                guiGraphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, 0x80101010);
                drawSlotBorder(guiGraphics, leftPos + slot.x - 1, topPos + slot.y - 1);
            }
        }

        // 6. Draw Vicinity List (Ground Items + Container slots titles)
        renderVicinityList(guiGraphics, mouseX, mouseY);

        // 7. Draw Scrollbar
        int contentHeight = getScrollContentHeight();
        int viewportHeight = imageHeight - 40;
        if (contentHeight > viewportHeight) {
            int maxScroll = contentHeight - viewportHeight;
            int barHeight = Math.max(20, (viewportHeight * viewportHeight) / contentHeight);
            int barTop = topPos + 25 + (int) ((scrollAmount * (viewportHeight - barHeight)) / maxScroll);
            guiGraphics.fill(leftColumnX + 163, topPos + 25, leftColumnX + 165, topPos + 25 + viewportHeight, 0xFF151515); // background
            guiGraphics.fill(leftColumnX + 163, barTop, leftColumnX + 165, barTop + barHeight, 0xFF555555); // thumb
        }
    }

    private void renderVicinityList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int leftColumnX = getColumnX(0);
        int startY = topPos + 25;
        int viewportHeight = this.imageHeight - 40;
        int endY = startY + viewportHeight;

        guiGraphics.enableScissor(leftColumnX, startY, leftColumnX + 162, endY);

        int relY = 5;

        // Draw Ground Items header
        guiGraphics.drawString(this.font, "Ground Items", leftColumnX + 4, startY + relY - (int) scrollAmount, 0x88FFFFFF, false);
        relY += 15;

        if (vicinityItems.isEmpty()) {
            guiGraphics.drawString(this.font, "No items nearby", leftColumnX + 4, startY + relY - (int) scrollAmount, 0x44FFFFFF, false);
            relY += 15;
        } else {
            for (int i = 0; i < vicinityItems.size(); i++) {
                ItemEntity itemEntity = vicinityItems.get(i);
                int col = i % 9;
                int row = i / 9;
                int relX = col * 18;
                int rowRelY = relY + row * 18;
                
                int itemX = leftColumnX + relX;
                int itemY = startY + rowRelY - (int) scrollAmount;

                if (itemY + 18 >= startY && itemY <= endY) {
                    ItemStack stack = itemEntity.getItem();

                    boolean hovering = mouseX >= itemX && mouseX < itemX + 18 && mouseY >= itemY && mouseY <= itemY + 18;
                    int bgColor = hovering ? 0x40FFFFFF : 0x80101010;
                    guiGraphics.fill(itemX, itemY, itemX + 18, itemY + 18, bgColor);
                    drawSlotBorder(guiGraphics, itemX, itemY);

                    guiGraphics.renderFakeItem(stack, itemX + 1, itemY + 1);
                    guiGraphics.renderItemDecorations(this.font, stack, itemX + 1, itemY + 1);
                }
            }
            int rows = (int) Math.ceil(vicinityItems.size() / 9.0);
            relY += rows * 18;
        }

        // Draw Container header
        if (this.menu.getContainerInventory() != null) {
            relY += 10;
            guiGraphics.drawString(this.font, "Container", leftColumnX + 4, startY + relY - (int) scrollAmount, 0x88FFFFFF, false);
        }

        guiGraphics.disableScissor();
    }

    private void drawSlotBorder(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 1, 0xFF333333); // top
        guiGraphics.fill(x, y + 17, x + 18, y + 18, 0xFF333333); // bottom
        guiGraphics.fill(x, y, x + 1, y + 18, 0xFF333333); // left
        guiGraphics.fill(x + 17, y, x + 18, y + 18, 0xFF333333); // right
    }
}

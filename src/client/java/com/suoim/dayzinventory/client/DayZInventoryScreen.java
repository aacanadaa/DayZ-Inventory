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
import net.minecraft.sounds.SoundEvents;
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

    // Custom slot dragging state
    private Slot draggedSlot = null;

    // Nearby containers list
    public static class ContainerBlockInfo {
        public final net.minecraft.core.BlockPos pos;
        public final String name;
        public ContainerBlockInfo(net.minecraft.core.BlockPos pos, String name) {
            this.pos = pos;
            this.name = name;
        }
    }
    private final List<ContainerBlockInfo> nearbyContainers = new ArrayList<>();

    public DayZInventoryScreen(DayZInventoryScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 540;
        this.imageHeight = 220;
    }

    private float getGuiScale() {
        float scaleX = (float) this.width / 540.0f;
        float scaleY = (float) this.height / 220.0f;
        return Math.min(1.0f, Math.min(scaleX, scaleY));
    }

    @Override
    protected void init() {
        super.init();
        
        float scale = getGuiScale();
        this.leftPos = (int) (((this.width / scale) - this.imageWidth) / 2);
        this.topPos = (int) (((this.height / scale) - this.imageHeight) / 2);
        
        this.updateVicinityList();
        this.updateNearbyContainers();
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
                updateNearbyContainers();
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

    private void updateNearbyContainers() {
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) return;
        
        net.minecraft.core.BlockPos playerPos = this.minecraft.player.blockPosition();
        this.nearbyContainers.clear();
        
        int radius = 3;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    net.minecraft.core.BlockPos pos = playerPos.offset(x, y, z);
                    net.minecraft.world.level.block.state.BlockState state = this.minecraft.level.getBlockState(pos);
                    
                    boolean isContainer = false;
                    if (state.getBlock() instanceof net.minecraft.world.level.block.ChestBlock || 
                        state.getBlock() instanceof net.minecraft.world.level.block.BarrelBlock || 
                        state.getBlock() instanceof net.minecraft.world.level.block.ShulkerBoxBlock) {
                        isContainer = true;
                    } else {
                        net.minecraft.world.level.block.entity.BlockEntity be = this.minecraft.level.getBlockEntity(pos);
                        if (be instanceof net.minecraft.world.Container) {
                            isContainer = true;
                        }
                    }
                    
                    if (isContainer) {
                        String displayName = state.getBlock().getName().getString();
                        this.nearbyContainers.add(new ContainerBlockInfo(pos, displayName));
                    }
                }
            }
        }
    }

    private int getColumnX(int colIndex) {
        int remaining = this.imageWidth - 486; // 486 is 3 * 162
        int gap = remaining / 4;
        return leftPos + gap + colIndex * (162 + gap);
    }

    private int getScrollContentHeight() {
        int height = 5 + 15 + (vicinityItems.isEmpty() ? 15 : (int) Math.ceil(vicinityItems.size() / 9.0) * 18);
        
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        if (containerSize > 0) {
            int rows = (int) Math.ceil(containerSize / 9.0);
            height += 25 + rows * 18;
        }

        if (!nearbyContainers.isEmpty()) {
            height += 25 + nearbyContainers.size() * 18;
        }
        
        return height;
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
        float scale = getGuiScale();
        double scaledX = mouseX / scale;
        double scaledY = mouseY / scale;
        
        if (isMouseOverVicinity(scaledX, scaledY)) {
            this.scrollAmount = Math.max(0.0, this.scrollAmount - amount * 12.0);
            this.updateSlotPositions();
            return true;
        }
        return super.mouseScrolled(scaledX, scaledY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scale = getGuiScale();
        double scaledX = mouseX / scale;
        double scaledY = mouseY / scale;

        // Store dragged slot for click-hold-drag-release UX
        Slot hoveredSlot = this.getSlotAt(scaledX, scaledY);
        if (hoveredSlot != null && !hoveredSlot.getItem().isEmpty()) {
            this.draggedSlot = hoveredSlot;
        }

        if (isMouseOverVicinity(scaledX, scaledY)) {
            int leftColumnX = getColumnX(0);
            int startY = topPos + 25;
            double clickX = scaledX - leftColumnX;
            double clickY = scaledY - startY + scrollAmount;
            
            // 1. Ground items
            int relY = 5 + 15;
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
                            FriendlyByteBuf buf = PacketByteBufs.create();
                            buf.writeInt(itemEntity.getId());
                            ClientPlayNetworking.send(DayZInventoryPackets.QUICK_PICKUP_ITEM_PACKET, buf);
                        } else {
                            this.draggedEntity = itemEntity;
                            this.draggedStack = itemEntity.getItem().copy();
                        }
                        return true;
                    }
                }
                relY += groundGridHeight;
            }
            
            // 2. Container slots
            int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
            if (containerSize > 0) {
                int rows = (int) Math.ceil(containerSize / 9.0);
                relY += 25 + rows * 18;
            }
            
            // 3. Nearby Storage Buttons
            if (!nearbyContainers.isEmpty()) {
                relY += 25; // Skip header
                for (int i = 0; i < nearbyContainers.size(); i++) {
                    int btnY = relY + i * 18;
                    if (clickY >= btnY && clickY < btnY + 16) {
                        ContainerBlockInfo container = nearbyContainers.get(i);
                        this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        FriendlyByteBuf buf = PacketByteBufs.create();
                        buf.writeBlockPos(container.pos);
                        ClientPlayNetworking.send(DayZInventoryPackets.OPEN_CONTAINER_PACKET, buf);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(scaledX, scaledY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        float scale = getGuiScale();
        double scaledX = mouseX / scale;
        double scaledY = mouseY / scale;

        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        int middleColumnX = getColumnX(1);
        boolean releasedOverSurvivor = scaledX >= (middleColumnX - 4) && scaledX <= (middleColumnX + 166) 
                && scaledY >= (topPos + 25) && scaledY <= (topPos + imageHeight - 5);

        // Custom drag release for vicinity items
        if (this.draggedStack != null) {
            Slot hoveredSlot = this.getSlotAt(scaledX, scaledY);
            if (hoveredSlot != null) {
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(this.draggedEntity.getId());
                buf.writeInt(hoveredSlot.index);
                buf.writeInt(this.draggedStack.getCount());
                ClientPlayNetworking.send(DayZInventoryPackets.PICKUP_ITEM_PACKET, buf);
            } else if (releasedOverSurvivor) {
                // Auto equip ground item if equippable
                net.minecraft.world.entity.EquipmentSlot equipSlot = net.minecraft.world.entity.LivingEntity.getEquipmentSlotForItem(this.draggedStack);
                int targetSlotIdx = -1;
                if (equipSlot == net.minecraft.world.entity.EquipmentSlot.HEAD) {
                    targetSlotIdx = containerSize + 36;
                } else if (equipSlot == net.minecraft.world.entity.EquipmentSlot.CHEST) {
                    targetSlotIdx = containerSize + 37;
                } else if (equipSlot == net.minecraft.world.entity.EquipmentSlot.LEGS) {
                    targetSlotIdx = containerSize + 38;
                } else if (equipSlot == net.minecraft.world.entity.EquipmentSlot.FEET) {
                    targetSlotIdx = containerSize + 39;
                } else if (equipSlot == net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
                    targetSlotIdx = containerSize + 40;
                }
                
                if (targetSlotIdx != -1) {
                    FriendlyByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(this.draggedEntity.getId());
                    buf.writeInt(targetSlotIdx);
                    buf.writeInt(this.draggedStack.getCount());
                    ClientPlayNetworking.send(DayZInventoryPackets.PICKUP_ITEM_PACKET, buf);
                }
            } else {
                int handsSlotX = middleColumnX + 72;
                int handsSlotY = topPos + imageHeight - 50;
                if (scaledX >= handsSlotX && scaledX <= handsSlotX + 18 && scaledY >= handsSlotY && scaledY <= handsSlotY + 18) {
                    if (this.minecraft != null && this.minecraft.player != null) {
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

        // Custom slot drag release (true drag-and-drop UX)
        if (this.draggedSlot != null) {
            Slot releaseSlot = this.getSlotAt(scaledX, scaledY);
            if (releaseSlot != null && releaseSlot != this.draggedSlot) {
                this.slotClicked(releaseSlot, releaseSlot.index, 0, net.minecraft.world.inventory.ClickType.PICKUP);
                this.draggedSlot = null;
                return true;
            } else if (releasedOverSurvivor) {
                // Dragged onto survivor panel -> auto equip if equippable
                ItemStack draggedItem = this.draggedSlot.getItem();
                net.minecraft.world.entity.EquipmentSlot equipSlot = net.minecraft.world.entity.LivingEntity.getEquipmentSlotForItem(draggedItem);
                int targetSlotIdx = -1;
                if (equipSlot == net.minecraft.world.entity.EquipmentSlot.HEAD) {
                    targetSlotIdx = containerSize + 36;
                } else if (equipSlot == net.minecraft.world.entity.EquipmentSlot.CHEST) {
                    targetSlotIdx = containerSize + 37;
                } else if (equipSlot == net.minecraft.world.entity.EquipmentSlot.LEGS) {
                    targetSlotIdx = containerSize + 38;
                } else if (equipSlot == net.minecraft.world.entity.EquipmentSlot.FEET) {
                    targetSlotIdx = containerSize + 39;
                } else if (equipSlot == net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
                    targetSlotIdx = containerSize + 40;
                }
                
                if (targetSlotIdx != -1) {
                    Slot armorSlot = this.menu.slots.get(targetSlotIdx);
                    this.slotClicked(armorSlot, targetSlotIdx, 0, net.minecraft.world.inventory.ClickType.PICKUP);
                    this.draggedSlot = null;
                    return true;
                }
            } else if (scaledX < leftPos || scaledY < topPos || scaledX > leftPos + imageWidth || scaledY > topPos + imageHeight) {
                // Drop item outside bounds
                this.slotClicked(null, -999, 0, net.minecraft.world.inventory.ClickType.PICKUP);
                this.draggedSlot = null;
                return true;
            }
            this.draggedSlot = null;
        }

        return super.mouseReleased(scaledX, scaledY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        float scale = getGuiScale();
        return super.mouseDragged(mouseX / scale, mouseY / scale, button, dragX / scale, dragY / scale);
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
        if (getSlotAt(mouseX, mouseY) != null) {
            return false;
        }
        if (isMouseOverVicinity(mouseX, mouseY)) {
            return true;
        }
        return mouseX < left || mouseY < top || mouseX > left + imageWidth || mouseY > top + imageHeight;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw the background dim overlay at full physical 1x scale
        this.renderBackground(guiGraphics);

        float scale = getGuiScale();
        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);

        // Push pose and apply scale
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);

        this.updateSlotPositions();
        super.render(guiGraphics, scaledMouseX, scaledMouseY, partialTick);
        this.renderTooltip(guiGraphics, scaledMouseX, scaledMouseY);

        // Render custom dragged stack on cursor
        if (this.draggedStack != null && !this.draggedStack.isEmpty()) {
            guiGraphics.renderFakeItem(this.draggedStack, scaledMouseX - 8, scaledMouseY - 8);
            guiGraphics.renderItemDecorations(this.font, this.draggedStack, scaledMouseX - 8, scaledMouseY - 8);
        }

        // Render tooltip for vicinity items
        if (this.draggedStack == null && isMouseOverVicinity(scaledMouseX, scaledMouseY)) {
            int leftColumnX = getColumnX(0);
            int startY = topPos + 25;
            double clickX = scaledMouseX - leftColumnX;
            double clickY = scaledMouseY - startY + scrollAmount;
            int relY = 5 + 15;
            if (!vicinityItems.isEmpty()) {
                int rows = (int) Math.ceil(vicinityItems.size() / 9.0);
                int groundGridHeight = rows * 18;
                if (clickY >= relY && clickY < relY + groundGridHeight) {
                    int col = (int) (clickX / 18);
                    int row = (int) ((clickY - relY) / 18);
                    int index = row * 9 + col;
                    if (index >= 0 && index < vicinityItems.size()) {
                        guiGraphics.renderTooltip(this.font, vicinityItems.get(index).getItem(), scaledMouseX, scaledMouseY);
                    }
                }
            }
        }
        
        guiGraphics.pose().popPose();
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
        int viewportHeight = imageHeight - 40;

        // 1. Draw Section Panels (separate translucent dark rectangles with outlines)
        
        // Left Column (Vicinity)
        drawSectionPanel(guiGraphics, leftColumnX - 4, topPos + 5, 170, 17); // Header
        drawSectionPanel(guiGraphics, leftColumnX - 4, topPos + 25, 170, viewportHeight); // Content
        
        // Middle Column (Survivor)
        drawSectionPanel(guiGraphics, middleColumnX - 4, topPos + 5, 170, 17); // Header
        drawSectionPanel(guiGraphics, middleColumnX + 11, topPos + 31, 26, 101); // Armor
        drawSectionPanel(guiGraphics, middleColumnX + 125, topPos + 31, 26, 26); // Offhand
        
        // Right Column (Inventory)
        drawSectionPanel(guiGraphics, rightColumnX - 4, topPos + 5, 170, 17); // Header
        drawSectionPanel(guiGraphics, rightColumnX - 4, topPos + 26, 170, 62); // Main inventory
        drawSectionPanel(guiGraphics, rightColumnX - 4, topPos + imageHeight - 34, 170, 26); // Hotbar

        // 2. Draw Column Header Texts centered with shadows
        int leftTextX = leftColumnX + (162 - this.font.width("VICINITY")) / 2;
        guiGraphics.drawString(this.font, "VICINITY", leftTextX, topPos + 9, 0xFFFFFFFF, true);

        int middleTextX = middleColumnX + (162 - this.font.width("SURVIVOR")) / 2;
        guiGraphics.drawString(this.font, "SURVIVOR", middleTextX, topPos + 9, 0xFFFFFFFF, true);

        int rightTextX = rightColumnX + (162 - this.font.width("INVENTORY")) / 2;
        guiGraphics.drawString(this.font, "INVENTORY", rightTextX, topPos + 9, 0xFFFFFFFF, true);

        // 3. Hands Mirror Slot & Panel
        int handsSlotX = middleColumnX + 72;
        int handsSlotY = topPos + imageHeight - 50;

        drawSectionPanel(guiGraphics, handsSlotX - 4, handsSlotY - 4, 26, 26);
        guiGraphics.drawString(this.font, "HANDS", handsSlotX + 9 - (this.font.width("HANDS") / 2), handsSlotY - 11, 0xFFDFDFDF, false);
        drawDayZSlot(guiGraphics, handsSlotX, handsSlotY);

        if (this.minecraft != null && this.minecraft.player != null) {
            ItemStack handsStack = this.minecraft.player.getMainHandItem();
            if (!handsStack.isEmpty()) {
                guiGraphics.renderFakeItem(handsStack, handsSlotX + 1, handsSlotY + 1);
                guiGraphics.renderItemDecorations(this.font, handsStack, handsSlotX + 1, handsSlotY + 1);
            }
            
            // Draw 3D Player entity (larger scale, centered and placed higher)
            int renderX = middleColumnX + 81;
            int renderY = topPos + 135;
            int renderScale = 55;
            
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
                renderScale,
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

        // 4. Draw Modern DayZ-style Slot Backgrounds & Borders behind visible slots
        for (Slot slot : this.menu.slots) {
            if (slot.x >= 0) {
                drawDayZSlot(guiGraphics, leftPos + slot.x - 1, topPos + slot.y - 1);
            }
        }

        // 5. Draw Vicinity List (Ground Items + Container slots titles + Nearby Storages)
        float scale = getGuiScale();
        renderVicinityList(guiGraphics, mouseX, mouseY, scale);

        // 6. Draw Scrollbar
        int contentHeight = getScrollContentHeight();
        if (contentHeight > viewportHeight) {
            int maxScroll = contentHeight - viewportHeight;
            int barHeight = Math.max(20, (viewportHeight * viewportHeight) / contentHeight);
            int barTop = topPos + 25 + (int) ((scrollAmount * (viewportHeight - barHeight)) / maxScroll);
            guiGraphics.fill(leftColumnX + 163, topPos + 25, leftColumnX + 165, topPos + 25 + viewportHeight, 0xFF373737); // track
            guiGraphics.fill(leftColumnX + 163, barTop, leftColumnX + 165, barTop + barHeight, 0xFFFFFFFF); // active thumb
        }
    }

    private void renderVicinityList(GuiGraphics guiGraphics, int mouseX, int mouseY, float scale) {
        int leftColumnX = getColumnX(0);
        int startY = topPos + 25;
        int viewportHeight = this.imageHeight - 40;
        int endY = startY + viewportHeight;

        // Apply scaling factor to scissor box to ensure clipping alignment on scaled displays
        guiGraphics.enableScissor(
                (int) ((leftColumnX - 4) * scale), 
                (int) (startY * scale), 
                (int) ((leftColumnX + 166) * scale), 
                (int) (endY * scale)
        );

        int relY = 5;

        // Draw Ground Items subheader in light gray
        guiGraphics.drawString(this.font, "Ground Items", leftColumnX + 4, startY + relY - (int) scrollAmount, 0xFFDFDFDF, false);
        relY += 15;

        if (vicinityItems.isEmpty()) {
            guiGraphics.drawString(this.font, "No items nearby", leftColumnX + 4, startY + relY - (int) scrollAmount, 0xFF888888, false);
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

                    drawDayZSlot(guiGraphics, itemX, itemY);
                    
                    boolean hovering = mouseX >= itemX && mouseX < itemX + 18 && mouseY >= itemY && mouseY <= itemY + 18;
                    if (hovering) {
                        guiGraphics.fill(itemX + 1, itemY + 1, itemX + 17, itemY + 17, 0x80FFFFFF); // Classic hover
                    }

                    guiGraphics.renderFakeItem(stack, itemX + 1, itemY + 1);
                    guiGraphics.renderItemDecorations(this.font, stack, itemX + 1, itemY + 1);
                }
            }
            int rows = (int) Math.ceil(vicinityItems.size() / 9.0);
            relY += rows * 18;
        }

        // Draw Container subheader
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        if (containerSize > 0) {
            relY += 10;
            guiGraphics.drawString(this.font, "Container", leftColumnX + 4, startY + relY - (int) scrollAmount, 0xFFDFDFDF, false);
            int rows = (int) Math.ceil(containerSize / 9.0);
            relY += 15 + rows * 18;
        }

        // Draw Nearby Storages dropdown list
        if (!nearbyContainers.isEmpty()) {
            relY += 10;
            guiGraphics.drawString(this.font, "Nearby Storages", leftColumnX + 4, startY + relY - (int) scrollAmount, 0xFFDFDFDF, false);
            relY += 15;

            for (int i = 0; i < nearbyContainers.size(); i++) {
                ContainerBlockInfo container = nearbyContainers.get(i);
                int itemY = startY + relY + i * 18 - (int) scrollAmount;
                
                if (itemY + 16 >= startY && itemY <= endY) {
                    drawSectionPanel(guiGraphics, leftColumnX, itemY, 162, 16);
                    
                    boolean hovering = mouseX >= leftColumnX && mouseX < leftColumnX + 162 && mouseY >= itemY && mouseY <= itemY + 16;
                    if (hovering) {
                        guiGraphics.fill(leftColumnX + 1, itemY + 1, leftColumnX + 161, itemY + 15, 0x30FFFFFF);
                    }
                    
                    String btnText = container.name;
                    guiGraphics.drawString(this.font, btnText, leftColumnX + 6, itemY + 4, 0xFFFFFFFF, false);
                }
            }
        }

        guiGraphics.disableScissor();
    }

    private void drawSectionPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Dark translucent panel background
        guiGraphics.fill(x, y, x + width, y + height, 0xD0101010);
        
        // 3D-like thin borders for panel separation
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF3A3A3A); // top
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF3A3A3A); // bottom
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF3A3A3A); // left
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF3A3A3A); // right
    }

    private void drawDayZSlot(GuiGraphics guiGraphics, int x, int y) {
        // Flat modern semi-transparent slot fill
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0x1AFFFFFF); // 10% white opacity
        
        // Thin borders representing flat slot edges
        guiGraphics.fill(x, y, x + 18, y + 1, 0x26FFFFFF); // top
        guiGraphics.fill(x, y + 17, x + 18, y + 18, 0x26FFFFFF); // bottom
        guiGraphics.fill(x, y, x + 1, y + 18, 0x26FFFFFF); // left
        guiGraphics.fill(x + 17, y, x + 18, y + 18, 0x26FFFFFF); // right
    }
}

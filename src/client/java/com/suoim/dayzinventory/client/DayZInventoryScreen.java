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

    // Nearby containers list with icons
    public static class ContainerBlockInfo {
        public final net.minecraft.core.BlockPos pos;
        public final String name;
        public final ItemStack icon;
        public ContainerBlockInfo(net.minecraft.core.BlockPos pos, String name, ItemStack icon) {
            this.pos = pos;
            this.name = name;
            this.icon = icon;
        }
    }
    private final List<ContainerBlockInfo> nearbyContainers = new ArrayList<>();
    private ContainerBlockInfo hoveredContainer = null;

    // Mouse cursor position restoration state
    private static double lastMouseX = -1;
    private static double lastMouseY = -1;
    private static boolean needsMouseRestore = false;

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
                        ItemStack iconStack = new ItemStack(state.getBlock().asItem());
                        this.nearbyContainers.add(new ContainerBlockInfo(pos, displayName, iconStack));
                    }
                }
            }
        }
    }

    public int getColumnX(int colIndex) {
        int remaining = this.imageWidth - 486; // 486 is 3 * 162
        int gap = remaining / 4;
        return leftPos + gap + colIndex * (162 + gap);
    }

    private int getScrollContentHeight() {
        int totalCount = vicinityItems.size() + nearbyContainers.size();
        int rows = (int) Math.ceil(totalCount / 9.0);
        int height = 5 + (rows == 0 ? 18 : rows * 18);
        
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        if (containerSize > 0) {
            int containerRows = (int) Math.ceil(containerSize / 9.0);
            height += 10 + 18 + containerRows * 18 + 5;
        }
        
        return height;
    }

    private void updateSlotPositions() {
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        int totalCount = vicinityItems.size() + nearbyContainers.size();
        int rows = (int) Math.ceil(totalCount / 9.0);
        int groundGridHeight = 5 + (rows == 0 ? 18 : rows * 18);
        
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

        // Renders active container grid directly below vicinity items grid
        int containerRelY = groundGridHeight + 10 + 18;

        // 1. Position Container Slots
        for (int i = 0; i < containerSize; i++) {
            Slot slot = this.menu.slots.get(i);
            int relY = containerRelY + (i / 9) * 18;
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
        // All 9 hotbar slots are positioned normally in the right column row.
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
            
            // 1. Vicinity Grid Row check
            int totalCount = vicinityItems.size() + nearbyContainers.size();
            int rows = (int) Math.ceil(totalCount / 9.0);
            int groundGridHeight = 5 + (rows == 0 ? 18 : rows * 18);
            
            if (clickY >= 5 && clickY < groundGridHeight) {
                int col = (int) (clickX / 18);
                int row = (int) ((clickY - 5) / 18);
                int index = row * 9 + col;
                
                if (index >= 0 && index < totalCount) {
                    if (index < vicinityItems.size()) {
                        // Ground Item Clicked
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
                    } else {
                        // Container Item Clicked
                        int containerIndex = index - vicinityItems.size();
                        ContainerBlockInfo container = nearbyContainers.get(containerIndex);
                        net.minecraft.core.BlockPos openPos = this.menu.getContainerPos();
                        boolean isOpen = openPos != null && openPos.equals(container.pos);
                        
                        this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        
                        // Save cursor positions right before container view changes
                        lastMouseX = this.minecraft.mouseHandler.xpos();
                        lastMouseY = this.minecraft.mouseHandler.ypos();
                        needsMouseRestore = true;

                        if (isOpen) {
                            // Collapse it
                            ClientPlayNetworking.send(DayZInventoryPackets.OPEN_INVENTORY_PACKET, PacketByteBufs.create());
                        } else {
                            // Expand it
                            FriendlyByteBuf buf = PacketByteBufs.create();
                            buf.writeBlockPos(container.pos);
                            ClientPlayNetworking.send(DayZInventoryPackets.OPEN_CONTAINER_PACKET, buf);
                        }
                        return true;
                    }
                }
            }

            // 2. Active open container close button header click
            int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
            if (containerSize > 0) {
                int headerY = groundGridHeight + 10;
                if (clickY >= headerY && clickY < headerY + 18 && clickX >= 147 && clickX < 161) {
                    this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    
                    // Save cursor positions right before container closes
                    lastMouseX = this.minecraft.mouseHandler.xpos();
                    lastMouseY = this.minecraft.mouseHandler.ypos();
                    needsMouseRestore = true;

                    ClientPlayNetworking.send(DayZInventoryPackets.OPEN_INVENTORY_PACKET, PacketByteBufs.create());
                    return true;
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
            } else if (releasedOverSurvivor && releaseSlot != this.draggedSlot) {
                // Dragged onto survivor panel -> auto equip if equippable (skip if dropping back on the source slot)
                ItemStack draggedItem = this.menu.getCarried();
                if (!draggedItem.isEmpty()) {
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
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        
        // Replicates click/hover hit check over the virtual Hands slot in the middle column
        int middleColumnX = getColumnX(1);
        int handsSlotX = middleColumnX + 72;
        int handsSlotY = topPos + imageHeight - 44;
        if (mouseX >= (handsSlotX - 4) && mouseX < (handsSlotX + 22) 
                && mouseY >= (handsSlotY - 4) && mouseY < (handsSlotY + 22)) {
            return this.menu.slots.get(containerSize + 27);
        }

        for (Slot slot : this.menu.slots) {
            if (slot.x >= 0) {
                if (mouseX >= (leftPos + slot.x) && mouseX < (leftPos + slot.x + 18) 
                        && mouseY >= (topPos + slot.y) && mouseY < (topPos + slot.y + 18)) {
                    return slot;
                }
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
        // Restore cursor positions on render frame tick if requested
        if (needsMouseRestore && this.minecraft != null) {
            needsMouseRestore = false;
            org.lwjgl.glfw.GLFW.glfwSetCursorPos(this.minecraft.getWindow().getWindow(), lastMouseX, lastMouseY);
            
            // Re-calculate scaled mouseX and mouseY so hover checks resolve correctly on this frame
            mouseX = (int) (lastMouseX * (double) this.minecraft.getWindow().getGuiScaledWidth() / (double) this.minecraft.getWindow().getWidth());
            mouseY = (int) (lastMouseY * (double) this.minecraft.getWindow().getGuiScaledHeight() / (double) this.minecraft.getWindow().getHeight());
        }

        // Draw the background dim overlay at full physical 1x scale
        this.renderBackground(guiGraphics);

        float scale = getGuiScale();
        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);

        // Reset hovered container state
        this.hoveredContainer = null;

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
            
            int totalCount = vicinityItems.size() + nearbyContainers.size();
            int rows = (int) Math.ceil(totalCount / 9.0);
            int groundGridHeight = 5 + (rows == 0 ? 18 : rows * 18);
            
            if (clickY >= 5 && clickY < groundGridHeight) {
                int col = (int) (clickX / 18);
                int row = (int) ((clickY - 5) / 18);
                int index = row * 9 + col;
                if (index >= 0 && index < vicinityItems.size()) {
                    guiGraphics.renderTooltip(this.font, vicinityItems.get(index).getItem(), scaledMouseX, scaledMouseY);
                }
            }
        }
        
        guiGraphics.pose().popPose();

        // Render unscaled Container Details Tooltip at 1x global scale if hovered
        if (this.hoveredContainer != null && this.minecraft != null && this.minecraft.player != null) {
            List<Component> tooltipText = new ArrayList<>();
            tooltipText.add(Component.literal(this.hoveredContainer.name).withStyle(net.minecraft.ChatFormatting.YELLOW));
            tooltipText.add(Component.literal("Location: " + this.hoveredContainer.pos.getX() + ", " + this.hoveredContainer.pos.getY() + ", " + this.hoveredContainer.pos.getZ()).withStyle(net.minecraft.ChatFormatting.GRAY));
            double dist = Math.sqrt(this.minecraft.player.distanceToSqr(this.hoveredContainer.pos.getX() + 0.5, this.hoveredContainer.pos.getY() + 0.5, this.hoveredContainer.pos.getZ() + 0.5));
            tooltipText.add(Component.literal("Distance: " + String.format("%.1f", dist) + "m").withStyle(net.minecraft.ChatFormatting.GREEN));
            
            guiGraphics.renderComponentTooltip(this.font, tooltipText, mouseX, mouseY);
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
        int viewportHeight = imageHeight - 40;

        float scale = getGuiScale();
        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);

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

        // 3. Draw Hands Panel at the bottom of the middle column
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        int handsPanelY = topPos + imageHeight - 75;
        int handsSlotX = middleColumnX + 72;
        int handsSlotY = topPos + imageHeight - 44;
        
        // Single unified panel for the entire Hands area
        drawSectionPanel(guiGraphics, middleColumnX - 4, handsPanelY, 170, 70);
        
        // Separate header section using a thin line
        guiGraphics.fill(middleColumnX - 4, handsPanelY + 15, middleColumnX + 166, handsPanelY + 16, 0x26FFFFFF); // 15% opacity white border line
        guiGraphics.drawString(this.font, "HANDS", middleColumnX + 81 - (this.font.width("HANDS") / 2), handsPanelY + 4, 0xFFDFDFDF, false);
        
        Slot handsSlot = this.menu.slots.get(containerSize + 27);
        ItemStack handsStack = handsSlot.getItem();
        if (!handsStack.isEmpty()) {
            String handsItemName = handsStack.getHoverName().getString().toUpperCase();
            // Separate subheader using another thin line
            guiGraphics.fill(middleColumnX - 4, handsPanelY + 27, middleColumnX + 166, handsPanelY + 28, 0x26FFFFFF);
            guiGraphics.drawString(this.font, handsItemName, middleColumnX + 81 - (this.font.width(handsItemName) / 2), handsPanelY + 17, 0xFFDFDFDF, false);
        }

        // Draw a larger slot background for the Hands slot!
        drawDayZSlotLarge(guiGraphics, handsSlotX - 4, handsSlotY - 4, 26, 26);

        // Draw custom 26x26 hover highlight for the Hands slot
        boolean hoverHands = scaledMouseX >= handsSlotX - 4 && scaledMouseX < handsSlotX + 22 
            && scaledMouseY >= handsSlotY - 4 && scaledMouseY < handsSlotY + 22;
        if (hoverHands) {
            guiGraphics.fill(handsSlotX - 3, handsSlotY - 3, handsSlotX + 23, handsSlotY + 23, 0x30FFFFFF);
        }

        // Render the 1.5x scaled hand item icon manually
        if (!handsStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            // Draw at 1.5x scale centered in the 26x26 slot.
            // Outer bounds starts at handsSlotX - 4. Inner starts at handsSlotX - 3.
            guiGraphics.pose().translate(handsSlotX - 3, handsSlotY - 3, 100);
            guiGraphics.pose().scale(1.5F, 1.5F, 1.0F);
            guiGraphics.renderFakeItem(handsStack, 0, 0);
            guiGraphics.renderItemDecorations(this.font, handsStack, 0, 0);
            guiGraphics.pose().popPose();
        }

        if (this.minecraft != null && this.minecraft.player != null) {
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
                // Slot 0 of the hotbar is rendered normally as a small slot in the right column.
                // We draw the standard outline behind it there.
                drawDayZSlot(guiGraphics, leftPos + slot.x - 1, topPos + slot.y - 1);
            }
        }

        // 5. Draw Vicinity List (Ground Items + Container slots titles + Nearby Storages)
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

        // 1. Draw unified Vicinity grid slots (Ground items first, then nearby container icons)
        int totalCount = vicinityItems.size() + nearbyContainers.size();
        int rows = (int) Math.ceil(totalCount / 9.0);
        
        if (totalCount == 0) {
            guiGraphics.drawString(this.font, "No items nearby", leftColumnX + 4, startY + relY - (int) scrollAmount, 0xFF888888, false);
            relY += 15;
        } else {
            for (int i = 0; i < totalCount; i++) {
                int col = i % 9;
                int row = i / 9;
                int relX = col * 18;
                int rowRelY = relY + row * 18;
                
                int itemX = leftColumnX + relX;
                int itemY = startY + rowRelY - (int) scrollAmount;

                if (itemY + 18 >= startY && itemY <= endY) {
                    drawDayZSlot(guiGraphics, itemX, itemY);
                    
                    boolean hovering = mouseX >= itemX && mouseX < itemX + 18 && mouseY >= itemY && mouseY <= itemY + 18;
                    if (hovering) {
                        guiGraphics.fill(itemX + 1, itemY + 1, itemX + 17, itemY + 17, 0x80FFFFFF); // Hover highlight
                    }

                    if (i < vicinityItems.size()) {
                        // Ground Item Icon
                        ItemStack stack = vicinityItems.get(i).getItem();
                        guiGraphics.renderFakeItem(stack, itemX + 1, itemY + 1);
                        guiGraphics.renderItemDecorations(this.font, stack, itemX + 1, itemY + 1);
                    } else {
                        // Container Icon
                        int containerIndex = i - vicinityItems.size();
                        ContainerBlockInfo container = nearbyContainers.get(containerIndex);
                        guiGraphics.renderFakeItem(container.icon, itemX + 1, itemY + 1);
                        
                        if (hovering) {
                            this.hoveredContainer = container; // Set hovered for tooltip details
                        }
                        
                        // Draw chevron overlay: active open/closed status indicators
                        net.minecraft.core.BlockPos openPos = this.menu.getContainerPos();
                        boolean isOpen = openPos != null && openPos.equals(container.pos);
                        String chevron = isOpen ? "^" : "v";
                        
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(0, 0, 200.0F);
                        guiGraphics.drawString(this.font, chevron, itemX + 11, itemY + 9, 0xFFFFFFFF, true);
                        guiGraphics.pose().popPose();
                    }
                }
            }
            relY += rows * 18;
        }

        // 2. Draw expanded active container slots grid below vicinity grid
        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        net.minecraft.core.BlockPos openPos = this.menu.getContainerPos();
        
        if (containerSize > 0 && openPos != null) {
            relY += 10;
            int headerY = startY + relY - (int) scrollAmount;
            
            // Find container block name from registry/block pos
            String containerName = "Container";
            if (this.minecraft != null && this.minecraft.level != null) {
                net.minecraft.world.level.block.state.BlockState state = this.minecraft.level.getBlockState(openPos);
                if (state != null && !state.isAir()) {
                    containerName = state.getBlock().getName().getString().toUpperCase();
                }
            }
            
            if (headerY + 18 >= startY && headerY <= endY) {
                // Renders the header panel of expanded active container
                drawSectionPanel(guiGraphics, leftColumnX - 1, headerY, 162, 18);
                guiGraphics.drawString(this.font, containerName, leftColumnX + 5, headerY + 5, 0xFFDFDFDF, false);
                
                // Draw a close button 'x' on header bar right
                boolean hoverClose = mouseX >= leftColumnX + 147 && mouseX < leftColumnX + 161 && mouseY >= headerY && mouseY <= headerY + 18;
                int closeColor = hoverClose ? 0xFFE04040 : 0xFF888888;
                guiGraphics.drawString(this.font, "x", leftColumnX + 150, headerY + 4, closeColor, false);
            }
            
            relY += 18; // Shift past the drawer header bar to leave space for slot grid
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

    private void drawDayZSlotLarge(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Flat modern semi-transparent slot fill
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x1AFFFFFF); // 10% white opacity
        
        // Thin borders representing flat slot edges
        guiGraphics.fill(x, y, x + width, y + 1, 0x26FFFFFF); // top
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0x26FFFFFF); // bottom
        guiGraphics.fill(x, y, x + 1, y + height, 0x26FFFFFF); // left
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0x26FFFFFF); // right
    }
}

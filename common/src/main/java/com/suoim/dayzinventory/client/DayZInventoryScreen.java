package com.suoim.dayzinventory.client;

import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.DayZInventoryPackets;
import com.suoim.dayzinventory.mixin.SlotAccessor;
import com.suoim.dayzinventory.platform.Platform;
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

        // 6. Position Crafting Slots (Result is index 41, Inputs are 42 to 45)
        Slot craftResultSlot = this.menu.slots.get(containerSize + 41);
        ((SlotAccessor) craftResultSlot).setX(rightColumnX + 97 - leftPos);
        ((SlotAccessor) craftResultSlot).setY(126);

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                int index = containerSize + 42 + c + r * 2;
                Slot slot = this.menu.slots.get(index);
                ((SlotAccessor) slot).setX(rightColumnX + 25 + c * 18 - leftPos);
                ((SlotAccessor) slot).setY(117 + r * 18);
            }
        }
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

        // Handle recipe viewer button click if JEI/REI/EMI is present
        boolean hasJei = Platform.HELPER.isModLoaded("jei");
        boolean hasRei = Platform.HELPER.isModLoaded("roughlyenoughitems");
        boolean hasEmi = Platform.HELPER.isModLoaded("emi");
        boolean showToggleBtn = hasJei || hasRei || hasEmi;
        if (showToggleBtn) {
            int rightColumnX = getColumnX(2);
            int btnX = rightColumnX + 130;
            int btnY = topPos + 7;
            int btnW = 30;
            int btnH = 13;
            if (scaledX >= btnX && scaledX < btnX + btnW &&
                scaledY >= btnY && scaledY < btnY + btnH) {
                this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                toggleRecipeViewerOverlay();
                return true;
            }
        }

        // Handle Curios button click if Curios is present
        boolean hasCurios = Platform.HELPER.isModLoaded("curios");
        boolean hasTrinkets = Platform.HELPER.isModLoaded("trinkets");
        if (hasCurios) {
            int middleColumnX = getColumnX(1);
            int btnX = hasTrinkets ? (middleColumnX + 4) : (middleColumnX + 125);
            int btnY = topPos + 7;
            int btnW = 35;
            int btnH = 13;
            if (scaledX >= btnX && scaledX < btnX + btnW &&
                scaledY >= btnY && scaledY < btnY + btnH) {
                this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                toggleCuriosOverlay();
                return true;
            }
        }

        // Handle Trinkets button click if Trinkets is present
        if (hasTrinkets) {
            int middleColumnX = getColumnX(1);
            int btnX = middleColumnX + 115;
            int btnY = topPos + 7;
            int btnW = 45;
            int btnH = 13;
            if (scaledX >= btnX && scaledX < btnX + btnW &&
                scaledY >= btnY && scaledY < btnY + btnH) {
                this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                // Bypass redirection to open the vanilla InventoryScreen where Trinkets slots render perfectly
                Platform.allowVanillaInventory = true;
                this.minecraft.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(this.minecraft.player));
                Platform.allowVanillaInventory = false;
                return true;
            }
        }

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
                            FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                            buf.writeInt(itemEntity.getId());
                            Platform.HELPER.sendPacketToServer(DayZInventoryPackets.QUICK_PICKUP_ITEM_PACKET, buf);
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
                            Platform.HELPER.sendPacketToServer(DayZInventoryPackets.OPEN_INVENTORY_PACKET, new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer()));
                        } else {
                            // Expand it
                            FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                            buf.writeBlockPos(container.pos);
                            Platform.HELPER.sendPacketToServer(DayZInventoryPackets.OPEN_CONTAINER_PACKET, buf);
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

                    Platform.HELPER.sendPacketToServer(DayZInventoryPackets.OPEN_INVENTORY_PACKET, new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer()));
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
                FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                buf.writeInt(this.draggedEntity.getId());
                buf.writeInt(hoveredSlot.index);
                buf.writeInt(this.draggedStack.getCount());
                Platform.HELPER.sendPacketToServer(DayZInventoryPackets.PICKUP_ITEM_PACKET, buf);
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
                    FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                    buf.writeInt(this.draggedEntity.getId());
                    buf.writeInt(targetSlotIdx);
                    buf.writeInt(this.draggedStack.getCount());
                    Platform.HELPER.sendPacketToServer(DayZInventoryPackets.PICKUP_ITEM_PACKET, buf);
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
        
        // Replicates click/hover hit check over the virtual Hands slot in the middle column (absolute coordinates)
        int middleColumnX = getColumnX(1);
        int handsPanelY = topPos + imageHeight - 75;
        
        int selectedSlot = 0;
        if (this.minecraft != null && this.minecraft.player != null) {
            selectedSlot = this.minecraft.player.getInventory().selected;
        }
        Slot handsSlot = this.menu.slots.get(containerSize + 27 + selectedSlot);
        ItemStack handsStack = handsSlot.getItem();
        int bodyY = handsPanelY + (handsStack.isEmpty() ? 15 : 27);
        int bodyHeight = handsStack.isEmpty() ? 55 : 43;

        if (mouseX >= (middleColumnX - 4) && mouseX < (middleColumnX + 166) 
                && mouseY >= bodyY && mouseY < (bodyY + bodyHeight)) {
            return handsSlot;
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

        // 1. Draw Section Panels derived from getColumnX() to guarantee alignment with slots
        int lx = getColumnX(0); // left column origin
        int mx = getColumnX(1); // middle column origin
        int rx = getColumnX(2); // right column origin
        final int PANEL_W = 164;
        final int PAD = 2;

        // Left Column (Vicinity)
        drawSectionPanel(guiGraphics, lx - PAD, topPos + 5, PANEL_W, 17); // Header
        drawSectionPanel(guiGraphics, lx - PAD, topPos + 25, PANEL_W, viewportHeight); // Content

        // Middle Column (Survivor)
        drawSectionPanel(guiGraphics, mx - PAD, topPos + 5, PANEL_W, 17); // Header
        drawSectionPanel(guiGraphics, mx + 13, topPos + 33, 20, 20); // Helmet
        drawSectionPanel(guiGraphics, mx + 13, topPos + 58, 20, 20); // Chestplate
        drawSectionPanel(guiGraphics, mx + 13, topPos + 83, 20, 20); // Leggings
        drawSectionPanel(guiGraphics, mx + 13, topPos + 108, 20, 20); // Boots
        drawSectionPanel(guiGraphics, mx + 127, topPos + 33, 20, 20); // Offhand

        // Right Column (Inventory)
        drawSectionPanel(guiGraphics, rx - PAD, topPos + 5, PANEL_W, 17); // Header
        drawSectionPanel(guiGraphics, rx - PAD, topPos + 28, PANEL_W, 56);
        drawSectionPanel(guiGraphics, rx - PAD, topPos + 90, PANEL_W, 90);
        guiGraphics.fill(rx - PAD, topPos + 107, rx - PAD + PANEL_W, topPos + 108, 0x10FFFFFF); // separator
        guiGraphics.drawString(this.font, "CRAFTING", rx + 80 - (this.font.width("CRAFTING") / 2), topPos + 96, 0xFFDFDFDF, false);
        guiGraphics.drawString(this.font, "->", rx + 72, topPos + 130, 0xFFDFDFDF, false);
        drawSectionPanel(guiGraphics, rx - PAD, topPos + imageHeight - 32, PANEL_W, 20);

        // Draw recipe viewer toggle button if JEI/REI/EMI is loaded
        boolean hasJei = Platform.HELPER.isModLoaded("jei");
        boolean hasRei = Platform.HELPER.isModLoaded("roughlyenoughitems");
        boolean hasEmi = Platform.HELPER.isModLoaded("emi");
        boolean showToggleBtn = hasJei || hasRei || hasEmi;
        if (showToggleBtn) {
            String toggleBtnText = hasJei ? "JEI" : (hasRei ? "REI" : "EMI");
            int btnX = rx + 130;
            int btnY = topPos + 7;
            int btnW = 30;
            int btnH = 13;
            boolean hoverBtn = scaledMouseX >= btnX && scaledMouseX < btnX + btnW &&
                               scaledMouseY >= btnY && scaledMouseY < btnY + btnH;
            guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, hoverBtn ? 0x35FFFFFF : 0x15FFFFFF);
            int textW = this.font.width(toggleBtnText);
            guiGraphics.drawString(this.font, toggleBtnText, btnX + (btnW - textW) / 2, btnY + (btnH - 8) / 2 + 1, 0xFFDFDFDF, false);
        }

        // Draw Curios button if Curios is loaded
        boolean hasCurios = Platform.HELPER.isModLoaded("curios");
        boolean hasTrinkets = Platform.HELPER.isModLoaded("trinkets");
        if (hasCurios) {
            int btnX = hasTrinkets ? (mx + 4) : (mx + 125);
            int btnY = topPos + 7;
            int btnW = 35;
            int btnH = 13;
            boolean hoverBtn = scaledMouseX >= btnX && scaledMouseX < btnX + btnW &&
                               scaledMouseY >= btnY && scaledMouseY < btnY + btnH;
            guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, hoverBtn ? 0x35FFFFFF : 0x15FFFFFF);
            int textW = this.font.width("CURIOS");
            guiGraphics.drawString(this.font, "CURIOS", btnX + (btnW - textW) / 2, btnY + (btnH - 8) / 2 + 1, 0xFFDFDFDF, false);
        }

        // Draw Trinkets button if Trinkets is loaded
        if (hasTrinkets) {
            int btnX = mx + 115;
            int btnY = topPos + 7;
            int btnW = 45;
            int btnH = 13;
            boolean hoverBtn = scaledMouseX >= btnX && scaledMouseX < btnX + btnW &&
                               scaledMouseY >= btnY && scaledMouseY < btnY + btnH;
            guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, hoverBtn ? 0x35FFFFFF : 0x15FFFFFF);
            int textW = this.font.width("TRINKETS");
            guiGraphics.drawString(this.font, "TRINKETS", btnX + (btnW - textW) / 2, btnY + (btnH - 8) / 2 + 1, 0xFFDFDFDF, false);
        }

        // 2. Draw Column Header Texts
        int lx2 = getColumnX(0), mx2 = getColumnX(1), rx2 = getColumnX(2);
        guiGraphics.drawString(this.font, "VICINITY",  lx2 + (162 - this.font.width("VICINITY"))  / 2, topPos + 9, 0xFFFFFFFF, true);
        guiGraphics.drawString(this.font, "SURVIVOR",  mx2 + (162 - this.font.width("SURVIVOR"))  / 2, topPos + 9, 0xFFFFFFFF, true);
        guiGraphics.drawString(this.font, "INVENTORY", rx2 + (162 - this.font.width("INVENTORY")) / 2, topPos + 9, 0xFFFFFFFF, true);

        // 3. Draw Hands Panel at the bottom of the middle column
        int mx3 = getColumnX(1);
        int containerSize2 = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        int handsPanelY = topPos + imageHeight - 75;
        
        drawSectionPanel(guiGraphics, mx3 - 2, handsPanelY, 166, 70);
        guiGraphics.fill(mx3 - 2, handsPanelY + 15, mx3 + 164, handsPanelY + 16, 0x10FFFFFF); // divider line
        guiGraphics.drawString(this.font, "HANDS", mx3 + 81 - (this.font.width("HANDS") / 2), handsPanelY + 4, 0xFFDFDFDF, false);
        
        int selectedSlot = 0;
        if (this.minecraft != null && this.minecraft.player != null) {
            selectedSlot = this.minecraft.player.getInventory().selected;
        }
        Slot handsSlot = this.menu.slots.get(containerSize2 + 27 + selectedSlot);
        ItemStack handsStack = handsSlot.getItem();
        int bodyY = handsPanelY + (handsStack.isEmpty() ? 15 : 27);
        int bodyHeight = handsStack.isEmpty() ? 55 : 43;

        if (!handsStack.isEmpty()) {
            String handsItemName = handsStack.getHoverName().getString().toUpperCase();
            guiGraphics.fill(mx3 - 2, handsPanelY + 27, mx3 + 164, handsPanelY + 28, 0x10FFFFFF);
            guiGraphics.drawString(this.font, handsItemName, mx3 + 81 - (this.font.width(handsItemName) / 2), handsPanelY + 17, 0xFFDFDFDF, false);
        }

        // Draw custom hover highlight for the Hands slot body
        boolean hoverHands = scaledMouseX >= middleColumnX - 4 && scaledMouseX < middleColumnX + 166 
            && scaledMouseY >= bodyY && scaledMouseY < (bodyY + bodyHeight);
        if (hoverHands) {
            guiGraphics.fill(middleColumnX - 3, bodyY + 1, middleColumnX + 165, bodyY + bodyHeight - 1, 0x30FFFFFF);
        }

        // Render the 2.0x scaled hand item icon
        if (!handsStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(middleColumnX + 65, bodyY + 5, 100);
            guiGraphics.pose().scale(2.0F, 2.0F, 1.0F);
            guiGraphics.renderFakeItem(handsStack, 0, 0);
            guiGraphics.renderItemDecorations(this.font, handsStack, 0, 0);
            guiGraphics.pose().popPose();
        }

        if (this.minecraft != null && this.minecraft.player != null) {
            // Draw 3D Player entity
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

        // 4. Draw DayZ-style Slot Backgrounds
        for (Slot slot : this.menu.slots) {
            if (slot.x >= 0) {
                guiGraphics.fill(
                    leftPos + slot.x, topPos + slot.y,
                    leftPos + slot.x + 16, topPos + slot.y + 16,
                    0x18FFFFFF
                );
            }
        }

        // 5. Draw Vicinity List
        renderVicinityList(guiGraphics, mouseX, mouseY, scale);

        // 6. Draw Scrollbar
        int contentHeight = getScrollContentHeight();
        if (contentHeight > viewportHeight) {
            int maxScroll = contentHeight - viewportHeight;
            int barHeight = Math.max(20, (viewportHeight * viewportHeight) / contentHeight);
            int barTop = topPos + 25 + (int) ((scrollAmount * (viewportHeight - barHeight)) / maxScroll);
            guiGraphics.fill(leftColumnX + 163, topPos + 25, leftColumnX + 165, topPos + 25 + viewportHeight, 0xFF373737);
            guiGraphics.fill(leftColumnX + 163, barTop, leftColumnX + 165, barTop + barHeight, 0xFFFFFFFF);
        }
    }

    private void renderVicinityList(GuiGraphics guiGraphics, int mouseX, int mouseY, float scale) {
        int leftColumnX = getColumnX(0);
        int startY = topPos + 25;
        int viewportHeight = this.imageHeight - 40;
        int endY = startY + viewportHeight;

        guiGraphics.enableScissor(
                (int) ((leftColumnX - 4) * scale), 
                (int) (startY * scale), 
                (int) ((leftColumnX + 166) * scale), 
                (int) (endY * scale)
        );

        int relY = 5;

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
                        guiGraphics.fill(itemX + 1, itemY + 1, itemX + 17, itemY + 17, 0x80FFFFFF);
                    }

                    if (i < vicinityItems.size()) {
                        ItemStack stack = vicinityItems.get(i).getItem();
                        guiGraphics.renderFakeItem(stack, itemX + 1, itemY + 1);
                        guiGraphics.renderItemDecorations(this.font, stack, itemX + 1, itemY + 1);
                    } else {
                        int containerIndex = i - vicinityItems.size();
                        ContainerBlockInfo container = nearbyContainers.get(containerIndex);
                        guiGraphics.renderFakeItem(container.icon, itemX + 1, itemY + 1);
                        
                        if (hovering) {
                            this.hoveredContainer = container;
                        }
                        
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

        int containerSize = this.menu.getContainerInventory() != null ? this.menu.getContainerInventory().getContainerSize() : 0;
        net.minecraft.core.BlockPos openPos = this.menu.getContainerPos();
        
        if (containerSize > 0 && openPos != null) {
            relY += 10;
            int headerY = startY + relY - (int) scrollAmount;
            
            String containerName = "Container";
            if (this.minecraft != null && this.minecraft.level != null) {
                net.minecraft.world.level.block.state.BlockState state = this.minecraft.level.getBlockState(openPos);
                if (state != null && !state.isAir()) {
                    containerName = state.getBlock().getName().getString().toUpperCase();
                }
            }
            
            if (headerY + 18 >= startY && headerY <= endY) {
                drawSectionPanel(guiGraphics, leftColumnX - 1, headerY, 162, 18);
                guiGraphics.drawString(this.font, containerName, leftColumnX + 5, headerY + 5, 0xFFDFDFDF, false);
                
                boolean hoverClose = mouseX >= leftColumnX + 147 && mouseX < leftColumnX + 161 && mouseY >= headerY && mouseY <= headerY + 18;
                int closeColor = hoverClose ? 0xFFE04040 : 0xFF888888;
                guiGraphics.drawString(this.font, "x", leftColumnX + 150, headerY + 4, closeColor, false);
            }
            
            relY += 18;
        }

        guiGraphics.disableScissor();
    }

    private void drawSectionPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0x9E0C0C0C);
    }

    private void drawDayZSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0x18FFFFFF);
    }

    private void drawDayZSlotLarge(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x15FFFFFF);
    }

    private void simulateKeyTap(int keyCode) {
        if (this.minecraft == null) return;
        long window = this.minecraft.getWindow().getWindow();
        this.minecraft.keyboardHandler.keyPress(window, keyCode, 0, 1, 0); // PRESS
        this.minecraft.keyboardHandler.keyPress(window, keyCode, 0, 0, 0); // RELEASE
    }

    private void toggleRecipeViewerOverlay() {
        simulateKeyTap(79); // 79 is GLFW_KEY_O
    }

    private void toggleCuriosOverlay() {
        if (this.minecraft == null) return;
        net.minecraft.client.KeyMapping targetKey = null;
        for (net.minecraft.client.KeyMapping key : this.minecraft.options.keyMappings) {
            String name = key.getName();
            if (name.equals("key.curios.open.desc") || name.contains("curios.open")) {
                targetKey = key;
                break;
            }
        }
        if (targetKey != null && !targetKey.isUnbound()) {
            com.mojang.blaze3d.platform.InputConstants.Key key = getBoundKey(targetKey);
            if (key != null) {
                simulateKeyTap(key.getValue());
            }
        }
    }

    private static com.mojang.blaze3d.platform.InputConstants.Key getBoundKey(net.minecraft.client.KeyMapping keyMapping) {
        try {
            for (java.lang.reflect.Field field : net.minecraft.client.KeyMapping.class.getDeclaredFields()) {
                if (field.getType().getName().endsWith("$Key") || field.getType().getSimpleName().equals("Key")) {
                    field.setAccessible(true);
                    Object obj = field.get(keyMapping);
                    if (obj instanceof com.mojang.blaze3d.platform.InputConstants.Key) {
                        return (com.mojang.blaze3d.platform.InputConstants.Key) obj;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

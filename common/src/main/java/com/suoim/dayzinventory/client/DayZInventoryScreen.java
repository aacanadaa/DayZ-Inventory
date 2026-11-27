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
package com.suoim.dayzinventory.client;

import com.suoim.dayzinventory.DayZInventoryPackets;
import com.suoim.dayzinventory.DayZInventoryScreenHandler;
import com.suoim.dayzinventory.DayZMenus;
import com.suoim.dayzinventory.client.compat.ModCompat;
import com.suoim.dayzinventory.client.compat.ThirstCompat;
import com.suoim.dayzinventory.config.DayZConfig;
import com.suoim.dayzinventory.mixin.SlotAccessor;
import com.suoim.dayzinventory.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.sounds.SoundEvents;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DayZInventoryScreen extends AbstractContainerScreen<DayZInventoryScreenHandler> {
    private static final int PANEL_WIDTH = 540;
    private static final int PANEL_HEIGHT = 220;
    private static final int BUTTON_HEIGHT = 13;
    private static final int TEXT_COLOR = 0xFFDFDFDF;
    private static final int MAX_EFFECT_ICONS = 4;

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
        public final BlockPos pos;
        public final String name;
        public final ItemStack icon;
        public ContainerBlockInfo(BlockPos pos, String name, ItemStack icon) {
            this.pos = pos;
            this.name = name;
            this.icon = icon;
        }
    }
    private final List<ContainerBlockInfo> nearbyContainers = new ArrayList<>();
    private ContainerBlockInfo hoveredContainer = null;

    /** A small text button in one of the column headers. */
    private record HeaderButton(int x, int y, int width, String label, Runnable action) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + BUTTON_HEIGHT;
        }
    }
    private final List<HeaderButton> headerButtons = new ArrayList<>();
    private boolean survivorTitleLeft = false;
    private boolean containerSortable = false;

    // Tooltip collected while drawing under the virtual scale, drawn afterwards at 1x.
    private List<Component> pendingTooltip = null;

    private int thirst = -1;

    // Mouse Tweaks support (see MouseTweaksCompatMixin)
    private boolean skipNextReleaseFromCompat = false;
    private int lastQuickCraftButton = -1;

    // Mouse cursor position restoration state
    private static double lastMouseX = -1;
    private static double lastMouseY = -1;
    private static boolean needsMouseRestore = false;

    public DayZInventoryScreen(DayZInventoryScreenHandler handler, Inventory inventory, Component title) {
        // 26.2 made imageWidth/imageHeight final, so the size has to be passed
        // through the five-argument constructor rather than assigned here.
        // Below that the fields are still assignable in the constructor body.
//? if >=26.1 {
        super(handler, inventory, title, 540, 220);
//?} else {
        super(handler, inventory, title);
        this.imageWidth = 540;
        this.imageHeight = 220;
//?}
    }

    private float getGuiScale() {
        float scaleX = (float) this.width / (float) PANEL_WIDTH;
        float scaleY = (float) this.height / (float) PANEL_HEIGHT;
        return Math.min(1.0f, Math.min(scaleX, scaleY));
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    @Override
    protected void init() {
        super.init();

        float scale = getGuiScale();
        this.leftPos = (int) (((this.width / scale) - this.imageWidth) / 2);
        this.topPos = (int) (((this.height / scale) - this.imageHeight) / 2);

        BlockPos containerPos = this.menu.getContainerPos();
        BlockEntity containerEntity = containerPos != null && this.minecraft != null && this.minecraft.level != null
            ? this.minecraft.level.getBlockEntity(containerPos) : null;
        this.containerSortable = this.menu.getContainerSize() > 0 && DayZMenus.isSortable(containerEntity);

        this.updateVicinityList();
        this.updateNearbyContainers();
        this.updateSlotPositions();
        this.layoutHeaderButtons();
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
            this.thirst = DayZConfig.get().showVitals ? ThirstCompat.thirst(this.minecraft.player) : -1;
        }
    }

    // ------------------------------------------------------------------
    // Vicinity scan
    // ------------------------------------------------------------------

    private void updateVicinityList() {
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) return;

        double radius = DayZConfig.get().vicinityRadius;
        List<ItemEntity> entities = this.minecraft.level.getEntitiesOfClass(
                ItemEntity.class,
                this.minecraft.player.getBoundingBox().inflate(radius),
                entity -> entity.isAlive() && !entity.getItem().isEmpty()
        );

        this.vicinityItems.clear();
        this.vicinityItems.addAll(entities);
    }

    /**
     * Finds the containers around the player.
     * <p>
     * Only blocks that have a block entity can hold items, so everything else is
     * skipped after a single block state read. A double chest is listed once,
     * and the list is ordered nearest first.
     */
    private void updateNearbyContainers() {
        if (this.minecraft == null || this.minecraft.level == null || this.minecraft.player == null) return;

        BlockPos playerPos = this.minecraft.player.blockPosition();
        this.nearbyContainers.clear();

        int radius = DayZConfig.get().vicinityRadius;
        Set<BlockPos> seen = new HashSet<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    cursor.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                    BlockState state = this.minecraft.level.getBlockState(cursor);
                    if (!state.hasBlockEntity()) {
                        continue;
                    }
                    BlockEntity blockEntity = this.minecraft.level.getBlockEntity(cursor);
                    if (!(blockEntity instanceof net.minecraft.world.Container)) {
                        continue;
                    }

                    BlockPos pos = cursor.immutable();
                    if (!seen.add(pos)) {
                        continue;
                    }
                    if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                        // The other half opens the same double chest.
                        seen.add(pos.relative(ChestBlock.getConnectedDirection(state)));
                    }

                    String displayName = state.getBlock().getName().getString();
                    ItemStack iconStack = new ItemStack(state.getBlock().asItem());
                    this.nearbyContainers.add(new ContainerBlockInfo(pos, displayName, iconStack));
                }
            }
        }

        BlockPos center = playerPos;
        this.nearbyContainers.sort((a, b) -> Double.compare(a.pos.distSqr(center), b.pos.distSqr(center)));
    }

    // ------------------------------------------------------------------
    // Header buttons
    // ------------------------------------------------------------------

    private void layoutHeaderButtons() {
        this.headerButtons.clear();
        int buttonY = this.topPos + 7;
        int mx = getColumnX(1);
        int rx = getColumnX(2);

        // INVENTORY: sort on the left, the recipe viewer toggle on the right.
        String sort = tr("dayz_inventory.button.sort");
        this.headerButtons.add(new HeaderButton(rx + 2, buttonY, buttonWidth(sort), sort,
            () -> this.sendSort(DayZInventoryPackets.SORT_INVENTORY)));
        String viewer = ModCompat.recipeViewerName();
        if (viewer != null) {
            int width = Math.max(30, buttonWidth(viewer));
            this.headerButtons.add(new HeaderButton(rx + 160 - width, buttonY, width, viewer, ModCompat::toggleRecipeViewer));
        }

        // SURVIVOR: one button per installed equipment mod, right-aligned, with
        // the column title moved to the left to make room. Short labels are used
        // when the full ones would run into the title.
        List<String[]> equipment = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();
        if (ModCompat.hasCurios()) {
            equipment.add(new String[]{tr("dayz_inventory.button.curios"), tr("dayz_inventory.button.curios.short")});
            actions.add(this::openCurios);
        }
        if (ModCompat.hasAccessories()) {
            equipment.add(new String[]{tr("dayz_inventory.button.accessories"), tr("dayz_inventory.button.accessories.short")});
            actions.add(this::openAccessories);
        }
        if (ModCompat.hasTrinkets()) {
            equipment.add(new String[]{tr("dayz_inventory.button.trinkets"), tr("dayz_inventory.button.trinkets.short")});
            actions.add(this::openVanillaInventoryForOptionalMods);
        }
        this.survivorTitleLeft = !equipment.isEmpty();
        if (!equipment.isEmpty()) {
            int available = 160 - (8 + this.font.width(tr("dayz_inventory.survivor")));
            int fullWidth = 0;
            for (String[] labels : equipment) fullWidth += buttonWidth(labels[0]) + 2;
            boolean useShort = fullWidth > available;
            int right = mx + 160;
            for (int i = 0; i < equipment.size(); i++) {
                String label = equipment.get(i)[useShort ? 1 : 0];
                int width = buttonWidth(label);
                right -= width;
                this.headerButtons.add(new HeaderButton(right, buttonY, width, label, actions.get(i)));
                right -= 2;
            }
        }
    }

    private int buttonWidth(String label) {
        return this.font.width(label) + 8;
    }

    private void sendSort(int target) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeByte(target);
        Platform.HELPER.sendPacketToServer(DayZInventoryPackets.SORT_PACKET, buf);
    }

    private void playClickSound() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    // ------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------

    /**
     * Marks the Hands slot as hovered when the cursor is over the virtual Hands
     * panel.
     * <p>
     * Called from {@code AbstractContainerScreenMixin} at the point where
     * vanilla assigns {@code hoveredSlot}. Writing the field happens here rather
     * than in the mixin because this class inherits {@code hoveredSlot} from
     * {@link AbstractContainerScreen}, so no {@code @Shadow} is needed - and
     * shadowing that field does not resolve on 1.21 production runtimes.
     */
    public void applyVirtualHandsHover(int mouseX, int mouseY) {
        Slot handsSlot = getVirtualHandsHoverSlot(mouseX, mouseY);
        if (handsSlot != null) {
            this.hoveredSlot = handsSlot;
        }
    }

    private Slot getHandsSlot() {
        int selectedSlot = 0;
        if (this.minecraft != null && this.minecraft.player != null) {
            selectedSlot = this.minecraft.player.getInventory().getSelectedSlot();
        }
        int handsSlotIdx = this.menu.hotbarStart() + selectedSlot;
        if (handsSlotIdx < 0 || handsSlotIdx >= this.menu.slots.size()) {
            return null;
        }
        return this.menu.slots.get(handsSlotIdx);
    }

    private Slot getVirtualHandsHoverSlot(double mouseX, double mouseY) {
        Slot handsSlot = getHandsSlot();
        if (handsSlot == null) {
            return null;
        }

        ItemStack handsStack = handsSlot.getItem();
        int middleColumnX = this.getColumnX(1);
        int handsPanelY = this.topPos + this.imageHeight - 75;
        int bodyY = handsPanelY + (handsStack.isEmpty() ? 15 : 27);
        int bodyHeight = handsStack.isEmpty() ? 55 : 43;

        boolean hovering = mouseX >= (middleColumnX - 4) && mouseX < (middleColumnX + 166)
            && mouseY >= bodyY && mouseY < (bodyY + bodyHeight);

        return hovering ? handsSlot : null;
    }

    public int getColumnX(int colIndex) {
        int remaining = this.imageWidth - 486; // 486 is 3 * 162
        int gap = remaining / 4;
        return leftPos + gap + colIndex * (162 + gap);
    }

    private int groundGridHeight() {
        int totalCount = vicinityItems.size() + nearbyContainers.size();
        int rows = (int) Math.ceil(totalCount / 9.0);
        return 5 + (rows == 0 ? 18 : rows * 18);
    }

    private int getScrollContentHeight() {
        int height = groundGridHeight();
        int containerSize = this.menu.getContainerSize();
        if (containerSize > 0) {
            int containerRows = (int) Math.ceil(containerSize / 9.0);
            height += 10 + 18 + containerRows * 18 + 5;
        }
        return height;
    }

    private void updateSlotPositions() {
        int containerSize = this.menu.getContainerSize();
        int groundGridHeight = groundGridHeight();

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

        // 1. Container slots, scrolled with the Vicinity column
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

        // 2. Main inventory (27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                Slot slot = this.menu.slots.get(this.menu.inventoryStart() + col + row * 9);
                SlotAccessor accessor = (SlotAccessor) slot;
                accessor.setX(rightColumnX + col * 18 - leftPos);
                accessor.setY(30 + row * 18);
            }
        }

        // 3. Hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            Slot slot = this.menu.slots.get(this.menu.hotbarStart() + col);
            SlotAccessor accessor = (SlotAccessor) slot;
            accessor.setX(rightColumnX + col * 18 - leftPos);
            accessor.setY(imageHeight - 30);
        }

        // 4. Armour: helmet, chestplate, leggings, boots
        for (int i = 0; i < 4; i++) {
            Slot armorSlot = this.menu.slots.get(this.menu.armorStart() + i);
            ((SlotAccessor) armorSlot).setX(middleColumnX + 15 - leftPos);
            ((SlotAccessor) armorSlot).setY(35 + i * 25);
        }

        // 5. Offhand
        Slot offhandSlot = this.menu.slots.get(this.menu.offhandIndex());
        ((SlotAccessor) offhandSlot).setX(middleColumnX + 129 - leftPos);
        ((SlotAccessor) offhandSlot).setY(35);

        // 6. Crafting result, then the 2x2 grid
        Slot craftResultSlot = this.menu.slots.get(this.menu.resultIndex());
        ((SlotAccessor) craftResultSlot).setX(rightColumnX + 97 - leftPos);
        ((SlotAccessor) craftResultSlot).setY(126);

        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 2; c++) {
                Slot slot = this.menu.slots.get(this.menu.craftStart() + c + r * 2);
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

    // ------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------

    // 1.20.2 added the horizontal scroll axis to mouseScrolled; 1.20.1 only has
    // the vertical amount.
//? if >=1.20.2 {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
//?} else {
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        double scrollX = 0.0;
//?}
        float scale = getGuiScale();
        double scaledX = mouseX / scale;
        double scaledY = mouseY / scale;

        if (isMouseOverVicinity(scaledX, scaledY)) {
            this.scrollAmount = Math.max(0.0, this.scrollAmount - scrollY * 12.0);
            this.updateSlotPositions();
            return true;
        }
//? if >=1.20.2 {
        return super.mouseScrolled(scaledX, scaledY, scrollX, scrollY);
//?} else {
        return super.mouseScrolled(scaledX, scaledY, scrollY);
//?}
    }

    @Override
// 1.21.11 replaced the raw (mouseX, mouseY, button) input parameters with
// event objects; below that the coordinates still arrive as arguments.
//? if >=1.21.9 {
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
//?} else {
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean doubleClick = false;
//?}
        float scale = getGuiScale();
        double scaledX = mouseX / scale;
        double scaledY = mouseY / scale;

// 26.2 added InputWithModifiers#hasShiftDown, which is also the only correct
// test from 26.3 on: Minecraft moved from GLFW to SDL there, so the raw
// modifier bits are SDL keymods and GLFW_MOD_SHIFT no longer matches them.
//? if >=26.1 {
        boolean shiftDown = event.hasShiftDown();
//?} elif >=1.21.9 {
        boolean shiftDown = (event.modifiers() & org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT) != 0;
//?} else {
        boolean shiftDown = Screen.hasShiftDown();
//?}

        for (HeaderButton headerButton : this.headerButtons) {
            if (headerButton.contains(scaledX, scaledY)) {
                playClickSound();
                headerButton.action().run();
                return true;
            }
        }

        // Drag-and-drop between slots: only from an empty cursor, with the left
        // button and no shift, so vanilla's shift-click and drag-to-split keep
        // working. Mouse Tweaks brings its own drag gestures, so it is off there.
        Slot hoveredSlot = this.getSlotAt(scaledX, scaledY);
        boolean customDrag = DayZConfig.get().dragAndDrop && !ModCompat.hasMouseTweaks()
            && button == 0 && !shiftDown && this.menu.getCarried().isEmpty()
            && hoveredSlot != null && !hoveredSlot.getItem().isEmpty();
        this.draggedSlot = customDrag ? hoveredSlot : null;

        // Remember which button starts a drag-to-split, for Mouse Tweaks.
        if (!this.isQuickCrafting && !this.menu.getCarried().isEmpty()) {
            this.lastQuickCraftButton = button;
        }

        if (isMouseOverVicinity(scaledX, scaledY)) {
            int leftColumnX = getColumnX(0);
            int startY = topPos + 25;
            double clickX = scaledX - leftColumnX;
            double clickY = scaledY - startY + scrollAmount;

            // 1. Vicinity Grid Row check
            int totalCount = vicinityItems.size() + nearbyContainers.size();
            int groundGridHeight = groundGridHeight();

            if (clickY >= 5 && clickY < groundGridHeight && clickX >= 0 && clickX < 162) {
                int col = (int) (clickX / 18);
                int row = (int) ((clickY - 5) / 18);
                int index = row * 9 + col;

                if (index >= 0 && index < totalCount) {
                    if (index < vicinityItems.size()) {
                        // Ground Item Clicked
                        ItemEntity itemEntity = vicinityItems.get(index);
                        if (shiftDown) {
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
                        BlockPos openPos = this.menu.getContainerPos();
                        boolean isOpen = openPos != null && openPos.equals(container.pos);

                        playClickSound();

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

            // 2. The open container's header: sort and close buttons
            int containerSize = this.menu.getContainerSize();
            if (containerSize > 0) {
                int headerY = groundGridHeight + 10;
                if (clickY >= headerY && clickY < headerY + 18) {
                    if (clickX >= 147 && clickX < 161) {
                        playClickSound();

                        // Save cursor positions right before container closes
                        lastMouseX = this.minecraft.mouseHandler.xpos();
                        lastMouseY = this.minecraft.mouseHandler.ypos();
                        needsMouseRestore = true;

                        Platform.HELPER.sendPacketToServer(DayZInventoryPackets.OPEN_INVENTORY_PACKET, new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer()));
                        return true;
                    }
                    int sortX = containerSortX();
                    if (this.containerSortable && clickX >= sortX && clickX < sortX + buttonWidth(tr("dayz_inventory.button.sort"))) {
                        playClickSound();
                        this.sendSort(DayZInventoryPackets.SORT_CONTAINER);
                        return true;
                    }
                }
            }
        }
//? if >=1.21.9 {
        return super.mouseClicked(new MouseButtonEvent(scaledX, scaledY, event.buttonInfo()), doubleClick);
//?} else {
        return super.mouseClicked(scaledX, scaledY, button);
//?}
    }

    /** Where the container header's sort button starts, relative to the Vicinity column. */
    private int containerSortX() {
        return 144 - buttonWidth(tr("dayz_inventory.button.sort"));
    }

    @Override
//? if >=1.21.9 {
    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
//?} else {
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
//?}
        if (this.skipNextReleaseFromCompat) {
            this.skipNextReleaseFromCompat = false;
            return true;
        }

        float scale = getGuiScale();
        double scaledX = mouseX / scale;
        double scaledY = mouseY / scale;

        int middleColumnX = getColumnX(1);
        boolean releasedOverSurvivor = scaledX >= (middleColumnX - 4) && scaledX <= (middleColumnX + 166)
                && scaledY >= (topPos + 25) && scaledY <= (topPos + imageHeight - 5);

        // Custom drag release for vicinity items
        if (this.draggedStack != null) {
            Slot hoveredSlot = this.getSlotAt(scaledX, scaledY);
            int targetSlotIdx = -1;
            if (hoveredSlot != null) {
                targetSlotIdx = hoveredSlot.index;
            } else if (releasedOverSurvivor && this.minecraft.player != null) {
                // Dropped onto the Survivor panel: equip it, if it is equippable.
                targetSlotIdx = this.menu.equipmentSlotIndex(this.minecraft.player, this.draggedStack);
            }
            if (targetSlotIdx != -1) {
                FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                buf.writeInt(this.draggedEntity.getId());
                buf.writeInt(targetSlotIdx);
                buf.writeInt(this.draggedStack.getCount());
                Platform.HELPER.sendPacketToServer(DayZInventoryPackets.PICKUP_ITEM_PACKET, buf);
            }
            this.draggedEntity = null;
            this.draggedStack = null;
            return true;
        }

        // Custom slot drag release (true drag-and-drop UX)
        if (this.draggedSlot != null) {
            Slot sourceSlot = this.draggedSlot;
            this.draggedSlot = null;
            Slot releaseSlot = this.getSlotAt(scaledX, scaledY);
            ItemStack carried = this.menu.getCarried();
            if (!carried.isEmpty()) {
                if (releaseSlot != null && releaseSlot != sourceSlot) {
                    this.slotClicked(releaseSlot, releaseSlot.index, 0, ContainerInput.PICKUP);
                    return true;
                } else if (releasedOverSurvivor && releaseSlot != sourceSlot && this.minecraft.player != null) {
                    // Dragged onto the Survivor panel: equip it, if it is equippable.
                    int targetSlotIdx = this.menu.equipmentSlotIndex(this.minecraft.player, carried);
                    if (targetSlotIdx != -1) {
                        this.slotClicked(this.menu.slots.get(targetSlotIdx), targetSlotIdx, 0, ContainerInput.PICKUP);
                        return true;
                    }
                } else if (scaledX < leftPos || scaledY < topPos || scaledX > leftPos + imageWidth || scaledY > topPos + imageHeight) {
                    // Dropped outside the panels: throw it.
                    this.slotClicked(null, -999, 0, ContainerInput.PICKUP);
                    return true;
                }
            }
        }

//? if >=1.21.9 {
        return super.mouseReleased(new MouseButtonEvent(scaledX, scaledY, event.buttonInfo()));
//?} else {
        return super.mouseReleased(scaledX, scaledY, button);
//?}
    }

    @Override
//? if >=1.21.9 {
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        float scale = getGuiScale();
        return super.mouseDragged(new MouseButtonEvent(event.x() / scale, event.y() / scale, event.buttonInfo()),
                                   dragX / scale, dragY / scale);
    }
//?} else {
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        float scale = getGuiScale();
        return super.mouseDragged(mouseX / scale, mouseY / scale, button, dragX / scale, dragY / scale);
    }
//?}

    private Slot getSlotAt(double mouseX, double mouseY) {
        // The virtual Hands slot in the middle column
        Slot handsSlot = getVirtualHandsHoverSlot(mouseX, mouseY);
        if (handsSlot != null) {
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
//? if >=1.21.9 {
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
//?} else {
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
//?}
        if (getSlotAt(mouseX, mouseY) != null) {
            return false;
        }
        if (isMouseOverVicinity(mouseX, mouseY)) {
            return true;
        }
        return mouseX < left || mouseY < top || mouseX > left + imageWidth || mouseY > top + imageHeight;
    }

    // ------------------------------------------------------------------
    // Mouse Tweaks support (called from MouseTweaksCompatMixin)
    // ------------------------------------------------------------------

    /** Whether the cursor is over the Vicinity panel right now. */
    public boolean isCursorOverVicinity() {
        if (this.minecraft == null || this.minecraft.getWindow().getWidth() <= 0 || this.minecraft.getWindow().getHeight() <= 0) {
            return false;
        }
        double guiX = this.minecraft.mouseHandler.xpos() * this.minecraft.getWindow().getGuiScaledWidth() / this.minecraft.getWindow().getWidth();
        double guiY = this.minecraft.mouseHandler.ypos() * this.minecraft.getWindow().getGuiScaledHeight() / this.minecraft.getWindow().getHeight();
        float scale = getGuiScale();
        return isMouseOverVicinity(guiX / scale, guiY / scale);
    }

    /** The slot at a position in unscaled GUI coordinates. */
    public Slot findSlotAtScreenPosition(double mouseX, double mouseY) {
        float scale = getGuiScale();
        return getSlotAt(mouseX / scale, mouseY / scale);
    }

    /** Cancels a right-button drag-to-split, the way vanilla's own handler would. */
    public boolean cancelRightClickDrag() {
        this.skipNextReleaseFromCompat = true;
        if (this.isQuickCrafting && this.lastQuickCraftButton == 1) {
            this.isQuickCrafting = false;
            return true;
        }
        return false;
    }

    public void clickSlotFromCompat(Slot slot, int mouseButton, ContainerInput containerInput) {
        this.slotClicked(slot, slot.index, mouseButton, containerInput);
    }

    // ------------------------------------------------------------------
    // Equipment mods
    // ------------------------------------------------------------------

    /**
     * Opens the Curios screen by registering a click on Curios' own "open"
     * key mapping, which Curios polls every client tick. If the mapping cannot
     * be found, the vanilla inventory opens instead: Curios shows its button
     * there.
     */
    private void openCurios() {
        if (!ModCompat.clickKeyMapping(this.minecraft, new String[]{"key.curios.open.desc"}, "curios", "open")) {
            openVanillaInventoryForOptionalMods();
        }
    }

    /**
     * Opens the Accessories screen. Accessories only acts on its key while no
     * screen is open, so this screen closes first.
     */
    private void openAccessories() {
        this.onClose();
        if (!ModCompat.clickKeyMapping(this.minecraft,
                new String[]{"accessories.key.open_accessories_screen", "key.accessories.open_accessories_screen"},
                "accessories", "open_accessories")) {
            openVanillaInventoryForOptionalMods();
        }
    }

    /**
     * Lets the vanilla inventory screen open even though the redirect mixin would
     * normally swap it for the DayZ one. Curios, Accessories and Trinkets all add
     * their slots or buttons to the vanilla screen, so this is the dependable
     * fallback.
     */
    private void openVanillaInventoryForOptionalMods() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        Platform.allowVanillaInventory = true;
        try {
            this.minecraft.gui.setScreen(new InventoryScreen(this.minecraft.player));
        } finally {
            Platform.allowVanillaInventory = false;
        }
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    /**
     * Deliberately empty.
     * <p>
     * From 1.21.6 on the game calls this from {@code Screen#renderWithTooltip}
     * (26.x renamed that to {@code extractRenderStateWithTooltipAndSubtitles}),
     * which runs <b>before</b> {@link #extractRenderState} and outside this screen's
     * GUI scale. Vanilla puts two things here - the full-screen dim and the container
     * background (this screen's DayZ panels) - and they need opposite treatment: the
     * dim must be drawn at identity pose or it only covers {@code width/scale} of the
     * screen, while the panels must be drawn <i>under</i> the scale or the layout is
     * wrong.
     * <p>
     * So neither is done here. {@link #extractRenderState} draws the dim at identity
     * and then calls {@link #drawDayZPanels} once the scale is applied.
     */
    // The boundary is 1.21.6, not 1.21.11: that is where vanilla moved
    // `renderBackground` out of `Screen#render` into `Screen#renderWithTooltip`,
    // which runs *before* this screen's `render`. Drawing the panels from there
    // puts them outside our GUI-scale push - laid out for the virtual
    // `width/scale` space but drawn at 1x - so they come out too large, and the
    // full-screen dim drawn later in `render` covers them. From 1.21.6 on they
    // are drawn in `extractRenderState` instead, under the scale, exactly as
    // 26.x does it. On 1.20.2-1.21.5 `Screen#render` still calls
    // `renderBackground`, so the panels stay inside the scale push and the
    // override below is correct.
    //
    // 1.20.1 has no renderBackground(GuiGraphics, int, int, float) to override - it
    // has only the one-argument form, and it draws the panels from renderBg via
    // vanilla's render path - so on that node this method does not exist.
//? if >=1.21.6 {
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        return;
    }
//?} elif >=1.20.2 {
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }
//?}

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Restore cursor positions on render frame tick if requested
        if (needsMouseRestore && this.minecraft != null) {
            needsMouseRestore = false;
// 26.3 replaced GLFW with SDL3, so `org.lwjgl.glfw` is no longer on the compile
// classpath at all. SDL_WarpMouseInWindow is the direct equivalent and takes
// floats rather than doubles.
//? if >=26.3 {
            org.lwjgl.sdl.SDLMouse.SDL_WarpMouseInWindow(this.minecraft.getWindow().handle(), (float) lastMouseX, (float) lastMouseY);
//?} else {
            org.lwjgl.glfw.GLFW.glfwSetCursorPos(this.minecraft.getWindow().handle(), lastMouseX, lastMouseY);
//?}

            // Re-calculate scaled mouseX and mouseY so hover checks resolve correctly on this frame
            mouseX = (int) (lastMouseX * (double) this.minecraft.getWindow().getGuiScaledWidth() / (double) this.minecraft.getWindow().getWidth());
            mouseY = (int) (lastMouseY * (double) this.minecraft.getWindow().getGuiScaledHeight() / (double) this.minecraft.getWindow().getHeight());
        }

        // Full-screen dim, drawn here at identity pose before the GUI scale is
        // applied below, so it always covers the entire screen. These are the
        // same colours vanilla's Screen#renderTransparentBackground uses.
//? if >=1.20.2 {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
//?} else {
        this.renderBackground(guiGraphics);
//?}

        float scale = getGuiScale();
        int scaledMouseX = (int) (mouseX / scale);
        int scaledMouseY = (int) (mouseY / scale);

        // Reset per-frame hover state
        this.hoveredContainer = null;
        this.pendingTooltip = null;

        // Push pose and apply scale
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);

        this.updateSlotPositions();
        // The DayZ panels are laid out in the scaled coordinate space, so they
        // have to be drawn here rather than from extractBackground - which on
        // 1.21.6+ runs before this method and outside the scale. See the comment
        // on extractBackground above.
//? if >=1.21.6 {
        this.drawDayZPanels(guiGraphics, partialTick, scaledMouseX, scaledMouseY);
//?}
        super.extractRenderState(guiGraphics, scaledMouseX, scaledMouseY, partialTick);
        this.extractTooltip(guiGraphics, scaledMouseX, scaledMouseY);

        // Render custom dragged stack on cursor
        if (this.draggedStack != null && !this.draggedStack.isEmpty()) {
            guiGraphics.fakeItem(this.draggedStack, scaledMouseX - 8, scaledMouseY - 8);
            guiGraphics.itemDecorations(this.font, this.draggedStack, scaledMouseX - 8, scaledMouseY - 8);
        }

        // Render tooltip for vicinity items
        if (this.draggedStack == null && isMouseOverVicinity(scaledMouseX, scaledMouseY)) {
            int leftColumnX = getColumnX(0);
            int startY = topPos + 25;
            double clickX = scaledMouseX - leftColumnX;
            double clickY = scaledMouseY - startY + scrollAmount;

            if (clickY >= 5 && clickY < groundGridHeight() && clickX >= 0 && clickX < 162) {
                int col = (int) (clickX / 18);
                int row = (int) ((clickY - 5) / 18);
                int index = row * 9 + col;
                if (index >= 0 && index < vicinityItems.size()) {
                    guiGraphics.setTooltipForNextFrame(this.font, vicinityItems.get(index).getItem(), scaledMouseX, scaledMouseY);
                }
            }
        }

        guiGraphics.pose().popMatrix();

        // Container details tooltip, at 1x global scale
        if (this.hoveredContainer != null && this.minecraft != null && this.minecraft.player != null) {
            List<Component> tooltipText = new ArrayList<>();
            tooltipText.add(Component.literal(this.hoveredContainer.name).withStyle(ChatFormatting.YELLOW));
            BlockPos pos = this.hoveredContainer.pos;
            tooltipText.add(Component.translatable("dayz_inventory.container.location", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GRAY));
            double dist = Math.sqrt(this.minecraft.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            tooltipText.add(Component.translatable("dayz_inventory.container.distance", String.format(Locale.ROOT, "%.1f", dist)).withStyle(ChatFormatting.GREEN));
            this.pendingTooltip = tooltipText;
        }

        if (this.pendingTooltip != null && !this.pendingTooltip.isEmpty()) {
            guiGraphics.setComponentTooltipForNextFrame(this.font, this.pendingTooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // Disable default label rendering (we render custom headers in drawDayZPanels)
    }

    /**
     * Draws the DayZ panels. {@code mouseX}/{@code mouseY} are already in this
     * screen's scaled layout space.
     * <p>
     * This is no longer an override: 26.2 removed
     * {@code AbstractContainerScreen#renderBg} entirely, so the method it used to
     * override does not exist. It is now a plain private helper called from
     * {@link #extractRenderState}, which is what it always effectively was.
     */
//? if >=26.1 {
    private void drawDayZPanels(GuiGraphicsExtractor guiGraphics, float partialTick, int mouseX, int mouseY) {
//?} else {
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
//?}
        int leftColumnX = getColumnX(0);
        int middleColumnX = getColumnX(1);
        int rightColumnX = getColumnX(2);
        int viewportHeight = imageHeight - 40;

        float scale = getGuiScale();

        // 1. Section panels, derived from getColumnX() to guarantee alignment with slots
        int lx = leftColumnX;
        int mx = middleColumnX;
        int rx = rightColumnX;
        final int PANEL_W = 164;
        final int PAD = 2;

        // Left Column (Vicinity)
        drawSectionPanel(guiGraphics, lx - PAD, topPos + 5, PANEL_W, 17); // Header
        drawSectionPanel(guiGraphics, lx - PAD, topPos + 25, PANEL_W, viewportHeight); // Content

        // Middle Column (Survivor)
        drawSectionPanel(guiGraphics, mx - PAD, topPos + 5, PANEL_W, 17); // Header
        for (int i = 0; i < 4; i++) {
            drawSectionPanel(guiGraphics, mx + 13, topPos + 33 + i * 25, 20, 20); // Armour
        }
        drawSectionPanel(guiGraphics, mx + 127, topPos + 33, 20, 20); // Offhand

        // Right Column (Inventory)
        drawSectionPanel(guiGraphics, rx - PAD, topPos + 5, PANEL_W, 17); // Header
        drawSectionPanel(guiGraphics, rx - PAD, topPos + 28, PANEL_W, 56);
        drawSectionPanel(guiGraphics, rx - PAD, topPos + 90, PANEL_W, 90);
        guiGraphics.fill(rx - PAD, topPos + 107, rx - PAD + PANEL_W, topPos + 108, 0x10FFFFFF); // separator
        String crafting = tr("dayz_inventory.crafting");
        guiGraphics.text(this.font, crafting, rx + 80 - (this.font.width(crafting) / 2), topPos + 96, TEXT_COLOR, false);
        guiGraphics.text(this.font, "->", rx + 72, topPos + 130, TEXT_COLOR, false);
        drawSectionPanel(guiGraphics, rx - PAD, topPos + imageHeight - 32, PANEL_W, 20);

        // 2. Header buttons
        for (HeaderButton headerButton : this.headerButtons) {
            drawButton(guiGraphics, headerButton.x(), headerButton.y(), headerButton.width(), headerButton.label(),
                headerButton.contains(mouseX, mouseY));
        }

        // 3. Column titles
        String vicinity = tr("dayz_inventory.vicinity");
        String survivor = tr("dayz_inventory.survivor");
        String inventory = tr("dayz_inventory.inventory");
        guiGraphics.text(this.font, vicinity, lx + (162 - this.font.width(vicinity)) / 2, topPos + 9, 0xFFFFFFFF, true);
        int survivorX = this.survivorTitleLeft ? mx + 4 : mx + (162 - this.font.width(survivor)) / 2;
        guiGraphics.text(this.font, survivor, survivorX, topPos + 9, 0xFFFFFFFF, true);
        guiGraphics.text(this.font, inventory, rx + (162 - this.font.width(inventory)) / 2, topPos + 9, 0xFFFFFFFF, true);

        // 4. Hands panel at the bottom of the middle column
        int handsPanelY = topPos + imageHeight - 75;

        drawSectionPanel(guiGraphics, mx - 2, handsPanelY, 166, 70);
        guiGraphics.fill(mx - 2, handsPanelY + 15, mx + 164, handsPanelY + 16, 0x10FFFFFF); // divider line
        String hands = tr("dayz_inventory.hands");
        guiGraphics.text(this.font, hands, mx + 81 - (this.font.width(hands) / 2), handsPanelY + 4, TEXT_COLOR, false);

        Slot handsSlot = getHandsSlot();
        ItemStack handsStack = handsSlot != null ? handsSlot.getItem() : ItemStack.EMPTY;
        int bodyY = handsPanelY + (handsStack.isEmpty() ? 15 : 27);
        int bodyHeight = handsStack.isEmpty() ? 55 : 43;

        if (!handsStack.isEmpty()) {
            String handsItemName = handsStack.getHoverName().getString().toUpperCase(Locale.ROOT);
            guiGraphics.fill(mx - 2, handsPanelY + 27, mx + 164, handsPanelY + 28, 0x10FFFFFF);
            guiGraphics.text(this.font, handsItemName, mx + 81 - (this.font.width(handsItemName) / 2), handsPanelY + 17, TEXT_COLOR, false);
        }

        // Hover highlight for the Hands slot body
        boolean hoverHands = mouseX >= middleColumnX - 4 && mouseX < middleColumnX + 166
            && mouseY >= bodyY && mouseY < (bodyY + bodyHeight);
        if (hoverHands) {
            guiGraphics.fill(middleColumnX - 3, bodyY + 1, middleColumnX + 165, bodyY + bodyHeight - 1, 0x30FFFFFF);
        }

        // The held item, drawn at 2x
        if (!handsStack.isEmpty()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(middleColumnX + 65, bodyY + 5);
            guiGraphics.pose().scale(2.0F, 2.0F);
            guiGraphics.fakeItem(handsStack, 0, 0);
            guiGraphics.itemDecorations(this.font, handsStack, 0, 0);
            guiGraphics.pose().popMatrix();
        }

        if (this.minecraft != null && this.minecraft.player != null) {
//? if >=1.20.2 {
            // Draw 3D Player entity.
            //
            // 1.21.11 removed InventoryScreen.renderEntityInInventory; only the
            // follows-mouse variant survives, and it performs the manual body/head
            // rotation this block used to do by hand. The bounding box and scale
            // mirror vanilla's own inventory preview, scaled up for this panel.
            int renderX = middleColumnX + 81;
            int renderY = topPos + 135;
            int renderScale = 55;
            int halfWidth = 45;
            int halfHeight = 64;

            // This does NOT draw immediately: it queues a render state that is
            // drawn in a later pass, outside this screen's GUI scale. So the box
            // has to be converted from our scaled layout space into real screen
            // coordinates, or the model lands wherever those numbers happen to
            // fall on screen instead of inside the panel.
            float guiScale = getGuiScale();

            // renderY used to mean the model's FEET - the old API translated to
            // (x, y) before drawing, and models are drawn from the feet up. The
            // replacement centres the model in the box instead, so the box centre
            // has to be raised by half the model's rendered height to land the
            // model where it used to sit.
            float modelHeightPx = this.minecraft.player.getBbHeight() * (float) renderScale;
            int boxCentreY = renderY - Math.round(modelHeightPx / 2.0f);

            // Submit the preview at the identity pose.
            //
            // The box below is in real screen coordinates, but
            // `renderEntityInInventoryFollowsMouse` scissors it, and from 1.21.4
            // GuiGraphics#enableScissor transforms the rectangle through the
            // CURRENT pose. The panels are drawn under our virtual GUI scale, so
            // leaving that scale on scissors the box a second time
            // (`scale * box`) while the model itself is submitted from the raw box:
            // it lands in the right place and is then clipped by a smaller, offset
            // rectangle. Cancelling the scale for the call makes the model and its
            // scissor agree on every version.
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().scale(1.0F / guiScale, 1.0F / guiScale);

            InventoryScreen.extractEntityInInventoryFollowsMouse(
                guiGraphics,
                Math.round((renderX - halfWidth) * guiScale), Math.round((boxCentreY - halfHeight) * guiScale),
                Math.round((renderX + halfWidth) * guiScale), Math.round((boxCentreY + halfHeight) * guiScale),
                Math.round(renderScale * guiScale),
                0.0625F,
                (float) mouseX * guiScale, (float) mouseY * guiScale,
                this.minecraft.player
            );

            guiGraphics.pose().popMatrix();
//?} else {
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
//?}

            // 5. Vitals and status effects
            if (DayZConfig.get().showVitals) {
                drawVitals(guiGraphics, mx, mouseX, mouseY);
            }
            if (DayZConfig.get().showStatusEffects) {
                drawStatusEffects(guiGraphics, mx, mouseX, mouseY);
            }
        }

        // 6. Slot backgrounds
        for (Slot slot : this.menu.slots) {
            if (slot.x >= 0) {
                guiGraphics.fill(
                    leftPos + slot.x, topPos + slot.y,
                    leftPos + slot.x + 16, topPos + slot.y + 16,
                    0x18FFFFFF
                );
            }
        }

        // 7. Vicinity list
        renderVicinityList(guiGraphics, mouseX, mouseY, scale);

        // 8. Scrollbar
        int contentHeight = getScrollContentHeight();
        if (contentHeight > viewportHeight) {
            int maxScroll = contentHeight - viewportHeight;
            int barHeight = Math.max(20, (viewportHeight * viewportHeight) / contentHeight);
            int barTop = topPos + 25 + (int) ((scrollAmount * (viewportHeight - barHeight)) / maxScroll);
            guiGraphics.fill(leftColumnX + 163, topPos + 25, leftColumnX + 165, topPos + 25 + viewportHeight, 0xFF373737);
            guiGraphics.fill(leftColumnX + 163, barTop, leftColumnX + 165, barTop + barHeight, 0xFFFFFFFF);
        }
    }

    /**
     * Health, food and - with a supported thirst mod - thirst, as three thin
     * vertical bars to the left of the armour column.
     */
    private void drawVitals(GuiGraphicsExtractor guiGraphics, int mx, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        int top = topPos + 33;
        int height = 95;

        float maxHealth = Math.max(1.0F, this.minecraft.player.getMaxHealth());
        float health = this.minecraft.player.getHealth();
        int food = this.minecraft.player.getFoodData().getFoodLevel();

        drawVitalBar(guiGraphics, mx + 1, top, height, health / maxHealth, 0xFFD03A3A);
        drawVitalBar(guiGraphics, mx + 5, top, height, food / 20.0F, 0xFFC98A3C);
        if (this.thirst >= 0) {
            drawVitalBar(guiGraphics, mx + 9, top, height, this.thirst / (float) ThirstCompat.MAX, 0xFF3C8DD0);
        }

        if (mouseY >= top && mouseY < top + height) {
            if (mouseX >= mx && mouseX < mx + 4) {
                this.pendingTooltip = List.of(Component.translatable("dayz_inventory.vitals.health",
                    (int) Math.ceil(health), (int) maxHealth));
            } else if (mouseX >= mx + 4 && mouseX < mx + 8) {
                this.pendingTooltip = List.of(Component.translatable("dayz_inventory.vitals.food", food, 20));
            } else if (this.thirst >= 0 && mouseX >= mx + 8 && mouseX < mx + 12) {
                this.pendingTooltip = List.of(Component.translatable("dayz_inventory.vitals.thirst", this.thirst, ThirstCompat.MAX));
            }
        }
    }

    private void drawVitalBar(GuiGraphicsExtractor guiGraphics, int x, int top, int height, float fraction, int color) {
        guiGraphics.fill(x, top, x + 3, top + height, 0x40FFFFFF);
        int filled = Math.round(height * Math.max(0.0F, Math.min(1.0F, fraction)));
        if (filled > 0) {
            guiGraphics.fill(x, top + height - filled, x + 3, top + height, color);
        }
    }

    /**
     * Active status effects as icons in the column right of the player model,
     * below the offhand slot. Hovering one shows its name, level and time left;
     * an icon blinks during its last ten seconds, as in vanilla.
     */
    private void drawStatusEffects(GuiGraphicsExtractor guiGraphics, int mx, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        List<MobEffectInstance> effects = new ArrayList<>();
        for (MobEffectInstance instance : this.minecraft.player.getActiveEffects()) {
            if (instance.showIcon()) {
                effects.add(instance);
            }
        }
        if (effects.isEmpty()) return;
        Collections.sort(effects);

        int x = mx + 128;
        int y = topPos + 58;
        boolean overflow = effects.size() > MAX_EFFECT_ICONS;
        int shown = overflow ? MAX_EFFECT_ICONS - 1 : effects.size();

        for (int i = 0; i < shown; i++) {
            MobEffectInstance instance = effects.get(i);
            int iconY = y + i * 20;
            drawSectionPanel(guiGraphics, x - 1, iconY - 1, 20, 20);
            drawEffectIcon(guiGraphics, instance, x, iconY);
            int duration = instance.getDuration();
            if (!instance.isInfiniteDuration() && duration <= 200 && (duration / 10) % 2 == 0) {
                guiGraphics.fill(x, iconY, x + 18, iconY + 18, 0x90000000);
            }
            if (mouseX >= x - 1 && mouseX < x + 19 && mouseY >= iconY - 1 && mouseY < iconY + 19) {
                this.pendingTooltip = List.of(effectName(instance),
                    Component.literal(formatDuration(instance)).withStyle(ChatFormatting.GRAY));
            }
        }

        if (overflow) {
            int iconY = y + shown * 20;
            drawSectionPanel(guiGraphics, x - 1, iconY - 1, 20, 20);
            String more = "+" + (effects.size() - shown);
            guiGraphics.text(this.font, more, x + 9 - this.font.width(more) / 2, iconY + 5, TEXT_COLOR, false);
            if (mouseX >= x - 1 && mouseX < x + 19 && mouseY >= iconY - 1 && mouseY < iconY + 19) {
                List<Component> lines = new ArrayList<>();
                for (int i = shown; i < effects.size(); i++) {
                    lines.add(effectName(effects.get(i)).copy().append(" ")
                        .append(Component.literal(formatDuration(effects.get(i))).withStyle(ChatFormatting.GRAY)));
                }
                this.pendingTooltip = lines;
            }
        }
    }

    private Component effectName(MobEffectInstance instance) {
        net.minecraft.network.chat.MutableComponent name = instance.getEffect().value().getDisplayName().copy();
        int amplifier = instance.getAmplifier();
        if (amplifier >= 1 && amplifier <= 9) {
            name.append(" ").append(Component.translatable("enchantment.level." + (amplifier + 1)));
        }
        return name.withStyle(instance.getEffect().value().isBeneficial() ? ChatFormatting.WHITE : ChatFormatting.RED);
    }

    private static String formatDuration(MobEffectInstance instance) {
        if (instance.isInfiniteDuration()) {
            return "∞";
        }
        int seconds = Math.max(0, instance.getDuration() / 20);
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }

    /**
     * Draws a mob effect's icon at 18x18. The icon moved twice: an atlas sprite
     * blitted directly until 1.21.1, the same sprite through a render-type
     * function on 1.21.2-1.21.5, and a GUI-atlas sprite id from 1.21.6 (looked up
     * through Hud rather than Gui from 26.2).
     */
    private void drawEffectIcon(GuiGraphicsExtractor guiGraphics, MobEffectInstance instance, int x, int y) {
//? if >=26.2 {
        guiGraphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, net.minecraft.client.gui.Hud.getMobEffectSprite(instance.getEffect()), x, y, 18, 18);
//?} elif >=1.21.6 {
        guiGraphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, net.minecraft.client.gui.Gui.getMobEffectSprite(instance.getEffect()), x, y, 18, 18);
//?} elif >=1.21.2 {
        guiGraphics.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, this.minecraft.getMobEffectTextures().get(instance.getEffect()), x, y, 18, 18);
//?} else {
        guiGraphics.blit(x, y, 0, 18, 18, this.minecraft.getMobEffectTextures().get(instance.getEffect()));
//?}
    }

    private void renderVicinityList(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float scale) {
        int leftColumnX = getColumnX(0);
        int startY = topPos + 25;
        int viewportHeight = this.imageHeight - 40;
        int endY = startY + viewportHeight;

        // The clip rectangle is given in real screen coordinates with this
        // screen's scale cancelled, for the same reason as the player preview:
        // from 1.21.4 enableScissor transforms the rectangle through the current
        // pose, and below that it does not. Cancelling the scale makes both agree;
        // passing pre-scaled numbers under the scale clipped the list a second
        // time on small windows.
        float guiScale = scale;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(1.0F / guiScale, 1.0F / guiScale);
        guiGraphics.enableScissor(
                (int) ((leftColumnX - 4) * guiScale),
                (int) (startY * guiScale),
                (int) Math.ceil((leftColumnX + 166) * guiScale),
                (int) Math.ceil(endY * guiScale)
        );
        guiGraphics.pose().popMatrix();

        int relY = 5;

        int totalCount = vicinityItems.size() + nearbyContainers.size();
        int rows = (int) Math.ceil(totalCount / 9.0);

        if (totalCount == 0) {
            guiGraphics.text(this.font, tr("dayz_inventory.vicinity.empty"), leftColumnX + 4, startY + relY - (int) scrollAmount, 0xFF888888, false);
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
                        guiGraphics.fakeItem(stack, itemX + 1, itemY + 1);
                        guiGraphics.itemDecorations(this.font, stack, itemX + 1, itemY + 1);
                    } else {
                        int containerIndex = i - vicinityItems.size();
                        ContainerBlockInfo container = nearbyContainers.get(containerIndex);
                        guiGraphics.fakeItem(container.icon, itemX + 1, itemY + 1);

                        if (hovering) {
                            this.hoveredContainer = container;
                        }

                        BlockPos openPos = this.menu.getContainerPos();
                        boolean isOpen = openPos != null && openPos.equals(container.pos);
                        String chevron = isOpen ? "^" : "v";

                        guiGraphics.pose().pushMatrix();
                        guiGraphics.pose().translate(0, 0);
                        guiGraphics.text(this.font, chevron, itemX + 11, itemY + 9, 0xFFFFFFFF, true);
                        guiGraphics.pose().popMatrix();
                    }
                }
            }
            relY += rows * 18;
        }

        int containerSize = this.menu.getContainerSize();
        BlockPos openPos = this.menu.getContainerPos();

        if (containerSize > 0 && openPos != null) {
            relY += 10;
            int headerY = startY + relY - (int) scrollAmount;

            String containerName = tr("dayz_inventory.container.default");
            if (this.minecraft != null && this.minecraft.level != null) {
                BlockState state = this.minecraft.level.getBlockState(openPos);
                if (state != null && !state.isAir()) {
                    containerName = state.getBlock().getName().getString().toUpperCase(Locale.ROOT);
                }
            }

            if (headerY + 18 >= startY && headerY <= endY) {
                drawSectionPanel(guiGraphics, leftColumnX - 1, headerY, 162, 18);
                guiGraphics.text(this.font, containerName, leftColumnX + 5, headerY + 5, TEXT_COLOR, false);

                if (this.containerSortable) {
                    String sort = tr("dayz_inventory.button.sort");
                    int sortX = leftColumnX + containerSortX();
                    boolean hoverSort = mouseX >= sortX && mouseX < sortX + buttonWidth(sort) && mouseY >= headerY + 2 && mouseY < headerY + 2 + BUTTON_HEIGHT;
                    drawButton(guiGraphics, sortX, headerY + 2, buttonWidth(sort), sort, hoverSort);
                }

                boolean hoverClose = mouseX >= leftColumnX + 147 && mouseX < leftColumnX + 161 && mouseY >= headerY && mouseY <= headerY + 18;
                int closeColor = hoverClose ? 0xFFE04040 : 0xFF888888;
                guiGraphics.text(this.font, "x", leftColumnX + 150, headerY + 4, closeColor, false);
            }

            relY += 18;
        }

        guiGraphics.disableScissor();
    }

    private void drawButton(GuiGraphicsExtractor guiGraphics, int x, int y, int width, String label, boolean hovered) {
        guiGraphics.fill(x, y, x + width, y + BUTTON_HEIGHT, hovered ? 0x35FFFFFF : 0x15FFFFFF);
        int textWidth = this.font.width(label);
        guiGraphics.text(this.font, label, x + (width - textWidth) / 2, y + (BUTTON_HEIGHT - 8) / 2 + 1, TEXT_COLOR, false);
    }

    private void drawSectionPanel(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0x9E0C0C0C);
    }

    private void drawDayZSlot(GuiGraphicsExtractor guiGraphics, int x, int y) {
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0x18FFFFFF);
    }
}

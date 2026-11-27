/*
 * Compile-time stand-in for Mouse Tweaks' public API interface
 * (https://github.com/YaLTeR/MouseTweaks, src/main/java/yalter/mousetweaks/api).
 *
 * It exists only so MouseTweaksCompatMixin compiles. `dayz-loader` strips the
 * whole `yalter` package from every jar, and the compat mixin that adds this
 * interface to the DayZ screen is only applied when Mouse Tweaks itself - and
 * therefore the real interface - is installed. The signatures must match the
 * real ones exactly; `ContainerInput` is written as `ClickType` below 26.1 by the
 * same build replacement the rest of the sources use.
 */
package yalter.mousetweaks.api;

import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public interface IMTModGuiContainer3Ex {
    boolean MT_isMouseTweaksDisabled();

    boolean MT_isWheelTweakDisabled();

    List<Slot> MT_getSlots();

    Slot MT_getSlotUnderMouse(double mouseX, double mouseY);

    boolean MT_isCraftingOutput(Slot slot);

    boolean MT_isIgnored(Slot slot);

    boolean MT_disableRMBDraggingFunctionality();

    void MT_clickSlot(Slot slot, int mouseButton, ContainerInput containerInput);
}

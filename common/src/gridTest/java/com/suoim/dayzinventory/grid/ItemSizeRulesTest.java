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
package com.suoim.dayzinventory.grid;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemSizeRulesTest {
    private static ItemSizeRules.Size size(int width, int height) {
        return new ItemSizeRules.Size(width, height);
    }

    private static ItemSizeRules parse(String text) {
        List<String> warnings = new ArrayList<>();
        ItemSizeRules rules = ItemSizeRules.parse(text, true, warnings::add);
        assertTrue(warnings.isEmpty(), () -> "unexpected warnings: " + warnings);
        return rules;
    }

    @Test
    void theDefaultFileParsesWithoutWarnings() {
        ItemSizeRules rules = parse(ItemSizeRules.defaultFileText());
        assertTrue(rules.enabled());
        assertEquals(size(1, 2), rules.sizeOf("minecraft:diamond_sword"));
        assertEquals(size(1, 2), rules.sizeOf("minecraft:netherite_pickaxe"));
        assertEquals(size(1, 2), rules.sizeOf("minecraft:iron_axe"));
        assertEquals(size(1, 3), rules.sizeOf("minecraft:trident"));
        assertEquals(size(2, 2), rules.sizeOf("minecraft:shield"));
        assertEquals(size(2, 2), rules.sizeOf("minecraft:diamond_chestplate"));
        assertEquals(size(2, 2), rules.sizeOf("minecraft:shulker_box"));
        assertEquals(size(2, 2), rules.sizeOf("minecraft:red_shulker_box"));
        assertEquals(ItemSizeRules.ONE, rules.sizeOf("minecraft:cobblestone"));
        assertEquals(ItemSizeRules.ONE, rules.sizeOf("minecraft:diamond_helmet"));
    }

    @Test
    void wildcardsMatchModdedItemsToo() {
        ItemSizeRules rules = parse(ItemSizeRules.defaultFileText());
        assertEquals(size(1, 2), rules.sizeOf("somemod:obsidian_sword"));
        assertEquals(size(1, 2), rules.sizeOf("somemod:battle_axe"));
    }

    @Test
    void pickaxesAreNotAxesByAccident() {
        // "_axe" must not match "pickaxe" without its underscore; both are 1x2
        // in the defaults, so check with rules where they differ.
        ItemSizeRules rules = parse("*_axe = 2x2\n*_pickaxe = 1x3");
        assertEquals(size(1, 3), rules.sizeOf("minecraft:iron_pickaxe"));
        assertEquals(size(2, 2), rules.sizeOf("minecraft:iron_axe"));
    }

    @Test
    void theFirstMatchingRuleWins() {
        ItemSizeRules rules = parse("minecraft:wooden_sword = 1x1\n*_sword = 1x3");
        assertEquals(ItemSizeRules.ONE, rules.sizeOf("minecraft:wooden_sword"));
        assertEquals(size(1, 3), rules.sizeOf("minecraft:stone_sword"));
    }

    @Test
    void idsWithoutANamespaceAreMinecraftIds() {
        ItemSizeRules rules = parse("trident = 1x3");
        assertEquals(size(1, 3), rules.sizeOf("minecraft:trident"));
        assertEquals(ItemSizeRules.ONE, rules.sizeOf("othermod:trident"));
    }

    @Test
    void tagRulesAskTheGame() {
        ItemSizeRules rules = parse("#minecraft:swords = 1x3");
        Object item = new Object();
        assertEquals(size(1, 3), rules.sizeOf(item, "minecraft:iron_sword", tag -> tag.equals("minecraft:swords")));
        Object other = new Object();
        assertEquals(ItemSizeRules.ONE, rules.sizeOf(other, "minecraft:stick", tag -> false));
        // Id-only lookups never match tags.
        assertEquals(ItemSizeRules.ONE, rules.sizeOf("minecraft:iron_sword"));
    }

    @Test
    void sizesAreCachedPerItem() {
        ItemSizeRules rules = parse("#minecraft:swords = 1x3");
        Object item = new Object();
        int[] asked = {0};
        rules.sizeOf(item, "minecraft:iron_sword", tag -> { asked[0]++; return true; });
        rules.sizeOf(item, "minecraft:iron_sword", tag -> { asked[0]++; return true; });
        assertEquals(1, asked[0]);
    }

    @Test
    void commentsAndBlankLinesAreIgnored() {
        ItemSizeRules rules = parse("# a comment\n\n## another\n#\n   \nminecraft:bow = 1x2\n");
        assertEquals(List.of("minecraft:bow=1x2"), rules.lines());
    }

    @Test
    void spacingAndCaseDoNotMatter() {
        ItemSizeRules rules = parse("  Minecraft:Bow=1X2  ");
        assertEquals(size(1, 2), rules.sizeOf("minecraft:bow"));
    }

    @Test
    void badLinesAreReportedAndSkipped() {
        List<String> warnings = new ArrayList<>();
        ItemSizeRules rules = ItemSizeRules.parse(String.join("\n",
            "minecraft:bow 1x2",       // no '='
            "= 1x2",                   // no pattern
            "minecraft:bow = 10x1",    // wider than the inventory
            "minecraft:bow = 1x4",     // taller than the inventory
            "minecraft:bow = 0x1",
            "minecraft:bow = big",
            "#swords = 1x2",           // a tag needs a namespace
            "minecraft:b?w = 1x2",     // not an id
            "minecraft:bow = 1x2"),    // the only good line
            true, warnings::add);
        assertEquals(8, warnings.size(), () -> String.valueOf(warnings));
        assertTrue(warnings.get(0).startsWith("line 1: "));
        assertEquals(List.of("minecraft:bow=1x2"), rules.lines());
    }

    @Test
    void disabledRulesMakeEverythingOneCell() {
        ItemSizeRules rules = ItemSizeRules.parse("*_sword = 1x3", false, message -> { });
        assertSame(ItemSizeRules.DISABLED, rules);
        assertFalse(rules.enabled());
        assertEquals(ItemSizeRules.ONE, rules.sizeOf("minecraft:iron_sword"));
        assertEquals("", rules.serialize());
    }

    @Test
    void serializedRulesReadBackTheSame() {
        ItemSizeRules rules = parse(ItemSizeRules.defaultFileText());
        ItemSizeRules copy = ItemSizeRules.deserialize(rules.serialize());
        assertTrue(copy.enabled());
        assertEquals(rules.lines(), copy.lines());
        assertSame(ItemSizeRules.DISABLED, ItemSizeRules.deserialize(""));
    }

    @Test
    void enabledWithNoRulesStillSendsSomething() {
        // Sizes on with an empty file: every item is 1x1, but the client must still
        // learn that sizes are on (covered cells and overflow still apply).
        ItemSizeRules rules = parse("");
        assertTrue(rules.enabled());
        assertTrue(ItemSizeRules.deserialize(rules.serialize()).enabled());
    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * How many inventory cells each item takes.
 * <p>
 * The rules come from {@code config/dayz_inventory_item_sizes.txt}, one per
 * line, {@code pattern = WIDTHxHEIGHT}. A pattern is an item id
 * ({@code minecraft:trident}), an item tag ({@code #minecraft:swords}) or an id
 * with {@code *} wildcards ({@code *_sword}). The first rule that matches an item
 * decides its size; an item no rule matches takes one cell.
 * <p>
 * The server's rules are the ones that count. They travel to the client as
 * {@link #serialize()} when the DayZ screen opens, so the screen draws and
 * predicts exactly what the server enforces. Plain Java, so it is unit-tested
 * without Minecraft.
 */
public final class ItemSizeRules {
    /** The main inventory is 9 cells wide and 3 high; nothing can be larger. */
    public static final int MAX_WIDTH = 9;
    public static final int MAX_HEIGHT = 3;

    public static final Size ONE = new Size(1, 1);

    /** Sizes switched off: every item takes one cell and nothing is enforced. */
    public static final ItemSizeRules DISABLED = new ItemSizeRules(false, List.of());

    public record Size(int width, int height) {
        public boolean isSingle() {
            return this.width == 1 && this.height == 1;
        }

        @Override
        public String toString() {
            return this.width + "x" + this.height;
        }
    }

    /** One parsed line. Exactly one of {@code tag} and {@code idPattern} is set. */
    private record Rule(String source, String tag, Pattern idPattern, Size size) {
        boolean matches(String itemId, Predicate<String> hasTag) {
            return this.tag != null ? hasTag.test(this.tag) : this.idPattern.matcher(itemId).matches();
        }
    }

    private final boolean enabled;
    private final List<Rule> rules;
    /** Sizes already worked out, keyed by the caller's item object. */
    private final Map<Object, Size> cache = new ConcurrentHashMap<>();

    private ItemSizeRules(boolean enabled, List<Rule> rules) {
        this.enabled = enabled;
        this.rules = rules;
    }

    /** Whether item sizes are on at all. */
    public boolean enabled() {
        return this.enabled;
    }

    /** The rules, as {@code pattern=WxH}, in the order they are tried. */
    public List<String> lines() {
        List<String> lines = new ArrayList<>(this.rules.size());
        for (Rule rule : this.rules) {
            lines.add(rule.source() + "=" + rule.size());
        }
        return Collections.unmodifiableList(lines);
    }

    /**
     * The size of an item. {@code key} identifies the item for the cache (the
     * {@code Item} object), {@code itemId} is its registry id and {@code hasTag}
     * answers whether it is in an item tag, given as {@code namespace:path}.
     */
    public Size sizeOf(Object key, String itemId, Predicate<String> hasTag) {
        if (!this.enabled) return ONE;
        Size cached = this.cache.get(key);
        if (cached != null) return cached;
        Size size = ONE;
        for (Rule rule : this.rules) {
            if (rule.matches(itemId, hasTag)) {
                size = rule.size();
                break;
            }
        }
        this.cache.put(key, size);
        return size;
    }

    /** The size of an item by id alone, uncached; tag rules never match. For tests and tools. */
    public Size sizeOf(String itemId) {
        if (!this.enabled) return ONE;
        for (Rule rule : this.rules) {
            if (rule.matches(itemId, tag -> false)) return rule.size();
        }
        return ONE;
    }

    // ------------------------------------------------------------------
    // Parsing
    // ------------------------------------------------------------------

    /**
     * Parses a rules file. Lines that cannot be read are reported to
     * {@code warn} and skipped; this never throws.
     * <p>
     * A line starting with {@code #} followed by a space (or nothing) is a
     * comment, so {@code #minecraft:swords = 1x2} is a tag rule while
     * {@code # swords} is a comment.
     */
    public static ItemSizeRules parse(String text, boolean enabled, Consumer<String> warn) {
        if (!enabled) return DISABLED;
        List<Rule> rules = new ArrayList<>();
        String[] lines = text.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].strip();
            if (line.isEmpty() || isComment(line)) continue;
            int lineNumber = i + 1;
            Rule rule = parseRule(line, message -> warn.accept("line " + lineNumber + ": " + message));
            if (rule != null) rules.add(rule);
        }
        return new ItemSizeRules(true, List.copyOf(rules));
    }

    /**
     * The compact form sent to clients: a header line, then the rules one per
     * line; empty when sizes are off. The header keeps "on, with no rules"
     * apart from "off".
     */
    public String serialize() {
        if (!this.enabled) return "";
        List<String> lines = new ArrayList<>(lines());
        lines.add(0, NETWORK_HEADER);
        return String.join("\n", lines);
    }

    /** Reads {@link #serialize()}'s output. Anything else means sizes are off. */
    public static ItemSizeRules deserialize(String text) {
        if (text == null || !text.startsWith(NETWORK_HEADER)) return DISABLED;
        return parse(text.substring(NETWORK_HEADER.length()), true, message -> { });
    }

    private static final String NETWORK_HEADER = "dayz_inventory:item_sizes:1";

    private static boolean isComment(String line) {
        return line.startsWith("#") && (line.length() == 1 || Character.isWhitespace(line.charAt(1)) || line.charAt(1) == '#');
    }

    private static Rule parseRule(String line, Consumer<String> warn) {
        int equals = line.indexOf('=');
        if (equals < 0) {
            warn.accept("'" + line + "' has no '=': expected pattern = WIDTHxHEIGHT");
            return null;
        }
        String pattern = line.substring(0, equals).strip().toLowerCase(Locale.ROOT);
        String value = line.substring(equals + 1).strip().toLowerCase(Locale.ROOT);
        if (pattern.isEmpty()) {
            warn.accept("'" + line + "' has no pattern before '='");
            return null;
        }
        Size size = parseSize(value);
        if (size == null) {
            warn.accept("'" + value + "' is not a size: expected WIDTHxHEIGHT, from 1x1 to " + MAX_WIDTH + "x" + MAX_HEIGHT);
            return null;
        }
        if (pattern.startsWith("#")) {
            String tag = pattern.substring(1);
            if (!isResourceId(tag)) {
                warn.accept("'" + pattern + "' is not an item tag: expected #namespace:path");
                return null;
            }
            return new Rule(pattern, tag, null, size);
        }
        if (!pattern.matches("[a-z0-9_.\\-:/*]+")) {
            warn.accept("'" + pattern + "' is not an item id or wildcard pattern");
            return null;
        }
        return new Rule(pattern, null, glob(pattern), size);
    }

    private static Size parseSize(String value) {
        int x = value.indexOf('x');
        if (x <= 0 || x == value.length() - 1) return null;
        try {
            int width = Integer.parseInt(value.substring(0, x).strip());
            int height = Integer.parseInt(value.substring(x + 1).strip());
            if (width < 1 || height < 1 || width > MAX_WIDTH || height > MAX_HEIGHT) return null;
            return width == 1 && height == 1 ? ONE : new Size(width, height);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isResourceId(String id) {
        int colon = id.indexOf(':');
        return colon > 0 && colon < id.length() - 1 && id.indexOf(':', colon + 1) < 0
            && id.matches("[a-z0-9_.\\-]+:[a-z0-9_.\\-/]+");
    }

    /** {@code *} matches any run of characters; an id without a namespace gets {@code minecraft:}. */
    private static Pattern glob(String pattern) {
        String full = pattern.contains(":") || pattern.startsWith("*") ? pattern : "minecraft:" + pattern;
        StringBuilder regex = new StringBuilder();
        int start = 0;
        for (int star = full.indexOf('*'); star >= 0; star = full.indexOf('*', start)) {
            regex.append(Pattern.quote(full.substring(start, star))).append(".*");
            start = star + 1;
        }
        regex.append(Pattern.quote(full.substring(start)));
        return Pattern.compile(regex.toString());
    }

    // ------------------------------------------------------------------
    // The default file
    // ------------------------------------------------------------------

    /** What {@code dayz_inventory_item_sizes.txt} holds when it is first created. */
    public static String defaultFileText() {
        return String.join("\n",
            "# DayZ Inventory - item sizes",
            "#",
            "# Only used when itemSizes=true in dayz_inventory.properties. On a server, the",
            "# server's file is the one that counts: players receive it when they open the",
            "# DayZ screen.",
            "#",
            "# One rule per line:   pattern = WIDTHxHEIGHT",
            "#",
            "# The main inventory is 9 cells wide and 3 high, so sizes go up to 9x3. The",
            "# hotbar, armour and offhand slots always hold one item each, whatever its size.",
            "# Anything no rule matches takes one cell.",
            "#",
            "# A pattern is an item id (minecraft:trident), an item tag (#minecraft:swords)",
            "# or an id with * wildcards (*_sword is every item whose id ends in _sword, from",
            "# any mod). The first rule that matches an item wins, so keep exact items above",
            "# the wildcards that would also match them. Lines starting with '# ' are comments.",
            "",
            "# Weapons and tools",
            "minecraft:trident = 1x3",
            "minecraft:mace = 1x2",
            "minecraft:bow = 1x2",
            "minecraft:crossbow = 2x1",
            "minecraft:fishing_rod = 1x2",
            "minecraft:carrot_on_a_stick = 1x2",
            "minecraft:warped_fungus_on_a_stick = 1x2",
            "minecraft:shield = 2x2",
            "*_spear = 1x3",
            "*_sword = 1x2",
            "*_pickaxe = 1x2",
            "*_axe = 1x2",
            "*_shovel = 1x2",
            "*_hoe = 1x2",
            "",
            "# Armour carried in the inventory",
            "minecraft:elytra = 2x2",
            "*_chestplate = 2x2",
            "*_leggings = 1x2",
            "*_horse_armor = 2x2",
            "",
            "# Containers",
            "*shulker_box = 2x2",
            "");
    }
}

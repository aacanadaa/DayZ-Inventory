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

import java.util.Arrays;

/**
 * Where items sit on a grid of slots when one item can cover several of them.
 * <p>
 * Cells are numbered row by row: {@code cell = row * columns + column}. An item
 * is stored in the slot of its top-left cell - its <em>anchor</em> - and its
 * <em>footprint</em> covers {@code width x height} cells to the right and below.
 * The other cells of a footprint stay empty; they are only covered.
 * <p>
 * An item whose footprint does not fit where it is stored - it runs off the
 * grid, another item lies inside it, or its own cell is already covered - is an
 * <em>overflow</em> item. It keeps only its own cell. Item sizes never delete
 * anything: overflow is how an item put somewhere it does not fit (by a command,
 * another mod, or by turning sizes on in a full inventory) is represented until
 * it can be moved.
 * <p>
 * Footprints are claimed in cell order, so of two items that conflict the one
 * stored first keeps its footprint. The class is plain Java, without Minecraft
 * types, so that it can be unit-tested on its own; instances are immutable.
 */
public final class GridLayout {
    /** "No cell": no owner, or no free place. */
    public static final int NONE = -1;

    private final int columns;
    private final int rows;
    /** Per cell: the size of the item stored there, 0 when the slot is empty. */
    private final int[] widths;
    private final int[] heights;
    /** Per cell: the anchor whose footprint covers it (an overflow item owns only its own, uncovered cell), or NONE. */
    private final int[] owner;
    /** Per cell: the item stored there is an overflow item. */
    private final boolean[] overflow;

    private GridLayout(int columns, int rows, int[] widths, int[] heights) {
        this.columns = columns;
        this.rows = rows;
        this.widths = widths;
        this.heights = heights;
        int cells = columns * rows;
        this.owner = new int[cells];
        this.overflow = new boolean[cells];
        Arrays.fill(this.owner, NONE);

        for (int anchor = 0; anchor < cells; anchor++) {
            if (widths[anchor] <= 0) continue;
            if (footprintIsFree(anchor, widths[anchor], heights[anchor])) {
                claim(anchor, widths[anchor], heights[anchor]);
            } else {
                this.overflow[anchor] = true;
            }
        }
        // An overflow item still occupies its own cell, unless that cell lies in
        // another item's footprint: then it sits on top of that footprint.
        for (int cell = 0; cell < cells; cell++) {
            if (this.overflow[cell] && this.owner[cell] == NONE) {
                this.owner[cell] = cell;
            }
        }
    }

    /**
     * The layout of a grid. {@code widths[cell]} and {@code heights[cell]} are the
     * size of the item stored in that cell's slot, or 0 for an empty slot.
     */
    public static GridLayout of(int columns, int rows, int[] widths, int[] heights) {
        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException("A grid needs at least one column and one row");
        }
        int cells = columns * rows;
        if (widths.length != cells || heights.length != cells) {
            throw new IllegalArgumentException("Expected " + cells + " sizes, got " + widths.length + " and " + heights.length);
        }
        int[] w = new int[cells];
        int[] h = new int[cells];
        for (int cell = 0; cell < cells; cell++) {
            boolean empty = widths[cell] <= 0 || heights[cell] <= 0;
            w[cell] = empty ? 0 : widths[cell];
            h[cell] = empty ? 0 : heights[cell];
        }
        return new GridLayout(columns, rows, w, h);
    }

    /** An empty grid. */
    public static GridLayout empty(int columns, int rows) {
        return of(columns, rows, new int[columns * rows], new int[columns * rows]);
    }

    public int columns() { return this.columns; }
    public int rows() { return this.rows; }
    public int cells() { return this.columns * this.rows; }

    /** The slot of this cell holds nothing. It may still be covered by a footprint. */
    public boolean isEmpty(int cell) { return this.widths[cell] <= 0; }

    /** The anchor whose footprint covers this cell, or {@link #NONE}. */
    public int ownerOf(int cell) { return this.owner[cell]; }

    /** This cell holds an item that does not fit where it is. */
    public boolean isOverflow(int cell) { return this.overflow[cell]; }

    /** This cell holds an item whose whole footprint is in place. */
    public boolean isAnchor(int cell) { return this.widths[cell] > 0 && !this.overflow[cell]; }

    /** This cell is empty and lies inside another item's footprint. */
    public boolean isCovered(int cell) { return this.widths[cell] <= 0 && this.owner[cell] != NONE; }

    /** The width of the item stored in this cell, 0 when empty. */
    public int width(int cell) { return this.widths[cell]; }

    /** The height of the item stored in this cell, 0 when empty. */
    public int height(int cell) { return this.heights[cell]; }

    /**
     * Whether an item of {@code width x height} may be stored in the slot of
     * {@code anchor}, replacing whatever that slot holds now (as a swap does).
     * <p>
     * The footprint has to lie on the grid, every other cell in it has to be an
     * empty slot, and no cell in it may belong to another item's footprint.
     */
    public boolean canPlace(int anchor, int width, int height) {
        if (anchor < 0 || anchor >= cells() || width < 1 || height < 1) return false;
        int column = anchor % this.columns;
        int row = anchor / this.columns;
        if (column + width > this.columns || row + height > this.rows) return false;
        for (int r = row; r < row + height; r++) {
            for (int c = column; c < column + width; c++) {
                int cell = r * this.columns + c;
                if (cell != anchor && this.widths[cell] > 0) return false;
                if (this.owner[cell] != NONE && this.owner[cell] != anchor) return false;
            }
        }
        return true;
    }

    /** The first empty cell, in cell order, where an item of this size can be stored; {@link #NONE} if there is none. */
    public int findPlace(int width, int height) {
        for (int cell = 0; cell < cells(); cell++) {
            if (this.widths[cell] <= 0 && canPlace(cell, width, height)) return cell;
        }
        return NONE;
    }

    /** This layout with the slot of {@code cell} holding an item of the given size (0 x 0 to empty it). */
    public GridLayout with(int cell, int width, int height) {
        int[] w = this.widths.clone();
        int[] h = this.heights.clone();
        w[cell] = width;
        h[cell] = height;
        return of(this.columns, this.rows, w, h);
    }

    /** This layout with the slot of {@code cell} emptied. */
    public GridLayout without(int cell) {
        return with(cell, 0, 0);
    }

    /**
     * Places items of the given sizes on an empty grid of this shape, in order,
     * each at the first place it fits. Returns the anchor cell of each item, or
     * {@code null} when one of them does not fit.
     */
    public static int[] pack(int columns, int rows, int[] widths, int[] heights) {
        GridLayout layout = empty(columns, rows);
        int[] anchors = new int[widths.length];
        for (int i = 0; i < widths.length; i++) {
            int cell = layout.findPlace(widths[i], heights[i]);
            if (cell == NONE) return null;
            anchors[i] = cell;
            layout = layout.with(cell, widths[i], heights[i]);
        }
        return anchors;
    }

    private boolean footprintIsFree(int anchor, int width, int height) {
        int column = anchor % this.columns;
        int row = anchor / this.columns;
        if (column + width > this.columns || row + height > this.rows) return false;
        for (int r = row; r < row + height; r++) {
            for (int c = column; c < column + width; c++) {
                int cell = r * this.columns + c;
                if (this.owner[cell] != NONE) return false;
                if (cell != anchor && this.widths[cell] > 0) return false;
            }
        }
        return true;
    }

    private void claim(int anchor, int width, int height) {
        int column = anchor % this.columns;
        int row = anchor / this.columns;
        for (int r = row; r < row + height; r++) {
            for (int c = column; c < column + width; c++) {
                this.owner[r * this.columns + c] = anchor;
            }
        }
    }
}

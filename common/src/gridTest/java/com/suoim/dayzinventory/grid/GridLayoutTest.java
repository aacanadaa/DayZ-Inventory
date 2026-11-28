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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The main inventory's shape: 9 x 3, cells numbered row by row. */
class GridLayoutTest {
    private static final int COLUMNS = 9;
    private static final int ROWS = 3;

    private static int cell(int column, int row) {
        return row * COLUMNS + column;
    }

    /** A 9x3 grid with items of the given sizes: {cell, width, height} triples. */
    private static GridLayout grid(int... items) {
        int[] widths = new int[COLUMNS * ROWS];
        int[] heights = new int[COLUMNS * ROWS];
        for (int i = 0; i < items.length; i += 3) {
            widths[items[i]] = items[i + 1];
            heights[items[i]] = items[i + 2];
        }
        return GridLayout.of(COLUMNS, ROWS, widths, heights);
    }

    @Test
    void emptyGridAcceptsAnythingThatFitsOnIt() {
        GridLayout layout = GridLayout.empty(COLUMNS, ROWS);
        assertTrue(layout.canPlace(cell(0, 0), 9, 3));
        assertTrue(layout.canPlace(cell(8, 2), 1, 1));
        assertTrue(layout.canPlace(cell(7, 1), 2, 2));
        assertEquals(cell(0, 0), layout.findPlace(2, 2));
    }

    @Test
    void footprintMayNotRunOffTheGrid() {
        GridLayout layout = GridLayout.empty(COLUMNS, ROWS);
        assertFalse(layout.canPlace(cell(8, 0), 2, 1), "past the right edge");
        assertFalse(layout.canPlace(cell(0, 2), 1, 2), "past the bottom edge");
        assertFalse(layout.canPlace(cell(0, 0), 10, 1));
        assertFalse(layout.canPlace(cell(0, 0), 0, 1));
        assertFalse(layout.canPlace(-1, 1, 1));
        assertFalse(layout.canPlace(COLUMNS * ROWS, 1, 1));
    }

    @Test
    void aFootprintCoversItsCellsAndOnlyThose() {
        GridLayout layout = grid(cell(2, 0), 2, 2);
        assertTrue(layout.isAnchor(cell(2, 0)));
        assertEquals(cell(2, 0), layout.ownerOf(cell(3, 0)));
        assertEquals(cell(2, 0), layout.ownerOf(cell(2, 1)));
        assertEquals(cell(2, 0), layout.ownerOf(cell(3, 1)));
        assertTrue(layout.isCovered(cell(3, 1)));
        assertFalse(layout.isCovered(cell(2, 0)), "the anchor holds the item, it is not covered");
        assertEquals(GridLayout.NONE, layout.ownerOf(cell(4, 0)));
        assertEquals(GridLayout.NONE, layout.ownerOf(cell(2, 2)));
    }

    @Test
    void coveredCellsRejectOtherItems() {
        GridLayout layout = grid(cell(0, 0), 1, 2);
        assertFalse(layout.canPlace(cell(0, 1), 1, 1), "covered by the sword above");
        assertTrue(layout.canPlace(cell(1, 0), 1, 3));
        assertTrue(layout.canPlace(cell(1, 0), 2, 1));
        assertTrue(layout.canPlace(cell(0, 0), 2, 2), "the sword may be replaced by a 2x2 in place");
    }

    @Test
    void footprintsMayNotOverlapOtherItemsOrFootprints() {
        // A 2x2 at (0,0) and a 1x1 at (3,0).
        GridLayout layout = grid(cell(0, 0), 2, 2, cell(3, 0), 1, 1);
        assertFalse(layout.canPlace(cell(1, 0), 1, 1), "covered");
        assertFalse(layout.canPlace(cell(2, 0), 2, 1), "would cover the item at (3,0)");
        assertTrue(layout.canPlace(cell(2, 0), 1, 3));
        assertFalse(layout.canPlace(cell(1, 1), 2, 2), "starts in a covered cell");
    }

    @Test
    void anItemMayBeReplacedInItsOwnFootprint() {
        // Swapping the 2x2 for a 1x2, or for another 2x2, in place.
        GridLayout layout = grid(cell(4, 1), 2, 2);
        assertTrue(layout.canPlace(cell(4, 1), 2, 2));
        assertTrue(layout.canPlace(cell(4, 1), 1, 2));
        assertTrue(layout.canPlace(cell(4, 1), 3, 2), "grows into free cells");
        assertFalse(layout.canPlace(cell(4, 1), 2, 3), "would run off the bottom");
    }

    @Test
    void findPlaceSkipsCellsThatDoNotFit() {
        // Column 0 is taken by a 1x3; the first 2x2 that fits starts at (1,0).
        GridLayout layout = grid(cell(0, 0), 1, 3);
        assertEquals(cell(1, 0), layout.findPlace(2, 2));
        assertEquals(cell(1, 0), layout.findPlace(1, 1));
        // A full-width item fits nowhere.
        assertEquals(GridLayout.NONE, layout.findPlace(9, 1));
    }

    @Test
    void findPlaceReturnsNoneWhenTheGridIsFull() {
        GridLayout layout = grid(cell(0, 0), 9, 3);
        assertEquals(GridLayout.NONE, layout.findPlace(1, 1));
    }

    @Test
    void anItemInsideAFootprintIsOverflow() {
        // A 1x2 sword at (0,0), and a block that something put into (0,1).
        GridLayout layout = grid(cell(0, 0), 1, 2, cell(0, 1), 1, 1);
        assertTrue(layout.isOverflow(cell(0, 0)), "the sword's footprint is not free, so the sword is the one out of place");
        assertTrue(layout.isAnchor(cell(0, 1)));
        assertEquals(cell(0, 1), layout.ownerOf(cell(0, 1)));
    }

    @Test
    void anItemWithAnotherItemInsideItsFootprintIsOverflow() {
        // The 1x2 at (1,1) lies inside the 2x2's footprint, so the 2x2 is the one
        // out of place and the 1x2 keeps its own footprint.
        GridLayout layout = grid(cell(0, 0), 2, 2, cell(1, 1), 1, 2);
        assertTrue(layout.isOverflow(cell(0, 0)));
        assertTrue(layout.isAnchor(cell(1, 1)));
        assertEquals(cell(1, 1), layout.ownerOf(cell(1, 2)));
    }

    @Test
    void aFootprintOverlappingAnEarlierOneIsOverflow() {
        // The 2x2 at (1,0) is stored first and claims (1,1); the 2x1 at (0,1)
        // would also need (1,1).
        GridLayout layout = grid(cell(1, 0), 2, 2, cell(0, 1), 2, 1);
        assertTrue(layout.isAnchor(cell(1, 0)));
        assertTrue(layout.isOverflow(cell(0, 1)));
        assertEquals(cell(0, 1), layout.ownerOf(cell(0, 1)), "it keeps its own cell");
        assertEquals(cell(1, 0), layout.ownerOf(cell(1, 1)));
    }

    @Test
    void anItemRunningOffTheGridIsOverflowAndKeepsItsCell() {
        GridLayout layout = grid(cell(8, 2), 2, 2);
        assertTrue(layout.isOverflow(cell(8, 2)));
        assertEquals(cell(8, 2), layout.ownerOf(cell(8, 2)));
        assertTrue(layout.canPlace(cell(8, 2), 1, 1), "a 1x1 may replace it in place");
        assertEquals(GridLayout.NONE, layout.findPlace(9, 3), "its cell is not free");
    }

    @Test
    void overflowNeverHidesAnotherItem() {
        // Every stored item is either an anchor with its whole footprint or overflow.
        GridLayout layout = grid(cell(0, 0), 3, 3, cell(1, 1), 1, 1, cell(4, 0), 2, 1, cell(5, 0), 1, 1);
        for (int c = 0; c < layout.cells(); c++) {
            if (layout.isEmpty(c)) continue;
            assertTrue(layout.isAnchor(c) ^ layout.isOverflow(c));
        }
    }

    @Test
    void withAndWithoutChangeOneCell() {
        GridLayout layout = GridLayout.empty(COLUMNS, ROWS).with(cell(0, 0), 2, 2);
        assertFalse(layout.canPlace(cell(1, 1), 1, 1));
        GridLayout emptied = layout.without(cell(0, 0));
        assertTrue(emptied.canPlace(cell(1, 1), 1, 1));
        assertTrue(emptied.isEmpty(cell(0, 0)));
        // The original is unchanged.
        assertFalse(layout.canPlace(cell(1, 1), 1, 1));
    }

    @Test
    void packPlacesItemsInOrderAtTheirFirstFit() {
        int[] anchors = GridLayout.pack(COLUMNS, ROWS, new int[] {2, 1, 1}, new int[] {2, 3, 1});
        assertArrayEquals(new int[] {cell(0, 0), cell(2, 0), cell(3, 0)}, anchors);
    }

    @Test
    void packFailsWhenItemsDoNotFit() {
        // Two 9x2 bars cannot share a 9x3 grid.
        assertNull(GridLayout.pack(COLUMNS, ROWS, new int[] {9, 9}, new int[] {2, 2}));
        // Twenty-seven single items fill it exactly.
        int[] ones = new int[27];
        java.util.Arrays.fill(ones, 1);
        int[] anchors = GridLayout.pack(COLUMNS, ROWS, ones, ones);
        assertEquals(26, anchors[26]);
    }

    @Test
    void sizesMustMatchTheGrid() {
        assertThrows(IllegalArgumentException.class, () -> GridLayout.of(9, 3, new int[26], new int[27]));
        assertThrows(IllegalArgumentException.class, () -> GridLayout.of(0, 3, new int[0], new int[0]));
    }
}

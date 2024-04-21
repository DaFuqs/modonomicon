/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 * SPDX-FileCopyrightText: 2021 Authors of Arcana
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.client.gui.book;

import com.klikli_dev.modonomicon.book.*;
import com.klikli_dev.modonomicon.book.entries.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static java.lang.Math.*;

public class EntryConnectionRenderer {

    public int blitOffset;
    public ResourceLocation entryTextures;

    public EntryConnectionRenderer(ResourceLocation entryTextures) {
        this.entryTextures = entryTextures;
    }

    public void renderLinedUpEntries(GuiGraphics guiGraphics, BookEntry bookEntry, BookEntry parentBookEntry, BookEntryParent parent, boolean isVertical) {
        if (isVertical) {
            this.drawVerticalLine(guiGraphics, parentBookEntry.getX(), bookEntry.getY(), parentBookEntry.getY());
            if (parent.drawArrow()) {
                //move the arrow head one grid slot before the target, because it occupies 30x30
                if (parentBookEntry.getY() > bookEntry.getY())
                    this.drawUpArrow(guiGraphics, bookEntry.getX(), bookEntry.getY() + 1);
                else
                    this.drawDownArrow(guiGraphics, bookEntry.getX(), bookEntry.getY() - 1);
            }

        } else {
            this.drawHorizontalLine(guiGraphics, parentBookEntry.getY(), bookEntry.getX(), parentBookEntry.getX());
            if (parent.drawArrow()) {
                //move the arrow head one grid slot before the target, because it occupies 30x30
                if (parentBookEntry.getX() > bookEntry.getX())
                    this.drawLeftArrow(guiGraphics, bookEntry.getX() + 1, bookEntry.getY());
                else
                    this.drawRightArrow(guiGraphics, bookEntry.getX() - 1, bookEntry.getY());
            }
        }
    }

    public void renderSmallCurves(GuiGraphics guiGraphics, BookEntry bookEntry, BookEntry parentBookEntry, BookEntryParent parent) {
        this.drawVerticalLine(guiGraphics, bookEntry.getX(), parentBookEntry.getY(), bookEntry.getY());
        this.drawHorizontalLine(guiGraphics, parentBookEntry.getY(), parentBookEntry.getX(), bookEntry.getX());
        if (bookEntry.getX() > parentBookEntry.getX()) {
            if (bookEntry.getY() > parentBookEntry.getY()) {
                this.drawSmallCurveLeftDown(guiGraphics, bookEntry.getX(), parentBookEntry.getY());
                if (parent.drawArrow())
                    this.drawDownArrow(guiGraphics, bookEntry.getX(), bookEntry.getY() - 1);
            } else {
                this.drawSmallCurveLeftUp(guiGraphics, bookEntry.getX(), parentBookEntry.getY());
                if (parent.drawArrow())
                    this.drawUpArrow(guiGraphics, bookEntry.getX(), bookEntry.getY() + 1);
            }
        } else {
            if (bookEntry.getY() > parentBookEntry.getY()) {
                this.drawSmallCurveRightDown(guiGraphics, bookEntry.getX(), parentBookEntry.getY());
                if (parent.drawArrow())
                    this.drawDownArrow(guiGraphics, bookEntry.getX(), bookEntry.getY() - 1);
            } else {
                this.drawSmallCurveRightUp(guiGraphics, bookEntry.getX(), parentBookEntry.getY());
                if (parent.drawArrow())
                    this.drawUpArrow(guiGraphics, bookEntry.getX(), bookEntry.getY() + 1);
            }
        }
    }

    public void renderSmallCurvesReversed(GuiGraphics guiGraphics, BookEntry bookEntry, BookEntry parentBookEntry, BookEntryParent parent) {
        this.drawHorizontalLine(guiGraphics, bookEntry.getY(), bookEntry.getX(), parentBookEntry.getX());
        this.drawVerticalLine(guiGraphics, parentBookEntry.getX(), parentBookEntry.getY(), bookEntry.getY());
        if (bookEntry.getX() < parentBookEntry.getX()) {
            if (bookEntry.getY() > parentBookEntry.getY()) {
                this.drawSmallCurveLeftUp(guiGraphics, parentBookEntry.getX(), bookEntry.getY());
                if (parent.drawArrow())
                    this.drawLeftArrow(guiGraphics, bookEntry.getX() + 1, bookEntry.getY());
            } else {
                this.drawSmallCurveLeftDown(guiGraphics, parentBookEntry.getX(), bookEntry.getY());
                if (parent.drawArrow())
                    this.drawLeftArrow(guiGraphics, bookEntry.getX() + 1, bookEntry.getY());
            }
        } else {
            if (bookEntry.getY() > parentBookEntry.getY()) {
                this.drawSmallCurveRightUp(guiGraphics, parentBookEntry.getX(), bookEntry.getY());
                if (parent.drawArrow())
                    this.drawRightArrow(guiGraphics, bookEntry.getX() - 1, bookEntry.getY());
            } else {
                this.drawSmallCurveRightDown(guiGraphics, parentBookEntry.getX(), bookEntry.getY());
                if (parent.drawArrow())
                    this.drawRightArrow(guiGraphics, bookEntry.getX() - 1, bookEntry.getY());
            }
        }
    }

    public void renderLargeCurves(GuiGraphics guiGraphics, BookEntry bookEntry, BookEntry parentBookEntry, BookEntryParent parent) {
        this.drawHorizontalLineShortened(guiGraphics, parentBookEntry.getY(), parentBookEntry.getX(), bookEntry.getX());
        this.drawVerticalLineShortened(guiGraphics, bookEntry.getX(), bookEntry.getY(), parentBookEntry.getY());
        if (bookEntry.getX() > parentBookEntry.getX()) {
            if (bookEntry.getY() > parentBookEntry.getY()) {
                this.drawLargeCurveLeftDown(guiGraphics, bookEntry.getX() - 1, parentBookEntry.getY());
                if (parent.drawArrow())
                    this.drawDownArrow(guiGraphics, bookEntry.getX(), bookEntry.getY() - 1);
            } else {
                this.drawLargeCurveLeftUp(guiGraphics, bookEntry.getX() - 1, parentBookEntry.getY() - 1);
                if (parent.drawArrow())
                    this.drawUpArrow(guiGraphics, bookEntry.getX(), bookEntry.getY() + 1);
            }
        } else {
            if (bookEntry.getY() > parentBookEntry.getY()) {
                this.drawLargeCurveRightDown(guiGraphics, bookEntry.getX(), parentBookEntry.getY());
                if (parent.drawArrow())
                    this.drawDownArrow(guiGraphics, bookEntry.getX(), bookEntry.getY() - 1);
            } else {
                this.drawLargeCurveRightUp(guiGraphics, bookEntry.getX(), parentBookEntry.getY() - 1);
                if (parent.drawArrow())
                    this.drawUpArrow(guiGraphics, bookEntry.getX(), bookEntry.getY() + 1);
            }
        }
    }

    public void renderLargeCurvesReversed(GuiGraphics guiGraphics, BookEntry bookEntry, BookEntry parentBookEntry, BookEntryParent parent) {
        this.drawHorizontalLineShortened(guiGraphics, bookEntry.getY(), bookEntry.getX(), parentBookEntry.getX());
        this.drawVerticalLineShortened(guiGraphics, parentBookEntry.getX(), parentBookEntry.getY(), bookEntry.getY());
        if (bookEntry.getX() > parentBookEntry.getX()) {
            if (bookEntry.getY() > parentBookEntry.getY())
                this.drawLargeCurveRightUp(guiGraphics, parentBookEntry.getX(), bookEntry.getY() - 1);
            else
                this.drawLargeCurveRightDown(guiGraphics, parentBookEntry.getX(), bookEntry.getY());
            if (parent.drawArrow())
                this.drawRightArrow(guiGraphics, bookEntry.getX() - 1, bookEntry.getY());
        } else {
            if (bookEntry.getY() > parentBookEntry.getY())
                this.drawLargeCurveLeftUp(guiGraphics, parentBookEntry.getX() - 1, parentBookEntry.getY() + 1);
            else
                this.drawLargeCurveLeftDown(guiGraphics, parentBookEntry.getX() - 1, bookEntry.getY());
            if (parent.drawArrow())
                this.drawLeftArrow(guiGraphics, bookEntry.getX() + 1, bookEntry.getY());
        }
    }

    public void render(GuiGraphics guiGraphics, BookEntry bookEntry, BookEntryParent parent) {
        BookEntry parentBookEntry = parent.getEntry();

        //only render if line is enabled and if we are in the same category (other category -> other page!)
        if (parent.isLineEnabled() && parentBookEntry.getCategory().equals(bookEntry.getCategory())) {
            int deltaX = abs(bookEntry.getX() - parentBookEntry.getX());
            int deltaY = abs(bookEntry.getY() - parentBookEntry.getY());

            if (deltaX == 0 || deltaY == 0) {
                //if the entries are in a line, just draw a line
                this.renderLinedUpEntries(guiGraphics, bookEntry, parentBookEntry, parent, deltaX == 0);
            } else {
                if (deltaX < 2 || deltaY < 2) {
                    if (!parent.isLineReversed()) {
                        this.renderSmallCurves(guiGraphics, bookEntry, parentBookEntry, parent);
                    } else {
                        this.renderSmallCurvesReversed(guiGraphics, bookEntry, parentBookEntry, parent);
                    }
                } else {
                    if (!parent.isLineReversed()) {
                        this.renderLargeCurves(guiGraphics, bookEntry, parentBookEntry, parent);
                    } else {
                        this.renderLargeCurvesReversed(guiGraphics, bookEntry, parentBookEntry, parent);
                    }
                }
            }
        }
    }

    protected void setBlitOffset(int blitOffset) {
        this.blitOffset = blitOffset;
    }

    /**
     * Scales from grid coordinates (1, 2, 3, ... ) to screen coordinates (30, 60, 90)
     */
    protected int screenX(int x) {
        return x * BookCategoryScreen.ENTRY_GRID_SCALE;
    }

    /**
     * Scales from grid coordinates (1, 2, 3, ... ) to screen coordinates (30, 60, 90)
     */
    protected int screenY(int y) {
        return y * BookCategoryScreen.ENTRY_GRID_SCALE;
    }

    protected void blit(GuiGraphics guiGraphics, int pX, int pY, float pUOffset, float pVOffset, int pUWidth, int pVHeight) {
        guiGraphics.blit(this.entryTextures, pX, pY, this.blitOffset, pUOffset, pVOffset, pUWidth, pVHeight, 256, 256);
    }

    protected void drawSmallCurveLeftDown(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y), 0, 226, 30, 30);
    }

    protected void drawSmallCurveRightDown(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y), 30, 226, 30, 30);
    }

    protected void drawSmallCurveLeftUp(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y), 0, 196, 30, 30);
    }

    protected void drawSmallCurveRightUp(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y), 30, 196, 30, 30);
    }

    protected void drawLargeCurveLeftDown(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y), 62, 196, 60, 60);
    }

    protected void drawLargeCurveRightDown(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y), 122, 196, 60, 60);
    }

    protected void drawLargeCurveLeftUp(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y), 62, 134, 60, 60);
    }

    protected void drawLargeCurveRightUp(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y), 122, 134, 60, 60);
    }

    void drawVerticalLineAt(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y), 184, 164, 30, 31);
    }

    void drawHorizontalLineAt(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y), 184, 226, 31, 30);
    }

    void drawVerticalLine(GuiGraphics guiGraphics, int x, int startY, int endY) {
        int temp = startY;

        //swap them if endY > startY
        startY = min(startY, endY);
        endY = max(endY, temp);

        for (int j = startY + 1; j < endY; j++)
            this.drawVerticalLineAt(guiGraphics, x, j);
    }

    void drawHorizontalLine(GuiGraphics guiGraphics, int y, int startX, int endX) {
        int temp = startX;

        //swap them if endX > startX
        startX = min(startX, endX);
        endX = max(endX, temp);
        // *exclusive*
        for (int j = startX + 1; j < endX; j++) {
            this.drawHorizontalLineAt(guiGraphics, j, y);
        }
    }

    void drawHorizontalLineShortened(GuiGraphics guiGraphics, int y, int startX, int endX) {
        int temp = startX;

        // reduce length by one
        if (startX > endX)
            endX++;
        else
            endX--;

        //swap them if endX > startX
        startX = min(startX, endX);
        endX = max(endX, temp);

        for (int j = startX + 1; j < endX; j++)
            this.drawHorizontalLineAt(guiGraphics, j, y);
    }

    void drawVerticalLineShortened(GuiGraphics guiGraphics, int x, int startY, int endY) {
        int temp = startY;

        // reduce length by one
        if (startY > endY)
            endY++;
        else
            endY--;

        //swap them if endY > startY
        startY = min(startY, endY);
        endY = max(endY, temp);

        for (int j = startY + 1; j < endY; j++)
            this.drawVerticalLineAt(guiGraphics, x, j);
    }


    void drawUpArrow(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y) - 1, 0, 134, 30, 30);
    }

    void drawDownArrow(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x), this.screenY(y) + 1, 0, 164, 30, 30);
    }

    void drawRightArrow(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x) + 1, this.screenY(y), 30, 134, 30, 30);
    }

    void drawLeftArrow(GuiGraphics guiGraphics, int x, int y) {
        this.blit(guiGraphics, this.screenX(x) - 1, this.screenY(y), 30, 164, 30, 30);
    }
}

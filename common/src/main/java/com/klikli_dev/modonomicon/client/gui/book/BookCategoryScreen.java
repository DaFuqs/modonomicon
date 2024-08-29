/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 * SPDX-FileCopyrightText: 2021 Authors of Arcana
 *
 * SPDX-License-Identifier: MIT
 */
package com.klikli_dev.modonomicon.client.gui.book;

import com.klikli_dev.modonomicon.api.events.EntryClickedEvent;
import com.klikli_dev.modonomicon.book.BookCategory;
import com.klikli_dev.modonomicon.book.BookCategoryBackgroundParallaxLayer;
import com.klikli_dev.modonomicon.book.conditions.context.BookConditionEntryContext;
import com.klikli_dev.modonomicon.book.entries.BookEntry;
import com.klikli_dev.modonomicon.book.entries.ContentBookEntry;
import com.klikli_dev.modonomicon.bookstate.BookUnlockStateManager;
import com.klikli_dev.modonomicon.bookstate.BookVisualStateManager;
import com.klikli_dev.modonomicon.client.gui.BookGuiManager;
import com.klikli_dev.modonomicon.events.ModonomiconEvents;
import com.klikli_dev.modonomicon.networking.BookEntryReadMessage;
import com.klikli_dev.modonomicon.networking.SaveCategoryStateMessage;
import com.klikli_dev.modonomicon.platform.ClientServices;
import com.klikli_dev.modonomicon.platform.Services;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;


public class BookCategoryScreen {

    public static final int ENTRY_GRID_SCALE = 30;
    public static final int ENTRY_GAP = 2;
    public static final int MAX_SCROLL = 512;

    public static final int ENTRY_HEIGHT = 26;
    public static final int ENTRY_WIDTH = 26;

    private final BookOverviewScreen bookOverviewScreen;
    private final BookCategory category;
    private final EntryConnectionRenderer connectionRenderer;
    private float scrollX = 0;
    private float scrollY = 0;
    private boolean isScrolling;
    private float targetZoom;
    private float currentZoom;

    private ResourceLocation openEntry;

    public BookCategoryScreen(BookOverviewScreen bookOverviewScreen, BookCategory category) {
        this.bookOverviewScreen = bookOverviewScreen;
        this.category = category;

        this.connectionRenderer = new EntryConnectionRenderer(category.getEntryTextures());

        this.targetZoom = 0.7f;
        this.currentZoom = this.targetZoom;
    }

    public BookCategory getCategory() {
        return this.category;
    }

    public float getXOffset() {
        return ((this.bookOverviewScreen.getInnerWidth() / 2f) * (1 / this.currentZoom)) - this.scrollX / 2;
    }

    public float getYOffset() {
        return ((this.bookOverviewScreen.getInnerHeight() / 2f) * (1 / this.currentZoom)) - this.scrollY / 2;
    }

    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (ClientServices.CLIENT_CONFIG.enableSmoothZoom()) {
            float diff = this.targetZoom - this.currentZoom;
            this.currentZoom = this.currentZoom + Math.min(pPartialTick * (2 / 3f), 1) * diff;
        } else
            this.currentZoom = this.targetZoom;

        //GL Scissors to the inner frame area so entries do not stick out
        int innerX = this.bookOverviewScreen.getInnerX();
        int innerY = this.bookOverviewScreen.getInnerY();
        int innerWidth = this.bookOverviewScreen.getInnerWidth();
        int innerHeight = this.bookOverviewScreen.getInnerHeight();
        //the -1 are magic numbers to avoid an overflow of 1px.
        guiGraphics.enableScissor(innerX, innerY, innerX + innerWidth - 1, innerY + innerHeight - 1);
        this.renderEntries(guiGraphics, pMouseX, pMouseY);
        guiGraphics.disableScissor();
    }

    public void zoom(double delta) {
        float step = 1.2f;
        if ((delta < 0 && this.targetZoom > 0.5) || (delta > 0 && this.targetZoom < 1))
            this.targetZoom *= delta > 0 ? step : 1 / step;
        if (this.targetZoom > 1f)
            this.targetZoom = 1f;
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        //Based on advancementsscreen
        if (pButton != 0) {
            this.isScrolling = false;
            return false;
        } else {
            if (!this.isScrolling) {
                this.isScrolling = true;
            } else {
                this.scroll(pDragX * 1.5, pDragY * 1.5);
            }
            return true;
        }
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {

        float xOffset = this.getXOffset();
        float yOffset = this.getYOffset();
        for (var entry : this.category.getEntries().values()) {
            var displayStyle = entry.getEntryDisplayState(this.bookOverviewScreen.getMinecraft().player);

            if (this.isEntryHovered(entry, xOffset, yOffset, (int) pMouseX, (int) pMouseY)) {

                var event = new EntryClickedEvent(this.category.getBook().getId(), entry.getId(), pMouseX, pMouseY, pButton, displayStyle);
                //if event is canceled -> click was handled and we do not open the entry.
                if (ModonomiconEvents.client().entryClicked(event)) {
                    return true;
                }

                //only if the entry is unlocked we open it
                if (displayStyle.isUnlocked()) {
                    this.openEntry(entry);
                    return true;
                }
            }
        }

        return false;
    }

    public @Nullable BookContentScreen openEntry(BookEntry entry) {
        if (!BookUnlockStateManager.get().isReadFor(Minecraft.getInstance().player, entry)) {
            Services.NETWORK.sendToServer(new BookEntryReadMessage(entry.getBook().getId(), entry.getId()));
        }

        if(!(entry instanceof CategoryLinkBookEntry))
            this.openEntry = entry.getId(); //do not store link entries in history, otherwise we always jump to its target when opening categories
        
        return entry.openEntry(this);
    }

    public @Nullable BookContentScreen openContentEntry(ContentBookEntry entry) {
        //we check if the content screen was already added, e.g. by the book gui manager
        if (BookGuiManager.get().isEntryAlreadyDisplayed(entry))
            return (BookContentScreen) Minecraft.getInstance().screen;
        
        var bookContentScreen = new BookContentScreen(this.bookOverviewScreen.getCurrentCategoryScreen().bookOverviewScreen, entry);
        ClientServices.GUI.pushGuiLayer(bookContentScreen);

        this.openEntry = entry.getId();
        
        return bookContentScreen;
    }

    public void renderBackground(GuiGraphics guiGraphics) {
        //based on the frame's total width and its thickness, calculate where the inner area starts
        int innerX = this.bookOverviewScreen.getInnerX();
        int innerY = this.bookOverviewScreen.getInnerY();

        //then calculate the corresponding inner area width/height so we don't draw out of the frame
        int innerWidth = this.bookOverviewScreen.getInnerWidth();
        int innerHeight = this.bookOverviewScreen.getInnerHeight();

        //we do not use our static max_scroll here because it makes some issues, so we use the tex instead.
        int backgroundWidth = this.category.getBackgroundWidth();
        int backgroundHeight = this.category.getBackgroundHeight();
        final int MAX_SCROLL = Math.max(backgroundWidth, backgroundHeight);
        float backgroundTextureZoomMultiplier = this.category.getBackgroundTextureZoomMultiplier();


        // Adjust scale calculations to take into account actual texture size
        float xScale = MAX_SCROLL * 2.0f / ((float) MAX_SCROLL + this.bookOverviewScreen.getFrameThicknessW() - this.bookOverviewScreen.getFrameWidth());
        float yScale = MAX_SCROLL * 2.0f / ((float) MAX_SCROLL + this.bookOverviewScreen.getFrameThicknessH() - this.bookOverviewScreen.getFrameHeight());
        float scale = Math.max(xScale, yScale);
        float xOffset = xScale == scale ? 0 : (MAX_SCROLL - (innerWidth + MAX_SCROLL * 2.0f / scale)) / 2;
        float yOffset = yScale == scale ? 0 : (MAX_SCROLL - (innerHeight + MAX_SCROLL * 2.0f / scale)) / 2;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        //note we cannot translate -z here because even -1 immediately pushes us behind the scene -> not visible
        if (!this.category.getBackgroundParallaxLayers().isEmpty()) {
            this.category.getBackgroundParallaxLayers().forEach(layer -> {
                this.renderBackgroundParallaxLayer(guiGraphics, layer, innerX, innerY, innerWidth, innerHeight, this.scrollX, this.scrollY, scale, xOffset, yOffset, this.currentZoom, backgroundWidth, backgroundHeight, backgroundTextureZoomMultiplier);
            });
        } else {
            //for some reason on this one blit overload tex width and height are switched. It does correctly call the followup though, so we have to go along
            //force offset to int here to reduce difference to entry rendering which is pos based and thus int precision only
            guiGraphics.blit(this.category.getBackground(), innerX, innerY,
                    (this.scrollX + MAX_SCROLL) / scale + xOffset,
                    (this.scrollY + MAX_SCROLL) / scale + yOffset,
                    innerWidth, innerHeight, (int)(backgroundHeight * backgroundTextureZoomMultiplier), (int)(backgroundWidth * backgroundTextureZoomMultiplier));

        }
    }

    public void renderBackgroundParallaxLayer(GuiGraphics guiGraphics, BookCategoryBackgroundParallaxLayer layer, int x, int y, int width, int height, float scrollX, float scrollY, float parallax, float xOffset, float yOffset, float zoom, int backgroundWidth, int backgroundHeight, float backgroundTextureZoomMultiplier) {
        float parallax1 = parallax / layer.getSpeed();
        RenderSystem.setShaderTexture(0, layer.getBackground());

        if (layer.getVanishZoom() == -1 || layer.getVanishZoom() > zoom) {
            //for some reason on this one blit overload tex width and height are switched. It does correctly call the followup though, so we have to go along
            guiGraphics.blit(layer.getBackground(), x, y,
                    (scrollX + MAX_SCROLL) / parallax1 + xOffset,
                    (scrollY + MAX_SCROLL) / parallax1 + yOffset,
                    width, height,(int)(backgroundHeight * backgroundTextureZoomMultiplier), (int)(backgroundWidth * backgroundTextureZoomMultiplier));
        }

    }

    private void renderEntries(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        //calculate the render offset
        float xOffset = this.getXOffset();
        float yOffset = this.getYOffset();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(this.currentZoom, this.currentZoom, 1.0f);

        var player = this.bookOverviewScreen.getMinecraft().player;
        for (var entry : this.category.getEntries().values()) {
            var displayState = entry.getEntryDisplayState(player);
            var isHovered = this.isEntryHovered(entry, xOffset, yOffset, mouseX, mouseY);

            if (!displayState.isVisible())
                continue;

            int texX = entry.getEntryBackgroundVIndex() * ENTRY_HEIGHT;
            int texY = entry.getEntryBackgroundUIndex() * ENTRY_WIDTH;

            guiGraphics.pose().pushPose();
            //we translate instead of applying the offset to the entry x/y to avoid jittering when moving
            guiGraphics.pose().translate(xOffset, yOffset, 0);


            //we apply a z offset to push the entries before the connection arrows
            guiGraphics.pose().translate(0, 0, 10);

            //As of 1.20 this is not necessary, in fact it causes the entry to render behind the bg
            //guiGraphics.pose().translate(0, 0, -10); //push the whole entry behind the frame


            if (!displayState.isUnlocked()) {
                //Draw locked entries greyed out
                RenderSystem.setShaderColor(0.2F, 0.2F, 0.2F, 1.0F);
            } else if (isHovered) {
                //Draw hovered entries slightly greyed out
                RenderSystem.setShaderColor(0.8F, 0.8F, 0.8F, 1.0F);
            }
            //render entry background
            guiGraphics.blit(this.category.getEntryTextures(), entry.getX() * ENTRY_GRID_SCALE + ENTRY_GAP, entry.getY() * ENTRY_GRID_SCALE + ENTRY_GAP, texX, texY, ENTRY_WIDTH, ENTRY_HEIGHT);

            guiGraphics.pose().pushPose();

            //render icon
            entry.getIcon().render(guiGraphics, entry.getX() * ENTRY_GRID_SCALE + ENTRY_GAP + 5, entry.getY() * ENTRY_GRID_SCALE + ENTRY_GAP + 5);

            guiGraphics.pose().popPose();

            //render unread icon
            if (displayState.isUnlocked() && !BookUnlockStateManager.get().isReadFor(this.bookOverviewScreen.getMinecraft().player, entry)) {
                final int U = 350;
                final int V = 19;
                final int width = 11;
                final int height = 11;

                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                //RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();

                //testing
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 11); //and push the unread icon in front of the background and icon (they are at Z 10)
                //if focused, we go to the right of our normal button (instead of down, like mc buttons do)
                BookContentScreen.drawFromTexture(guiGraphics, this.bookOverviewScreen.getBook(),
                        entry.getX() * ENTRY_GRID_SCALE + ENTRY_GAP + 16 + 2,
                        entry.getY() * ENTRY_GRID_SCALE + ENTRY_GAP - 2, U + (isHovered ? width : 0), V, width, height);
                guiGraphics.pose().popPose();
            }

            guiGraphics.pose().popPose();

            //reset color to avoid greyed out carrying over
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            this.renderConnections(guiGraphics, entry, xOffset, yOffset);
        }
        guiGraphics.pose().popPose();
    }

    public void renderEntryTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //calculate the render offset
        float xOffset = this.getXOffset();
        float yOffset = this.getYOffset();

        for (var entry : this.category.getEntries().values()) {
            var displayState = entry.getEntryDisplayState(this.bookOverviewScreen.getMinecraft().player);
            if (!displayState.isVisible())
                continue;

            this.renderTooltip(guiGraphics, entry, displayState, xOffset, yOffset, mouseX, mouseY);
        }
    }

    private boolean isEntryHovered(BookEntry entry, float xOffset, float yOffset, int mouseX, int mouseY) {
        int x = (int) ((entry.getX() * ENTRY_GRID_SCALE + xOffset + 2) * this.currentZoom);
        int y = (int) ((entry.getY() * ENTRY_GRID_SCALE + yOffset + 2) * this.currentZoom);
        int innerX = this.bookOverviewScreen.getInnerX();
        int innerY = this.bookOverviewScreen.getInnerY();
        int innerWidth = this.bookOverviewScreen.getInnerWidth();
        int innerHeight = this.bookOverviewScreen.getInnerHeight();
        return mouseX >= x && mouseX <= x + (ENTRY_WIDTH * this.currentZoom)
                && mouseY >= y && mouseY <= y + (ENTRY_HEIGHT * this.currentZoom)
                && mouseX >= innerX && mouseX <= innerX + innerWidth
                && mouseY >= innerY && mouseY <= innerY + innerHeight;
    }

    private void renderTooltip(GuiGraphics guiGraphics, BookEntry entry, EntryDisplayState displayState, float xOffset, float yOffset, int mouseX, int mouseY) {
        //hovered?
        if (this.isEntryHovered(entry, xOffset, yOffset, mouseX, mouseY)) {

            var tooltip = new ArrayList<Component>();

            if (displayState == EntryDisplayState.LOCKED) {
                tooltip.addAll(entry.getCondition().getTooltip(this.bookOverviewScreen.getMinecraft().player, BookConditionEntryContext.of(this.bookOverviewScreen.getBook(), entry)));
            } else if (displayState == EntryDisplayState.UNLOCKED) {
                //add name in bold
                tooltip.add(Component.translatable(entry.getName()).withStyle(ChatFormatting.BOLD));
                //add description
                if (!entry.getDescription().isEmpty()) {
                    tooltip.add(Component.translatable(entry.getDescription()));
                }
            }

            //draw description
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, Optional.empty(), mouseX, mouseY);
        }
    }

    private void renderConnections(GuiGraphics guiGraphics, BookEntry entry, float xOffset, float yOffset) {
        //our arrows are aliased and need blending
        RenderSystem.enableBlend();

        for (var parent : entry.getParents()) {
            var parentDisplayState = parent.getEntry().getEntryDisplayState(this.bookOverviewScreen.getMinecraft().player);
            if (parentDisplayState == EntryDisplayState.HIDDEN)
                continue;

            int blitOffset = 0; //note: any negative blit offset will move it behind our category background
            this.connectionRenderer.setBlitOffset(blitOffset);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(xOffset, yOffset, 0);
            this.connectionRenderer.render(guiGraphics, entry, parent);
            guiGraphics.pose().popPose();
        }

        RenderSystem.disableBlend();
    }

    private void scroll(double pDragX, double pDragY) {
        this.scrollX = (float) Mth.clamp(this.scrollX - pDragX, -MAX_SCROLL, MAX_SCROLL);
        this.scrollY = (float) Mth.clamp(this.scrollY - pDragY, -MAX_SCROLL, MAX_SCROLL);
    }

    private void loadCategoryState() {
        var state = BookVisualStateManager.get().getCategoryStateFor(this.bookOverviewScreen.getMinecraft().player, this.category);
        BookGuiManager.get().currentCategory = this.category;
        BookGuiManager.get().currentCategoryScreen = this;
        if (state != null) {
            this.scrollX = state.scrollX;
            this.scrollY = state.scrollY;
            this.targetZoom = state.targetZoom;
            this.currentZoom = state.targetZoom;
            if (state.openEntry != null) {
                var openEntry = this.category.getEntry(state.openEntry);
                //also check for link entries here if they pollute old history -> they are not allowed to be stored, otherwise opening one category auto-jumps to the next
                if (openEntry != null && !(openEntry instanceof CategoryLinkBookEntry)) {
                    //no need to load history here, will be handled by book content screen
                    this.openEntry(openEntry);
                }
            }
        }
    }

    public void onDisplay() {
        this.loadCategoryState();
    }

    public void onClose() {
        Services.NETWORK.sendToServer(new SaveCategoryStateMessage(this.category, this.scrollX, this.scrollY, this.currentZoom, this.openEntry));
    }

    public void onCloseEntry(BookContentScreen screen) {
        this.openEntry = null;
    }
    
    public BookOverviewScreen getBookOverviewScreen() {
        return this.bookOverviewScreen;
    }

    public void setOpenEntry(ResourceLocation openEntry) {
        this.openEntry = openEntry;
    }
}

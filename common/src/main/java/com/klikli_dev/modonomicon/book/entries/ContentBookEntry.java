/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.book.entries;

import com.google.gson.*;
import com.klikli_dev.modonomicon.api.*;
import com.klikli_dev.modonomicon.book.*;
import com.klikli_dev.modonomicon.book.conditions.BookCondition;
import com.klikli_dev.modonomicon.book.conditions.BookNoneCondition;
import com.klikli_dev.modonomicon.book.error.BookErrorManager;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.bookstate.BookUnlockStateManager;
import com.klikli_dev.modonomicon.client.gui.book.*;
import com.klikli_dev.modonomicon.client.gui.book.markdown.BookTextRenderer;
import com.klikli_dev.modonomicon.data.LoaderRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class ContentBookEntry extends BookEntry {

    protected List<BookPage> pages;

    public ContentBookEntry(ResourceLocation id, ResourceLocation categoryId, String name, String description, BookIcon icon, int x, int y, int entryBackgroundUIndex, int entryBackgroundVIndex, boolean hideWhileLocked, boolean showWhenAnyParentUnlocked, BookCondition condition, List<BookEntryParent> parents, ResourceLocation commandToRunOnFirstReadId, List<BookPage> pages) {
        super(id, categoryId, parents, name, description, icon, x, y, entryBackgroundUIndex, entryBackgroundVIndex, hideWhileLocked, showWhenAnyParentUnlocked, condition, commandToRunOnFirstReadId);
        this.pages = pages;
    }
    
    @Override
    public ResourceLocation getType() {
        return ModonomiconConstants.Data.EntryType.CONTENT;
    }

    public static ContentBookEntry fromJson(ResourceLocation id, JsonObject json) {
        var categoryId = new ResourceLocation(GsonHelper.getAsString(json, "category"));
        var name = GsonHelper.getAsString(json, "name");
        var description = GsonHelper.getAsString(json, "description", "");
        var icon = BookIcon.fromJson(json.get("icon"));
        var x = GsonHelper.getAsInt(json, "x");
        var y = GsonHelper.getAsInt(json, "y");
        var entryBackgroundUIndex = GsonHelper.getAsInt(json, "background_u_index", 0);
        var entryBackgroundVIndex = GsonHelper.getAsInt(json, "background_v_index", 0);
        var hideWhileLocked = GsonHelper.getAsBoolean(json, "hide_while_locked", false);
        var showWhenAnyParentUnlocked = GsonHelper.getAsBoolean(json, "show_when_any_parent_unlocked", false);

        var parentEntries = new ArrayList<BookEntryParent>();

        if (json.has("parents")) {
            JsonArray parents = GsonHelper.getAsJsonArray(json, "parents");
            for (var parent : parents) {
                parentEntries.add(BookEntryParent.fromJson(parent.getAsJsonObject()));
            }
        }
        BookCondition condition = new BookNoneCondition(); //default to unlocked
        if (json.has("condition")) {
            condition = BookCondition.fromJson(json.getAsJsonObject("condition"));
        }
        
        ResourceLocation commandToRunOnFirstReadId = null;
        if (json.has("command_to_run_on_first_read")) {
            commandToRunOnFirstReadId = new ResourceLocation(GsonHelper.getAsString(json, "command_to_run_on_first_read"));
        }
        
        var pages = new ArrayList<BookPage>();
        if (json.has("pages")) {
            var jsonPages = GsonHelper.getAsJsonArray(json, "pages");
            for (var pageElem : jsonPages) {
                BookErrorManager.get().setContext("Page Index: {}", pages.size());
                var pageJson = GsonHelper.convertToJsonObject(pageElem, "page");
                var type = new ResourceLocation(GsonHelper.getAsString(pageJson, "type"));
                var loader = LoaderRegistry.getPageJsonLoader(type);
                var page = loader.fromJson(pageJson);
                pages.add(page);
            }
        }
        
        return new ContentBookEntry(id, categoryId, name, description, icon, x, y, entryBackgroundUIndex, entryBackgroundVIndex, hideWhileLocked, showWhenAnyParentUnlocked, condition, parentEntries, commandToRunOnFirstReadId, pages);
    }
    
    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        super.toNetwork(buffer);
        
        buffer.writeVarInt(this.pages.size());
        for (var page : this.pages) {
            buffer.writeResourceLocation(page.getType());
            page.toNetwork(buffer);
        }
    }
    
    public static ContentBookEntry fromNetwork(FriendlyByteBuf buffer) {
        var id = buffer.readResourceLocation();
        var categoryId = buffer.readResourceLocation();
        var name = buffer.readUtf();
        var description = buffer.readUtf();
        var icon = BookIcon.fromNetwork(buffer);
        var x = buffer.readVarInt();
        var y = buffer.readVarInt();
        var entryBackgroundUIndex = buffer.readVarInt();
        var entryBackgroundVIndex = buffer.readVarInt();
        var hideWhileLocked = buffer.readBoolean();
        var showWhenAnyParentUnlocked = buffer.readBoolean();
        
        var parentEntries = new ArrayList<BookEntryParent>();
        
        var parentCount = buffer.readVarInt();
        for (var i = 0; i < parentCount; i++) {
            parentEntries.add(BookEntryParent.fromNetwork(buffer));
        }
        
        var condition = BookCondition.fromNetwork(buffer);
        ResourceLocation commandToRunOnFirstReadId = buffer.readNullable(FriendlyByteBuf::readResourceLocation);
        
        var pages = new ArrayList<BookPage>();
        var pageCount = buffer.readVarInt();
        for (var i = 0; i < pageCount; i++) {
            var type = buffer.readResourceLocation();
            var loader = LoaderRegistry.getPageNetworkLoader(type);
            var page = loader.fromNetwork(buffer);
            pages.add(page);
        }
        
        return new ContentBookEntry(id, categoryId, name, description, icon, x, y, entryBackgroundUIndex, entryBackgroundVIndex, hideWhileLocked, showWhenAnyParentUnlocked, condition, parentEntries, commandToRunOnFirstReadId, pages);
    }
    
    /**
     * call after loading the book jsons to finalize.
     */
    @Override
    public void build(Level level, BookCategory category) {
        super.build(level, category);

        //build pages
        int pageNum = 0;
        for (var page : this.pages) {
            BookErrorManager.get().getContextHelper().pageNumber = pageNum;
            page.build(level, this, pageNum);
            BookErrorManager.get().getContextHelper().pageNumber = -1;
            pageNum++;
        }
    }
    
    @Override
    public void prerenderMarkdown(BookTextRenderer textRenderer) {
        for (var page : this.pages) {
            BookErrorManager.get().getContextHelper().pageNumber = page.getPageNumber();
            page.prerenderMarkdown(textRenderer);
            BookErrorManager.get().getContextHelper().pageNumber = -1;
        }
    }
    
    @Override
    public List<BookPage> getPages() {
        return this.pages;
    }
    
    @Override
    public List<BookPage> getUnlockedPagesFor(Player player) {
        BookUnlockStateManager unlockManager = BookUnlockStateManager.get();
        return unlockManager.getUnlockedPagesFor(player, this);
    }
    
    @Override
    public int getPageNumberForAnchor(String anchor) {
        var pages = this.getPages();
        for (int i = 0; i < pages.size(); i++) {
            var page = pages.get(i);
            if (anchor.equals(page.getAnchor())) {
                return i;
            }
        }

        return -1;
    }
    
    @Override
    public boolean matchesQuery(String query) {
        if(super.matchesQuery(query)) {
            return true;
        }

        for (var page : this.getPages()) {
            if (page.matchesQuery(query)) {
                return true;
            }
        }

        return false;
    }
    
    public BookContentScreen openEntry(BookCategoryScreen categoryScreen) {
        return categoryScreen.openContentEntry(this);
    }
}

/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.book.entries;

import com.google.gson.*;
import com.klikli_dev.modonomicon.api.*;
import com.klikli_dev.modonomicon.book.*;
import com.klikli_dev.modonomicon.book.conditions.*;
import com.klikli_dev.modonomicon.book.error.*;
import com.klikli_dev.modonomicon.client.gui.book.*;
import net.minecraft.network.*;
import net.minecraft.resources.*;
import net.minecraft.util.*;
import net.minecraft.world.level.*;

import java.util.*;

public class CategoryLinkBookEntry extends BookEntry {
    
    /**
     * The category to open on click
     */
    protected ResourceLocation categoryToOpenId;
    protected BookCategory categoryToOpen;
    
    public CategoryLinkBookEntry(ResourceLocation id, ResourceLocation categoryId, String name, String description, BookIcon icon, int x, int y, int entryBackgroundUIndex, int entryBackgroundVIndex, boolean hideWhileLocked, boolean showWhenAnyParentUnlocked, BookCondition condition, List<BookEntryParent> parents, ResourceLocation commandToRunOnFirstReadId, ResourceLocation categoryToOpenId) {
        super(id, categoryId, parents, name, description, icon, x, y, entryBackgroundUIndex, entryBackgroundVIndex, hideWhileLocked, showWhenAnyParentUnlocked, condition, commandToRunOnFirstReadId);
        this.categoryToOpenId = categoryToOpenId;
    }
    
    @Override
    public ResourceLocation getType() {
        return ModonomiconConstants.Data.EntryType.CATEGORY_LINK;
    }
    
    public static CategoryLinkBookEntry fromJson(ResourceLocation id, JsonObject json) {
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

        ResourceLocation categoryToOpenId = null;
        if (json.has("category_to_open")) {
            categoryToOpenId = new ResourceLocation(GsonHelper.getAsString(json, "category_to_open"));
        }

        return new CategoryLinkBookEntry(id, categoryId, name, description, icon, x, y, entryBackgroundUIndex, entryBackgroundVIndex, hideWhileLocked, showWhenAnyParentUnlocked, condition, parentEntries, commandToRunOnFirstReadId, categoryToOpenId);
    }
    
    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        super.toNetwork(buffer);
        
        buffer.writeResourceLocation(this.categoryToOpenId);
    }
    
    public static CategoryLinkBookEntry fromNetwork(FriendlyByteBuf buffer) {
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
        
        ResourceLocation categoryToOpen = buffer.readResourceLocation();

        return new CategoryLinkBookEntry(id, categoryId, name, description, icon, x, y, entryBackgroundUIndex, entryBackgroundVIndex, hideWhileLocked, showWhenAnyParentUnlocked, condition, parentEntries, commandToRunOnFirstReadId, categoryToOpen);
    }
    
    @Override
    public void build(Level level, BookCategory category) {
        super.build(level, category);
        
        if (this.categoryToOpenId != null) {
            this.categoryToOpen = this.book.getCategory(this.categoryToOpenId);
            
            if (this.categoryToOpen == null) {
                BookErrorManager.get().error("Category to open \"" + this.categoryToOpenId + "\" does not exist in this book. Set to null.");
                this.categoryToOpenId = null;
            }
        }
    }

    public BookCategory getCategoryToOpen() {
        return this.categoryToOpen;
    }
    
    public BookContentScreen openEntry(BookCategoryScreen categoryScreen) {
        categoryScreen.getBookOverviewScreen().changeCategory(getCategoryToOpen());
        return null;
    }
    
}

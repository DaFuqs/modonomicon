/*
 * SPDX-FileCopyrightText: 2024 DaFuqs
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.book.conditions;

import com.google.gson.JsonObject;
import com.klikli_dev.modonomicon.api.ModonomiconConstants;
import com.klikli_dev.modonomicon.book.conditions.context.BookConditionContext;
import com.klikli_dev.modonomicon.book.conditions.context.BookConditionEntryContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * This BookCondition evaluates to true
 * if a category has at least
 */
public class BookCategoryHasVisibleEntriesCondition extends BookCondition {
    
    protected ResourceLocation categoryId;
    
    public BookCategoryHasVisibleEntriesCondition(Component tooltip, ResourceLocation categoryId) {
        super(tooltip);
        this.categoryId = categoryId;
    }
    
    public static BookCategoryHasVisibleEntriesCondition fromJson(JsonObject json) {
        ResourceLocation categoryId = new ResourceLocation(GsonHelper.getAsString(json, "category_id"));
        Component tooltip = Component.translatable(ModonomiconConstants.I18n.Tooltips.CONDITION_CATEGORY_HAS_VISIBLE_ENTRIES, categoryId);
        return new BookCategoryHasVisibleEntriesCondition(tooltip, categoryId);
    }
    @Override
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.tooltip != null);
        if (this.tooltip != null) {
            buffer.writeComponent(this.tooltip);
        }
        buffer.writeResourceLocation(this.categoryId);
    }
    
    public static BookCategoryHasVisibleEntriesCondition fromNetwork(FriendlyByteBuf buffer) {
        var tooltip = buffer.readBoolean() ? buffer.readComponent() : null;
        var entryId = buffer.readResourceLocation();
        return new BookCategoryHasVisibleEntriesCondition(tooltip, entryId);
    }
    
    @Override
    public ResourceLocation getType() {
        return ModonomiconConstants.Data.Condition.CATEGORY_HAS_VISIBLE_ENTRIES;
    }
    
    @Override
    public boolean test(BookConditionContext context, Player player) {
        var category = context.book.getCategory(this.categoryId);
        if (category == null) {
            throw new IllegalArgumentException("Category with id " + this.categoryId + " not found in book " + context.getBook().getId() + "for BookCategoryHasVisibleEntriesCondition. This happened while trying to unlock " + context);
        }

        if(category.getEntries().isEmpty()) {
            return false;
        }

        for(var entry : category.getEntries().values()) {
            if(entry.getEntryDisplayState(player).isVisible()) {
                return true;
            }
        }

        return false;
    }
    
    @Override
    public List<Component> getTooltip(Player player, BookConditionContext context) {
        if (this.tooltip == null && context instanceof BookConditionEntryContext entryContext) {
            this.tooltip = Component.translatable(ModonomiconConstants.I18n.Tooltips.CONDITION_CATEGORY_HAS_VISIBLE_ENTRIES, Component.translatable(entryContext.getBook().getEntry(this.categoryId).getName()));
        }
        return super.getTooltip(player, context);
    }
}

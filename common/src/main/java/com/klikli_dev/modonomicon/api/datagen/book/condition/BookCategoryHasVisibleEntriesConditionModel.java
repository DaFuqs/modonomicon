/*
 *
 *  * SPDX-FileCopyrightText: 2022 klikli-dev
 *  *
 *  * SPDX-License-Identifier: MIT
 *
 */

package com.klikli_dev.modonomicon.api.datagen.book.condition;

import com.google.gson.JsonObject;
import com.klikli_dev.modonomicon.api.ModonomiconConstants.Data.Condition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BookCategoryHasVisibleEntriesConditionModel extends BookConditionModel {
    protected String categoryId;

    protected BookCategoryHasVisibleEntriesConditionModel(String categoryId, Component tooltip, String tooltipString) {
        super(Condition.CATEGORY_HAS_VISIBLE_ENTRIES, tooltip, tooltipString);
        this.categoryId = categoryId;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public JsonObject toJson() {
        var json = super.toJson();
        json.addProperty("category_id", this.categoryId);
        return json;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public static final class Builder {
        private String categoryId;
        private Component tooltip;
        private String tooltipString;

        private Builder() {
        }

        public static Builder aBookAdvancementConditionModel() {
            return new Builder();
        }

        public String getCategoryId() {
            return this.categoryId;
        }

        public Component getTooltip() {
            return this.tooltip;
        }

        public String getTooltipString() {
            return this.tooltipString;
        }

        public Builder withCategory(ResourceLocation entryId) {
            this.categoryId = entryId.toString();
            return this;
        }

        public Builder withCategory(String entryId) {
            this.categoryId = entryId;
            return this;
        }

        public Builder withTooltip(Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        /**
         * Will overwrite withTooltip
         */
        public Builder withTooltipString(String tooltipString) {
            this.tooltipString = tooltipString;
            return this;
        }
        
        public BookCategoryHasVisibleEntriesConditionModel build() {
            return new BookCategoryHasVisibleEntriesConditionModel(this.categoryId, this.tooltip, this.tooltipString);
        }
    }
}

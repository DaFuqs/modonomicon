/*
 *
 *  * SPDX-FileCopyrightText: 2022 klikli-dev
 *  *
 *  * SPDX-License-Identifier: MIT
 *
 */

package com.klikli_dev.modonomicon.api.datagen.book.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.klikli_dev.modonomicon.api.ModonomiconConstants.Data.Condition;

public class BookAndConditionModel extends BookConditionModel<BookAndConditionModel> {

    protected BookConditionModel<?>[] children;

    protected BookAndConditionModel() {
        super(Condition.AND);
    }

    public static BookAndConditionModel create() {
        return new BookAndConditionModel();
    }

    public BookConditionModel<?>[] getChildren() {
        return this.children;
    }

    @Override
    public JsonObject toJson() {
        var json = super.toJson();
        var children = new JsonArray();
        for (var child : this.children) {
            children.add(child.toJson());
        }
        json.add("children", children);
        return json;
    }

    public BookAndConditionModel withChildren(BookConditionModel<?>... children) {
        this.children = children;
        return this;
    }
}

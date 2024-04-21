/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.book.conditions.context;

import com.klikli_dev.modonomicon.book.Book;
import com.klikli_dev.modonomicon.book.entries.BookEntry;

public class BookConditionEntryContext extends BookConditionContext {
    public final BookEntry bookEntry;

    public BookConditionEntryContext(Book book, BookEntry bookEntry) {
        super(book);
        this.bookEntry = bookEntry;
    }

    @Override
    public String toString() {
        return "BookConditionEntryContext{" +
                "book=" + this.book +
                ", entry=" + this.bookEntry +
                '}';
    }

    public BookEntry getEntry() {
        return this.bookEntry;
    }
}

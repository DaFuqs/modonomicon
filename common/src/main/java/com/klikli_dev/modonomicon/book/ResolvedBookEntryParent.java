/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.book;

import com.klikli_dev.modonomicon.book.entries.BookEntry;

public class ResolvedBookEntryParent extends BookEntryParent {
    protected BookEntry bookEntry;

    public ResolvedBookEntryParent(BookEntryParent unresolved, BookEntry bookEntry) {
        super(bookEntry.getId());
        this.bookEntry = bookEntry;
        this.drawArrow = unresolved.drawArrow;
        this.lineEnabled = unresolved.lineEnabled;
        this.lineReversed = unresolved.lineReversed;
    }

    @Override
    public BookEntry getEntry() {
        return this.bookEntry;
    }
}

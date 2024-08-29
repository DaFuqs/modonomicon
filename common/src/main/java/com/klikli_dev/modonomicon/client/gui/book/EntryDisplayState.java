/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.client.gui.book;

public enum EntryDisplayState {
    HIDDEN(false, false),
    LOCKED(true, false),
    UNLOCKED(true, true);

    private final boolean visible;
    private final boolean unlocked;

    EntryDisplayState(boolean visible, boolean unlocked) {
        this.visible = visible;
        this.unlocked = unlocked;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isUnlocked() {
        return this.unlocked;
    }

}

/*
 * SPDX-FileCopyrightText: 2023 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.bookstate;

import com.klikli_dev.modonomicon.Modonomicon;
import com.klikli_dev.modonomicon.book.*;
import com.klikli_dev.modonomicon.book.entries.*;
import com.klikli_dev.modonomicon.bookstate.visual.BookVisualState;
import com.klikli_dev.modonomicon.bookstate.visual.CategoryVisualState;
import com.klikli_dev.modonomicon.bookstate.visual.EntryVisualState;
import com.klikli_dev.modonomicon.networking.RequestSyncBookStatesMessage;
import com.klikli_dev.modonomicon.networking.SyncBookVisualStatesMessage;
import com.klikli_dev.modonomicon.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class BookVisualStateManager {

    private static final BookVisualStateManager instance = new BookVisualStateManager();
    public BookStatesSaveData saveData;

    public static BookVisualStateManager get() {
        return instance;
    }

    private BookVisualStates getStateFor(Player player) {
        this.getSaveDataIfNecessary(player);
        return this.saveData.getVisualStates(player.getUUID());
    }

    public BookVisualState getBookStateFor(Player player, Book book) {
        return this.getStateFor(player).getBookState(book);
    }

    public CategoryVisualState getCategoryStateFor(Player player, BookCategory category) {
        return this.getStateFor(player).getCategoryState(category);
    }

    public EntryVisualState getEntryStateFor(Player player, BookEntry entry) {
        return this.getStateFor(player).getEntryState(entry);
    }

    public void setEntryStateFor(ServerPlayer player, BookEntry entry, EntryVisualState state) {
        this.getStateFor(player).setEntryState(entry, state);
        this.saveData.setDirty();
    }

    public void setCategoryStateFor(ServerPlayer player, BookCategory category, CategoryVisualState state) {
        this.getStateFor(player).setCategoryState(category, state);
        this.saveData.setDirty();
    }

    public void setBookStateFor(ServerPlayer player, Book book, BookVisualState state) {
        this.getStateFor(player).setBookState(book, state);
        this.saveData.setDirty();
    }

    public void syncFor(ServerPlayer player) {
        Services.NETWORK.sendTo(player, new SyncBookVisualStatesMessage(this.getStateFor(player)));
    }

    private void getSaveDataIfNecessary(Player player) {
        if (this.saveData == null) {
            if (player instanceof ServerPlayer serverPlayer) {
                this.saveData = serverPlayer.getServer().overworld().getDataStorage().computeIfAbsent(
                        BookStatesSaveData::load,
                        BookStatesSaveData::new, BookStatesSaveData.ID);
            } else {
                //this should not happen, we set an empty object to prevent a crash
                this.saveData = new BookStatesSaveData();
                //and we request a sync
                Services.NETWORK.sendToServer(new RequestSyncBookStatesMessage());
                Modonomicon.LOG.error("Tried to get Modonomicon save data for player on client side, but was not set. This should not happen. Requesting a sync from the server. Please re-open the book in a few seconds to see your progress.");
            }
        }
    }


}

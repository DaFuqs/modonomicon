/*
 * SPDX-FileCopyrightText: 2023 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.bookstate;

import com.klikli_dev.modonomicon.Modonomicon;
import com.klikli_dev.modonomicon.book.Book;
import com.klikli_dev.modonomicon.book.BookCategory;
import com.klikli_dev.modonomicon.book.BookCommand;
import com.klikli_dev.modonomicon.book.entries.BookEntry;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.data.BookDataManager;
import com.klikli_dev.modonomicon.networking.RequestSyncBookStatesMessage;
import com.klikli_dev.modonomicon.networking.SyncBookUnlockStatesMessage;
import com.klikli_dev.modonomicon.platform.Services;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BookUnlockStateManager {

    private static final BookUnlockStateManager instance = new BookUnlockStateManager();
    private final Set<UUID> syncRequestedPlayers = new ObjectLinkedOpenHashSet<>();
    public BookStatesSaveData saveData;
    private boolean wasLoaded = false;

    public static BookUnlockStateManager get() {
        return instance;
    }

    private BookUnlockStates getStateFor(Player player) {
        this.getSaveDataIfNecessary(player);
        return this.saveData.getUnlockStates(player.getUUID());
    }

    public String getUnlockCodeFor(Player player, Book book) {
        return this.getStateFor(player).getUnlockCode(book);
    }

    public Book applyUnlockCodeFor(ServerPlayer player, String unlockCode) {
        var book = this.getStateFor(player).applyUnlockCode(unlockCode);
        if (book != null) {
            this.saveData.setDirty();
            this.syncFor(player);
        }
        return book;
    }

    public void syncFor(ServerPlayer player) {
        Services.NETWORK.sendTo(player, new SyncBookUnlockStatesMessage(this.getStateFor(player)));
    }

    public void updateAndSyncFor(ServerPlayer player) {
        if (BookDataManager.get().areBooksBuilt()) {
            this.getStateFor(player).update(player);
            this.saveData.setDirty();
            this.syncFor(player);
        } else {
            this.syncRequestedPlayers.add(player.getUUID());
            this.wasLoaded = false;
        }
    }

    public List<ResourceLocation> getBooksFor(Player player) {
        return this.getStateFor(player).getBooks();
    }

    public void resetFor(ServerPlayer player, Book book) {
        this.getStateFor(player).reset(book);
        this.saveData.setDirty();
        this.syncFor(player);
    }

    public boolean isUnlockedFor(Player player, BookCategory category) {
        return this.getStateFor(player).isUnlocked(category);
    }

    public boolean isUnlockedFor(Player player, BookEntry entry) {
        return this.getStateFor(player).isUnlocked(entry);
    }

    public boolean isUnlockedFor(Player player, BookPage page) {
        return this.getStateFor(player).isUnlocked(page);
    }

    public List<BookPage> getUnlockedPagesFor(Player player, BookEntry entry) {
        return this.getStateFor(player).getUnlockedPagesIn(entry);
    }

    public boolean isReadFor(Player player, BookEntry entry) {
        return this.getStateFor(player).isRead(entry);
    }

    public boolean canRunFor(Player player, BookCommand command) {
        return this.getStateFor(player).canRun(command);
    }

    /**
     * Sets the given command as run (counts up the run count).
     * Modifies state, but needs to be run on client side to update the book gui quickly.
     * Does not call syncFor, needs to be done by the caller side if needed.
     */
    public void setRunFor(Player player, BookCommand command) {
        this.getStateFor(player).setRun(command);
    }

    /**
     * Modifies state, but does not call syncFor, needs to be done by the caller side if needed.
     */
    public boolean readFor(ServerPlayer player, BookEntry entry) {
        return this.getStateFor(player).read(entry, player);
    }

    public void onAdvancement(ServerPlayer player) {
        this.updateAndSyncFor(player);
    }

    /**
     * On the server will use the player reference to obtain the BookStatesSaveData from the overworld data storage.
     * Won't do anything on the client, clients get their save data set by the server via packet.
     */
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

    public void onServerTickEnd(MinecraftServer server) {
        if (server.getTickCount() % 100 != 0) return; //We only update every 5 seconds (100 ticks)
        boolean newState = BookDataManager.get().areBooksBuilt();
        if (newState != this.wasLoaded) { // we only check for things if the state changed for some reason.
            if (!this.wasLoaded && !this.syncRequestedPlayers.isEmpty()) { //we only process players if we have any and the state was correct.
                var list = server.getPlayerList();
                for (UUID id : this.syncRequestedPlayers) {
                    ServerPlayer player = list.getPlayer(id);
                    if (player != null) this.updateAndSyncFor(player);
                }
                this.syncRequestedPlayers.clear();
            }
            this.wasLoaded = newState;
        }
    }
}

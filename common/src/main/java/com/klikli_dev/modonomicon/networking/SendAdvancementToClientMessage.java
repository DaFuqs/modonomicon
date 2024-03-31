/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.networking;

import com.klikli_dev.modonomicon.Modonomicon;
import com.klikli_dev.modonomicon.data.BookDataManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class SendAdvancementToClientMessage implements Message {

    public static final ResourceLocation ID = new ResourceLocation(Modonomicon.MOD_ID, "send_advancement_to_client");

    public AdvancementHolder advancement;

    public SendAdvancementToClientMessage(AdvancementHolder advancement) {
        this.advancement = advancement;
    }

    public SendAdvancementToClientMessage(FriendlyByteBuf buf) {
        this.decode(buf);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        this.advancement.write(buf);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        this.advancement = AdvancementHolder.read(buf);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void onClientReceived(Minecraft minecraft, Player player) {
        BookDataManager.Client.get().addAdvancement(this.advancement);
    }
}

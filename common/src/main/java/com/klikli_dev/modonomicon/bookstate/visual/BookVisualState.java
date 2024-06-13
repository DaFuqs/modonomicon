/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.bookstate.visual;

import com.klikli_dev.modonomicon.util.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public class BookVisualState {
    public static final Codec<BookVisualState> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    Codecs.mutableMap(ResourceLocation.CODEC, CategoryVisualState.CODEC).fieldOf("categoryStates").forGetter((state) -> state.categoryStates),
                    ResourceLocation.CODEC.optionalFieldOf("openCategory").forGetter((state) -> Optional.ofNullable(state.openCategory)),
                    Codec.INT.fieldOf("openPagesIndex").forGetter((state) -> state.openPagesIndex)
            ).apply(instance, BookVisualState::new));

    public Map<ResourceLocation, CategoryVisualState> categoryStates;

    public ResourceLocation openCategory;

    /**
     * For books in index mode
     */
    public int openPagesIndex;

    public BookVisualState() {
        this(Object2ObjectMaps.emptyMap(), Optional.empty(), 0);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public BookVisualState(Map<ResourceLocation, CategoryVisualState> categoryStates, Optional<ResourceLocation> openCategory, int openPagesIndex) {
        this.categoryStates = new Object2ObjectOpenHashMap<>(categoryStates);
        this.openCategory = openCategory.orElse(null);
        this.openPagesIndex = openPagesIndex;
    }
}

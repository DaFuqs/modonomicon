package com.klikli_dev.modonomicon.api.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;

public class NeoBookProvider {
    public static BookProvider of(GatherDataEvent event, BookSubProvider... subProviders) {
        return new BookProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getModContainer().getModId(), List.of(subProviders));
    }
}

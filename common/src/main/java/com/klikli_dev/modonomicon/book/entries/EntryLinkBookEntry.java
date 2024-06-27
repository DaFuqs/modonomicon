package com.klikli_dev.modonomicon.book.entries;

import com.google.gson.JsonObject;
import com.klikli_dev.modonomicon.api.ModonomiconConstants;
import com.klikli_dev.modonomicon.book.BookCategory;
import com.klikli_dev.modonomicon.book.error.BookErrorManager;
import com.klikli_dev.modonomicon.client.gui.book.BookCategoryScreen;
import com.klikli_dev.modonomicon.client.gui.book.BookContentScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

/**
 * A node that is a link to another existing entry
 */
public class EntryLinkBookEntry extends BookEntry {
	
	/**
	 * The entry to open on click
	 */
	protected ResourceLocation entryToOpenId;
	protected BookEntry entryToOpen;
	
	public EntryLinkBookEntry(ResourceLocation id, BookEntryData data, ResourceLocation commandToRunOnFirstReadId, ResourceLocation entryToOpenId) {
		super(id, data, commandToRunOnFirstReadId);
		this.entryToOpenId = entryToOpenId;
	}
	
	@Override
	public ResourceLocation getType() {
		return ModonomiconConstants.Data.EntryType.ENTRY_LINK;
	}
	
	public static EntryLinkBookEntry fromJson(ResourceLocation id, JsonObject json, boolean autoAddReadConditions) {
		BookEntryData data = BookEntryData.fromJson(json, autoAddReadConditions);
		
		ResourceLocation commandToRunOnFirstReadId = null;
		if (json.has("command_to_run_on_first_read")) {
			commandToRunOnFirstReadId = new ResourceLocation(GsonHelper.getAsString(json, "command_to_run_on_first_read"));
		}
		ResourceLocation entryToOpen = new ResourceLocation(GsonHelper.getAsString(json, "entry_to_open"));
		
		return new EntryLinkBookEntry(id, data, commandToRunOnFirstReadId, entryToOpen);
	}
	
	@Override
	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(this.id);
		this.data.toNetwork(buffer);
		buffer.writeNullable(this.commandToRunOnFirstReadId, FriendlyByteBuf::writeResourceLocation);
		buffer.writeNullable(this.entryToOpenId, FriendlyByteBuf::writeResourceLocation);
	}
	
	public static EntryLinkBookEntry fromNetwork(FriendlyByteBuf buffer) {
		var id = buffer.readResourceLocation();
		BookEntryData data = BookEntryData.fromNetwork(buffer);
		ResourceLocation commandToRunOnFirstReadId = buffer.readNullable(FriendlyByteBuf::readResourceLocation);
		ResourceLocation entryToOpen = buffer.readNullable(FriendlyByteBuf::readResourceLocation);
		
		return new EntryLinkBookEntry(id, data, commandToRunOnFirstReadId, entryToOpen);
	}
	
	@Override
	public void build(Level level, BookCategory category) {
		super.build(level, category);
		
		if (this.entryToOpenId != null) {
			this.entryToOpen = this.getBook().getEntry(this.entryToOpenId);
			
			if (this.entryToOpen == null) {
				BookErrorManager.get().error("Entry to open \"" + this.entryToOpenId + "\" does not exist in this book. Set to null.");
				this.entryToOpenId = null;
			}
		}
	}
	
	public BookEntry getEntryToOpen() {
		return this.entryToOpen;
	}
	
	public BookContentScreen openEntry(BookCategoryScreen categoryScreen) {
		categoryScreen.openEntry(getEntryToOpen());
		return null;
	}
	
}

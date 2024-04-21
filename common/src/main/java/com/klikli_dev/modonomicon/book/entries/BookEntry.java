package com.klikli_dev.modonomicon.book.entries;

import com.klikli_dev.modonomicon.book.*;
import com.klikli_dev.modonomicon.book.conditions.*;
import com.klikli_dev.modonomicon.book.error.*;
import com.klikli_dev.modonomicon.book.page.*;
import com.klikli_dev.modonomicon.client.gui.book.*;
import com.klikli_dev.modonomicon.client.gui.book.markdown.*;
import net.minecraft.network.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.level.*;

import java.util.*;

public abstract class BookEntry {
	protected ResourceLocation id;
	protected ResourceLocation categoryId;
	protected BookCategory category;
	protected Book book;
	protected List<BookEntryParent> parents;
	protected String name;
	protected String description;
	protected BookIcon icon;
	protected int x;
	protected int y;

	//The first two rows in "entry_textures.png" are reserved for the entry icons.
	//the entry background is selected by querying the texture at entryBackgroundUIndex * 26 (= Y Axis / Up-Down), entryBackgroundUIndex * 26 (= X Axis / Left-Right)

	/**
	 * = Y Axis / Up-Down
	 */
	protected int entryBackgroundUIndex;
	/**
	 * = X Axis / Left-Right
	 */
	protected int entryBackgroundVIndex;
	protected boolean hideWhileLocked;
	/**
	 * If true, the entry will show (locked) as soon as any parent is unlocked.
	 * If false, the entry will only show (locked) as soon as all parents are unlocked.
	 */
	protected boolean showWhenAnyParentUnlocked;
	protected BookCondition condition;

	/**
	 * if this is not null, the command will be run when the entry is first read.
	 */
	protected ResourceLocation commandToRunOnFirstReadId;
	protected BookCommand commandToRunOnFirstRead;

	public BookEntry(ResourceLocation id, ResourceLocation categoryId, List<BookEntryParent> parents, String name, String description, BookIcon icon, int x, int y, int entryBackgroundUIndex, int entryBackgroundVIndex, boolean hideWhileLocked, boolean showWhenAnyParentUnlocked, BookCondition condition, ResourceLocation commandToRunOnFirstReadId) {
		this.id = id;
		this.categoryId = categoryId;
		this.parents = parents;
		this.name = name;
		this.description = description;
		this.icon = icon;
		this.x = x;
		this.y = y;
		this.entryBackgroundUIndex = entryBackgroundUIndex;
		this.entryBackgroundVIndex = entryBackgroundVIndex;
		this.hideWhileLocked = hideWhileLocked;
		this.showWhenAnyParentUnlocked = showWhenAnyParentUnlocked;
		this.condition = condition;
		this.commandToRunOnFirstReadId = commandToRunOnFirstReadId;
	}

	public abstract ResourceLocation getType();

	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(this.id);
		buffer.writeResourceLocation(this.categoryId);
		buffer.writeUtf(this.name);
		buffer.writeUtf(this.description);
		this.icon.toNetwork(buffer);
		buffer.writeVarInt(this.x);
		buffer.writeVarInt(this.y);
		buffer.writeVarInt(this.entryBackgroundUIndex);
		buffer.writeVarInt(this.entryBackgroundVIndex);
		buffer.writeBoolean(this.hideWhileLocked);
		buffer.writeBoolean(this.showWhenAnyParentUnlocked);

		buffer.writeVarInt(this.parents.size());
		for (var parent : this.parents) {
			parent.toNetwork(buffer);
		}

		BookCondition.toNetwork(this.condition, buffer);
		buffer.writeNullable(this.commandToRunOnFirstReadId, FriendlyByteBuf::writeResourceLocation);
	}

	/**
	 * call after loading the book jsons to finalize.
	 */
	public void build(Level level, BookCategory category) {
		this.category = category;
		this.book = category.getBook();

		//resolve parents
		var newParents = new ArrayList<BookEntryParent>();
		for (var parent : this.getParents()) {
			var parentEntry = this.book.getEntry(parent.getEntryId());
			if (parentEntry == null) {
				BookErrorManager.get().error("Entry \"" + this.getId() + "\" has a parent that does not exist in this book: \"" + parent.getEntryId() + "\". This parent will be ignored");
			} else {
				newParents.add(new ResolvedBookEntryParent(parent, parentEntry));
			}
		}
		this.parents = newParents;

		if (this.commandToRunOnFirstReadId != null) {
			this.commandToRunOnFirstRead = this.book.getCommand(this.commandToRunOnFirstReadId);

			if (this.commandToRunOnFirstRead == null) {
				BookErrorManager.get().error("Command to run on first read \"" + this.commandToRunOnFirstReadId + "\" does not exist in this book. Set to null.");
				this.commandToRunOnFirstReadId = null;
			}
		}
	}

	public int getY() {
		return this.y;
	}

	public int getX() {
		return this.x;
	}

	public boolean hideWhileLocked() {
		return this.hideWhileLocked;
	}

	public boolean showWhenAnyParentUnlocked() {
		return this.showWhenAnyParentUnlocked;
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public ResourceLocation getCategoryId() {
		return this.categoryId;
	}

	public BookCategory getCategory() {
		return this.category;
	}

	public List<BookEntryParent> getParents() {
		return this.parents;
	}

	public String getName() {
		return this.name;
	}

	public BookIcon getIcon() {
		return this.icon;
	}

	public String getDescription() {
		return this.description;
	}

	public Book getBook() {
		return this.book;
	}

	public BookCondition getCondition() {
		return this.condition;
	}

	public void setCondition(BookCondition condition) {
		this.condition = condition;
	}

	/**
	 * = Y Axis / Up-Down
	 */
	public int getEntryBackgroundUIndex() {
		return this.entryBackgroundUIndex;
	}

	/**
	 * = X Axis / Left-Right
	 */
	public int getEntryBackgroundVIndex() {
		return this.entryBackgroundVIndex;
	}

	/**
	 * Returns true if this entry should show up in search for the given query.
	 */
	public boolean matchesQuery(String query) {
		return this.getName().toLowerCase().contains(query);
	}

	/**
	 * Called after build() (after loading the book jsons) to render markdown and store any errors
	 */
	public void prerenderMarkdown(BookTextRenderer textRenderer) {

	}

	public int getPageNumberForAnchor(String anchor) {
		return -1;
	}

	public List<BookPage> getPages() {
		return List.of();
	}

	public List<BookPage> getUnlockedPagesFor(Player player) {
		return List.of();
	}

	public abstract BookContentScreen openEntry(BookCategoryScreen categoryScreen);

	public BookCommand getCommandToRunOnFirstRead() {
		return this.commandToRunOnFirstRead;
	}


}

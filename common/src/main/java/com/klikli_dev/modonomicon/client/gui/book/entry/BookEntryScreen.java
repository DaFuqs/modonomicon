/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.client.gui.book.entry;

import com.klikli_dev.modonomicon.Modonomicon;
import com.klikli_dev.modonomicon.api.ModonomiconConstants.I18n.Gui;
import com.klikli_dev.modonomicon.book.Book;
import com.klikli_dev.modonomicon.book.BookLink;
import com.klikli_dev.modonomicon.book.CommandLink;
import com.klikli_dev.modonomicon.book.entries.BookContentEntry;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.bookstate.BookUnlockStateManager;
import com.klikli_dev.modonomicon.bookstate.BookVisualStateManager;
import com.klikli_dev.modonomicon.bookstate.visual.EntryVisualState;
import com.klikli_dev.modonomicon.client.gui.BookGuiManager;
import com.klikli_dev.modonomicon.client.gui.book.BookAddress;
import com.klikli_dev.modonomicon.client.gui.book.BookContentRenderer;
import com.klikli_dev.modonomicon.client.gui.book.BookPaginatedScreen;
import com.klikli_dev.modonomicon.client.gui.book.BookParentScreen;
import com.klikli_dev.modonomicon.client.gui.book.button.AddBookmarkButton;
import com.klikli_dev.modonomicon.client.gui.book.button.BackButton;
import com.klikli_dev.modonomicon.client.gui.book.button.RemoveBookmarkButton;
import com.klikli_dev.modonomicon.client.gui.book.button.SearchButton;
import com.klikli_dev.modonomicon.client.gui.book.entry.linkhandler.*;
import com.klikli_dev.modonomicon.client.render.page.BookPageRenderer;
import com.klikli_dev.modonomicon.client.render.page.PageRendererRegistry;
import com.klikli_dev.modonomicon.data.BookDataManager;
import com.klikli_dev.modonomicon.fluid.FluidHolder;
import com.klikli_dev.modonomicon.integration.ModonomiconJeiIntegration;
import com.klikli_dev.modonomicon.networking.AddBookmarkMessage;
import com.klikli_dev.modonomicon.networking.SyncBookVisualStatesMessage;
import com.klikli_dev.modonomicon.platform.ClientServices;
import com.klikli_dev.modonomicon.platform.Services;
import com.klikli_dev.modonomicon.platform.services.FluidHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class BookEntryScreen extends BookPaginatedScreen {

    public static final int TOP_PADDING = 15;
    public static final int LEFT_PAGE_X = 12;
    public static final int RIGHT_PAGE_X = 141;
    public static final int SINGLE_PAGE_X = LEFT_PAGE_X;
    public static final int PAGE_WIDTH = 124;
    public static final int PAGE_HEIGHT = 128; //TODO: Adjust to what is real

    public static final int MAX_TITLE_WIDTH = PAGE_WIDTH - 4;

    public static final int CLICK_SAFETY_MARGIN = 20;

    protected final BookParentScreen parentScreen;
    protected final BookContentEntry entry;
    protected final ResourceLocation bookContentTexture;

    public int ticksInBook;
    protected List<BookPage> unlockedPages;

    /**
     * The index of the leftmost unlocked page being displayed.
     */
    protected int openPagesIndex;
    protected List<LinkHandler> linkHandlers;
    private List<Component> tooltip;
    private ItemStack tooltipStack;
    private FluidHolder tooltipFluidStack;
    private boolean isHoveringItemLink;

    public BookEntryScreen(BookParentScreen parentScreen, BookContentEntry entry) {
        super(Component.literal(""));

        this.parentScreen = parentScreen;

        this.minecraft = Minecraft.getInstance();


        this.entry = entry;

        this.bookContentTexture = this.parentScreen.getBook().getBookContentTexture();

        //We're doing that here to ensure unlockedPages is available for state modification during loading
        this.unlockedPages = this.entry.getUnlockedPagesFor(this.minecraft.player);

        this.linkHandlers = List.of(
                new BookLinkHandler(this),
                new PatchouliLinkHandler(this),
                new ItemLinkHandler(this),
                new CommandLinkHandler(this)
        );
    }

    public int getCurrentPageNumber() {
        return this.unlockedPages.get(this.openPagesIndex).getPageNumber();
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public BookContentEntry getEntry() {
        return this.entry;
    }

    @Override
    public Book getBook() {
        return this.entry.getBook();
    }

    public void setTooltip(Component... strings) {
        this.setTooltip(List.of(strings));
    }

    @Override
    public void setTooltip(List<Component> tooltip) {
        this.resetTooltip();
        this.tooltip = tooltip;
    }

    //TODO: not sure if content renderer should call this on a parent screen it is handed over, or if it should call it on the top most screen?
    public void setTooltipStack(ItemStack stack) {
        this.resetTooltip();
        this.tooltipStack = stack;
    }

    public void setTooltipStack(FluidHolder stack) {
        this.resetTooltip();
        this.tooltipFluidStack = stack;
    }

    //TODO should no longer be called in relative range .. and can probably move the book content renderer to reduce dependency on screen

    /**
     * Doesn't actually translate, as it's not necessary, just checks if mouse is in given area
     */
    public boolean isMouseInRelativeRange(double absMx, double absMy, int x, int y, int w, int h) {
        double mx = absMx; //this.getRelativeX(absMx);
        double my = absMy; //this.getRelativeY(absMy);

        return mx > x && my > y && mx <= (x + w) && my <= (y + h);
    }


    //TODO move to book content renderer
    public void renderItemStack(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, ItemStack stack) {
        if (stack.isEmpty() || !PageRendererRegistry.isRenderable(stack)) {
            return;
        }

        guiGraphics.renderItem(stack, x, y);
        guiGraphics.renderItemDecorations(this.font, stack, x, y);

        if (this.isMouseInRelativeRange(mouseX, mouseY, x, y, 16, 16)) {
            this.setTooltipStack(stack);
        }
    }

    public void renderItemStacks(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, Collection<ItemStack> stacks) {
        this.renderItemStacks(guiGraphics, x, y, mouseX, mouseY, stacks, -1);
    }

    public void renderItemStacks(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, Collection<ItemStack> stacks, int countOverride) {
        var filteredStacks = PageRendererRegistry.filterRenderableItemStacks(stacks);
        if (filteredStacks.size() > 0) {
            var currentStack = filteredStacks.get((this.ticksInBook / 20) % filteredStacks.size());
            this.renderItemStack(guiGraphics, x, y, mouseX, mouseY, countOverride > 0 ? currentStack.copyWithCount(countOverride) : currentStack);
        }
    }

    public void renderIngredient(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, Ingredient ingr) {
        this.renderItemStacks(guiGraphics, x, y, mouseX, mouseY, Arrays.asList(ingr.getItems()), -1);
    }

    public void renderIngredient(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, Ingredient ingr, int countOverride) {
        this.renderItemStacks(guiGraphics, x, y, mouseX, mouseY, Arrays.asList(ingr.getItems()), countOverride);
    }

    public void renderFluidStack(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, FluidHolder stack) {
        this.renderFluidStack(guiGraphics, x, y, mouseX, mouseY, stack, FluidHolder.BUCKET_VOLUME);
    }

    public void renderFluidStack(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, FluidHolder stack, int capacity) {
        if (stack.isEmpty() || !PageRendererRegistry.isRenderable(stack)) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        ClientServices.FLUID.drawFluid(guiGraphics, 18, 18, stack, capacity);
        guiGraphics.pose().popPose();

        if (this.isMouseInRelativeRange(mouseX, mouseY, x, y, 18, 18)) {
            this.setTooltipStack(stack);
        }
    }

    public void renderFluidStacks(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, Collection<FluidHolder> stacks) {
        this.renderFluidStacks(guiGraphics, x, y, mouseX, mouseY, stacks, FluidHolder.BUCKET_VOLUME);
    }

    public void renderFluidStacks(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, Collection<FluidHolder> stacks, int capacity) {
        var filteredStacks = PageRendererRegistry.filterRenderableFluidStacks(stacks);
        if (filteredStacks.size() > 0) {
            this.renderFluidStack(guiGraphics, x, y, mouseX, mouseY, filteredStacks.get((this.ticksInBook / 20) % filteredStacks.size()), capacity);
        }
    }

    public void setOpenPagesIndex(int openPagesIndex) {
        this.openPagesIndex = openPagesIndex;
    }

    /**
     * Will change to the specified page, if not open already
     */
    public void goToPage(int pageIndex, boolean playSound) {
        int openPagesIndex = this.getOpenPagesIndexForPage(pageIndex);
        if (openPagesIndex >= 0 && openPagesIndex < this.unlockedPages.size()) {
            if (this.openPagesIndex != openPagesIndex) {
                this.openPagesIndex = openPagesIndex;

                this.onPageChanged();
                if (playSound) {
                    BookContentRenderer.playTurnPageSound(this.getBook());
                }
            }
        } else {
            Modonomicon.LOG.warn("Tried to change to page index {} corresponding with " +
                    "openPagesIndex {} but max open pages index is {}.", pageIndex, openPagesIndex, this.unlockedPages.size());
        }
    }

    protected Style getClickedComponentStyleAtForPage(BookPageRenderer<?> page, double pMouseX, double pMouseY) {
        if (page != null) {
            return page.getClickedComponentStyleAt(pMouseX - this.bookLeft - page.left, pMouseY - this.bookTop - page.top);
        }

        return null;
    }

    public int getBookLeft() {
        return this.bookLeft;
    }

    public int getBookTop() {
        return this.bookTop;
    }

    public void removeRenderableWidgets(@NotNull Collection<? extends Renderable> renderables) {
        this.renderables.removeIf(renderables::contains);
        this.children().removeIf(c -> c instanceof Renderable && renderables.contains(c));
        this.narratables.removeIf(n -> n instanceof Renderable && renderables.contains(n));
    }

    protected void drawTooltip(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        if (this.tooltipStack != null) {
            List<Component> tooltip = this.getTooltipFromItem(this.tooltipStack);
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, pMouseX, pMouseY);
        } else if (this.tooltipFluidStack != null) {
            List<Component> tooltip = this.getTooltipFromFluid(this.tooltipFluidStack);
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, pMouseX, pMouseY);
        } else if (this.tooltip != null && !this.tooltip.isEmpty()) {
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, this.tooltip, pMouseX, pMouseY);
        }
    }

    protected boolean clickPage(BookPageRenderer<?> page, double mouseX, double mouseY, int mouseButton) {
        if (page != null) {
            return page.mouseClicked(mouseX - this.bookLeft - page.left, mouseY - this.bookTop - page.top, mouseButton);
        }

        return false;
    }

    protected void renderPage(GuiGraphics guiGraphics, BookPageRenderer<?> page, int pMouseX, int pMouseY, float pPartialTick) {
        if (page == null) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(page.left, page.top, 0);
        page.render(guiGraphics, pMouseX - this.bookLeft - page.left, pMouseY - this.bookTop - page.top, pPartialTick);
        guiGraphics.pose().popPose();
    }

    protected void onPageChanged() {
        this.beginDisplayPages();
    }

    protected void resetTooltip() {
        this.tooltip = null;
        this.tooltipStack = null;
        this.tooltipFluidStack = null;
    }

    public void loadState(EntryVisualState state) {
        this.openPagesIndex = state.openPagesIndex;
    }

    public void saveState(EntryVisualState state, boolean savePage) {
        state.openPagesIndex = savePage ? this.openPagesIndex : 0;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            BookGuiManager.get().closeScreenStack(this);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        //do not call super, as it would close the screen stack
        //In most cases closeEntryScreen should be called directly, but if our parent BookPaginatedScreen wants us to close we need to handle that
        BookGuiManager.get().closeEntryScreen(this);
    }

    /**
     * Make public to access from pages
     */
    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T pWidget) {
        return super.addRenderableWidget(pWidget);
    }

    /**
     * Our copy of guiGraphics.renderComponentHoverEffect(); to handle book links
     */
    public void renderComponentHoverEffect(GuiGraphics guiGraphics, @Nullable Style style, int mouseX, int mouseY) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 1000);
        var newStyle = style;
        if (style != null && style.getHoverEvent() != null) {
            if (style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
                var clickEvent = style.getClickEvent();
                if (clickEvent != null) {
                    if (clickEvent.getAction() == Action.CHANGE_PAGE) {

                        //handle book links -> check if locked
                        if (BookLink.isBookLink(clickEvent.getValue())) {
                            var link = BookLink.from(this.getBook(), clickEvent.getValue());
                            var book = BookDataManager.get().getBook(link.bookId);
                            if (link.entryId != null) {
                                var entry = book.getEntry(link.entryId);

                                Integer page = link.pageNumber;
                                if (link.pageAnchor != null) {
                                    page = entry.getPageNumberForAnchor(link.pageAnchor);
                                }

                                //if locked, append lock warning
                                //handleComponentClicked will prevent the actual click

                                if (!BookUnlockStateManager.get().isUnlockedFor(this.minecraft.player, entry)) {
                                    var oldComponent = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);

                                    var newComponent = Component.translatable(
                                            Gui.HOVER_BOOK_LINK_LOCKED,
                                            oldComponent,
                                            Component.translatable(Gui.HOVER_BOOK_ENTRY_LINK_LOCKED_INFO)
                                                    .withStyle(s -> s.withColor(0xff0015).withBold(true))
                                                    .append("\n")
                                                    .append(
                                                            Component.translatable(
                                                                    Gui.HOVER_BOOK_ENTRY_LINK_LOCKED_INFO_HINT,
                                                                    Component.translatable(entry.getCategory().getName())
                                                                            .withStyle(s -> s.withColor(ChatFormatting.GRAY).withItalic(true))
                                                            ).withStyle(s -> s.withBold(false).withColor(ChatFormatting.WHITE))
                                                    )
                                    );

                                    newStyle = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newComponent));
                                } else if (page != null && !BookUnlockStateManager.get().isUnlockedFor(this.minecraft.player, entry.getPages().get(page))) {
                                    var oldComponent = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);

                                    var newComponent = Component.translatable(
                                            Gui.HOVER_BOOK_LINK_LOCKED,
                                            oldComponent,
                                            Component.translatable(Gui.HOVER_BOOK_PAGE_LINK_LOCKED_INFO)
                                                    .withStyle(s -> s.withColor(0xff0015).withBold(true))
                                                    .append("\n")
                                                    .append(
                                                            Component.translatable(
                                                                    Gui.HOVER_BOOK_PAGE_LINK_LOCKED_INFO_HINT,
                                                                    Component.translatable(entry.getName())
                                                                            .withStyle(s -> s.withColor(ChatFormatting.GRAY).withItalic(true)),
                                                                    Component.translatable(entry.getCategory().getName())
                                                                            .withStyle(s -> s.withColor(ChatFormatting.GRAY).withItalic(true))
                                                            ).withStyle(s -> s.withBold(false).withColor(ChatFormatting.WHITE))
                                                    )
                                    );

                                    newStyle = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newComponent));
                                }
                            }
                        }
                    }

                    if (clickEvent.getAction() == Action.RUN_COMMAND) {
                        if (CommandLink.isCommandLink(clickEvent.getValue())) {
                            var link = CommandLink.from(this.getBook(), clickEvent.getValue());
                            var book = BookDataManager.get().getBook(link.bookId);
                            if (link.commandId != null) {
                                var command = book.getCommand(link.commandId);

                                var oldComponent = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);

                                if (!BookUnlockStateManager.get().canRunFor(this.minecraft.player, command)) {
                                    var hoverComponent = Component.translatable(Gui.HOVER_COMMAND_LINK_UNAVAILABLE).withStyle(ChatFormatting.RED);
                                    newStyle = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent));
                                    oldComponent = hoverComponent;
                                }

                                if (hasShiftDown()) {
                                    var newComponent = oldComponent.copy().append(Component.literal("\n")).append(
                                            Component.literal(command.getCommand()).withStyle(ChatFormatting.GRAY));
                                    newStyle = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, newComponent));
                                }
                            }
                        }
                    }
                }
            }
        }

        style = newStyle;

        //original GuiGraphics.renderComponentHoverEffect(pPoseStack, newStyle, mouseX, mouseY);
        // our own copy of the render code that limits width for the show_text action to not go out of screen
        if (style != null && style.getHoverEvent() != null) {
            HoverEvent hoverevent = style.getHoverEvent();
            HoverEvent.ItemStackInfo hoverevent$itemstackinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ITEM);
            if (hoverevent$itemstackinfo != null) {
                //special handling for item link hovers -> we append another line in this.getTooltipFromItem
                if (style.getClickEvent() != null)// && ItemLinkRenderer.isItemLink(style.getClickEvent().getValue()))
                    this.isHoveringItemLink = true;

                //temporarily modify width to force forge to handle wrapping correctly
                var backupWidth = this.width;
                this.width = this.width / 2; //not quite sure why exaclty / 2 works, but then forge wrapping handles it correctly on gui scale 3+4
                guiGraphics.renderTooltip(this.minecraft.font, hoverevent$itemstackinfo.getItemStack(), mouseX, mouseY);
                this.width = backupWidth;

                //then we reset so other item tooltip renders are not affected
                this.isHoveringItemLink = false;
            } else {
                HoverEvent.EntityTooltipInfo hoverevent$entitytooltipinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ENTITY);
                if (hoverevent$entitytooltipinfo != null) {
                    if (this.minecraft.options.advancedItemTooltips) {
                        guiGraphics.renderComponentTooltip(this.minecraft.font, hoverevent$entitytooltipinfo.getTooltipLines(), mouseX, mouseY);
                    }
                } else {
                    Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
                    if (component != null) {
                        //var width = Math.max(this.width / 2, 200); //original width calc
                        var width = (this.width / 2) - mouseX - 10; //our own
                        guiGraphics.renderTooltip(this.minecraft.font, this.minecraft.font.split(component, width), mouseX, mouseY);
                    }
                }
            }

        }
        guiGraphics.pose().popPose();
    }

    /**
     * Used to be override of < 1.20.0 Screen.getTooltipFromItem, which is now static
     */
    public List<Component> getTooltipFromItem(ItemStack pItemStack) {
        var tooltip = getTooltipFromItem(Minecraft.getInstance(), pItemStack);

        if (this.isHoveringItemLink) {
            tooltip.add(Component.literal(""));
            if (ModonomiconJeiIntegration.get().isJeiLoaded()) {
                tooltip.add(Component.translatable(Gui.HOVER_ITEM_LINK_INFO).withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GREEN)));
                tooltip.add(Component.translatable(Gui.HOVER_ITEM_LINK_INFO_LINE2).withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY)));
            } else {
                tooltip.add(Component.translatable(Gui.HOVER_ITEM_LINK_INFO_NO_JEI).withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.RED)));
            }
        }

        return tooltip;
    }

    public List<Component> getTooltipFromFluid(FluidHolder fluidStack) {
        var tooltip = ClientServices.FLUID.getTooltip(fluidStack, FluidHolder.BUCKET_VOLUME, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL, FluidHelper.TooltipMode.SHOW_AMOUNT_AND_CAPACITY);

        if (this.isHoveringItemLink) {
            tooltip.add(Component.literal(""));
            if (ModonomiconJeiIntegration.get().isJeiLoaded()) {
                tooltip.add(Component.translatable(Gui.HOVER_ITEM_LINK_INFO).withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GREEN)));
                tooltip.add(Component.translatable(Gui.HOVER_ITEM_LINK_INFO_LINE2).withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY)));
            } else {
                tooltip.add(Component.translatable(Gui.HOVER_ITEM_LINK_INFO_NO_JEI).withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.RED)));
            }
        }

        return tooltip;
    }


    @Override
    public boolean handleComponentClicked(@Nullable Style pStyle) {
        if (pStyle != null) {
            for (LinkHandler handler : this.linkHandlers) {
                var result = handler.handleClick(pStyle);

                //before the command pattern was implemented we returned false for failures
                //however, I believe failure should also be treated as "handled" to avoid vanilla code doing fun stuff.
                //We retain the failure result in case that turns out to be wrong
                if (result == LinkHandler.ClickResult.FAILURE)
                    return true;

                if (result == LinkHandler.ClickResult.SUCCESS)
                    return true;

                //unhandled -> continue to next
            }
        }
        return super.handleComponentClicked(pStyle);
    }

    @Override
    protected void init() {
        super.init();

        this.unlockedPages = this.entry.getUnlockedPagesFor(this.minecraft.player);
        this.beginDisplayPages();
    }

    @Override
    protected void initNavigationButtons() {
        super.initNavigationButtons();

        this.addRenderableWidget(new BackButton(this, this.width / 2 - BackButton.WIDTH / 2, this.bookTop + FULL_HEIGHT - BackButton.HEIGHT / 2));

        this.updateBookmarksButton();
    }

    protected boolean isBookmarked() {
        return BookVisualStateManager.get().getBookmarksFor(this.minecraft.player, this.entry.getBook()).stream().anyMatch(b -> b.entryId().equals(this.entry.getId()));
    }

    protected void updateBookmarksButton() {
        this.renderables.removeIf(b -> b instanceof AddBookmarkButton || b instanceof SearchButton);
        this.children().removeIf(b -> b instanceof AddBookmarkButton || b instanceof SearchButton);
        this.narratables.removeIf(b -> b instanceof AddBookmarkButton || b instanceof SearchButton);

        int buttonHeight = 20;
        int searchButtonX = this.bookLeft + FULL_WIDTH - 5;
        int searchButtonY = this.bookTop + FULL_HEIGHT - 30;
        int searchButtonWidth = 44; //width in png
        int scissorX = this.bookLeft + FULL_WIDTH;//this is the render location of our frame so our search button never overlaps


        if (this.isBookmarked()) {
            var removeBookMarkButton = new RemoveBookmarkButton(this, searchButtonX, searchButtonY,
                    scissorX,
                    searchButtonWidth, buttonHeight,
                    (b) -> this.onRemoveBookmarksButtonClick((RemoveBookmarkButton) b),
                    Tooltip.create(Component.translatable(Gui.ADD_BOOKMARK)));
            this.addRenderableWidget(removeBookMarkButton);
        } else {
            var addBookmarkButton = new AddBookmarkButton(this, searchButtonX, searchButtonY,
                    scissorX,
                    searchButtonWidth, buttonHeight,
                    (b) -> this.onAddBookmarksButtonClick((AddBookmarkButton) b),
                    Tooltip.create(Component.translatable(Gui.ADD_BOOKMARK)));
            this.addRenderableWidget(addBookmarkButton);
        }

    }

    protected void onAddBookmarksButtonClick(AddBookmarkButton button) {
        if (!this.isBookmarked()) {
            var bookmarkAddress = BookAddress.ignoreSaved(this.entry, this.getPageForOpenPagesIndex(this.openPagesIndex));
            BookVisualStateManager.get().addBookmarkFor(this.minecraft.player, this.entry.getBook(), bookmarkAddress);

            Services.NETWORK.sendToServer(new AddBookmarkMessage(bookmarkAddress));

            this.updateBookmarksButton();
        }
    }

    protected void onRemoveBookmarksButtonClick(RemoveBookmarkButton button) {
        //no need to check for is bookmarked because we query the bookmark in question directly anyway
        var bookmarkAddress = BookVisualStateManager.get().getBookmarksFor(this.minecraft.player, this.entry.getBook()).stream().filter(b -> b.entryId().equals(this.entry.getId())).findFirst().orElse(null);
        if (bookmarkAddress != null) {
            BookVisualStateManager.get().removeBookmarkFor(this.minecraft.player, this.entry.getBook(), bookmarkAddress);

            this.updateBookmarksButton();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasShiftDown()) {
            this.ticksInBook++;
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            var style = this.getClickedComponentStyleAt(pMouseX, pMouseY);
            if (style != null && this.handleComponentClicked(style)) {
                return true;
            }
        }

        if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
        }

        return this.mouseClickedPage(pMouseX, pMouseY, pButton);
    }

    public void onSyncBookVisualStatesMessage(SyncBookVisualStatesMessage message) {
        this.updateBookmarksButton();
    }

    protected abstract int getOpenPagesIndexForPage(int pageIndex);

    /**
     * Gets the page index for the first page to display for the given open pages index.
     */
    protected abstract int getPageForOpenPagesIndex(int openPagesIndex);

    @Nullable
    protected abstract Style getClickedComponentStyleAt(double pMouseX, double pMouseY);

    protected abstract boolean mouseClickedPage(double pMouseX, double pMouseY, int pButton);

    protected abstract void beginDisplayPages();


}

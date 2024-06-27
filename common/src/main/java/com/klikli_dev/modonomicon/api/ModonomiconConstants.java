/*
 * SPDX-FileCopyrightText: 2022 klikli-dev
 *
 * SPDX-License-Identifier: MIT
 */

package com.klikli_dev.modonomicon.api;

import com.klikli_dev.modonomicon.Modonomicon;
import com.klikli_dev.modonomicon.book.BookFrameOverlay;
import net.minecraft.resources.ResourceLocation;

public class ModonomiconConstants {

    public static class Data {
        public static final String MODONOMICON_DATA_PATH = ModonomiconAPI.ID + "/books";
        public static final String MULTIBLOCK_DATA_PATH = ModonomiconAPI.ID + "/multiblocks";

        public static class Book {
            public static final String DEFAULT_OVERVIEW_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "textures/gui/book_overview.png").toString();
            public static final String DEFAULT_FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "textures/gui/book_frame.png").toString();

            public static final BookFrameOverlay DEFAULT_TOP_FRAME_OVERLAY = new BookFrameOverlay(
                    Modonomicon.loc("textures/gui/book_frame_top_overlay.png"),
                    256, 256, 72, 7, 0, 4);

            public static final BookFrameOverlay DEFAULT_BOTTOM_FRAME_OVERLAY = new BookFrameOverlay(
                    Modonomicon.loc("textures/gui/book_frame_bottom_overlay.png"),
                    256, 256, 72, 8, 0, -4);

            public static final BookFrameOverlay DEFAULT_LEFT_FRAME_OVERLAY = new BookFrameOverlay(
                    Modonomicon.loc("textures/gui/book_frame_left_overlay.png"),
                    256, 256, 7, 70, 3, 0);

            public static final BookFrameOverlay DEFAULT_RIGHT_FRAME_OVERLAY = new BookFrameOverlay(
                    Modonomicon.loc("textures/gui/book_frame_right_overlay.png"),
                    256, 256, 8, 70, -4, 0);

            public static final String DEFAULT_CONTENT_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "textures/gui/book_content.png").toString();
            public static final String DEFAULT_SINGLE_PAGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "textures/gui/single_page_entry.png").toString();
            public static final String DEFAULT_FONT = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "default").toString();
            public static final String DEFAULT_CRAFTING_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "textures/gui/crafting_textures.png").toString();
            public static final String DEFAULT_PAGE_TURN_SOUND = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "turn_page").toString();
            public static final String DEFAULT_MODEL = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "modonomicon_purple").toString();
            public static final ResourceLocation ITEM_ID = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "modonomicon");

        }

        public static class Category {
            public static final String DEFAULT_ICON = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "modonomicon_purple").toString();
            public static final String DEFAULT_BACKGROUND = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "textures/gui/dark_slate_seamless.png").toString();
            public static final int DEFAULT_BACKGROUND_WIDTH = 512;
            public static final int DEFAULT_BACKGROUND_HEIGHT = 512;
            public static final float DEFAULT_BACKGROUND_TEXTURE_ZOOM_MULTIPLIER = 1f;
            public static final String DEFAULT_ENTRY_TEXTURES = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "textures/gui/entry_textures.png").toString();
        }

        public static class Icon {
            public static final int DEFAULT_WIDTH = 16;
            public static final int DEFAULT_HEIGHT = 16;
        }

        public static class Command {
            public static final int DEFAULT_MAX_USES = 1;
            public static final int DEFAULT_PERMISSION_LEVEL = 0;
        }

        public static class EntryType {
            public static final ResourceLocation CONTENT = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "content");
            public static final ResourceLocation CATEGORY_LINK = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "category_link");
            public static final ResourceLocation ENTRY_LINK = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "entry_link");
        }

        public static class Page {
            public static final ResourceLocation TEXT = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "text");
            public static final ResourceLocation MULTIBLOCK = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "multiblock");
            public static final ResourceLocation CRAFTING_RECIPE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "crafting_recipe");
            public static final ResourceLocation SMOKING_RECIPE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "smoking_recipe");
            public static final ResourceLocation SMELTING_RECIPE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "smelting_recipe");
            public static final ResourceLocation BLASTING_RECIPE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "blasting_recipe");
            public static final ResourceLocation CAMPFIRE_COOKING_RECIPE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "campfire_cooking_recipe");
            public static final ResourceLocation STONECUTTING_RECIPE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "stonecutting_recipe");
            public static final ResourceLocation SMITHING_RECIPE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "smithing_recipe");
            public static final ResourceLocation SPOTLIGHT = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "spotlight");
            public static final ResourceLocation EMPTY = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "empty");
            public static final ResourceLocation ENTITY = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "entity");
            public static final ResourceLocation IMAGE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "image");
        }

        public static class Condition {

            public static final ResourceLocation NONE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "none");
            public static final ResourceLocation ADVANCEMENT = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "advancement");
            public static final ResourceLocation MOD_LOADED = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "mod_loaded");
            public static final ResourceLocation OR = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "or");
            public static final ResourceLocation AND = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "and");

            public static final ResourceLocation TRUE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "true");

            public static final ResourceLocation FALSE = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "false");

            public static final ResourceLocation ENTRY_UNLOCKED = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "entry_unlocked");

            public static final ResourceLocation ENTRY_READ = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "entry_read");

            public static final ResourceLocation CATEGORY_HAS_ENTRIES = ResourceLocation.fromNamespaceAndPath(ModonomiconAPI.ID, "category_has_entries");
        }
    }

    public static class I18n {
        public static final String BOOK_PREFIX = "book." + ModonomiconAPI.ID + ".";
        public static final String ITEM_GROUP = "itemGroup." + ModonomiconAPI.ID;

        public static class Gui {
            public static final String PREFIX = ModonomiconAPI.ID + ".gui.";
            public static final String BUTTON_NEXT = PREFIX + "button.next_page";
            public static final String BUTTON_PREVIOUS = PREFIX + "button.previous_page";
            public static final String BUTTON_BACK = PREFIX + "button.back";
            public static final String BUTTON_BACK_TOOLTIP = PREFIX + "button.back.tooltip";
            public static final String BUTTON_EXIT = PREFIX + "button.exit";

            public static final String BUTTON_VISUALIZE = PREFIX + "button.visualize";
            public static final String BUTTON_VISUALIZE_TOOLTIP = PREFIX + "button.visualize.tooltip";

            public static final String BUTTON_READ_ALL = PREFIX + "button.read_all";
            public static final String BUTTON_READ_ALL_TOOLTIP_READ_UNLOCKED = PREFIX + "button.read_all.tooltip.read_unlocked";
            public static final String BUTTON_READ_ALL_TOOLTIP_READ_ALL = PREFIX + "button.read_all.tooltip.read_all";
            public static final String BUTTON_READ_ALL_TOOLTIP_NONE = PREFIX + "button.read_all.tooltip.none";
            public static final String BUTTON_READ_ALL_TOOLTIP_SHIFT_INSTRUCTIONS = PREFIX + "button.read_all.tooltip.shift";
            public static final String BUTTON_READ_ALL_TOOLTIP_SHIFT_WARNING = PREFIX + "button.read_all.tooltip.shift_warning";

            public static final String HOVER_BOOK_LINK = PREFIX + "hover.book_link";
            public static final String HOVER_BOOK_LINK_LOCKED = PREFIX + "hover.book_link_locked";
            public static final String HOVER_BOOK_ENTRY_LINK_LOCKED_INFO = PREFIX + "hover.book_entry_link_locked_info";
            public static final String HOVER_BOOK_ENTRY_LINK_LOCKED_INFO_HINT = PREFIX + "hover.book_entry_link_locked_info.hint";
            public static final String HOVER_BOOK_PAGE_LINK_LOCKED_INFO = PREFIX + "hover.book_page_link_locked_info";
            public static final String HOVER_BOOK_PAGE_LINK_LOCKED_INFO_HINT = PREFIX + "hover.book_page_link_locked_info.hint";
            public static final String HOVER_HTTP_LINK = PREFIX + "hover.http_link";
            public static final String HOVER_ITEM_LINK_INFO = PREFIX + "hover.item_link_info";
            public static final String HOVER_ITEM_LINK_INFO_LINE2 = PREFIX + "hover.item_link_info_line2";
            public static final String HOVER_ITEM_LINK_INFO_NO_JEI = PREFIX + "hover.item_link_info.no_jei";
            public static final String HOVER_COMMAND_LINK = PREFIX + "hover.command_link";
            public static final String HOVER_COMMAND_LINK_UNAVAILABLE = PREFIX + "hover.command_link.unavailable";
            public static final String NO_ERRORS_FOUND = PREFIX + "no_errors_found";
            public static final String PAGE_ENTITY_LOADING_ERROR = PREFIX + "page.entity.loading_error";

            public static final String SEARCH_SCREEN_TITLE = PREFIX + "search.screen.title";
            public static final String SEARCH_ENTRY_LOCKED = PREFIX + "search.entry.locked";
            public static final String SEARCH_NO_RESULTS = PREFIX + "search.no_results";
            public static final String SEARCH_NO_RESULTS_SAD = PREFIX + "search.sad";
            public static final String SEARCH_INFO_TEXT = PREFIX + "search.info";
            public static final String SEARCH_ENTRY_LIST_TITLE = PREFIX + "search.entry_list_title";

            public static final String BOOK_INDEX_LIST_TITLE = PREFIX + "book.index_list_title";
            public static final String CATEGORY_INDEX_LIST_TITLE = PREFIX + "category.index_list_title";

            public static final String BOOKMARKS_SCREEN_TITLE = PREFIX + "bookmarks.screen.title";
            public static final String BOOKMARKS_INFO_TEXT = PREFIX + "bookmarks.info";
            public static final String BOOKMARKS_ENTRY_LIST_TITLE = PREFIX + "bookmarks.entry_list_title";
            public static final String BOOKMARKS_NO_RESULTS = PREFIX + "bookmarks.no_results";

            public static final String OPEN_SEARCH = PREFIX + "open_search";
            public static final String OPEN_BOOKMARKS = PREFIX + "open_bookmarks";
            public static final String ADD_BOOKMARK = PREFIX + "add_bookmark";
            public static final String REMOVE_BOOKMARK = PREFIX + "remove_bookmark";

            public static final String RECIPE_PAGE_RECIPE_MISSING = PREFIX + "recipe_page.recipe_missing";
        }

        public static class Multiblock {
            public static final String PREFIX = ModonomiconAPI.ID + ".multiblock.";
            public static final String COMPLETE = PREFIX + "complete";
            public static final String NOT_ANCHORED = PREFIX + "not_anchored";
            public static final String REMOVE_BLOCKS = PREFIX + "remove_blocks";
        }

        public static class Subtitles {
            public static final String PREFIX = ModonomiconAPI.ID + ".subtitle.";
            public static final String TURN_PAGE = PREFIX + "turn_page";
        }

        public static class Tooltips {
            public static final String PREFIX = "tooltip." + ModonomiconAPI.ID + ".";
            public static final String CONDITION_PREFIX = PREFIX + "condition.";
            public static final String CONDITION_ADVANCEMENT = CONDITION_PREFIX + "advancement";
            public static final String CONDITION_ADVANCEMENT_LOADING = CONDITION_ADVANCEMENT + ".loading";
            public static final String CONDITION_ADVANCEMENT_HIDDEN = CONDITION_ADVANCEMENT + ".hidden";
            public static final String CONDITION_MOD_LOADED = CONDITION_PREFIX + "mod_loaded";
            public static final String CONDITION_ENTRY_UNLOCKED = CONDITION_PREFIX + "entry_unlocked";
            public static final String CONDITION_CATEGORY_HAS_ENTRIES = CONDITION_PREFIX + "has_entries";
            public static final String CONDITION_ENTRY_READ = CONDITION_PREFIX + "entry_read";
            public static final String RECIPE_PREFIX = PREFIX + "recipe.";
            public static final String RECIPE_CRAFTING_SHAPELESS = RECIPE_PREFIX + "crafting_shapeless";
            public static final String ITEM_NO_BOOK_FOUND_FOR_STACK = PREFIX + "no_book_found_for_stack";
            public static final String FLUID_AMOUNT = PREFIX + "fluid.amount";
            public static final String FLUID_AMOUNT_AND_CAPACITY = PREFIX + "fluid.amount_and_capacity";

        }

        public static class Command {
            public static final String PREFIX = ModonomiconAPI.ID + ".command.";

            public static final String ERROR_PREFIX = PREFIX + "error.";
            public static final String ERROR_UNKNOWN_BOOK = ERROR_PREFIX + "unknown_book";
            public static final String ERROR_LOAD_PROGRESS = ERROR_PREFIX + "load_progress";
            public static final String ERROR_LOAD_PROGRESS_CLIENT = ERROR_PREFIX + "load_progress_client";
            public static final String SUCCESS_PREFIX = PREFIX + "success.";
            public static final String SUCCESS_RESET_BOOK = SUCCESS_PREFIX + "reset_book";
            public static final String SUCCESS_SAVE_PROGRESS = SUCCESS_PREFIX + "save_progress";
            public static final String SUCCESS_LOAD_PROGRESS = SUCCESS_PREFIX + "load_progress";
            public static final String RELOAD_SUCCESS = SUCCESS_PREFIX + "reload_requested";
            public static final String RELOAD_REQUESTED = PREFIX + "reload_requested";
            public static final String DEFAULT_FAILURE_MESSAGE = PREFIX + "failure";
        }
    }
}

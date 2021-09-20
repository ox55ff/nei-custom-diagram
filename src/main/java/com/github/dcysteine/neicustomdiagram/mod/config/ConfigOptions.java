package com.github.dcysteine.neicustomdiagram.mod.config;

import com.github.dcysteine.neicustomdiagram.api.diagram.DiagramGroupInfo;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public final class ConfigOptions {
    private static final List<Option<?>> allOptions = new ArrayList<>();

    public static final Option<Boolean> CTRL_FAST_FORWARD =
            new BooleanOption(
                    Category.OPTIONS, "ctrl_fast_forward", true,
                    "Enables fast-forwarding through component groups by holding down <Ctrl>."
                            + "\nFast-forward backwards with <Ctrl + Shift>.")
                    .register();

    public static final Option<Boolean> SHOW_STACK_SIZE_ONE =
            new BooleanOption(
                    Category.OPTIONS, "show_stack_size_one", false,
                    "Enables always showing stack size on item components, even if it's 1.")
                    .register();

    public static final Option<Boolean> SHOW_IDS =
            new BooleanOption(
                    Category.OPTIONS, "show_ids", false,
                    "Enables showing ID numbers, such as item ID, item metadata, and fluid ID."
                            + "\nSome diagrams may also show other IDs if this option is enabled.")
                    .register();

    // TODO add config option for smaller resolutions? Will probably need to modify layouts...
    //  Maybe make it affect layouts per page in diagram info as well? Ehhh...
    //  Better than adding a config would be checking GUI height somehow and auto-detecting.

    // Static class.
    private ConfigOptions() {}

    /** This method is only intended to be called during mod initialization. */
    static void setCategoryComments(Configuration config) {
        config.setCategoryComment(
                Category.OPTIONS.toString(),
                "General usage options."
                        + " These should be safe to change without requiring a restart.");

        StringBuilder diagramGroupCategoryCommentBuilder = new StringBuilder();
        diagramGroupCategoryCommentBuilder
                .append("Visibility options for diagram groups."
                        + " These control when diagram groups are shown."
                        + "\nAll options are safe to change without requiring a restart,"
                        + " except for the special DISABLED value."
                        + "\nChanging from DISABLED requires a restart,"
                        + " because it causes diagram groups to not be generated at all."
                        + "\n\nValid values:");
        Arrays.stream(DiagramGroupVisibility.values()).forEach(
                visibility -> diagramGroupCategoryCommentBuilder
                        .append("\n * ").append(visibility.toString()));
        config.setCategoryComment(
                Category.DIAGRAM_GROUPS.toString(), diagramGroupCategoryCommentBuilder.toString());
    }

    public static ImmutableList<Option<?>> getAllOptions() {
        return ImmutableList.copyOf(allOptions);
    }

    public static DiagramGroupVisibility getDiagramGroupVisibility(DiagramGroupInfo info) {
        String visibilityName =
                Config.getConfig().get(
                                Category.DIAGRAM_GROUPS.toString(), info.groupId(),
                                info.defaultVisibility().toString(),
                                buildDiagramGroupVisibilityComment(info))
                        .getString();

        return DiagramGroupVisibility.getByName(visibilityName);
    }

    private static String buildDefaultComment(Object defaultValue) {
        return String.format("\nDefault: %s", defaultValue);
    }

    private static String buildDiagramGroupVisibilityComment(DiagramGroupInfo info) {
        return String.format(
                "Sets the visibility of the %s diagram group.\nDefault: %s",
                info.groupName(), info.defaultVisibility());
    }

    public enum Category {
        OPTIONS("options"),
        DIAGRAM_GROUPS("diagram_groups");

        private final String name;

        Category(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public abstract static class Option<T> implements Supplier<T> {
        final Category category;
        final String key;
        final T defaultValue;
        final String comment;
        final boolean requiresRestart;

        Property property;

        Option(
                Category category, String key, T defaultValue, String comment,
                boolean requiresRestart) {
            this.category = category;
            this.key = key;
            this.defaultValue = defaultValue;
            this.comment = comment + buildDefaultComment(defaultValue);
            this.requiresRestart = requiresRestart;
        }

        Option(Category category, String key, T defaultValue, String comment) {
            this(category, key, defaultValue, comment, false);
        }

        /** Chain this method right after construction. */
        Option<T> register() {
            allOptions.add(this);
            return this;
        }

        public void initialize(Configuration config) {
            property = getProperty(config);
            property.setRequiresMcRestart(requiresRestart);

            // Load this option, so that it gets saved if it's missing from the config.
            get();
        }

        /**
         * Sadly, this abstract method is needed because we cannot in-line getting the property in
         * {@link #initialize(Configuration)} due to type shenanigans.
         */
        abstract Property getProperty(Configuration config);

        @Override
        public abstract T get();
    }

    public static final class BooleanOption extends Option<Boolean> {
        private BooleanOption(Category category, String key, boolean defaultValue, String comment) {
            super(category, key, defaultValue, comment);
        }

        private BooleanOption(
                Category category, String key, boolean defaultValue, String comment,
                boolean requiresRestart) {
            super(category, key, defaultValue, comment, requiresRestart);
        }

        @Override
        Property getProperty(Configuration config) {
            return Config.getConfig().get(category.toString(), key, defaultValue, comment);
        }

        @Override
        public Boolean get() {
            return property.getBoolean();
        }
    }
}
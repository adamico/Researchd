package com.portingdeadmods.researchd.content.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.portingdeadmods.portingdeadlibs.utils.Result;
import com.portingdeadmods.researchd.Researchd;
import com.portingdeadmods.researchd.resources.example.ExampleDatapackWriter;
import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;

public class ExampleCommands {
    public static final ExampleDatapackWriter EXAMPLE_DATAPACK_WRITER = new ExampleDatapackWriter(true);

    public static LiteralCommandNode<CommandSourceStack> build() {
        // TODO(26.1 port, KubeJS): bring back the "kubejs" subcommand (KubeJSExample.createExample()) once KubeJS 8 is
        // out
        // of beta
        return Commands.literal("example")
                .then(Commands.literal("datapack")
                        .executes(ExampleCommands::createDatapackExample)
                        // Optional "pack-name" param
                        .then(Commands.argument("pack-name", StringArgumentType.string())
                                .executes(ExampleCommands::createDatapackExample)
                                // Optional "pack-desc" param
                                .then(Commands.argument("pack-desc", StringArgumentType.string())
                                        .executes(ExampleCommands::createDatapackExample))))
                .build();
    }

    private static int createDatapackExample(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String name = getArgOrDefault(ctx, "pack-name", String.class, "researchd_examples_pack");
        String description = getArgOrDefault(ctx, "pack-desc", String.class, "Auto-created researchd example pack");
        Result<Path, Exception> result = EXAMPLE_DATAPACK_WRITER.write(
                ctx.getSource().getServer().getWorldPath(LevelResource.DATAPACK_DIR), name, description, "rd_examples");
        if (result instanceof Result.Ok(Path value)) {
            String filePath = value.toString();
            String gameDirPath = FMLPaths.GAMEDIR.get().toString();
            Researchd.LOGGER.debug("Game dir: {}, datapack: {}", gameDirPath, filePath);
            StringBuilder shortPath = new StringBuilder(filePath);
            shortPath.insert(0, "..");
            source.sendSuccess(
                    () -> Component.literal("Successfully created example datapack at ")
                            .append(Component.literal(shortPath.toString())
                                    .withStyle(Style.EMPTY
                                            .withColor(ChatFormatting.GOLD)
                                            .withUnderlined(true)
                                            .withClickEvent(new ClickEvent.OpenFile(filePath))
                                            .withHoverEvent(
                                                    new HoverEvent.ShowText(Component.literal("Open directory")))))
                            .append(Component.literal(
                                            " - (run '/datapack enable \"file/%s\"' to enable pack".formatted(name))
                                    .withStyle(ChatFormatting.GRAY)),
                    true);
            // Detects the datapack
            source.getServer().getPackRepository().reload();
            return 1;
        } else {
            Exception error = result.error();
            source.sendFailure(Component.literal("Failed to create example datapack: ")
                    .append(error.getMessage())
                    .withStyle(ChatFormatting.RED));
        }
        return 0;
    }

    private static <T> T getArgOrDefault(
            CommandContext<CommandSourceStack> ctx, String name, Class<T> clazz, T defaultValue) {
        T value;
        try {
            value = ctx.getArgument(name, clazz);
        } catch (Exception e) {
            value = defaultValue;
        }
        return value;
    }
}

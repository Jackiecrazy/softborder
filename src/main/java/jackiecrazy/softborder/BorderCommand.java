package jackiecrazy.softborder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import jackiecrazy.softborder.capability.BorderData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class BorderCommand {
    public static final SimpleCommandExceptionType MISSING_ARGUMENT = new SimpleCommandExceptionType(new TranslationTextComponent("commands.missingargument"));
    private static int lambda = 0;

    public static int missingArgument(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        throw MISSING_ARGUMENT.create();
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("softborder")
                .requires(s -> s.hasPermissionLevel(2))
                .executes(BorderCommand::missingArgument)
//                .then(Commands.literal("setcenter")
//                        .then(Commands.argument("x", IntegerArgumentType.integer())
//                                .then(Commands.argument("y", IntegerArgumentType.integer())
//                                        .then(Commands.argument("z", IntegerArgumentType.integer())
//                                                .executes()
//                                        ))))
                .then(Commands.literal("set")
                        .executes(BorderCommand::missingArgument)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(BorderCommand::missingArgument)
                                .then(Commands.argument("radius", IntegerArgumentType.integer(0))
                                        .executes(BorderCommand::setBorderDefault)
                                        .then(Commands.literal("all")
                                                .executes(BorderCommand::setBorderAll))
                                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                                .executes(BorderCommand::setBorder)))))
                .then(Commands.literal("add")
                        .executes(BorderCommand::missingArgument)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(BorderCommand::missingArgument)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(BorderCommand::addBorderDefault)
                                        .then(Commands.literal("all")
                                                .executes(BorderCommand::addBorderAll))
                                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                                .executes(BorderCommand::addBorder)))))
                .then(Commands.literal("subtract")
                        .executes(BorderCommand::missingArgument)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(BorderCommand::missingArgument)
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(BorderCommand::subBorderDefault)
                                        .then(Commands.literal("all")
                                                .executes(BorderCommand::subBorderAll))
                                        .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                                .executes(BorderCommand::subBorder)))))
                .then(Commands.literal("get")
                        .executes(BorderCommand::missingArgument)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(BorderCommand::getBorderDefault)
                                .then(Commands.literal("all")
                                        .executes(BorderCommand::getBorderAll))
                                .then(Commands.argument("dimension", DimensionArgument.getDimension())
                                        .executes(BorderCommand::getBorder))));
        dispatcher.register(builder);
    }

    public static int setCenter(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        World w = DimensionArgument.getDimensionArgument(ctx, "dimension");
        int i = IntegerArgumentType.getInteger(ctx, "radius");
        BorderData.getCap(player).ifPresent((a) -> a.setBorderFor(w, i));
        ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.setSuccess", player.getDisplayName(), w.getDimensionKey().getLocation(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int setBorder(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        World w = DimensionArgument.getDimensionArgument(ctx, "dimension");
        int i = IntegerArgumentType.getInteger(ctx, "radius");
        BorderData.getCap(player).ifPresent((a) -> a.setBorderFor(w, i));
        ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.setSuccess", player.getDisplayName(), w.getDimensionKey().getLocation(), i), false);
        return Command.SINGLE_SUCCESS;
    }

    public static int addBorder(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        World w = DimensionArgument.getDimensionArgument(ctx, "dimension");
        int i = IntegerArgumentType.getInteger(ctx, "amount");
        BorderData.getCap(player).ifPresent((a) -> {
            a.setBorderFor(w, a.getBorderFor(w) + i);
            ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.addSuccess", player.getDisplayName(), w.getDimensionKey().getLocation(), i, a.getBorderFor(w)), false);
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int subBorder(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        World w = DimensionArgument.getDimensionArgument(ctx, "dimension");
        int i = IntegerArgumentType.getInteger(ctx, "amount");
        BorderData.getCap(player).ifPresent((a) -> {
            a.setBorderFor(w, a.getBorderFor(w) - i);
            ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.subSuccess", player.getDisplayName(), w.getDimensionKey().getLocation(), i, a.getBorderFor(w)), false);
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int getBorder(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        World w = DimensionArgument.getDimensionArgument(ctx, "dimension");
        BorderData.getCap(player).ifPresent((a) -> lambda = a.getBorderFor(w));
        ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.getSuccess", player.getDisplayName(), w.getDimensionKey().getLocation(), lambda), false);
        return lambda;
    }

    public static int setBorderAll(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        int i = IntegerArgumentType.getInteger(ctx, "radius");
        BorderData.getCap(player).ifPresent((f) -> {
            for (RegistryKey<World> a : ctx.getSource().getServer().func_240770_D_())
                f.setBorderFor(ctx.getSource().getServer().getWorld(a), i);
            ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.setAllSuccess", player.getDisplayName(), i), false);
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int addBorderAll(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        int i = IntegerArgumentType.getInteger(ctx, "amount");
        BorderData.getCap(player).ifPresent((f) -> {
            for (RegistryKey<World> a : ctx.getSource().getServer().func_240770_D_())
                f.setBorderFor(ctx.getSource().getServer().getWorld(a), f.getBorderFor(ctx.getSource().getServer().getWorld(a)) + i);
            ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.addAllSuccess", player.getDisplayName(), i), false);
        });

        return Command.SINGLE_SUCCESS;
    }

    public static int subBorderAll(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        int i = IntegerArgumentType.getInteger(ctx, "amount");
        BorderData.getCap(player).ifPresent((f) -> {
            for (RegistryKey<World> a : ctx.getSource().getServer().func_240770_D_())
                f.setBorderFor(ctx.getSource().getServer().getWorld(a), f.getBorderFor(ctx.getSource().getServer().getWorld(a)) - i);
            ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.subAllSuccess", player.getDisplayName(), i), false);
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int getBorderAll(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        BorderData.getCap(player).ifPresent((f) -> {
            for (RegistryKey<World> a : ctx.getSource().getServer().func_240770_D_()) {
                int ret = f.getBorderFor(ctx.getSource().getServer().getWorld(a));
                ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.getAllSuccess", player.getDisplayName(), a.getLocation(), ret), false);
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int setBorderDefault(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        World w = player.world;
        int i = IntegerArgumentType.getInteger(ctx, "radius");
        BorderData.getCap(player).ifPresent((f) -> {
            f.setBorderFor(w, i);
            ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.setSuccess", player.getDisplayName(), w.getDimensionKey().getLocation(), i), false);
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int addBorderDefault(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        World w = player.world;
        int i = IntegerArgumentType.getInteger(ctx, "amount");
        BorderData.getCap(player).ifPresent((f) -> {
            f.setBorderFor(w, f.getBorderFor(w) + i);
            ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.addSuccess", player.getDisplayName(), w.getDimensionKey().getLocation(), i, f.getBorderFor(w)), false);
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int subBorderDefault(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        World w = player.world;
        int i = IntegerArgumentType.getInteger(ctx, "amount");
        BorderData.getCap(player).ifPresent((f) -> {
            f.setBorderFor(w, f.getBorderFor(w) - i);
            ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.subSuccess", player.getDisplayName(), w.getDimensionKey().getLocation(), i, f.getBorderFor(w)), false);
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int getBorderDefault(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgument.getPlayer(ctx, "player");
        World w = player.world;
        BorderData.getCap(player).ifPresent((f) -> {
            lambda = f.getBorderFor(w);
            ctx.getSource().sendFeedback(new TranslationTextComponent("softborders.getSuccess", player.getDisplayName(), w.getDimensionKey().getLocation(), lambda), false);
        });
        return lambda;
    }
}

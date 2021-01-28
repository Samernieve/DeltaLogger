package com.github.fabricservertools.deltalogger.command;

import com.github.fabricservertools.deltalogger.Chat;
import com.github.fabricservertools.deltalogger.SQLUtils;
import com.github.fabricservertools.deltalogger.beans.Placement;
import com.github.fabricservertools.deltalogger.dao.DAO;
import com.github.fabricservertools.deltalogger.util.TimeParser;
import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.block.Blocks;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class RollbackCommand {
    public static void register(LiteralCommandNode<ServerCommandSource> root) {
        LiteralCommandNode<ServerCommandSource> rollbackNode = literal("rollback").then(
                argument("radius", IntegerArgumentType.integer()).then(argument("time", StringArgumentType.string())
                        .executes(ctx -> execute(ctx.getSource(), "", ctx.getSource().getPlayer(),
                                IntegerArgumentType.getInteger(ctx, "radius"),
                                StringArgumentType.getString(ctx, "time")))
                        .then(argument("criteria", StringArgumentType.greedyString())
                                .suggests(RollbackParser.getInstance())
                                .executes(ctx -> execute(ctx.getSource(), StringArgumentType.getString(ctx, "criteria"),
                                        ctx.getSource().getPlayer(), IntegerArgumentType.getInteger(ctx, "radius"),
                                        StringArgumentType.getString(ctx, "time"))))))
                .build();
        root.addChild(rollbackNode);
    }

    private static int execute(ServerCommandSource source, String criteria, ServerPlayerEntity sourcePlayer, int radius,
            String timeString) {
        RollbackCommand.startRollback(source, criteria, sourcePlayer, radius, timeString);
        return 1;
    }

    private static void startRollback(ServerCommandSource source, String criteria, ServerPlayerEntity sourcePlayer,
            int radius, String timeString) {
        Duration duration = TimeParser.parseTime(timeString);
        String timeValue = SQLUtils.instantToUTCString(Instant.now().minus(duration.getSeconds(), ChronoUnit.SECONDS));
        BlockPos playerPos = sourcePlayer.getBlockPos();
        String parsedCriteria = RollbackParser.criteria(criteria, sourcePlayer, source);
        int x1 = playerPos.getX() + radius;
        int y1 = playerPos.getY() + radius;
        int z1 = playerPos.getZ() + radius;
        int x2 = playerPos.getX() - radius;
        int y2 = playerPos.getY() - radius;
        int z2 = playerPos.getZ() - radius;
        int[] boxBounds = { x1, y1, z1, x2, y2, z2 };
        BlockBox box = new BlockBox(boxBounds);
        BlockPos.stream(box).forEach(pos -> {
            try {
                RollbackCommand.addBlocks(source, parsedCriteria, pos, timeValue);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    public static void addBlocks(ServerCommandSource source, String criteria, BlockPos pos, String time)
            throws CommandSyntaxException {
        // Rollback blocks
        World world = source.getWorld();
        Identifier dimension = world.getRegistryKey().getValue();
        System.out.println("hallo");
        
        
        
    }
}
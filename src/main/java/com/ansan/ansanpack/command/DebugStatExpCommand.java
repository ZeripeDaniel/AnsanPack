package com.ansan.ansanpack.command;

import com.ansan.ansanpack.client.level.LocalPlayerLevelData;
import com.ansan.ansanpack.client.level.LocalPlayerStatData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * 테스트용 커맨드: /스탯추가, /경험치추가
 */
public class DebugStatExpCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /스탯추가 <str|agi|int|luck> <수치>
        dispatcher.register(
                Commands.literal("스탯추가")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            String type = StringArgumentType.getString(ctx, "type");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                            for (int i = 0; i < amount; i++) {
                                                LocalPlayerStatData.INSTANCE.gainPoint(type);
                                            }

                                            ctx.getSource().sendSystemMessage(Component.literal("스탯 " + type + " +" + amount));
                                            return 1;
                                        })))
        );

        // /경험치추가 <수치>
        dispatcher.register(
                Commands.literal("경험치추가")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> {
                                    double amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    LocalPlayerLevelData.INSTANCE.addExp(amount);

                                    ctx.getSource().sendSystemMessage(Component.literal("경험치 +" + amount));
                                    return 1;
                                }))
        );
    }
}

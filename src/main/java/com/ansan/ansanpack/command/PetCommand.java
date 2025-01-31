package com.ansan.ansanpack.command;

import com.ansan.ansanpack.PetManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PetCommand {

    private static final PetManager petManager = new PetManager();

    // "펫 넣기" 명령어 처리
    private static int executePetStore(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        int id = IntegerArgumentType.getInteger(context, "id");

        Entity targetEntity = getTargetedEntity(player);
        if (targetEntity == null) {
            source.sendFailure(Component.literal("바라보고 있는 펫이 없습니다."));
            return Command.SINGLE_SUCCESS;
        }

        if (!isOwner(player, targetEntity)) {
            source.sendFailure(Component.literal("이 펫의 주인이 아닙니다."));
            return Command.SINGLE_SUCCESS;
        }

        petManager.storePet(player, id, targetEntity);
        source.sendSuccess(() -> Component.literal("펫이 ID " + id + "로 저장되었습니다."), true);
        return Command.SINGLE_SUCCESS;
    }

    // "펫 꺼내기" 명령어 처리
    private static int executePetRetrieve(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        int id = IntegerArgumentType.getInteger(context, "id");

        petManager.retrievePet(player, id);
        return Command.SINGLE_SUCCESS;
    }

    // 플레이어가 바라보고 있는 엔티티 찾기
    private static Entity getTargetedEntity(ServerPlayer player) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookDirection = player.getViewVector(1.0F);
        Vec3 targetPosition = eyePosition.add(lookDirection.scale(5.0)); // 5 블록 거리 내의 엔티티를 찾음

        AABB searchBox = player.getBoundingBox().expandTowards(lookDirection.scale(5.0)).inflate(1.0D);
        for (Entity entity : player.level().getEntities(player, searchBox)) {
            if (entity instanceof TamableAnimal || entity instanceof AbstractHorse) {
                if (entity.getBoundingBox().intersects(searchBox)) {
                    return entity; // 플레이어가 바라보고 있는 엔티티 반환
                }
            }
        }
        return null; // 찾지 못한 경우 null 반환
    }

    // 엔티티의 소유자 확인
    private static boolean isOwner(ServerPlayer player, Entity pet) {
        if (pet instanceof TamableAnimal) {
            TamableAnimal tameable = (TamableAnimal) pet;
            return tameable.getOwnerUUID() != null && tameable.getOwnerUUID().equals(player.getUUID());
        } else if (pet instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse) pet;
            return horse.getOwnerUUID() != null && horse.getOwnerUUID().equals(player.getUUID());
        }
        return false;
    }

    // 명령어 등록
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("펫")
                .then(Commands.literal("넣기")
                        .then(Commands.argument("id", IntegerArgumentType.integer())
                                .executes(PetCommand::executePetStore)))
                .then(Commands.literal("꺼내기")
                        .then(Commands.argument("id", IntegerArgumentType.integer())
                                .executes(PetCommand::executePetRetrieve))));
    }
}

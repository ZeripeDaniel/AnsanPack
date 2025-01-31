package com.ansan.ansanpack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class PetManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public void storePet(ServerPlayer player, int id, Entity pet) {
        try {
            File petFolder = getPetFolder(player);
            File petFile = new File(petFolder, "pet_" + id + ".json");

            if (petFile.exists()) {
                player.sendSystemMessage(Component.literal("ID " + id + "는 이미 사용 중입니다. 다른 ID를 선택해주세요."));
                return;
            }

            EntityData data = new EntityData(pet);
            savePetData(player, id, data);
            pet.remove(Entity.RemovalReason.DISCARDED);
            player.sendSystemMessage(Component.literal("펫이 ID " + id + "로 저장되었습니다."));
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal("펫 저장 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    public void retrievePet(ServerPlayer player, int id) {
        try {
            EntityData data = loadPetData(player, id);
            if (data != null) {
                Entity pet = data.createEntity(player.serverLevel());
                if (pet != null) {
                    pet.setPos(player.getX(), player.getY(), player.getZ());
                    player.serverLevel().addFreshEntity(pet);
                    deletePetData(player, id);
                    player.sendSystemMessage(Component.literal("펫이 성공적으로 소환되었습니다."));
                } else {
                    player.sendSystemMessage(Component.literal("펫 소환에 실패했습니다."));
                }
            } else {
                player.sendSystemMessage(Component.literal("ID " + id + "에 해당하는 펫이 없습니다."));
            }
        } catch (IOException e) {
            player.sendSystemMessage(Component.literal("펫 불러오기 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    private void savePetData(ServerPlayer player, int id, EntityData data) throws IOException {
        File petFolder = getPetFolder(player);
        File petFile = new File(petFolder, "pet_" + id + ".json");

        if (petFile.exists()) {
            throw new IOException("ID " + id + "는 이미 사용 중입니다.");
        }

        try (FileWriter writer = new FileWriter(petFile)) {
            GSON.toJson(data, writer);
        }
    }

    private EntityData loadPetData(ServerPlayer player, int id) throws IOException {
        File petFolder = getPetFolder(player);
        File petFile = new File(petFolder, "pet_" + id + ".json");
        if (petFile.exists()) {
            try (FileReader reader = new FileReader(petFile)) {
                return GSON.fromJson(reader, EntityData.class);
            }
        }
        return null;
    }

    private void deletePetData(ServerPlayer player, int id) {
        File petFolder = getPetFolder(player);
        File petFile = new File(petFolder, "pet_" + id + ".json");
        petFile.delete();
    }

    private File getPetFolder(ServerPlayer player) {
        Path worldPath = player.server.getWorldPath(LevelResource.ROOT);
        File petFolder = new File(worldPath.toFile(), "data/pets/" + player.getUUID());
        petFolder.mkdirs();
        return petFolder;
    }

    private static class EntityData {
        private String entityType;
        private byte[] nbtData;

        public EntityData(Entity entity) throws IOException {
            this.entityType = EntityType.getKey(entity.getType()).toString();
            CompoundTag nbt = new CompoundTag();
            entity.save(nbt);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(nbt, baos);
            this.nbtData = baos.toByteArray();
        }

        public Entity createEntity(ServerLevel level) throws IOException {
            EntityType<?> type = EntityType.byString(entityType).orElse(null);
            if (type != null) {
                Entity entity = type.create(level);
                if (entity != null) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(nbtData);
                    CompoundTag nbt = NbtIo.readCompressed(bais);
                    entity.load(nbt);
                    return entity;
                }
            }
            return null;
        }
    }
}

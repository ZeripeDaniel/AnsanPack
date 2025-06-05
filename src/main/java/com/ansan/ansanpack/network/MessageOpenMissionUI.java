package com.ansan.ansanpack.network;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.gui.MissionScreen;
import com.ansan.ansanpack.mission.PlayerMissionData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MessageOpenMissionUI {

    public static class MissionInfo {
        public final String missionId;
        public final String description;
        public final int progress;
        public final int goalValue; // ✅ 추가
        public final boolean completed;
        public final boolean rewarded;
        public final String type;

        public MissionInfo(String missionId, String description, int progress, int goalValue, boolean completed, boolean rewarded, String type) {
            this.missionId = missionId;
            this.description = description;
            this.progress = progress;
            this.goalValue = goalValue; // ✅
            this.completed = completed;
            this.rewarded = rewarded;
            this.type = type;
        }
    }

    public final List<MissionInfo> missions;
    public final boolean canReset;

    public MessageOpenMissionUI(List<MissionInfo> missions, boolean canReset) {
        this.missions = missions;
        this.canReset = canReset;
    }

    public static void encode(MessageOpenMissionUI msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.missions.size());
        for (MissionInfo m : msg.missions) {
            buf.writeUtf(m.missionId);
            buf.writeUtf(m.description);
            buf.writeInt(m.progress);
            buf.writeInt(m.goalValue); // ✅ 추가
            buf.writeBoolean(m.completed);
            buf.writeBoolean(m.rewarded);
            buf.writeUtf(m.type);
        }
        buf.writeBoolean(msg.canReset);
    }

    public static MessageOpenMissionUI decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<MissionInfo> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String missionId = buf.readUtf();
            String description = buf.readUtf();
            int progress = buf.readInt();
            int goalValue = buf.readInt(); // ✅ 추가
            boolean completed = buf.readBoolean();
            boolean rewarded = buf.readBoolean();
            String type = buf.readUtf();
            list.add(new MissionInfo(missionId, description, progress, goalValue, completed, rewarded, type));
        }
        boolean canReset = buf.readBoolean();
        return new MessageOpenMissionUI(list, canReset);
    }

    public static void handle(MessageOpenMissionUI msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                MissionScreen.openFromInfo(msg.missions, msg.canReset); // 그대로
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sendToClient(ServerPlayer player, List<PlayerMissionData> missions, boolean canReset) {
        List<MissionInfo> infoList = missions.stream().map(m -> new MissionInfo(
                m.missionId,
                m.description,
                m.progress,
                m.goalValue, // ✅ goalValue 전달
                m.completed,
                m.rewarded,
                m.type
        )).toList();

        MessageOpenMissionUI packet = new MessageOpenMissionUI(infoList, canReset);
        AnsanPack.NETWORK.sendTo(
                packet,
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }
}

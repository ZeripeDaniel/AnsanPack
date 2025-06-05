package com.ansan.ansanpack.network;

import com.ansan.ansanpack.gui.MissionScreen;
import com.ansan.ansanpack.mission.PlayerMissionData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MessageOpenMissionUI {
    public static class MissionInfo {
        public final String missionId;
        public final String description;
        public final int progress;
        public final boolean completed;
        public final boolean rewarded;
        public final String type;

        public MissionInfo(String missionId, String description, int progress, boolean completed, boolean rewarded, String type) {
            this.missionId = missionId;
            this.description = description;
            this.progress = progress;
            this.completed = completed;
            this.rewarded = rewarded;
            this.type = type;
        }
    }

    public final List<MissionInfo> missions;

    public MessageOpenMissionUI(List<MissionInfo> missions) {
        this.missions = missions;
    }

    public static void encode(MessageOpenMissionUI msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.missions.size());
        for (MissionInfo m : msg.missions) {
            buf.writeUtf(m.missionId);
            buf.writeUtf(m.description);
            buf.writeInt(m.progress);
            buf.writeBoolean(m.completed);
            buf.writeBoolean(m.rewarded);
            buf.writeUtf(m.type);
        }
    }

    public static MessageOpenMissionUI decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<MissionInfo> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String missionId = buf.readUtf();
            String description = buf.readUtf();
            int progress = buf.readInt();
            boolean completed = buf.readBoolean();
            boolean rewarded = buf.readBoolean();
            String type = buf.readUtf();
            list.add(new MissionInfo(missionId, description, progress, completed, rewarded, type));
        }
        return new MessageOpenMissionUI(list);
    }
//    public static void openFromInfo(List<MessageOpenMissionUI.MissionInfo> infoList) {
//        List<PlayerMissionData> missions = infoList.stream().map(info -> {
//            PlayerMissionData data = new PlayerMissionData(
//                    "", // uuid는 클라이언트에서 필요 없음
//                    info.missionId,
//                    info.progress,
//                    info.completed,
//                    info.rewarded,
//                    null
//            );
//            data.type = info.type;
//            data.description = info.description;
//            return data;
//        }).toList();
//
//        Minecraft.getInstance().setScreen(new MissionScreen(missions));
//    }
    public static void handle(MessageOpenMissionUI msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                com.ansan.ansanpack.client.ClientMissionHandler.openFromInfo(msg.missions);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

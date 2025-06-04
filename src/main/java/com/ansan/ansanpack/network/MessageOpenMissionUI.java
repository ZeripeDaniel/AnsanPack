package com.ansan.ansanpack.network;

import com.ansan.ansanpack.gui.MissionScreen;
import com.ansan.ansanpack.mission.PlayerMissionData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class MessageOpenMissionUI {
    public final List<PlayerMissionData> missions;

    public MessageOpenMissionUI(List<PlayerMissionData> missions) {
        this.missions = missions;
    }

    public static void encode(MessageOpenMissionUI msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.missions.size());
        for (PlayerMissionData m : msg.missions) {
            buf.writeUtf(m.missionId);
            buf.writeInt(m.progress);
            buf.writeBoolean(m.completed);
            buf.writeBoolean(m.rewarded);
        }
    }

    public static MessageOpenMissionUI decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<PlayerMissionData> list = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new PlayerMissionData(
                    "", // UUID는 클라이언트에서 쓸 일 없음
                    buf.readUtf(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    null
            ));
        }
        return new MessageOpenMissionUI(list);
    }

    public static void handle(MessageOpenMissionUI msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 클라이언트 측에서 미션 UI 열기
            MissionScreen.open(msg.missions);  // → 너가 만들 MissionScreen 클래스에서 처리
        });
        ctx.get().setPacketHandled(true);
    }
}

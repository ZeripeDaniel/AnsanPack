package com.ansan.ansanpack.mission;

import java.sql.Timestamp;
import java.util.Objects;

public class PlayerMissionData {
    public final String uuid;
    public final String missionId;
    public int progress;
    public boolean completed;
    public boolean rewarded;
    public final Timestamp assignedAt;

    public String type; // ⭐ 클라이언트에서 미션 타입 구분용
    public String description; // 클라이언트에서 미션 타입 구분용
    public int goalValue; // ✅ 목표 수치 (서버에서 UI 전송용)

    public PlayerMissionData(String uuid, String missionId, int progress, boolean completed, boolean rewarded, Timestamp assignedAt) {
        this.uuid = uuid;
        this.missionId = missionId;
        this.progress = progress;
        this.completed = completed;
        this.rewarded = rewarded;
        this.assignedAt = assignedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerMissionData that)) return false;
        return uuid.equals(that.uuid) && missionId.equals(that.missionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, missionId);
    }
}

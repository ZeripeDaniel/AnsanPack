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

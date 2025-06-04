package com.ansan.ansanpack.mission;

public class MissionCondition {
    public final int id;
    public final String missionId;
    public final String key;
    public final String value;
    public final String comparison;

    public MissionCondition(int id, String missionId, String key, String value, String comparison) {
        this.id = id;
        this.missionId = missionId;
        this.key = key;
        this.value = value;
        this.comparison = comparison;
    }
}

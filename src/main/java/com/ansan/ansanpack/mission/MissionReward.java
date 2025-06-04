package com.ansan.ansanpack.mission;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// MissionReward.java - 하나의 보상 구조를 나타내는 DTO
public class MissionReward {
    public final int id;
    public final String itemId; // nullable
    public final int value;
    public final String rewardType; // "item", "money", "exp"
    public final String type;       // "daily", "weekly"

    public MissionReward(int id, String itemId, int value, String rewardType, String type) {
        this.id = id;
        this.itemId = itemId;
        this.value = value;
        this.rewardType = rewardType;
        this.type = type;
    }
}
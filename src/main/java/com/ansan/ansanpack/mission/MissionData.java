// 필요한 import 추가
package com.ansan.ansanpack.mission;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// MissionData.java - 하나의 미션 구조를 나타내는 DTO
public class MissionData {
    public final String id;
    public final String type; // "daily" or "weekly"
    public final String description;
    public final String goalType;
    public final int goalValue;
    public final Integer rewardId; // null = 랜덤 보상
    public final int priority;
    public final String requires;

    public MissionData(String id, String type, String description, String goalType, int goalValue,
                       Integer rewardId, int priority, String requires) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.goalType = goalType;
        this.goalValue = goalValue;
        this.rewardId = rewardId;
        this.priority = priority;
        this.requires = requires;
    }
} 
package com.ansan.ansanpack.config;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.mission.*;
import com.ansan.ansanpack.network.MessageRewardResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class MissionManager {

    private static final Map<String, MissionData> missions = new HashMap<>();
    private static final Map<Integer, MissionReward> rewardMap = new HashMap<>();
    private static final List<MissionReward> dailyRewardPool = new ArrayList<>();
    private static final List<MissionReward> weeklyRewardPool = new ArrayList<>();
    public static final Map<String, MissionData> missionMap = new HashMap<>();

    public static void load() {
        missions.clear();
        rewardMap.clear();
        dailyRewardPool.clear();
        weeklyRewardPool.clear();

        try {
            for (MissionData data : MissionDAO.loadAllMissions()) {
                if (data == null || data.id == null) {
                    AnsanPack.LOGGER.warn("로드된 미션 중 잘못된 항목 발견: {}", data);
                    continue;
                }
                missions.put(data.id, data);
            }

            for (MissionReward r : MissionDAO.loadAllRewards()) {
                if (r == null || r.rewardType == null) {
                    AnsanPack.LOGGER.warn("로드된 보상 중 잘못된 항목 발견: {}", r);
                    continue;
                }

                rewardMap.put(r.id, r);
                if ("daily".equals(r.type)) dailyRewardPool.add(r);
                else if ("weekly".equals(r.type)) weeklyRewardPool.add(r);
                else AnsanPack.LOGGER.warn("알 수 없는 보상 type: {}", r.type);
            }

            AnsanPack.LOGGER.info("미션 {}개, 보상 {}개 로드 완료", missions.size(), rewardMap.size());

        } catch (Exception e) {
            throw new RuntimeException("[AnsanPack] 미션 로딩 중 오류 발생", e);
        }
    }

    public static Collection<MissionData> getAllMissions() {
        return missions.values();
    }

    public static MissionData getMission(String id) {
        return missions.get(id);
    }

    public static MissionReward getReward(int rewardId) {
        return rewardMap.get(rewardId);
    }

    public static MissionReward getRandomReward(String type) {
        List<MissionReward> pool = "daily".equals(type) ? dailyRewardPool : weeklyRewardPool;
        if (pool.isEmpty()) return null;
        return pool.get(new Random().nextInt(pool.size()));
    }

    public static void claimReward(ServerPlayer player, String missionId) {
        UUID uuid = player.getUUID();
        PlayerMissionData data = getPlayerMission(uuid, missionId);
        if (data == null || !data.completed || data.rewarded) return;

        MissionReward reward = getRewardByMissionId(missionId);
        if (reward == null) {
            MissionData mission = getMission(missionId);
            if (mission != null && mission.rewardId != null && mission.rewardId == 0) {
                reward = getRandomReward(mission.type);
                if (reward != null) {
                    AnsanPack.LOGGER.info("[랜덤보상] 미션 ID={} 타입={} → 보상 ID={} ({}) 지급", missionId, mission.type, reward.id, reward.rewardType);
                }
            }
        }
        if (reward != null) {
            giveMissionReward(player, reward);
            AnsanPack.LOGGER.info("[확정보상] 미션 ID={} → 보상 ID={} ({}) 지급", missionId, reward.id, reward.rewardType);
        }

        MissionDAO.markMissionRewarded(uuid.toString(), missionId);
        data.rewarded = true;

        AnsanPack.NETWORK.sendTo(
                new MessageRewardResult(missionId),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }

    public static PlayerMissionData getPlayerMission(UUID uuid, String missionId) {
        List<PlayerMissionData> list = MissionService.getOrAssignMissions(uuid.toString());
        for (PlayerMissionData data : list) {
            if (data.missionId.equals(missionId)) return data;
        }
        return null;
    }

    private static MissionReward getRewardByMissionId(String missionId) {
        MissionData mission = getMission(missionId);
        if (mission == null || mission.rewardId == null) return null;
        return getReward(mission.rewardId);
    }

    private static void giveMissionReward(ServerPlayer player, MissionReward reward) {
        switch (reward.rewardType) {
            case "money" -> {
                Scoreboard scoreboard = player.getScoreboard();
                var objective = scoreboard.getObjective("ansan_money");
                if (objective != null) {
                    Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective);
                    score.setScore(score.getScore() + reward.value);
                }
                player.sendSystemMessage(Component.literal("💰 보상으로 돈 " + reward.value + "원을 받았습니다!"));
            }
            case "exp" -> {
                player.giveExperiencePoints(reward.value);
                player.sendSystemMessage(Component.literal("✨ 보상으로 경험치 " + reward.value + "을 받았습니다!"));
            }
            case "item" -> {
                if (reward.itemId == null) return;
                ResourceLocation id = new ResourceLocation(reward.itemId);
                Item item = ForgeRegistries.ITEMS.getValue(id);
                if (item != null) {
                    ItemStack stack = new ItemStack(item, reward.value);
                    player.getInventory().placeItemBackInInventory(stack);
                    player.sendSystemMessage(Component.literal("🎁 보상으로 " + reward.value + "개의 " + item.getDescription().getString() + "을(를) 받았습니다!"));
                }
            }
        }
    }

    public static List<MissionData> getRandomMissions(String type, int count) {
        List<MissionData> filtered = missionMap.values().stream()
                .filter(m -> type.equals(m.type))
                .collect(Collectors.toList());

        Collections.shuffle(filtered);
        return filtered.stream().limit(count).collect(Collectors.toList());
    }
}

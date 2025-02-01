package com.ansan.ansanpack.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;


public class WeaponUpgradeConfig {
    public static ForgeConfigSpec.DoubleValue BASE_SUCCESS_CHANCE;
    public static ForgeConfigSpec.IntValue MAX_UPGRADE_LEVEL;
    public static ForgeConfigSpec.DoubleValue CHANCE_DECREASE_PER_LEVEL;
    public static ForgeConfigSpec.DoubleValue DAMAGE_INCREASE_PER_LEVEL;

    public static void load() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Upgrade System Settings");
        BASE_SUCCESS_CHANCE = builder
                .comment("Base success chance for upgrades (0.0-1.0)")
                .defineInRange("baseSuccessChance", 0.8, 0.0, 1.0);
        MAX_UPGRADE_LEVEL = builder
                .comment("Maximum upgrade level")
                .defineInRange("maxUpgradeLevel", 10, 1, 100);
        CHANCE_DECREASE_PER_LEVEL = builder
                .comment("Success chance decrease per level (0.0-1.0)")
                .defineInRange("chanceDecreasePerLevel", 0.05, 0.0, 1.0);
        DAMAGE_INCREASE_PER_LEVEL = builder
                .comment("Damage multiplier increase per level")
                .defineInRange("damageIncreasePerLevel", 0.1, 0.0, 5.0);
        builder.pop();

        // 수정 코드 (1.20.1 권장 방식)
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                builder.build(),
                "ansanpack-server.toml"
        );
    }
}

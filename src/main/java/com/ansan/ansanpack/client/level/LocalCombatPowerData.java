package com.ansan.ansanpack.client.level;

public class LocalCombatPowerData {
    public static double combatPower = 0.0;

    public static void update(double value) {
        combatPower = value;
    }

    public static double get() {
        return combatPower;
    }
}

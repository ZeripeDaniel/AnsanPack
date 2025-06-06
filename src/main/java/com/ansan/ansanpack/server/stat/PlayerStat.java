package com.ansan.ansanpack.server.stat;

public class PlayerStat {
    public final int strength;
    public final int agility;
    public final int intelligence;
    public final int luck;
    public final int availableAP;

    public PlayerStat(int strength, int agility, int intelligence, int luck, int availableAP) {
        this.strength = strength;
        this.agility = agility;
        this.intelligence = intelligence;
        this.luck = luck;
        this.availableAP = availableAP;
    }

    public int getStrength() { return strength; }
    public int getAgility() { return agility; }
    public int getIntelligence() { return intelligence; }
    public int getLuck() { return luck; }
    public int getAvailableAP() { return availableAP; }
}

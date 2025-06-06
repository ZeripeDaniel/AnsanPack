package com.ansan.ansanpack.client.level;

import net.minecraft.nbt.CompoundTag;

/**
 * 클라이언트 측 스탯 데이터 (싱글톤)
 */
public class LocalPlayerStatData {

    public static final LocalPlayerStatData INSTANCE = new LocalPlayerStatData();

    private int str = 0;
    private int agi = 0;
    private int intel = 0;
    private int luck = 0;
    private int availableAP = 0;

    private LocalPlayerStatData() {}

    public int getStat(String type) {
        return switch (type) {
            case "str" -> str;
            case "agi" -> agi;
            case "int" -> intel;
            case "luck" -> luck;
            default -> 0;
        };
    }

    public boolean canSpendPoint() {
        return availableAP > 0;
    }

    public void gainPoint(String type) {
        if (!canSpendPoint()) return;

        switch (type) {
            case "str" -> str++;
            case "agi" -> agi++;
            case "int" -> intel++;
            case "luck" -> luck++;
        }
        availableAP--;
    }

    public void addAP(int amount) {
        this.availableAP += amount;
    }

    public int getAvailableAP() {
        return availableAP;
    }

    public void loadFromNBT(CompoundTag tag) {
        this.str = tag.getInt("str");
        this.agi = tag.getInt("agi");
        this.intel = tag.getInt("int");
        this.luck = tag.getInt("luck");
        this.availableAP = tag.getInt("availableAP");
    }

    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("str", str);
        tag.putInt("agi", agi);
        tag.putInt("int", intel);
        tag.putInt("luck", luck);
        tag.putInt("availableAP", availableAP);
        return tag;
    }

    // ✅ SQL 로딩용 메서드
    public void loadFromSQL(int str, int agi, int intel, int luck, int availableAP) {
        this.str = str;
        this.agi = agi;
        this.intel = intel;
        this.luck = luck;
        this.availableAP = availableAP;
    }
}

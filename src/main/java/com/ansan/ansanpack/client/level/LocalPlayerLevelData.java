package com.ansan.ansanpack.client.level;

public class LocalPlayerLevelData {

    public static final LocalPlayerLevelData INSTANCE = new LocalPlayerLevelData(); // ✅ 싱글톤 명시

    private int level = 1;
    private int exp = 0;

    private LocalPlayerLevelData() {} // ✅ 생성자 private

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public void addExp(int amount) {
        gainExp(amount);
    }
    public void gainExp(int amount) {
        this.exp += amount;
        while (this.exp >= getExpToNextLevel()) {
            this.exp -= getExpToNextLevel();
            this.level++;
        }
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    private int getExpToNextLevel() {
        return 100 + (level * 20); // ✅ 간단한 커스텀 공식, 필요시 외부 설정으로 분리 가능
    }

    public void reset() {
        this.level = 1;
        this.exp = 0;
    }
}

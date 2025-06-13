package com.ansan.ansanpack.client.level;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.client.LevelUpEffectRenderer;
import com.ansan.ansanpack.sound.ModSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import com.ansan.ansanpack.network.MessageLevelUpNotify;

public class LocalPlayerLevelData {

    public static final LocalPlayerLevelData INSTANCE = new LocalPlayerLevelData(); // ✅ 싱글톤 명시

    private int level = 1;
    private double  exp = 0;

    private LocalPlayerLevelData() {} // ✅ 생성자 private

    public int getLevel() {
        return level;
    }

    public double  getExp() {
        return exp;
    }

    public void addExp(double amount) {
        gainExp(amount);
    }
    public void gainExp(double amount) {
        this.exp += amount;
        //AnsanPack.LOGGER.warn("[WARN] EXP gained: " + amount+ " → current: " + exp + "/" + getExpToNextLevel());
        if (this.exp >= getExpToNextLevel()) {
            this.exp = 0;
            this.level++;
            AnsanPack.NETWORK.sendToServer(new MessageLevelUpNotify());
            LevelUpEffectRenderer.trigger();
            LocalPlayerStatData.INSTANCE.addAP(1);
            //AnsanPack.LOGGER.warn("레벨업! 레벨: {}", level);
            //AnsanPack.LOGGER.warn("사운드 위치: {}", ModSoundEvents.LEVEL_UP.get().getLocation());
            //AnsanPack.LOGGER.warn("이미지 트리거 호출!");

        }
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public void setExp(double exp) {
        this.exp = Math.max(0, exp); // 음수 방지
        //AnsanPack.LOGGER.warn("[SET] exp 초기화: " + exp);
    }
    public double getExpToNextLevel() {
        return (100 * Math.pow(level, 2.0));  // 난이도: 중간 이상
    }
    public void reset() {
        this.level = 1;
        this.exp = 0;
    }
}

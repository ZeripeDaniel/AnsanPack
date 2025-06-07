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


        while (this.exp >= getExpToNextLevel()) {
            this.exp -= getExpToNextLevel();
            this.level++;
            //AnsanPack.LOGGER.warn("Level Up! → " + level);
            // 사운드 재생
            //AnsanPack.NETWORK.sendToServer(new MessageLevelUpNotify());
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forLocalAmbience(ModSoundEvents.LEVEL_UP.get(), 1.0f, 1.0f)
            );
            LevelUpEffectRenderer.trigger();
            LocalPlayerStatData.INSTANCE.addAP(1);  // 반드시 INSTANCE로 접근
            AnsanPack.LOGGER.warn("레벨업! 레벨: {}", level);
            AnsanPack.LOGGER.warn("사운드 위치: {}", ModSoundEvents.LEVEL_UP.get().getLocation());
            AnsanPack.LOGGER.warn("이미지 트리거 호출!");
        }
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setExp(double exp) {
        this.exp = Math.max(0, exp); // 음수 방지
        AnsanPack.LOGGER.warn("[SET] exp 초기화: " + exp);
    }



    private int getExpToNextLevel() {
        return 100 + (level * 20); // ✅ 간단한 커스텀 공식, 필요시 외부 설정으로 분리 가능
    }

    public double getRemainingExp() {
        return getExpToNextLevel() - exp;
    }

    public float getExpProgress() {
        return (float) exp / getExpToNextLevel();
    }

    public void reset() {
        this.level = 1;
        this.exp = 0;
    }
}

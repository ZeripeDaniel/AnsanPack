package com.ansan.ansanpack.item.magic;

import com.ansan.ansanpack.AnsanPack;
import com.ansan.ansanpack.events.MagicCombatEventHandler;
import com.ansan.ansanpack.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.entity.projectile.ProjectileUtil;



public class MagicProjectileEntity extends Projectile implements ItemSupplier {

    private float magicDamage = 0f;

    public MagicProjectileEntity(EntityType<? extends MagicProjectileEntity> type, Level level) {
        super(type, level);
    }

    public MagicProjectileEntity(Level level, LivingEntity shooter, float damage) {
        this(ModMagicEntities.MAGIC_PROJECTILE.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY(), shooter.getZ());
        this.magicDamage = damage;
    }
    public ItemStack getItem() {
        // 렌더링에 사용할 아이템 → 없어도 되면 air 써도 되고, bullet 아이템 쓰면 추천
        return new ItemStack(ModItems.MAGIC_BULLET_LOW.get());
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        if (!level().isClientSide) {
            //AnsanPack.LOGGER.debug("[DEBUG] onHitEntity called!!");  // 강제 println 넣기

            if (result.getEntity() instanceof LivingEntity target) {
                //AnsanPack.LOGGER.info("[DEBUG] onHitEntity target = " + target.getName().getString());  // 강제 println
                MagicCombatEventHandler.applyMagicDamage(this, target, magicDamage); // 발사체가 source 역할

                this.discard();
            }
        }
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.BLOCK) {
            this.discard(); // 벽에 맞으면 제거
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 수명 제한
        if (this.tickCount > 60) {
            this.discard();
            return;
        }

        // 파티클
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0.01);
        }

        // 이동 처리 전 → 충돌 검사
        Vec3 startPos = this.position();
        Vec3 endPos = startPos.add(this.getDeltaMovement());

        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
                this.level(),
                this,
                startPos,
                endPos,
                this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D),
                this::canHitEntity
        );

        if (entityHitResult != null) {
            this.onHitEntity(entityHitResult);
        }



        // 이동 처리
        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);

        // 중력 없음
        this.setDeltaMovement(motion);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.magicDamage = tag.getFloat("MagicDamage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("MagicDamage", magicDamage);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void shootFromRotation(@NotNull Entity shooter, float pitch, float yaw, float roll, float speed, float inaccuracy) {
        float x = -Mth.sin(yaw * ((float) Math.PI / 180F)) * Mth.cos(pitch * ((float) Math.PI / 180F));
        float y = -Mth.sin((pitch + roll) * ((float) Math.PI / 180F));
        float z = Mth.cos(yaw * ((float) Math.PI / 180F)) * Mth.cos(pitch * ((float) Math.PI / 180F));
        this.shoot(x, y, z, speed, inaccuracy);
    }

    public void setMagicDamage(float dmg) {
        this.magicDamage = dmg;
    }

    public float getMagicDamage() {
        return this.magicDamage;
    }
}

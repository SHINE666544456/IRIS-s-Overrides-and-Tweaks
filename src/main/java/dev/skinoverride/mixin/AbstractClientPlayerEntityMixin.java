package dev.skinoverride.mixin;

import dev.skinoverride.SkinStore;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The renderer asks the entity for its skin every frame; we swap it for the assigned one. */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void skinoverride$replaceSkin(CallbackInfoReturnable<SkinTextures> cir) {
        AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;
        SkinTextures custom = SkinStore.override(self.getUuid(), cir.getReturnValue());
        if (custom != null) {
            cir.setReturnValue(custom);
        }
    }
}

package dev.skinoverride.mixin;

import dev.skinoverride.Zoom;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    /** While the zoom key is held, the scroll wheel changes the zoom instead of the hotbar slot. */
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void skinoverride$zoomScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (Zoom.onScroll(vertical)) {
            ci.cancel();
        }
    }
}

package com.wishtoday.ts.simpleminer.mixin;

import com.wishtoday.simpleservices.services.annotation.ServiceClass;
import com.wishtoday.simpleservices.services.annotation.ServiceField;
import com.wishtoday.ts.simpleminer.client.ShapeScrollHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ServiceClass
@Mixin(Mouse.class)
public class MouseMixin {
    @ServiceField
    private ShapeScrollHandler shapeSwitcher;
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onScrollHead(long handle, double xOffset, double yOffset, CallbackInfo ci) {
        if (!ci.isCancelled()) {
            MinecraftClient client = MinecraftClient.getInstance();
            boolean discrete = client.options.getDiscreteMouseScroll().getValue();
            double sensitivity = client.options.getMouseWheelSensitivity().getValue();
            double amountX = (discrete ? Math.signum(xOffset) : xOffset) * sensitivity;
            double amountY = (discrete ? Math.signum(yOffset) : yOffset) * sensitivity;

            if (shapeSwitcher.onMouseScrolled(amountX, amountY)) {
                ci.cancel();
            }
        }
    }
}

package su.plo.voice.flashback.mixin;

import com.moulberry.flashback.exporting.ExportJob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.flashback.event.FlashbackEvents;

@Mixin(ExportJob.class)
public class MixinExportJob {
    @Inject(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/client/sounds/SoundManager;stop()V",
                    ordinal = 0
            )
    )
    public void onRunStart(CallbackInfo ci) {
        FlashbackEvents.EXPORT_START.invoker().onStart();
    }

    @Inject(
            method = "run",
            remap = false,
            at = @At(value = "RETURN")
    )
    public void onRunEnd(CallbackInfo ci) {
        FlashbackEvents.EXPORT_END.invoker().onEnd();
    }
}

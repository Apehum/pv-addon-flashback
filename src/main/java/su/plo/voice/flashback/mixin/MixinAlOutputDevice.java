package su.plo.voice.flashback.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.flashback.Flashback;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import su.plo.voice.client.audio.device.AlOutputDevice;

import java.nio.IntBuffer;

@Mixin(value = AlOutputDevice.class, remap = false)
public final class MixinAlOutputDevice {

    @WrapOperation(method = "openSync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC11;alcCreateContext(JLjava/nio/IntBuffer;)J"))
    private long alcCreateContext(long devicePointer, IntBuffer attrList, Operation<Long> original) {
        if (Flashback.isExporting() && Flashback.EXPORT_JOB.getSettings().recordAudio()) {
            return Minecraft.getInstance().getSoundManager().soundEngine.library.context;
        }

        return original.call(devicePointer, attrList);
    }

    @WrapOperation(method = "closeSync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/ALC11;alcDestroyContext(J)V"))
    private void alcDestroyContext(long contextPointer, Operation<Void> original) {
        if (Minecraft.getInstance().getSoundManager().soundEngine.library.context == contextPointer) return;
        original.call(contextPointer);
    }
}

package city.windmill.ingameime.client.mixin;

import city.windmill.ingameime.client.ScreenEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import kotlin.Pair;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({BookEditScreen.class, SignEditScreen.class})
public class MixinEditScreen {
    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(CallbackInfo info) {
        ScreenEvents.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(0, 0));
    }

    @Inject(method = "removed", at = @At("TAIL"))
    public void onRemove(CallbackInfo info) {
        ScreenEvents.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }
}

@Mixin(BookEditScreen.class)
abstract class MixinBookEditScreen {
    @Shadow
    protected abstract BookEditScreen.Pos2i convertLocalToScreen(BookEditScreen.Pos2i position);

    @Inject(method = "renderCursor",
            at = @At("HEAD"))
    public void onCaret_Book(PoseStack poseStack, BookEditScreen.Pos2i pos2i, boolean bl, CallbackInfo ci) {
        pos2i = convertLocalToScreen(pos2i);
        ScreenEvents.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(pos2i.x, pos2i.y));
    }
}

@Mixin(SignEditScreen.class)
abstract class MixinSignEditScreen extends Screen {
    protected MixinSignEditScreen(Component component) {
        super(component);
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Font;drawInBatch(Ljava/lang/String;FFIZLcom/mojang/math/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;ZIIZ)I",
                    ordinal = 1,
                    shift = At.Shift.BEFORE),
            locals = LocalCapture.PRINT)
    public void onCaret_Sign(CallbackInfo info) {
        //p->x,l->y
        //ScreenEvents.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>((int) matrix4f.a03 + p, (int) matrix4f.a13 + l));
    }
}

package city.windmill.ingameime.fabric.mixin;

import city.windmill.ingameime.client.event.ClientScreenEventHooks;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({Screen.class, AbstractSignEditScreen.class})
class MixinScreen {
    @Inject(method = "removed", at = @At("TAIL"))
    private void onRemove(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CLOSE().invoker().onEditClose(this);
    }
}

@Mixin({BookEditScreen.class, SignEditScreen.class, AbstractSignEditScreen.class})
class MixinEditScreen {
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        ClientScreenEventHooks.INSTANCE.getEDIT_OPEN().invoker().onEditOpen(this, new Pair<>(0, 0));
    }
}

@Mixin(BookEditScreen.class)
abstract class MixinBookEditScreen {
    @Inject(method = "renderCursor",
            at = @At(value = "INVOKE",
                    shift = At.Shift.BY,
                    by = 2,
                    target = "Lnet/minecraft/client/gui/screens/inventory/BookEditScreen;convertLocalToScreen(Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$Pos2i;)Lnet/minecraft/client/gui/screens/inventory/BookEditScreen$Pos2i;")
    )
    private void onCaret_Book(GuiGraphics guiGraphics, BookEditScreen.Pos2i pos2i, boolean bl, CallbackInfo ci) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(pos2i.x, pos2i.y));
    }

    @Inject(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onCaret_Book(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci, int k, int l, boolean bl, FormattedCharSequence formattedCharSequence, int m, int n) {
        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(
                k + 36 + (114 - l) / 2 - Minecraft.getInstance().font.width("_"),
                50
        ));
    }
}

@Mixin(AbstractSignEditScreen.class)
abstract class MixinAbstractSignEditScreen extends Screen {
    @Shadow
    @Final
    protected SignBlockEntity sign;

    @Shadow
    protected abstract Vector3f getSignTextScale();

    @Shadow
    private int line;
    @Shadow
    private @Nullable TextFieldHelper signField;
    @Shadow
    @Final
    private String[] messages;

    private MixinAbstractSignEditScreen(Component component) {
        super(component);
    }

    @Inject(
            method = "renderSignText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
                    ordinal = 1
            )
    )
    private void onCaret_Sign(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (signField == null) return;

        String currentLine = messages[line];
        int cursorPos = signField.getCursorPos();

        assert minecraft != null;
        int textWidth = minecraft.font.width(currentLine);
        int horizontalOffset = minecraft.font.width(currentLine.substring(0, Math.min(cursorPos,
                currentLine.length())))
                - textWidth / 2;

        Vector3f scale = getSignTextScale();
        boolean isHanging = isHangingSign();

        int screenX = (int) (horizontalOffset * scale.x()) + width / 2;
        int screenY = calculateVerticalOffset(scale.y(), isHanging);

        ClientScreenEventHooks.INSTANCE.getEDIT_CARET().invoker().onEditCaret(this, new Pair<>(screenX, screenY));
    }

    @Unique
    private boolean isHangingSign() {
        BlockState state = sign.getBlockState();
        return state.getBlock() instanceof CeilingHangingSignBlock
                || state.getBlock() instanceof WallHangingSignBlock;
    }

    @Unique
    private int calculateVerticalOffset(float scaleY, boolean isHanging) {
        int baseYOffset = isHanging ? 125 : 90;

        int textLineHeight = sign.getTextLineHeight();
        int verticalPos = line * textLineHeight - (4 * textLineHeight / 2);

        return (int) (verticalPos * scaleY) + baseYOffset + (isHanging ? 2 : 0);
    }
}

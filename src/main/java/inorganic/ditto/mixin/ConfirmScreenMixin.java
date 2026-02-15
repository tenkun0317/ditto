package inorganic.ditto.mixin;

import inorganic.ditto.DittoConfig;
import inorganic.ditto.DittoUtil;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfirmScreen.class)
public abstract class ConfirmScreenMixin extends Screen {
    @Shadow @Final protected BooleanConsumer callback;
    @Shadow @Final private Text message;
    
    private CheckboxWidget dittoCheckbox;

    protected ConfirmScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        if (this.client != null && (InputUtil.isKeyPressed(this.client.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || 
                                    InputUtil.isKeyPressed(this.client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT))) {
            return;
        }

        String titleId = DittoUtil.getIdentifier(this.title);
        String messageId = DittoUtil.getIdentifier(this.message);
        Boolean savedChoice = DittoConfig.INSTANCE.getChoice(titleId, messageId);
        
        if (savedChoice != null) {
            this.callback.accept(savedChoice);
            if (this.client != null) {
                this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                if (this.client.currentScreen == this) {
                    this.close();
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInitFinished(CallbackInfo ci) {
        Text text = Text.translatable("ditto.dont_show_again");
        int checkboxWidth = this.textRenderer.getWidth(text) + 24;
        
        int x = (this.width - checkboxWidth) / 2;
        int y = DittoUtil.getCheckboxY(this);

        this.dittoCheckbox = CheckboxWidget.builder(text, this.textRenderer)
            .pos(x, y)
            .build();
        this.addDrawableChild(this.dittoCheckbox);
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/booleans/BooleanConsumer;accept(Z)V"))
    private void onCallbackInvoke(BooleanConsumer instance, boolean b) {
        if (this.dittoCheckbox != null && this.dittoCheckbox.isChecked()) {
            String titleId = DittoUtil.getIdentifier(this.title);
            String messageId = DittoUtil.getIdentifier(this.message);
            DittoConfig.INSTANCE.setChoice(titleId, messageId, b);
        }
        instance.accept(b);
    }
}

package inorganic.ditto.mixin;

import inorganic.ditto.DittoConfig;
import inorganic.ditto.DittoUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.NoticeScreen;
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

@Mixin(NoticeScreen.class)
public abstract class NoticeScreenMixin extends Screen {
    @Shadow @Final private Runnable actionHandler;
    @Shadow @Final private Text notice;
    
    private CheckboxWidget dittoCheckbox;

    protected NoticeScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        if (this.client != null && (InputUtil.isKeyPressed(this.client.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || 
                                    InputUtil.isKeyPressed(this.client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT))) {
            return;
        }

        String titleId = DittoUtil.getIdentifier(this.title);
        String messageId = DittoUtil.getIdentifier(this.notice);
        Boolean savedChoice = DittoConfig.INSTANCE.getChoice(titleId, messageId);
        
        if (savedChoice != null && savedChoice) {
            this.actionHandler.run();
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

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V"))
    private void onActionHandlerInvoke(Runnable instance) {
        if (instance == this.actionHandler && this.dittoCheckbox != null && this.dittoCheckbox.isChecked()) {
            String titleId = DittoUtil.getIdentifier(this.title);
            String messageId = DittoUtil.getIdentifier(this.notice);
            DittoConfig.INSTANCE.setChoice(titleId, messageId, true);
        }
        instance.run();
    }
}

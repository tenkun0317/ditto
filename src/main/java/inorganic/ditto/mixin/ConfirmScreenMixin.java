package inorganic.ditto.mixin;

import inorganic.ditto.DittoConfig;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
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
        String titleStr = this.title.getString();
        String messageStr = this.message.getString();
        Boolean savedChoice = DittoConfig.INSTANCE.getChoice(titleStr, messageStr);
        if (savedChoice != null) {
            this.callback.accept(savedChoice);
            if (this.client != null && this.client.currentScreen == this) {
                this.close();
            }
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInitFinished(CallbackInfo ci) {
        int minButtonY = this.height;
        boolean foundButton = false;

        for (Element element : this.children()) {
            if (element instanceof ClickableWidget widget) {
                if (widget.getY() > this.height / 2) {
                    if (widget.getY() < minButtonY) {
                        minButtonY = widget.getY();
                    }
                    foundButton = true;
                }
            }
        }

        Text text = Text.translatable("ditto.dont_show_again");
        int checkboxWidth = this.textRenderer.getWidth(text) + 24;
        int x = (this.width - checkboxWidth) / 2;
        int y;

        if (foundButton) {
            y = minButtonY - 24;
            if (y < this.height / 2) {
                y = this.height - 30;
            }
        } else {
            y = this.height - 40;
        }

        this.dittoCheckbox = CheckboxWidget.builder(text, this.textRenderer)
            .pos(x, y)
            .build();
        this.addDrawableChild(this.dittoCheckbox);
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/booleans/BooleanConsumer;accept(Z)V"))
    private void onCallbackInvoke(BooleanConsumer instance, boolean b) {
        if (this.dittoCheckbox != null && this.dittoCheckbox.isChecked()) {
            DittoConfig.INSTANCE.setChoice(this.title.getString(), this.message.getString(), b);
        }
        instance.accept(b);
    }
}

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
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInitFinished(CallbackInfo ci) {
        // 画面の下半分（通常ボタンがある場所）にあるウィジェットの中で、最も高い位置にあるものを探す
        int buttonY = -1;
        for (Element element : this.children()) {
            if (element instanceof ClickableWidget widget) {
                // 画面の下半分にあり、かつある程度の幅がある（ボタンらしい）ものを対象にする
                if (widget.getY() > this.height / 2 && widget.getWidth() > 20) {
                    if (buttonY == -1 || widget.getY() < buttonY) {
                        buttonY = widget.getY();
                    }
                }
            }
        }

        Text text = Text.translatable("ditto.dont_show_again");
        int checkboxWidth = this.textRenderer.getWidth(text) + 24;
        int x = (this.width - checkboxWidth) / 2;
        int y;

        if (buttonY != -1) {
            // ボタンが見つかった場合、その 21 ピクセル上（ボタンの直上）に配置
            y = buttonY - 21;
            
            // もしボタンの上がタイトルや文に近すぎる（上すぎる）場合は、ボタンのさらに下に配置する
            if (y < this.height / 2) {
                y = buttonY + 24; // ボタンの下側に配置
            }
        } else {
            // ボタンが見つからない場合のフォールバック（画面下部から 40 ピクセル上）
            y = this.height - 40;
        }

        this.dittoCheckbox = CheckboxWidget.builder(text, this.textRenderer)
            .pos(x, y)
            .build();
        this.addDrawableChild(this.dittoCheckbox);
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/booleans/BooleanConsumer;accept(Z)V"))
    private void onCallback(BooleanConsumer instance, boolean b) {
        if (this.dittoCheckbox != null && this.dittoCheckbox.isChecked()) {
            DittoConfig.INSTANCE.setChoice(this.title.getString(), this.message.getString(), b);
        }
        instance.accept(b);
    }
}

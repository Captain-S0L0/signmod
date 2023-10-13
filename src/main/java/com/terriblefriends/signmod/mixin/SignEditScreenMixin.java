package com.terriblefriends.signmod.mixin;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEventAction;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignEditScreen.class)
public class SignEditScreenMixin extends Screen {
    @Shadow private SignBlockEntity sign;
    @Shadow private int ticksSinceOpened;
    @Shadow private int currentRow;
    @Shadow private ButtonWidget doneButton;

    private final TextFieldWidget[] commands = new TextFieldWidget[4];

    @Inject(at=@At(value="INVOKE",target="Ljava/util/List;clear()V"),method="init")
    private void signmod$addCommandFields(CallbackInfo ci) {
        this.commands[0] = new TextFieldWidget(0, this.textRenderer, this.width / 2 + 50, 44, 150, 20);
        this.commands[1] = new TextFieldWidget(0, this.textRenderer, this.width / 2 + 50, 69, 150, 20);
        this.commands[2] = new TextFieldWidget(0, this.textRenderer, this.width / 2 + 50, 94, 150, 20);
        this.commands[3] = new TextFieldWidget(0, this.textRenderer, this.width / 2 + 50, 119, 150, 20);

        for (TextFieldWidget field : this.commands) {
            field.setMaxLength(32767);
        }
    }

    @Inject(at=@At("HEAD"),method="render")
    private void signmod$renderCommandFields(int mouseY, int tickDelta, float par3, CallbackInfo ci) {
        for (TextFieldWidget field : this.commands) {
            field.render();
        }
    }

    @Inject(at=@At("HEAD"),method="keyPressed",cancellable = true)
    private void signmod$overrideSignModification(char id, int code, CallbackInfo ci) {
        for (TextFieldWidget field : this.commands) {
            if (field.isFocused()) {
                field.keyPressed(id, code);
                this.sign.text[this.currentRow] = this.sign.text[this.currentRow].setStyle(new Style().setClickEvent(new ClickEvent(ClickEventAction.RUN_COMMAND, field.getText())));
                ci.cancel();
                return;
            }
        }

        if (code == 200) {
            this.currentRow = this.currentRow - 1 & 3;
        }

        if (code == 208 || code == 28 || code == 156) {
            this.currentRow = this.currentRow + 1 & 3;
        }

        String var3 = this.sign.text[this.currentRow].asUnformattedString();
        if (code == 14 && var3.length() > 0) {
            var3 = var3.substring(0, var3.length() - 1);
        }

        if (SharedConstants.isValidChar(id) && this.textRenderer.getStringWidth(var3 + id) <= 90) {
            var3 = var3 + id;
        }

        this.sign.text[this.currentRow] = new LiteralText(var3).setStyle(this.sign.text[this.currentRow].getStyle());

        if (code == 1) {
            this.buttonClicked(this.doneButton);
        }

        ci.cancel();
    }

    @Redirect(at=@At(value="FIELD",target="Lnet/minecraft/client/gui/screen/ingame/SignEditScreen;ticksSinceOpened:I"),method="render")
    private int signmod$overwriteRenderCursor(SignEditScreen instance) {
        for (TextFieldWidget field : this.commands) {
            if (field.isFocused()) {
                return 7;
            }
        }
        return this.ticksSinceOpened;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        for (TextFieldWidget field : this.commands) {
            field.mouseClicked(mouseX, mouseY, button);
        }
    }
}

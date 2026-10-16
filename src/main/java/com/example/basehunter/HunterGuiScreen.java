package com.example.basehunter;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HunterGuiScreen extends Screen {

    public HunterGuiScreen() {
        super(Text.literal("Base & Amethyst Hunter"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2 - 100;
        int startY = this.height / 2 - 55;

        this.addDrawableChild(ButtonWidget.builder(getBaseEspText(), button -> {
            BaseHunterMod.baseEspEnabled = !BaseHunterMod.baseEspEnabled;
            button.setMessage(getBaseEspText());
        }).dimensions(centerX, startY, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(getAmethystEspText(), button -> {
            BaseHunterMod.amethystEspEnabled = !BaseHunterMod.amethystEspEnabled;
            button.setMessage(getAmethystEspText());
        }).dimensions(centerX, startY + 25, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(getTracerText(), button -> {
            BaseHunterMod.tracersEnabled = !BaseHunterMod.tracersEnabled;
            button.setMessage(getTracerText());
        }).dimensions(centerX, startY + 50, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(getMinChestText(), button -> {
            if (BaseHunterMod.minBaseChestCount == 1) BaseHunterMod.minBaseChestCount = 3;
            else if (BaseHunterMod.minBaseChestCount == 3) BaseHunterMod.minBaseChestCount = 5;
            else if (BaseHunterMod.minBaseChestCount == 5) BaseHunterMod.minBaseChestCount = 10;
            else BaseHunterMod.minBaseChestCount = 1;
            button.setMessage(getMinChestText());
        }).dimensions(centerX, startY + 75, 200, 20).build());
    }

    private Text getBaseEspText() {
        return Text.literal("Base ESP: " + (BaseHunterMod.baseEspEnabled ? "ON" : "OFF"));
    }

    private Text getAmethystEspText() {
        return Text.literal("Amethyst ESP: " + (BaseHunterMod.amethystEspEnabled ? "ON" : "OFF"));
    }

    private Text getTracerText() {
        return Text.literal("Cluster Tracers: " + (BaseHunterMod.tracersEnabled ? "ON" : "OFF"));
    }

    private Text getMinChestText() {
        return Text.literal("Min Chests per Base: " + BaseHunterMod.minBaseChestCount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 80, 0xFFFFFF);

        String status = String.format("Clusters: %d Bases | %d Geodes",
                BaseHunterMod.baseClusters.size(), BaseHunterMod.amethystClusters.size());
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(status), this.width / 2, this.height / 2 + 55, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }
}
package cat.module.modules.render;

import cat.events.Subscriber;
import cat.events.impl.Render2DEvent;
import cat.module.Module;
import cat.module.ModuleCategory;
import cat.module.modules.combat.Aura;
import cat.module.value.types.BoolValue;
import cat.module.value.types.IntegerValue;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class TargetHUD extends Module {


    private float lastHealth = 0;

    public boolean dragging;

    float targetPrevHealth = 0;
    private EntityLivingBase lastTarget;

    public ScaledResolution sr = new ScaledResolution(mc);
    public int x = sr.getScaledWidth() / 2;
    public int y = sr.getScaledHeight() / 2;

    public TargetHUD() {
        super("TargetHUD", "", ModuleCategory.RENDER, "TargetHUD");
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Subscriber
    public void onRender2D(Render2DEvent e) {

        int width = e.getScaledResolution().getScaledWidth();
        int height = e.getScaledResolution().getScaledHeight();
        int mouseX = Mouse.getX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getY() * height / mc.displayHeight - 1;



        EntityLivingBase target = Aura.target == null ? mc.thePlayer : Aura.target;

        float health = target.getHealth();
        float healthPercentage = (health / target.getMaxHealth());
        float targetHealthPercentage = 0;
        if (healthPercentage != lastHealth) {
            float diff = healthPercentage - this.lastHealth;
            targetHealthPercentage = this.lastHealth;
            this.lastHealth += diff / 28;
        }

        if (mouseX >= x - 75 && mouseX <= x + 75 && mouseY >= y + 84.4 && mouseY <= y + 134 && Mouse.isButtonDown(0) && mc.currentScreen instanceof GuiChat) {
            this.dragging = true;
        } else {
            this.dragging = false;
        }
        if (this.dragging && mc.currentScreen instanceof GuiChat) {
            int newX = (int) (mouseX);
            x = (int) newX;
            int newY = (mouseY - 110);
            y = (int) newY;
        } else {
            this.dragging = false;
        }

        if (this.getState()) {
            if (Aura.target != null) {

                Gui.drawRect(x - 75, y + 84.4, x + 75, y + 134, new Color(0, 0, 0, 180).getRGB());

                Gui.drawRect(x - 46, y + 124.4, (x - 41 + (113 * targetHealthPercentage)), y + 131.46, Color.RED.darker().getRGB());
                Gui.drawRect(x - 46, y + 124.4, (x - 46 + (113 * targetHealthPercentage)), y + 131.46, Color.RED.brighter().getRGB());

                    mc.fontRendererObj.drawStringWithShadow(target.getName(), x - 45, y + 89, -1);
                    GL11.glPushMatrix();
                    GL11.glScalef(2, 2, 2);
                    mc.fontRendererObj.drawStringWithShadow("" + Math.round(target.getHealth()) / 2 + " ❤", x / 2 - 23, y / 2 + 52, Color.RED.getRGB());
                    GL11.glPopMatrix();
                    GL11.glColor3f(1, 1, 1);
                    if (target.isEntityAlive()) {
                        GuiInventory.drawEntityOnScreen(x - 61, y + 134, 23, target.rotationYaw, target.rotationPitch, target);
                }
            } else if (Aura.target == null && mc.currentScreen instanceof GuiChat) {
                    Gui.drawRect(x - 75, y + 84.4, x + 75, y + 134, new Color(0, 0, 0, 180).getRGB());


                    Gui.drawRect(x - 46, y + 124.4, (x - 41 + (113 * targetHealthPercentage)), y + 131.46, Color.RED.darker().getRGB());
                    Gui.drawRect(x - 46, y + 124.4, (x - 46 + (113 * targetHealthPercentage)), y + 131.46, Color.RED.brighter().getRGB());
                    mc.fontRendererObj.drawStringWithShadow(target.getName(), x - 45, y + 89, -1);
                    GL11.glPushMatrix();
                    GL11.glScalef(2, 2, 2);

                        mc.fontRendererObj.drawStringWithShadow("" + Math.round(target.getHealth()) / 2 + " ❤", x / 2 - 23, y / 2 + 52, Color.RED.getRGB());

                    GL11.glPopMatrix();
                    GL11.glColor3f(1, 1, 1);
                    if (target.isEntityAlive()) {
                        GuiInventory.drawEntityOnScreen(x - 61, y + 134, 23, target.rotationYaw, target.rotationPitch, target);
                }
            }
        }
    }
}

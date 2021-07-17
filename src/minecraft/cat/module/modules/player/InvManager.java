package cat.module.modules.player;

import cat.events.Subscriber;
import cat.events.impl.UpdateEvent;
import cat.events.impl.UpdatePlayerEvent;
import cat.module.Module;
import cat.module.ModuleCategory;
import cat.module.modules.combat.Aura;
import cat.module.value.types.BoolValue;
import cat.module.value.types.FloatValue;
import cat.module.value.types.IntegerValue;
import cat.util.MillisTimer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.ThreadLocalRandom;

public class InvManager extends Module {

    private static MillisTimer timer = new MillisTimer();


    private final IntegerValue delay = new IntegerValue("Delay", 60, 20, 200, 1, true, null);
    private final IntegerValue swordSlot = new IntegerValue("Sword Slot", 7, 0, 20, 1, true, null);
    private final BoolValue keepaxe = new BoolValue( "KeepAxe",true, true, null);
    private final BoolValue keepshovel = new BoolValue("KeepShovel",true, true, null);
    private final BoolValue keeppickaxe = new BoolValue("KeepPickaxe",true, true, null);
    private final BoolValue clean = new BoolValue("Clean",true, true, null);
    private final BoolValue cleanbad = new BoolValue( "CleanBad",true, true, null);

    public InvManager() {
        super("InvManager", "", ModuleCategory.PLAYER, Keyboard.KEY_I, "invManager", "in", "Inventory Manager");
    }


    @Subscriber
    public void onEvent(UpdatePlayerEvent e) {
            double realdelay = delay.get();
            double delay = Math.max(20, realdelay + ThreadLocalRandom.current().nextDouble(-40, 40));

            if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChat) || Aura.target != null) {
                timer.reset();
                return;
            }

            if (this.timer.delay((float) delay)) {
                this.invManager(delay);

                timer.reset();
            }
    }

    private void invManager(double delay) {
        int bestSword = -1;
        float bestDamage = 1F;

        for (int k = 0; k < mc.thePlayer.inventory.mainInventory.length; k++) {
            ItemStack item = mc.thePlayer.inventory.mainInventory[k];
            if (item != null) {
                if (item.getItem() instanceof ItemSword) {
                    ItemSword is = (ItemSword) item.getItem();
                    float damage = is.getDamageVsEntity();
                    damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, item) * 1.26F +
                            EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, item) * 0.01f;
                    if (damage > bestDamage) {
                        bestDamage = damage;
                        bestSword = k;
                    }
                }
            }
        }
        int swordSlot = (int) this.swordSlot.get();
        if (bestSword != -1 && bestSword != swordSlot - 1) {
            for (int i = 0; i < mc.thePlayer.inventoryContainer.inventorySlots.size(); i++) {
                Slot s = mc.thePlayer.inventoryContainer.inventorySlots.get(i);
                if (s.getHasStack() && s.getStack() == mc.thePlayer.inventory.mainInventory[bestSword]) {
                    int slot = swordSlot - 1;
                    mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, s.slotNumber, slot, 2, mc.thePlayer);
                    timer.reset();
                    return;
                }
            }
        }
        if (this.clean.get() && timer.delay((float) delay)) {
            for (int i = 9; i < 45; ++i) {
                if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                    final ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                    if (this.shouldDrop(is, i) && timer.delay((float) delay)) {
                        this.drop(i);
                        this.timer.reset();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer.reset();
    }

    public void drop(final int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 1, 4, mc.thePlayer);
    }

    public boolean shouldDrop(ItemStack is, int k) {
        int bestSword = getSwordSlot();
        if (is.getItem() instanceof ItemSword) {
            if (bestSword != -1 && bestSword != k) {
                return true;
            }
        }
        int bestPick = getPickaxeSlot();
        if (is.getItem() instanceof ItemPickaxe) {
            if (!this.keeppickaxe.get()) {
                return true;
            }
            if (bestPick != -1 && bestPick != k) {
                return true;
            }
        }

        int bestAxe = getAxeSlot();
        if (is.getItem() instanceof ItemAxe) {
            if (!this.keepaxe.get()) {
                return true;
            }
            if (bestAxe != -1 && bestAxe != k) {
                return true;
            }
        }

        int bestShovel = getShovelSlot();
        if (isShovel(is.getItem())) {
            if (!this.keepshovel.get()) {
                return true;
            }
            if (bestShovel != -1 && bestShovel != k) {
                return true;
            }
        }
        if (this.cleanbad.get() && isBad(is)) {
            return true;
        }
        return false;
    }

    public boolean isBad(final ItemStack item) {
        return !(item.getItem() instanceof ItemArmor || item.getItem() instanceof ItemTool || item.getItem() instanceof ItemBlock || item.getItem() instanceof ItemSword || item.getItem() instanceof ItemEnderPearl || item.getItem() instanceof ItemFood || (item.getItem() instanceof ItemPotion && !isBadPotion(item))) && !item.getDisplayName().toLowerCase().contains(EnumChatFormatting.GRAY +"(right click)");
    }

    public boolean isBadPotion(final ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemPotion) {
            final ItemPotion potion = (ItemPotion)stack.getItem();
            if (ItemPotion.isSplash(stack.getItemDamage())) {
                for (final Object o : potion.getEffects(stack)) {
                    final PotionEffect effect = (PotionEffect)o;
                    if (effect.getPotionID() == Potion.poison.getId() || effect.getPotionID() == Potion.harm.getId() || effect.getPotionID() == Potion.moveSlowdown.getId() || effect.getPotionID() == Potion.weakness.getId()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int getBestHelmet() {
        int bestSword = -1;
        float bestValue = 0F;

        for (int k = 0; k < 36; k++) {
            if (mc.thePlayer.inventoryContainer.getSlot(k).getHasStack()) {
                ItemStack item = mc.thePlayer.inventoryContainer.getSlot(k).getStack();
                if (item != null) {
                    if (item.getItem() instanceof ItemArmor) {
                        ItemArmor ia = (ItemArmor) item.getItem();
                        float value = getValue(item, ia);
                        if (ia.armorType == 0 && value > bestValue) {
                            bestValue = value;
                            bestSword = k;
                        }
                    }
                }
            }
        }

        for (int k = 0; k < 9; k++) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(k);
            if (item != null) {
                if (item.getItem() instanceof ItemArmor) {
                    ItemArmor ia = (ItemArmor) item.getItem();
                    float value = getValue(item, ia);
                    if (ia.armorType == 0 && value > bestValue) {
                        bestValue = value;
                        bestSword = k;
                    }
                }
            }
        }
        return bestSword;
    }

    private int getBestChestplate() {
        int bestSword = -1;
        float bestValue = 0F;

        for (int k = 0; k < 36; k++) {
            if (mc.thePlayer.inventoryContainer.getSlot(k).getHasStack()) {
                ItemStack item = mc.thePlayer.inventoryContainer.getSlot(k).getStack();
                if (item != null) {
                    if (item.getItem() instanceof ItemArmor) {
                        ItemArmor ia = (ItemArmor) item.getItem();
                        float value = getValue(item, ia);
                        if (ia.armorType == 1 && value > bestValue) {
                            bestValue = value;
                            bestSword = k;
                        }
                    }
                }
            }
        }

        for (int k = 0; k < 9; k++) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(k);
            if (item != null) {
                if (item.getItem() instanceof ItemArmor) {
                    ItemArmor ia = (ItemArmor) item.getItem();
                    float value = getValue(item, ia);
                    if (ia.armorType == 1 && value > bestValue) {
                        bestValue = value;
                        bestSword = k;
                    }
                }
            }
        }
        return bestSword;
    }

    private int getBestLeggings() {
        int bestSword = -1;
        float bestValue = 0F;

        for (int k = 0; k < 36; k++) {
            if (mc.thePlayer.inventoryContainer.getSlot(k).getHasStack()) {
                ItemStack item = mc.thePlayer.inventoryContainer.getSlot(k).getStack();
                if (item != null) {
                    if (item.getItem() instanceof ItemArmor) {
                        ItemArmor ia = (ItemArmor) item.getItem();
                        float value = getValue(item, ia);
                        if (ia.armorType == 2 && value > bestValue) {
                            bestValue = value;
                            bestSword = k;
                        }
                    }
                }
            }
        }

        for (int k = 0; k < 9; k++) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(k);
            if (item != null) {
                if (item.getItem() instanceof ItemArmor) {
                    ItemArmor ia = (ItemArmor) item.getItem();
                    float value = getValue(item, ia);
                    if (ia.armorType == 2 && value > bestValue) {
                        bestValue = value;
                        bestSword = k;
                    }
                }
            }
        }
        return bestSword;
    }

    private int getBestBoots() {
        int bestSword = -1;
        float bestValue = 0F;

        for (int k = 0; k < 36; k++) {
            if (mc.thePlayer.inventoryContainer.getSlot(k).getHasStack()) {
                ItemStack item = mc.thePlayer.inventoryContainer.getSlot(k).getStack();

                if (item != null) {
                    if (item.getItem() instanceof ItemArmor) {
                        ItemArmor ia = (ItemArmor) item.getItem();
                        float value = getValue(item, ia);
                        if (ia.armorType == 3 && value > bestValue) {
                            bestValue = value;
                            bestSword = k;
                        }
                    }
                }
            }
        }

        for (int k = 0; k < 9; k++) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(k);
            if (item != null) {
                if (item.getItem() instanceof ItemArmor) {
                    ItemArmor ia = (ItemArmor) item.getItem();
                    float value = getValue(item, ia);
                    if (ia.armorType == 3 && value > bestValue) {
                        bestValue = value;
                        bestSword = k;
                    }
                }
            }
        }
        return bestSword;
    }

    private float getValue(ItemStack is, ItemArmor ia) {
        int type = 0;
        if (ia.armorType == 0) {type = 0;}
        if (ia.armorType == 3) {type = 1;}
        if (ia.armorType == 2) {type = 2;}
        if (ia.armorType == 1) {type = 3;}

        int render = 0;
        if (ia.renderIndex == 0) {render = 0;}
        if (ia.renderIndex == 1) {render = 1;}
        if (ia.renderIndex == 4) {render = 2;}
        if (ia.renderIndex == 2) {render = 3;}
        if (ia.renderIndex == 3) {render = 4;}

        float value = (type + 1) * (render + 1);
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, is) * 2.5f;
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, is) * 1.25f;
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, is) * 1f;

        return value;
    }

    public int getSwordSlot() {
        if (mc.thePlayer == null) {
            return -1;
        }

        int bestSword = -1;
        float bestDamage = 1F;

        for (int i = 9; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack item = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (item != null) {
                    if (item.getItem() instanceof ItemSword) {
                        ItemSword is = (ItemSword) item.getItem();
                        float damage = is.getDamageVsEntity();
                        damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, item) * 1.26F +
                                EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, item) * 0.01f;
                        if (damage > bestDamage) {
                            bestDamage = damage;
                            bestSword = i;
                        }
                    }
                }
            }
        }
        return bestSword;
    }

    public int getPickaxeSlot() {
        if (mc.thePlayer == null) {
            return -1;
        }

        int bestSword = -1;
        float bestDamage = 1F;

        for (int i = 9; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack item = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (item != null) {
                    if (item.getItem() instanceof ItemPickaxe) {
                        ItemPickaxe is = (ItemPickaxe) item.getItem();
                        float damage = is.getStrVsBlock(item, Block.getBlockById(4));
                        if (damage > bestDamage) {
                            bestDamage = damage;
                            bestSword = i;
                        }
                    }
                }
            }
        }
        return bestSword;
    }

    public int getAxeSlot() {
        if (mc.thePlayer == null) {
            return -1;
        }

        int bestSword = -1;
        float bestDamage = 1F;

        for (int i = 9; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack item = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (item != null) {
                    if (item.getItem() instanceof ItemAxe) {
                        ItemAxe is = (ItemAxe) item.getItem();
                        float damage = is.getStrVsBlock(item, Block.getBlockById(17));
                        if (damage > bestDamage) {
                            bestDamage = damage;
                            bestSword = i;
                        }
                    }
                }
            }
        }
        return bestSword;
    }

    public int getShovelSlot() {
        if (mc.thePlayer == null) {
            return -1;
        }

        int bestSword = -1;
        float bestDamage = 1F;

        for (int i = 9; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack item = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (item != null) {
                    if (item.getItem() instanceof ItemTool) {
                        ItemTool is = (ItemTool) item.getItem();
                        if (isShovel(is)) {
                            float damage = is.getStrVsBlock(item, Block.getBlockById(3));
                            if (damage > bestDamage) {
                                bestDamage = damage;
                                bestSword = i;
                            }
                        }
                    }
                }
            }
        }
        return bestSword;
    }

    public void shiftClick(int slot) {
        if (mc.thePlayer.inventoryContainer.getSlot(slot).getHasStack()) {
            Slot s = mc.thePlayer.inventoryContainer.getSlot(slot);
            if (s.getStack().getItem() instanceof ItemArmor) {
                Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().thePlayer.inventoryContainer.windowId, slot, 0, 1, Minecraft.getMinecraft().thePlayer);
            }
        }
    }

    public static boolean isShovel(Item item) {
        return Item.getItemById(256) == item || Item.getItemById(269) == item || Item.getItemById(273) == item || Item.getItemById(277) == item || Item.getItemById(284) == item;
    }
}

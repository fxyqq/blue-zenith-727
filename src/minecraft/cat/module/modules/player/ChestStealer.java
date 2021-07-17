
package cat.module.modules.player;

import cat.events.Subscriber;
import cat.events.impl.UpdatePlayerEvent;
import cat.module.Module;
import cat.module.ModuleCategory;
import cat.module.value.types.IntegerValue;
import cat.util.MillisTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

    public final class ChestStealer extends Module {



        public boolean emptyChestCheck;

        private final IntegerValue delay = new IntegerValue("Delay", 50, 0, 300, 10, true, null);
        private final MillisTimer timer = new MillisTimer();
        public ChestStealer() {
            super("ChestStealer", "", ModuleCategory.PLAYER, "stealer");
        }



        @Subscriber
        public void onUpdate(UpdatePlayerEvent event) {
            final GuiChest chest = (GuiChest) mc.currentScreen;
            if(mc.thePlayer == null){
                return;
            }
            if (this.emptyChestCheck(chest)) {
                Minecraft.getMinecraft().thePlayer.closeScreen();
//				NotificationManager.queue("Info", "Closed The Chest", NotificationType.INFO);
                return;
            }
            if((this.mc.thePlayer.openContainer != null) && ((this.mc.thePlayer.openContainer instanceof ContainerChest))) {
                ContainerChest chest1 = (ContainerChest) this.mc.thePlayer.openContainer;
                for(int i = 0; i < chest1.getLowerChestInventory().getSizeInventory(); i++) {
                    if((chest1.getLowerChestInventory().getStackInSlot(i) != null && timer.hasTimeReached(delay.get()))) {
                        this.mc.playerController.windowClick(chest1.windowId, i, 1, 1, this.mc.thePlayer);
                        timer.reset();
                    }
                }
            }
        }

        public boolean emptyChestCheck(GuiChest guiChest) {
            for (int i = 0; i < guiChest.getLowerChestInventory().getSizeInventory(); i++) {
                ItemStack itemStack = guiChest.getLowerChestInventory().getStackInSlot(i);
                if (itemStack != null) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getTag() {
            return this.delay.get().toString();
        }
}

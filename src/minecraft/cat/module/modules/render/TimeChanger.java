package cat.module.modules.render;

import cat.events.Subscriber;
import cat.events.impl.PacketEvent;
import cat.module.Module;
import cat.module.ModuleCategory;
import cat.module.value.types.IntegerValue;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

public class TimeChanger extends Module {

	private final IntegerValue time = new IntegerValue("Time", 1000, 3000, 18000, 1, true, null);


	public TimeChanger() {
		super("TimeChanger", "", ModuleCategory.RENDER);
	}
	
	
	@Subscriber
	public void onEvent(PacketEvent e) {
		if (e.packet instanceof S03PacketTimeUpdate) {
			e.cancel();
			mc.theWorld.setWorldTime(time.get());
		}
	}
	
	
}

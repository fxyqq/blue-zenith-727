package cat.module.modules.render;

import cat.module.Module;
import cat.module.ModuleCategory;
import cat.module.value.types.ModeValue;

public class Animations extends Module {
    public ModeValue anim = new ModeValue("Animation", "SlideDown", true, null, "SlideDown", "Exhibition");
    public Animations() {
        super("Animations", "", ModuleCategory.RENDER, "animations", "anim");
    }
}

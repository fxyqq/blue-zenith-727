package cat.events.impl;

import cat.events.Event;

public class UpdatePlayerEvent extends Event {
    public float yaw, pitch;
    public double x, y, z;
    public boolean onGround;
    public UpdatePlayerEvent(float yaw, float pitch, double x, double y, double z, boolean onGround){
        this.yaw = yaw;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
    }
}

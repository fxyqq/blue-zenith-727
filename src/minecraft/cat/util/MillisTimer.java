package cat.util;

import net.minecraft.client.Minecraft;

public final class MillisTimer {
    public long millis = -1L;

    public boolean hasTimeReached(final long ms) {
        return System.currentTimeMillis() >= millis + ms;
    }

    public boolean delay(final float milliSec) {
        return this.getTime() - this.millis >= milliSec;
    }

    public long getTime() {
        return System.nanoTime() / 1000000L;
    }


    public long getTimeDiff(final long ms) {
        return (ms + millis) - System.currentTimeMillis();
    }

    public void reset() {
        millis = System.currentTimeMillis();
    }

    public boolean hasTicksPassed(final long ticks){
        return System.currentTimeMillis() >= millis + (ticks * 50);
    }
}

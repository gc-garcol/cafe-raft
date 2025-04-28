package gc.garcol.caferaft.core.time;

/**
 * @author thaivc
 * @since 2025
 */
public class YieldIdleStrategy implements IdleStrategy {
    @Override
    public void idle() {
        Thread.yield();
    }
}

package pactoria.horseCombatPaper.extraClasses;

import java.util.HashMap;
import java.util.UUID;

public class HorseDataManagement {
    private double momentum;
    private long lastEventFireTime;
    public static final HashMap<UUID, HorseDataManagement> playerHorseMovementMap = new HashMap<>();

    public HorseDataManagement(long lastEventFireTime) {
        this.momentum = 0;
        this.lastEventFireTime = lastEventFireTime;
    }

    public double getMomentum() { return momentum; }
    public void increaseMomentum(double incrementValue) { momentum = momentum + incrementValue; }
    public long getLastEventFireTime() { return lastEventFireTime; }
    public void setLastEventFireTime(long time) { lastEventFireTime = time; }
}
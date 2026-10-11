package pactoria.horseCombatPaper;

import pactoria.horseCombatPaper.Listeners.OnHorseMove;
import pactoria.horseCombatPaper.Listeners.OnLanceSwing;
import org.bukkit.plugin.java.JavaPlugin;

public final class HorseCombatPaper extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new OnHorseMove(getConfig()), this);
        getServer().getPluginManager().registerEvents(new OnLanceSwing(getConfig()), this);
        saveDefaultConfig();

    }
}

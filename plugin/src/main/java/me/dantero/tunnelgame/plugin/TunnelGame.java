package me.dantero.tunnelgame.plugin;

import com.gmail.furkanaxx34.dlibrary.bukkit.DLibrary;
import org.bukkit.plugin.java.JavaPlugin;

public final class TunnelGame extends JavaPlugin {

  @Override
  public void onEnable() {
    DLibrary.initialize(this);
  }
}

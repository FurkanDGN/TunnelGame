package com.github.furkandgn.tunnelgame.plugin.manager;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.github.furkandgn.tunnelgame.common.manager.WorldManager;
import org.bukkit.Bukkit;

import java.util.Objects;

/**
 * @author Furkan Doğan
 */
public class SlimeWorldManager implements WorldManager {

  private final SlimePlugin slimePlugin;
  private final SlimeLoader slimeLoader;
  private final SlimePropertyMap slimePropertyMap;

  public SlimeWorldManager(SlimePlugin slimePlugin) {
    this.slimePlugin = slimePlugin;
    this.slimeLoader = Objects.requireNonNull(slimePlugin).getLoader("file");
    this.slimePropertyMap = new SlimePropertyMap();
    this.slimePropertyMap.setBoolean(SlimeProperties.ALLOW_MONSTERS, true);
    this.slimePropertyMap.setBoolean(SlimeProperties.ALLOW_ANIMALS, false);
    this.slimePropertyMap.setString(SlimeProperties.DIFFICULTY, "normal");
  }

  @Override
  public void createEmptyWorld(String name) throws Exception {
    SlimeWorld emptyWorld = this.slimePlugin.createEmptyWorld(this.slimeLoader, name, false, this.slimePropertyMap);
    this.slimePlugin.generateWorld(emptyWorld);
  }

  @Override
  public void loadWorld(String name) throws Exception {
    SlimeWorld slimeWorld = this.slimePlugin.loadWorld(this.slimeLoader, name, false, this.slimePropertyMap);
    this.slimePlugin.generateWorld(slimeWorld);
  }

  @Override
  public void deleteWorld(String name) throws Exception {
    this.slimeLoader.deleteWorld(name);
    Bukkit.unloadWorld(name, false);
  }
}


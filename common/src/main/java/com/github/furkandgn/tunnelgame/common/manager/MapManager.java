package com.github.furkandgn.tunnelgame.common.manager;

/**
 * @author Furkan Doğan
 */
public interface MapManager {

  String getWorldName();

  void deleteWorld();

  void cloneAndLoadWorld();

  void reloadMap() throws Exception;
}

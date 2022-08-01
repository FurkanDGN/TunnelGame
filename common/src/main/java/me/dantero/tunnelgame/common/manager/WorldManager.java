package me.dantero.tunnelgame.common.manager;

/**
 * @author Furkan Doğan
 */
public interface WorldManager {

  void createEmptyWorld(String name) throws Exception;

  void loadWorld(String name) throws Exception;

  void deleteWorld(String name) throws Exception;
}

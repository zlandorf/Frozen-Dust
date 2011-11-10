package fr.frozen.iron.common.controller;

import java.util.Hashtable;
import java.util.List;

import fr.frozen.iron.common.GameObserver;
import fr.frozen.iron.common.IronPlayer;
import fr.frozen.iron.common.IronWorld;
import fr.frozen.iron.common.PlayerGameInfo;
import fr.frozen.iron.common.skills.Skill;

public interface IGameController {

	public abstract void addPlayer(IronPlayer player, int race);

	public abstract void setPlayersList(List<IronPlayer> list);

	public abstract void setPlayersMap(Hashtable<Integer, IronPlayer> map);

	public abstract void setInfoMap(Hashtable<Integer, PlayerGameInfo> map);

	public abstract IronWorld getWorld();

	public abstract boolean isGameStarted();

	public abstract boolean isGameOver();

	public abstract int getWinnerId();

	public abstract float getTimeLeftForTurn();

	public abstract int getTurnPlayerId();

	public abstract void update(float deltaTime);

	public abstract boolean canUndo();

	public abstract void undoMove();

	public abstract void onGameOver(int winnerId);

	public abstract void onGameOver(IronPlayer winner, IronPlayer loser);

	public abstract void switchTurns();

	public abstract void setTurn(int playerId);

	public abstract void abandon(int playerId);

	public abstract void startGame(int playerStartId);

	public abstract void startGame();

	public abstract void handleMove(int unitSrcId, int x, int y);

	public abstract void handleSkill(int unitSrcId, Skill skill, int x, int y);

	public abstract PlayerGameInfo getPlayerInfo(int playerId);

	public abstract void addGameObserver(GameObserver observer);

	public abstract void removeGameObserver(GameObserver observer);

	public abstract void init();

	public abstract IronPlayer getTurnPlayer();

	public abstract IronPlayer getWinner();

}
package com.AnthonyFuLearning.game;

import java.util.List;

public class Player {
    private String playerId;  // 玩家的唯一標識符

    public Player(String playerId) {
        this.setPlayerId(playerId);
    }

    public void joinRoom(Room room) {
        room.addPlayer(this);
    }

    public void syncPlayers(List<Player> players) {
        // 同步房間內的所有玩家列表
    }

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
}
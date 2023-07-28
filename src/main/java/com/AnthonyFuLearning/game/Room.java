package com.AnthonyFuLearning.game;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomId;  // 房間的唯一標識符
    private List<Player> players = new ArrayList<>();  // 房間內的玩家列表

    public Room(String roomId) {
        this.setRoomId(roomId);
    }

    public void addPlayer(Player player) {
        players.add(player);
        // 玩家加入後，同步房間內的所有玩家信息
        syncPlayersInfo();
    }

    private void syncPlayersInfo() {
        // 將房間內的所有玩家列表同步給每個玩家
        for (Player player : players) {
            player.syncPlayers(players);
        }
    }

    public void startGame() {
        // 開始遊戲
    }

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
}
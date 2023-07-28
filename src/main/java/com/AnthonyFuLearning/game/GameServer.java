package com.AnthonyFuLearning.game;

import java.util.HashMap;
import java.util.Map;

public class GameServer {
    private Map<String, Room> rooms = new HashMap<>();  // 所有的房間

    public void joinRoom(Player player, String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            room = new Room(roomId);
            rooms.put(roomId, room);
        }
        player.joinRoom(room);
    }
}
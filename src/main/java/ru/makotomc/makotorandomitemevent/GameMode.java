package ru.makotomc.makotorandomitemevent;

public enum GameMode {
    SOLO(1),
    DUO(2),
    TRIO(3),
    QUAD(4);
    public final int playerCount;
    GameMode(int playerCount){
        this.playerCount = playerCount;
    }
    public static GameMode CURRENT_GAME_MODE = GameMode.SOLO;
}

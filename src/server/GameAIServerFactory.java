package server;

import ray.networking.IGameConnection;

import java.io.IOException;

public class GameAIServerFactory {

    private GameAIServerUDP gameServer;
    protected NPCcontroller npcController;

    public GameAIServerFactory() {

    }

    public GameAIServerUDP GameServer(int localPort, IGameConnection.ProtocolType protocolType) throws IOException {
        gameServer = new GameAIServerUDP(localPort, protocolType);
        npcController = new NPCcontroller(gameServer);
        gameServer.npcController = npcController;
        return gameServer;
    }
}

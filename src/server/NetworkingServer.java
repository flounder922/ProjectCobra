package server;

import ray.networking.IGameConnection;

import java.io.IOException;

public class NetworkingServer {
    private GameServerUDP thisUDPServer;

    private NPCcontroller npcController;
    GameAIServerUDP udpServer;

    long startTime;
    long lastUpdateTime;


    public NetworkingServer(int serverPort, String protocol) {
        startTime = System.nanoTime();
        lastUpdateTime = startTime;

        try {
            if(protocol.toUpperCase().compareTo("TCP") == 0) {
                System.out.println("TCP is not supported");
            }
            else {
                //thisUDPServer = new GameServerUDP(serverPort, IGameConnection.ProtocolType.UDP);
                udpServer = new GameAIServerUDP(serverPort, IGameConnection.ProtocolType.UDP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        npcController = udpServer.npcController;
        npcController.setupNPCs();
        npcLoop();
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
        }
    }

    public void npcLoop() {
        while (true) {
            long frameStartTime = System.nanoTime();
            float elapsedTime = (frameStartTime - lastUpdateTime) / (1000000.0f);

            if (elapsedTime >= 50.0f) {
                lastUpdateTime = frameStartTime;
                npcController.updateNPCs();
                udpServer.sendNPCinfo();
            }
            Thread.yield();
        }
    }


}

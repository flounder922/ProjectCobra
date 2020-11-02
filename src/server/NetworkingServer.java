package server;

import ray.networking.IGameConnection;

import java.io.IOException;

public class NetworkingServer {
    private GameServerUDP thisUDPServer;

    public NetworkingServer(int serverPort, String protocol) {
        try {
            if(protocol.toUpperCase().compareTo("TCP") == 0) {
                System.out.println("TCP is not supported");
            }
            else {
                thisUDPServer = new GameServerUDP(serverPort, IGameConnection.ProtocolType.UDP);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]), args[1]);
        }
    }
}

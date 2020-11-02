package server;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

public class GameServerUDP extends GameConnectionServer<UUID> {

    public GameServerUDP(int localPort, ProtocolType protocolType) throws IOException {
        super(localPort, protocolType);
    }

    @Override
    public void processPacket(Object object, InetAddress senderIP, int senderPort) {
        String message = (String) object;
        String[] messageTokens = message.split(",");
        UUID clientID = UUID.fromString(messageTokens[1]);

        if (messageTokens.length > 0) {
            // Case where the server receives a JOIN message'
            // Format: join, localId
            if (messageTokens[0].compareTo("join") == 0) {
                try {
                    IClientInfo clientInfo = getServerSocket().createClientInfo(senderIP, senderPort);
                    addClient(clientInfo, clientID);
                    sendJoinedMessages(clientID, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // Case where server receives a CREATE message
        // Format: create, localId, x, y, z
        if(messageTokens[0].compareTo("create") == 0) {
            String[] position = {messageTokens[2], messageTokens[3], messageTokens[4]};
            sendCreateMessages(clientID, position);
            sendWantsDetailsMessages(clientID);
        }

        // Case where server receives a BYE message
        // Format: bye,localId
        if(messageTokens[0].compareTo("bye") == 0) {
            sendByeMessages(clientID);
            removeClient(clientID);
        }

        // Case where server receives a DETAILS-FOR message

        // Case where server receives a MOVE message
    }

    private void sendJoinedMessages(UUID clientID, boolean success) {
        try
        { String message = new String("join,");
            if (success)
                message += "success";
            else
                message += "failure";

            sendPacket(message, clientID);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCreateMessages(UUID clientID, String[] position) {
        try {
            String message = new String("create," + clientID.toString());
            message += "," + position[0];
            message += "," + position[1];
            message += "," + position[2];
            forwardPacketToAll(message, clientID);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendByeMessages(UUID clientID) {
        try {
            String message = new String("bye");
            sendPacket(message, clientID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendWantsDetailsMessages(UUID clientID) {
    }
}

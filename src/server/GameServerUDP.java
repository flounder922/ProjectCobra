package server;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.rml.Vector3;

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

            // Case where server receives a CREATE message
            // Format: create, localId, x, y, z
            if (messageTokens[0].compareTo("create") == 0) {
                String[] position = {messageTokens[2], messageTokens[3], messageTokens[4]};
                sendCreateMessages(clientID, position);
                sendWantsDetailsMessages(clientID);
            }

            // Case where server receives a BYE message
            // Format: bye,localId
            if (messageTokens[0].compareTo("bye") == 0) {
                sendByeMessages(clientID);
                removeClient(clientID);
            }

            // Case where server receives a DETAILS-FOR message
            if (messageTokens[0].compareTo("dsrf") == 0) {
                UUID remoteId = UUID.fromString(messageTokens[2]);
                String[] position = {messageTokens[3], messageTokens[4], messageTokens[5]};
                sendDetailsToMessage(clientID, remoteId ,position);

            }

            // Case where server receives a MOVE message
            if (messageTokens[0].compareTo("move") == 0) {
                String[] position = {messageTokens[2], messageTokens[3], messageTokens[4]};
                sendMoveMessages(clientID, position);
            }
        }
    }

    private void sendMoveMessages(UUID clientID, String[] position) {
        try {
            String message = "move," + clientID.toString();
            message += "," + position[0];
            message += "," + position[1];
            message += "," + position[2];
            forwardPacketToAll(message, clientID);
            //System.out.println("Sending a move message");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendJoinedMessages(UUID clientID, boolean success) {
        try {
            String message ="join,";
            if (success)
                message += "success";
            else
                message += "failure";

            sendPacket(message, clientID);
            System.out.println("Joined: " + message + " Client ID: " + clientID.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCreateMessages(UUID clientID, String[] position) {
        try {
            String message = "create," + clientID.toString();
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
            System.out.println("Disconnecting from: " + clientID.toString());
            String message = "bye," + clientID.toString();
            forwardPacketToAll(message, clientID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendWantsDetailsMessages(UUID clientID) {
        try {
            String message = "wsds," + clientID.toString();
            forwardPacketToAll(message, clientID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendDetailsToMessage(UUID clientId, UUID remoteId, String[] position) {

        System.out.println("Sending details to " + clientId + " for " + clientId);
        try {
            String message = "dsfr," + remoteId;
            message += "," + position[0];
            message += "," + position[1];
            message += "," + position[2];
            sendPacket(message, remoteId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

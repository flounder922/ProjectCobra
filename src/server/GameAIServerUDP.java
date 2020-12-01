package server;

import ray.ai.behaviortrees.BTCompositeType;
import ray.ai.behaviortrees.BTSequence;
import ray.ai.behaviortrees.BehaviorTree;
import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
import ray.rml.Vector3;
import ray.rml.Vector3f;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GameAIServerUDP extends GameConnectionServer<UUID> {

    public NPCcontroller npcController;
    public Map<UUID, Vector3> playersPositions = new HashMap<>();

    public GameAIServerUDP(int localPort, ProtocolType protocolType) throws IOException {
        super(localPort, protocolType);
        npcController = new NPCcontroller();
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
                    sendCreateNPC(clientID);
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
                playersPositions.remove(clientID);
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
                trackPlayer(clientID, position);
            }
        }
    }

    private void sendCreateNPC(UUID clientID) {
        Vector3 location = npcController.getNPCLocation();

        float x = location.x();
        float y = location.y();
        float z = location.z();

        try {
                String message = "cnpc," + clientID.toString();
                message += "," + x;
                message += "," + y;
                message += "," + z;
                sendPacket(message, clientID);
        } catch (IOException e) {
            e.printStackTrace();
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

        System.out.println("Sending details to " + clientId + " for " + remoteId);
        try {
            String message = "dsfr," + remoteId;
            message += "," + position[0];
            message += "," + position[1];
            message += "," + position[2];
            sendPacket(message, clientId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void trackPlayer(UUID clientID, String[] position) {

        Vector3 playerPosition = Vector3f.createFrom(
                Float.parseFloat(position[0]),
                Float.parseFloat(position[1]),
                Float.parseFloat(position[2]));

        if (playersPositions.containsKey(clientID))
            playersPositions.replace(clientID, playerPosition);
        else
            playersPositions.put(clientID, playerPosition);

        /*
        npcController.checkPlayerProximity(clientID,
                Vector3f.createFrom(
                    Float.parseFloat(position[0]),
                    Float.parseFloat(position[1]),
                    Float.parseFloat(position[2])));

         */
    }

    protected void sendDamagetoClient(UUID clientID) {
        try {
            String message ="dmg," + clientID.toString();
            sendPacket(message, clientID);
            System.out.println(clientID.toString() + " has been attacked by the NPC!!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendNPCinfo() {
        Vector3 location = npcController.getNPCLocation();

        float x = location.x();
        float y = location.y();
        float z = location.z();

        try {
            String message = "mnpc," + "1";
            message += "," + x;
            message += "," + y;
            message += "," + z;
            sendPacketToAll(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMoveTowardPlayer(UUID target) {
        Vector3 location = npcController.getNPCLocation();

        float x = location.x();
        float y = location.y();
        float z = location.z();

        try {
            String message = "npcmtp," + "1";
            message += "," + x;
            message += "," + y;
            message += "," + z;
            sendPacket(message, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class NPCcontroller {
        protected NPC npc;
        protected GameAIServerUDP gameServer;
        private Random randomNumber = new Random();

        protected BehaviorTree behaviorTree = new BehaviorTree(BTCompositeType.SELECTOR);

        long thinkStartTime;
        private long lastThinkUpdateTime;
        long lastThinkUpdate;

        private long tickStartTime;
        long lastTickUpdate;
        private long lastTickUpdateTime;

        long currentTime;
        long lastUpdateTime;
        long elapsedTime;


        public NPCcontroller() {
            start();
        }

        public void start() {
            thinkStartTime = System.nanoTime();
            tickStartTime = System.nanoTime();
            lastThinkUpdateTime = thinkStartTime;
            lastTickUpdateTime = tickStartTime;

            setupNPCs();
            setupBehaviorTree();
            npcLoop();
        }

        public void setupNPCs() {
            npc = new NPC();
            npc.randomizeLocation(randomNumber.nextInt(50), randomNumber.nextInt(50));
        }

        public void npcLoop() {
            while (true) {
                currentTime = System.nanoTime();
                elapsedTime = currentTime - lastUpdateTime;

                float elapsedThinkTime = (currentTime - lastThinkUpdate) / 1000000.0f;
                float elapsedTickTime = (currentTime - lastTickUpdate) / 1000000.0f;

                if (elapsedTickTime >= 50.0f) {
                    lastTickUpdate = currentTime;
                    npc.updateLocation();
                    sendNPCinfo();
                }

                if (elapsedThinkTime >= 500.0f) {
                    lastThinkUpdate = currentTime;
                    behaviorTree.update(elapsedTime);
                }

                lastUpdateTime = currentTime;
                Thread.yield();
            }
        }

        private void setupBehaviorTree() {
            behaviorTree.insertAtRoot(new BTSequence(1));
            behaviorTree.insertAtRoot(new BTSequence(2));
            behaviorTree.insert(1, new PlayerNear(gameServer, npc, false));
            behaviorTree.insert(1, new AttackPlayer(gameServer, npc));
            behaviorTree.insert(2, new PlayerPosition(gameServer, npc, false));
            behaviorTree.insert(2, new MoveTowardPlayer(gameServer, npc));
        }


        public void updateNPCs() {

        }

        public GameAIServerUDP.NPCcontroller.NPC getNPC() {
            return npc;
        }

        public int getNumberOfNPCs() {
            return 1;
        }



        public Vector3 getNPCLocation() {
            return Vector3f.createFrom(npc.getX(), npc.getY(), npc.getZ());
        }

        public void checkPlayerProximity(UUID clientID, Vector3 playerPosition) {
            float distanceToPlayer =
                    (float) Math.sqrt(Math.pow((npc.getX() - playerPosition.x()), 2) +
                            Math.pow((npc.getY() - playerPosition.y()), 2) +
                            Math.pow((npc.getZ() - playerPosition.z()), 2));

            if (distanceToPlayer < 1) gameServer.sendDamagetoClient(clientID);
        }


        public class NPC {
            private float locX = 1;
            private float locY = 1;
            private float locZ = 10; // other state info goes here (FSM)

            private Vector3 npcForwardAxis;
            private UUID target = null;

            NPC() {
                npcForwardAxis = Vector3f.createFrom(1, 0, 0);
            }

            public float getX() {
                return locX;
            }

            public float getY() {
                return locY;
            }

            public float getZ() {
                return locZ;
            }

            public void updateLocation() {

            }

            public void setTarget(UUID target) {
                this.target = target;
            }

            public void moveTowardTarget() {
                if (target != null)
                    gameServer.sendMoveTowardPlayer(target);
            }

            public void randomizeLocation(int x, int z) {
                locX = x;
                locY = 1;
                locZ = z;
            }
        }
    }
}

package server;

import ray.ai.behaviortrees.BTCompositeType;
import ray.ai.behaviortrees.BTSequence;
import ray.ai.behaviortrees.BehaviorTree;
import ray.rml.Vector3;

import java.util.UUID;

class NPCcontroller {
    private NPC npc;
    private GameAIServerUDP gameServer;

    BehaviorTree behaviorTree = new BehaviorTree(BTCompositeType.SELECTOR);

    long thinkStartTime;
    long tickStateTime;
    long lastThinkUpdate;
    long lastTickUpdate;
    long elapsedTime;
    long currentTime;
    long lastUpdateTime;



    public NPCcontroller(GameAIServerUDP gameServer) {
        this.gameServer = gameServer;
        start();
    }

    public void start() {
        thinkStartTime = System.nanoTime();
        tickStateTime = System.nanoTime();
        currentTime = System.nanoTime();

        lastThinkUpdate = thinkStartTime;
        lastTickUpdate = tickStateTime;
        lastUpdateTime = currentTime;
        elapsedTime = currentTime - lastUpdateTime;


        setupNPCs();
        setupBehaviorTree();
        npcLoop();
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

    public NPC getNPC() {
        return npc;
    }

    public int getNumberOfNPCs() {
        return 1;
    }

    public void setupNPCs() {

    }

    public void checkPlayerProximity(UUID clientID, Vector3 playerPosition) {
        float distanceToPlayer =
                (float) Math.sqrt(Math.pow((npc.getX() - playerPosition.x()), 2) +
                                  Math.pow((npc.getY() - playerPosition.y()), 2) +
                                  Math.pow((npc.getZ() - playerPosition.z()), 2));

        if (distanceToPlayer < 1) gameServer.sendDamagetoClient(clientID);
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
                gameServer.sendNPCinfo();
            }

            if (elapsedThinkTime >= 500.0f) {
                lastThinkUpdate = currentTime;
                behaviorTree.update(elapsedTime);
            }

            lastUpdateTime = currentTime;
            Thread.yield();
        }
    }



    public class NPC {
        double locX, locY, locZ; // other state info goes here (FSM)
        UUID target;

        NPC() {

        }

        public double getX() {
            return locX;
        }

        public double getY() {
            return locY;
        }

        public double getZ() {
            return locZ;
        }

        public void updateLocation() {

        }

        public void setTarget(UUID target) {
            this.target = target;
        }

        public void moveTowardTarget() {
            gameServer.sendMoveTowardPlayer(target);
        }
    }
}

/*
package server;

import ray.ai.behaviortrees.BTCompositeType;
import ray.ai.behaviortrees.BTSequence;
import ray.ai.behaviortrees.BehaviorTree;
import ray.rml.Vector3;
import ray.rml.Vector3f;

import java.util.Random;
import java.util.UUID;

public class NPCcontroller {
    protected NPC npc;
    protected GameAIServerUDP gameServer;
    private Random randomNumber = new Random();

    protected BehaviorTree behaviorTree = new BehaviorTree(BTCompositeType.SELECTOR);

    long thinkStartTime;
    long tickStateTime;
    long lastThinkUpdate;
    long lastTickUpdate;
    long elapsedTime;
    long currentTime;
    long lastUpdateTime;



    public NPCcontroller() {
        start();
    }

    public void start() {

        setupNPCs();
        setupBehaviorTree();
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
        npc = new NPC();
        npc.randomizeLocation(randomNumber.nextInt(50), randomNumber.nextInt(50));

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

 */

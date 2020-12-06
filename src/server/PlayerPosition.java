package server;

import ray.ai.behaviortrees.BTCondition;
import ray.rml.Vector3;

import java.util.Iterator;
import java.util.UUID;

public class PlayerPosition extends BTCondition {

    NPC npc;
    GameAIServerUDP gameServer;

    public PlayerPosition(GameAIServerUDP gameServer, NPC npc, boolean toNegate) {
        super(toNegate);
        this.npc = npc;
        this.gameServer = gameServer;
    }

    @Override
    protected boolean check() {
        UUID key = null;
        float distanceToPlayer = 0;

        Iterator iterator = gameServer.playersPositions.keySet().iterator();

        while (iterator.hasNext()){
            key = (UUID) iterator.next();

            if (key != null) {
                Vector3 position = gameServer.playersPositions.get(key);

                distanceToPlayer =
                        (float) Math.sqrt(Math.pow((npc.getX() - position.x()), 2) +
                                Math.pow((npc.getY() - position.y()), 2) +
                                Math.pow((npc.getZ() - position.z()), 2));
            }

            if ((distanceToPlayer < 5) && (key != null)) {
                npc.setTarget(key);
                return true;
            }
        }
        if (key != null)
            npc.setTarget(key);
        return false;
    }
}

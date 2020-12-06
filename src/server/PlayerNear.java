package server;

import ray.ai.behaviortrees.BTCondition;
import ray.rml.Vector3;

import java.util.UUID;

public class PlayerNear extends BTCondition {

    NPC npc;
    GameAIServerUDP gameServer;

    public PlayerNear(GameAIServerUDP gameServer, NPC npc, boolean toNegate) {
        super(toNegate);
        this.npc = npc;
        this.gameServer = gameServer;
    }

    @Override
    protected boolean check() {
        for (UUID key : gameServer.playersPositions.keySet()) {
            Vector3 position = gameServer.playersPositions.get(key);

            float distanceToPlayer =
                    (float) Math.sqrt(Math.pow((npc.getX() - position.x()), 2) +
                            Math.pow((npc.getY() - position.y()), 2) +
                            Math.pow((npc.getZ() - position.z()), 2));

            if (distanceToPlayer < 1)
                return true;

        }
        return false;
    }
}

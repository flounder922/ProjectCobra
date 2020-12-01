package server;

import ray.ai.behaviortrees.BTAction;
import ray.ai.behaviortrees.BTStatus;
import ray.rml.Vector3;

import java.util.UUID;

public class AttackPlayer extends BTAction {

    private final GameAIServerUDP gameServer;
    private final NPCcontroller.NPC npc;

    public AttackPlayer(GameAIServerUDP gameServer, NPCcontroller.NPC npc) {
        this.gameServer = gameServer;
        this.npc = npc;
    }

    @Override
    protected BTStatus update(float v) {
        for (UUID key : gameServer.playersPositions.keySet()) {
            Vector3 position = gameServer.playersPositions.get(key);

            float distanceToPlayer =
                    (float) Math.sqrt(Math.pow((npc.getX() - position.x()), 2) +
                            Math.pow((npc.getY() - position.y()), 2) +
                            Math.pow((npc.getZ() - position.z()), 2));

            if (distanceToPlayer < 1)
                gameServer.sendDamagetoClient(key);
                System.out.println("The player has been hurt! :)");
                return BTStatus.BH_SUCCESS;

        }
        System.out.println("I have failed to hurt the player. :(");
        return BTStatus.BH_FAILURE;
    }
}

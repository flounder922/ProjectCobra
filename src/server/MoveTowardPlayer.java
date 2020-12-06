package server;

import ray.ai.behaviortrees.BTAction;
import ray.ai.behaviortrees.BTStatus;

public class MoveTowardPlayer extends BTAction {

    private NPC npc;
    private GameAIServerUDP gameServer;

    public MoveTowardPlayer(GameAIServerUDP gameServer, NPC npc) {
        this.gameServer = gameServer;
        this.npc = npc;
    }

    @Override
    protected BTStatus update(float v) {
        npc.moveTowardTarget();
        return BTStatus.BH_SUCCESS;
    }
}

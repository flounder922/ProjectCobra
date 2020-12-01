package server;

import ray.ai.behaviortrees.BTAction;
import ray.ai.behaviortrees.BTStatus;

public class MoveTowardPlayer extends BTAction {

    GameAIServerUDP.NPCcontroller.NPC npc;
    GameAIServerUDP gameServer;

    public MoveTowardPlayer(GameAIServerUDP gameServer, GameAIServerUDP.NPCcontroller.NPC npc) {
        this.gameServer = gameServer;
        this.npc = npc;
    }

    @Override
    protected BTStatus update(float v) {
        npc.moveTowardTarget();
        return BTStatus.BH_SUCCESS;
    }
}

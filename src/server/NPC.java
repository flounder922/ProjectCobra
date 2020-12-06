package server;

import ray.rml.Vector3;
import ray.rml.Vector3f;

import java.util.UUID;

public class NPC {

    GameAIServerUDP gameServer;

    private float locX = 1;
    private float locY = 1;
    private float locZ = 10; // other state info goes here (FSM)

    private Vector3 npcForwardAxis;
    private UUID target = null;

    NPC(GameAIServerUDP gameServer) {
        this.gameServer = gameServer;
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

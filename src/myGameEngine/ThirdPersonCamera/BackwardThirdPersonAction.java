package myGameEngine.ThirdPersonCamera;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;

public class BackwardThirdPersonAction extends AbstractInputAction {

    private Camera3PController controller;
    private SceneNode actorNode;

    public BackwardThirdPersonAction(SceneNode actorNode, Camera3PController controller) {
        this.actorNode = actorNode;
        this.controller = controller;
    }

    @Override
    public void performAction(float v, Event event) {
        Vector3 actorPosition = actorNode.getLocalPosition();

        actorNode.getPhysicsObject().applyForce(0, 0, -5, actorPosition.x(), actorPosition.y(), actorPosition.z());
        controller.updateCameraPosition();
    }
}
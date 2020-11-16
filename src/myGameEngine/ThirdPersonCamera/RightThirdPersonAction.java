package myGameEngine.ThirdPersonCamera;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;

public class RightThirdPersonAction extends AbstractInputAction {

    private Camera3PController controller;
    private SceneNode actorNode;

    public RightThirdPersonAction(SceneNode actorNode, Camera3PController controller) {
        this.actorNode = actorNode;
        this.controller = controller;
    }

    @Override
    public void performAction(float v, Event event) {
        Vector3 actorPosition = actorNode.getLocalPosition();

        actorNode.getPhysicsObject().applyForce(-5, 0, 0, actorPosition.x(), actorPosition.y(), actorPosition.z());
        controller.updateCameraPosition();
    }
}

package myGameEngine.ThirdPersonCamera;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;

public class ThirdPersonOrbitLeft extends AbstractInputAction {

    private Camera3PController controller;
    private SceneNode actorNode;

    public ThirdPersonOrbitLeft(SceneNode actorNode, Camera3PController controller) {
        this.actorNode = actorNode;
        this.controller = controller;
    }

    @Override
    public void performAction(float v, Event event) {
        controller.increaseAzimuth(-0.5f);
    }
}

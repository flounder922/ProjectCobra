package myGameEngine.ThirdPersonCamera;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;

public class ThirdPersonElevationIncrease extends AbstractInputAction {

    private Camera3PController controller;
    private SceneNode actorNode;

    public ThirdPersonElevationIncrease(SceneNode actorNode, Camera3PController controller) {
        this.actorNode = actorNode;
        this.controller = controller;
    }

    @Override
    public void performAction(float v, Event event) {
        controller.increaseElevation(0.5f);
    }
}

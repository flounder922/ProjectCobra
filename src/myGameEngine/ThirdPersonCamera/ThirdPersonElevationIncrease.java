package myGameEngine.ThirdPersonCamera;

import Cobra.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;

public class ThirdPersonElevationIncrease extends AbstractInputAction {

    private final MyGame game;
    private SceneNode actorNode;

    public ThirdPersonElevationIncrease(SceneNode actorNode, MyGame game) {
        this.actorNode = actorNode;
        this.game = game;
    }

    @Override
    public void performAction(float v, Event event) {
        game.orbitController1.increaseElevation(0.5f);
    }
}

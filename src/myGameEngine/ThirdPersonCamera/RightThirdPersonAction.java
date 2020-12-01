package myGameEngine.ThirdPersonCamera;

import Cobra.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.Engine;
import ray.rage.scene.SceneNode;


public class RightThirdPersonAction extends AbstractInputAction {

    private SceneNode actorNode;
    private MyGame game;

    public RightThirdPersonAction(SceneNode actorNode, MyGame game) {
        this.game = game;
        this.actorNode = actorNode;
    }

    @Override
    public void performAction(float v, Event event) {
        //actorNode.moveLeft(0.02f);
        float xForce = actorNode.getLocalRightAxis().x();
        float zForce = actorNode.getLocalRightAxis().z();

        actorNode.getPhysicsObject().applyForce(xForce * 5, 0, zForce * 5, 0, 0, 0);

        game.orbitController1.updateCameraPosition();
    }
}

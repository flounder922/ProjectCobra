package myGameEngine.ThirdPersonCamera;


import Cobra.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;

public class LeftThirdPersonAction extends AbstractInputAction {

    private MyGame game;
    private SceneNode actorNode;


    public LeftThirdPersonAction(SceneNode actorNode, MyGame game) {
        this.actorNode = actorNode;
        this.game = game;
    }

    @Override
    public void performAction(float v, Event event) {
        //actorNode.moveRight(0.02f);
        float xForce = actorNode.getLocalRightAxis().x();
        float zForce = actorNode.getLocalRightAxis().z();

        actorNode.getPhysicsObject().applyForce(xForce * -5, 0, zForce * -5, 0, 0, 0);

        game.orbitController1.updateCameraPosition();
    }
}

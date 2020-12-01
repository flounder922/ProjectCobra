package myGameEngine.ThirdPersonCamera;

import Cobra.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3f;

public class TurnRightThirdPersonAction extends AbstractInputAction {

    private final MyGame game;
    private SceneNode actorNode;

    public TurnRightThirdPersonAction(SceneNode dolphinNode2, MyGame game) {
        actorNode = dolphinNode2;
        this.game = game;
    }

    @Override
    public void performAction(float v, Event event) {
        Vector3f worldUp = (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f);
        Matrix3 matrixRotation = Matrix3f.createRotationFrom(Degreef.createFrom(-1.0f), worldUp);

        actorNode.setLocalRotation(matrixRotation.mult(actorNode.getWorldRotation()));

    }

}

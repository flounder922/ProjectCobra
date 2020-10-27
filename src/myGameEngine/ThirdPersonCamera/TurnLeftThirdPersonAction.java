package myGameEngine.ThirdPersonCamera;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3f;

public class TurnLeftThirdPersonAction extends AbstractInputAction {

    private SceneNode actorNode;

    public TurnLeftThirdPersonAction(SceneNode dolphinNode2) {
        actorNode = dolphinNode2;
    }

    @Override
    public void performAction(float v, Event event) {
        Vector3f worldUp = (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f);
        Matrix3 matrixRotation = Matrix3f.createRotationFrom(Degreef.createFrom(1.0f), worldUp);
        actorNode.setLocalRotation(matrixRotation.mult(actorNode.getWorldRotation()));

    }
}

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
        //actorNode.moveLeft(0.02f);
        float xForce = actorNode.getLocalRightAxis().x();
        float zForce = actorNode.getLocalRightAxis().z();

        actorNode.getPhysicsObject().applyForce(xForce * -5, 0, zForce * -5, 0, 0, 0);

        //double[] tempVariable = toDouble(actorNode.getLocalTransform().toFloatArray());
        //actorNode.getPhysicsObject().setTransform(tempVariable);

        controller.updateCameraPosition();
    }

    private double[] toDouble(float[] floatArray) {
        if (floatArray == null)
            return null;

        int n = floatArray.length;
        double[] ret = new double[n];

        for (int i = 0; i < n; i++) {
            ret[i] = (double)floatArray[i];
        }
        return ret;
    }
}

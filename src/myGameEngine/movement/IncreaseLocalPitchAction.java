package myGameEngine.movement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class IncreaseLocalPitchAction extends AbstractInputAction {

    private final Camera camera;
    private final SceneNode node;

    public IncreaseLocalPitchAction(Camera camera, SceneNode node) {
        this.camera = camera;
        this.node = node;
    }

    @Override
    public void performAction(float time, Event event) {

        Angle rotationAmount = Degreef.createFrom(1.0f);

        if (camera.getMode() == 'c') {
            Vector3f u = camera.getRt(); // Get the sideways vector
            Vector3f v = camera.getUp(); // Gets the up vector
            Vector3f n = camera.getFd(); // Gets the forward vector


            Vector3 fv = (v.rotate(rotationAmount, u)).normalize();
            Vector3 fn = (n.rotate(rotationAmount, u)).normalize();

            camera.setUp((Vector3f) fv);
            camera.setFd((Vector3f) fn);

        } else if (camera.getMode() == 'n') {

            node.pitch(Degreef.createFrom(-1.0f));

        } else {
            System.out.println("I did something wrong in Increase Local Pitch!");
        }
    }
}

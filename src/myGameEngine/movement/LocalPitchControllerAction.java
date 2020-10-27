package myGameEngine.movement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class LocalPitchControllerAction extends AbstractInputAction {

    private final Camera camera;
    private final SceneNode node;

    public LocalPitchControllerAction(Camera camera, SceneNode node) {
        this.camera = camera;
        this.node = node;
    }

    @Override
    public void performAction(float time, Event event) {

        Angle rotationAmount = Degreef.createFrom(0.0f);

        if (camera.getMode() == 'c') {

            if (event.getValue() <= -0.27f) {
                rotationAmount = Degreef.createFrom(1.0f);
            }

            if (event.getValue() >= 0.5f) {
                rotationAmount = Degreef.createFrom(-1.0f);
            }

            Vector3f u = camera.getRt(); // Get the sideways vector
            Vector3f v = camera.getUp(); // Gets the up vector
            Vector3f n = camera.getFd(); // Gets the forward vector


            Vector3 fv = (v.rotate(rotationAmount, u)).normalize();
            Vector3 fn = (n.rotate(rotationAmount, u)).normalize();

            camera.setUp((Vector3f) fv);
            camera.setFd((Vector3f) fn);

        } else if (camera.getMode() == 'n') {

            if (event.getValue() <= -0.27f) {
                node.pitch(Degreef.createFrom(-1.0f));
            }

            if (event.getValue() >= 0.5f) {
                node.pitch(Degreef.createFrom(1.0f));
            }

        } else {
            System.out.println("I did something wrong in Increase Local Pitch!");
        }
    }
}

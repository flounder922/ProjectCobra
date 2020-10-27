package myGameEngine.movement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3f;

public class MoveXAxisJoystickAction extends AbstractInputAction {

    private final Camera camera;
    private final SceneNode node;

    public MoveXAxisJoystickAction(Camera camera, SceneNode node) {
        this.camera = camera;
        this.node = node;
    }

    @Override
    public void performAction(float time, Event event) {

        if (camera.getMode() == 'c') {
            Vector3f p = camera.getPo(); // Gets the current position and location of the camera.
            Vector3f v = camera.getRt(); // Gets the current sideways vector of the camera and stores it in v.

            // Creates a vector using the forward vector and multiplies each axis by 0.01.
            Vector3f p1;
            Vector3f p2;

            // Going left
            if (event.getValue() <= -0.5f) {
                p1 = (Vector3f)Vector3f.createFrom(0.01f*v.x(), 0.01f*v.y(), 0.01f*v.z());
                p2 = (Vector3f) p.sub(p1); // Move left
                camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
            }

            // Going right
            if (event.getValue() >= 0.5f) {
                p1 = (Vector3f)Vector3f.createFrom(0.01f*v.x(), 0.01f*v.y(), 0.01f*v.z());
                p2 = (Vector3f) p.add(p1); // Move right
                camera.setPo((Vector3f)Vector3f.createFrom(p2.x(), p2.y(), p2.z()));
            }


        } else if (camera.getMode() == 'n') {

            // Going left
            if (event.getValue() <= -0.5f) {
                node.moveRight(0.03f);
            }

            // Going right
            if (event.getValue() >= 0.5f) {
                node.moveLeft(0.03f);
            }


        } else {
            System.out.println("I Did something wrong in Move Forward Action");
        }
    }
}

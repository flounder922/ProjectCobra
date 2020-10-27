package myGameEngine.movement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.*;

public class GlobalYawControllerAction extends AbstractInputAction {

    private final Camera camera;
    private final SceneNode node;

    public GlobalYawControllerAction(Camera camera, SceneNode node) {
        this.camera = camera;
        this.node = node;
    }

    @Override
    public void performAction(float time, Event event) {

        Vector3f world = (Vector3f)Vector3f.createFrom(0, 1, 0);
        Angle rotationAmount = Degreef.createFrom(0.0f);

        if (camera.getMode() == 'c') {
            Vector3f v = camera.getUp();
            Vector3f u = camera.getRt();
            Vector3f n = camera.getFd();

            if (event.getValue() <= -0.5f) {
                rotationAmount = Degreef.createFrom(1.0f);
            }

            if (event.getValue() >= 0.27f) {
                rotationAmount = Degreef.createFrom(-1.0f);
            }

            Vector3 fu = u.rotate(rotationAmount, world).normalize();
            Vector3 fv = v.rotate(rotationAmount, world).normalize();
            Vector3 fn = n.rotate(rotationAmount, world).normalize();

            camera.setUp((Vector3f) fv);
            camera.setRt((Vector3f) fu);
            camera.setFd((Vector3f) fn);

        } else if (camera.getMode() == 'n') {

            if (event.getValue() <= -0.5f) {
                rotationAmount = Degreef.createFrom(1.0f);
            }

            if (event.getValue() >= 0.27f) {
                rotationAmount = Degreef.createFrom(-1.0f);
            }

            Vector3f worldUp = (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f);
            Matrix3 matrixRotation = Matrix3f.createRotationFrom(rotationAmount, worldUp);
            node.setLocalRotation(matrixRotation.mult(node.getWorldRotation()));

        } else {
            System.out.println("I did something wrong in Decrease Global Yaw!");
        }
    }
}

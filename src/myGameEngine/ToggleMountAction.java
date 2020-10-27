package myGameEngine;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.Node;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3f;

public class ToggleMountAction extends AbstractInputAction {

    private final Camera camera;
    private final Node node;

    public ToggleMountAction(Camera camera, SceneNode node) {
        this.camera = camera;
        this.node = node;
    }

    @Override
    public void performAction(float v, Event event) {

        char cameraMode = camera.getMode();

        if (cameraMode == 'n') {

            Vector3f nodePosition = (Vector3f) node.getWorldPosition();
            camera.setPo((Vector3f) Vector3f.createFrom(nodePosition.x(), nodePosition.y(), nodePosition.z()));
            Vector3f vector = camera.getRt();
            Vector3f vector2 = (Vector3f) Vector3f.createFrom(0.3f*vector.x(), 0.01f*vector.y(), 0.01f*vector.z());
            Vector3f vector3 = (Vector3f)camera.getPo().add(vector2);
            camera.setPo((Vector3f)Vector3f.createFrom(vector3.x(), vector3.y(), vector3.z()));

/*
            Vector3f nodePosition = (Vector3f) node.getWorldPosition();
            Vector3f nodeForwardAxis = (Vector3f) node.getLocalForwardAxis();
            Vector3f nodeRightAxis = (Vector3f) node.getLocalRightAxis();
            Vector3f nodeUpAxis = (Vector3f) node.getLocalUpAxis();
            camera.setPo((Vector3f) Vector3f.createFrom(nodePosition.x() , nodePosition.y(), nodePosition.z()));
            camera.setFd((Vector3f) Vector3f.createFrom(nodeForwardAxis.x(), nodeForwardAxis.y(), nodeForwardAxis.z()));
            camera.setRt((Vector3f) Vector3f.createFrom(nodeRightAxis.x(), nodeRightAxis.y(), nodeRightAxis.z()));
            camera.setUp((Vector3f) Vector3f.createFrom(nodeUpAxis.x(), nodeUpAxis.y(), nodeUpAxis.z()));
 */
            camera.setMode('c');

        } else if (cameraMode == 'c') {
            camera.setMode('n');
        }
    }
}

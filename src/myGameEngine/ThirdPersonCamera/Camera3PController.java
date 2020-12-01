package myGameEngine.ThirdPersonCamera;

import net.java.games.input.Component;
import net.java.games.input.Event;
import ray.input.InputManager;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Camera3PController {

    private final SceneNode cameraNode;

    // The target that the camera is looking at.
    private SceneNode target;

    // Camera position information relative to target.
    private float cameraAzimuth;
    private float cameraElevation;
    private float radias;

    // The targets position in the world and up in the world.
    private Vector3 targetPosition;
    private final Vector3 worldUpVector;

    public Camera3PController(SceneNode cameraNode, SceneNode target) {

        // Camera and camera node that are being controlled.
        this.cameraNode = cameraNode;
        this.target = target;

        cameraAzimuth = 225.0f;
        cameraElevation = 20f;
        radias = 2.0f;

        worldUpVector = Vector3f.createFrom(0.0f, 1.0f, 0.0f);

        updateCameraPosition();

    }

    public void updateCameraPosition() {

        double theta = Math.toRadians(cameraAzimuth); // rotation around target
        double phi = Math.toRadians(cameraElevation); // altitude angle

        // Calculate the x,y,z to create a vector to be used for update the cameras position.
        double x = radias * Math.cos(phi) * Math.sin(theta);
        double y = radias * Math.sin(phi);
        double z = radias * Math.cos(phi) * Math.cos(theta);

        cameraNode.setLocalPosition(Vector3f.createFrom((float)x, (float)y, (float)z).add(target.getWorldPosition()));

        cameraNode.lookAt(target, worldUpVector);
    }

    public void setupInput(InputManager inputManager, String controllerName) {

        Action orbitAroundAction = new OrbitAroundAction();
        Action orbitElevationAction = new OrbitElevationAction();
        Action orbitRadiasAction = new OrbitRadiasAction();

        inputManager.associateAction(controllerName, Component.Identifier.Axis.RX, orbitAroundAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        inputManager.associateAction(controllerName, Component.Identifier.Axis.RY, orbitElevationAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        inputManager.associateAction(controllerName, Component.Identifier.Axis.Y, orbitRadiasAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    public void setupInputKeyboard(InputManager inputManager, String controllerName) {
        Action orbitAroundLeftAction = new OrbitAroundLeftAction();
        Action orbitAroundRightAction = new OrbitAroundRightAction();
        Action orbitElevationIncreaseAction = new OrbitElevationIncreaseAction();
        Action orbitElevationDecreaseAction = new OrbitElevationDecreaseAction();
        Action orbitRadiasIncreaseAction = new OrbitRadiasIncreaseAction();
        Action orbitRadiasDecreaseAction = new OrbitRadiasDecreaseAction();


        inputManager.associateAction(controllerName, Component.Identifier.Key.LEFT, orbitAroundLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        inputManager.associateAction(controllerName, Component.Identifier.Key.RIGHT, orbitAroundRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        inputManager.associateAction(controllerName, Component.Identifier.Key.UP, orbitElevationIncreaseAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        inputManager.associateAction(controllerName, Component.Identifier.Key.DOWN, orbitElevationDecreaseAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        inputManager.associateAction(controllerName, Component.Identifier.Key.E, orbitRadiasIncreaseAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        inputManager.associateAction(controllerName, Component.Identifier.Key.Q, orbitRadiasDecreaseAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

    }

    public void increaseElevation(float v) {
        cameraElevation += v;

        if(cameraElevation < 0)
            cameraElevation = 0;
        else if(cameraElevation > 89)
            cameraElevation = 89;
        cameraElevation = cameraElevation % 360;
        updateCameraPosition();
    }

    public void increaseAzimuth(float v) {
        cameraAzimuth += v;

        cameraAzimuth = cameraAzimuth % 360;
        updateCameraPosition();
    }

    public void increaseRadias(double v) {
        radias += v;

        if(radias > 5)
            radias = 5;
        else if (radias < 1)
            radias = 1;
        //radias = radias % 10;
        updateCameraPosition();
    }

    public float getAzimuth() {
        return cameraAzimuth;
    }

    private class OrbitAroundAction extends AbstractInputAction {
        @Override
        public void performAction(float v, Event event) {

            float rotationAmount;

            if (event.getValue() < -0.2f)
                rotationAmount = -0.5f;
            else if (event.getValue() > 0.2f)
                rotationAmount = 0.5f;
            else
                rotationAmount = 0.0f;

            cameraAzimuth += rotationAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class OrbitRadiasAction extends AbstractInputAction {
        @Override
        public void performAction(float v, Event event) {

            float rotationAmount;

            if (event.getValue() < -0.2f)
                rotationAmount = -0.08f;
            else if (event.getValue() > 0.2f)
                rotationAmount = 0.08f;
            else
                rotationAmount = 0.0f;

            radias += rotationAmount;
            if(radias > 5)
                radias = 5;
            else if (radias < 1)
                radias = 1;
            //radias = radias % 10;
            updateCameraPosition();
        }
    }

    private class OrbitElevationAction extends AbstractInputAction {
        @Override
        public void performAction(float v, Event event) {

            float rotationAmount;

            if (event.getValue() < -0.2f)
                rotationAmount = 0.5f;
            else if (event.getValue() > 0.2f)
                rotationAmount = -0.5f;
            else
                rotationAmount = 0.0f;

            cameraElevation += rotationAmount;
            if(cameraElevation < 0)
                cameraElevation = 0;
            else if(cameraElevation > 89)
                cameraElevation = 89;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        }
    }

    private class OrbitAroundLeftAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            float rotationAmount;

            if (event.getValue() == 1)
                rotationAmount = 0.5f;
            else
                rotationAmount = 0.0f;

            cameraAzimuth += rotationAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class OrbitAroundRightAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            float rotationAmount;

            if (event.getValue() == 1)
                rotationAmount = -0.5f;
            else
                rotationAmount = 0.0f;

            cameraAzimuth += rotationAmount;
            cameraAzimuth = cameraAzimuth % 360;
            updateCameraPosition();
        }
    }

    private class OrbitRadiasIncreaseAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            float rotationAmount;

            if (event.getValue() == 1)
                rotationAmount = 0.08f;
            else
                rotationAmount = 0.0f;

            radias += rotationAmount;
            if(radias > 5)
                radias = 5;
            else if (radias < 1)
                radias = 1;
            //radias = radias % 10;
            updateCameraPosition();
        }
    }

    private class OrbitRadiasDecreaseAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            float rotationAmount;

            if (event.getValue() == 1)
                rotationAmount = -0.08f;
            else
                rotationAmount = 0.0f;

            radias += rotationAmount;
            if(radias > 5)
                radias = 5;
            else if (radias < 1)
                radias = 1;
            //radias = radias % 10;
            updateCameraPosition();
        }
    }

    private class OrbitElevationIncreaseAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            float rotationAmount;

            if (event.getValue() == 1.0f)
                rotationAmount = 0.5f;
            else
                rotationAmount = 0.0f;

            cameraElevation += rotationAmount;
            if(cameraElevation < 0)
                cameraElevation = 0;
            else if(cameraElevation > 180)
                cameraElevation = 180;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        }
    }

    private class OrbitElevationDecreaseAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            float rotationAmount = -0.5f;

            cameraElevation += rotationAmount;
            if(cameraElevation < 0)
                cameraElevation = 0;
            else if(cameraElevation > 180)
                cameraElevation = 180;
            cameraElevation = cameraElevation % 360;
            updateCameraPosition();
        }
    }
}

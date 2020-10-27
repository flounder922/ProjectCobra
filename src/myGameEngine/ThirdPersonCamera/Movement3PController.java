package myGameEngine.ThirdPersonCamera;

import net.java.games.input.Component;
import net.java.games.input.Event;
import ray.input.InputManager;
import ray.input.action.AbstractInputAction;
import ray.input.action.Action;
import ray.rage.scene.SceneNode;
import ray.rml.*;

public class Movement3PController {

    private final SceneNode mainActorNode;

    private Vector3 mainActorPosition;


    public Movement3PController(SceneNode mainActorNode) {

        this.mainActorNode = mainActorNode;

    }

    public void setupInput(InputManager inputManager, String controllerName) {
        Action moveForwardAction = new MoveForwardAction();
        Action moveBackwardAction = new MoveBackwardAction();
        Action moveLeftAction = new MoveLeftAction();
        Action moveRightAction = new MoveRightAction();
        Action turnLeftAction = new TurnLeftAction();
        Action turnRightAction = new TurnRightAction();

        inputManager.associateAction(controllerName, Component.Identifier.Button._3, moveForwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        inputManager.associateAction(controllerName, Component.Identifier.Button._0, moveBackwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        inputManager.associateAction(controllerName, Component.Identifier.Button._1, moveLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        inputManager.associateAction(controllerName, Component.Identifier.Button._2, moveRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        inputManager.associateAction(controllerName, Component.Identifier.Button._4, turnLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        inputManager.associateAction(controllerName, Component.Identifier.Button._5, turnRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

        
    }

    private class MoveForwardAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            mainActorNode.moveForward(0.05f);
        }
    }

    private class MoveBackwardAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            mainActorNode.moveBackward(0.05f);
        }
    }

    private class MoveLeftAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            mainActorNode.moveLeft(0.05f);
        }
    }

    private class MoveRightAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            mainActorNode.moveRight(0.05f);
        }
    }

    private class TurnLeftAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            Vector3f worldUp = (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f);
            Matrix3 matrixRotation = Matrix3f.createRotationFrom(Degreef.createFrom(1.0f), worldUp);
            mainActorNode.setLocalRotation(matrixRotation.mult(mainActorNode.getWorldRotation()));
        }
    }

    private class TurnRightAction extends AbstractInputAction {

        @Override
        public void performAction(float v, Event event) {
            Vector3f worldUp = (Vector3f) Vector3f.createFrom(0.0f, 1.0f, 0.0f);
            Matrix3 matrixRotation = Matrix3f.createRotationFrom(Degreef.createFrom(-1.0f), worldUp);
            mainActorNode.setLocalRotation(matrixRotation.mult(mainActorNode.getWorldRotation()));
        }
    }
}

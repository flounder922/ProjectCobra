package Cobra;

import myGameEngine.ThirdPersonCamera.*;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.rage.Engine;
import ray.rage.asset.texture.Texture;
import ray.rage.asset.texture.TextureManager;
import ray.rage.game.Game;
import ray.rage.game.VariableFrameRateGame;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.FrontFaceState;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.*;
import ray.rage.scene.controllers.RotationController;
import ray.rage.util.BufferUtil;
import ray.rage.util.Configuration;
import ray.rml.Radianf;
import ray.rml.Vector3;
import ray.rml.Vector3f;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class MyGame extends VariableFrameRateGame {

    private static final String SKYBOX_NAME = "SkyBox";
    private boolean skyBoxVisible = true;

    private GL4RenderSystem renderSystem; // render system for the game, is defined each update.
    private float elapsedTime = 0.0f;  // Used to keep track of how long the game has been running.
    private String displayStringPlayerOne; // Used to display the information on the HUD.


//    private boolean earthVisited = false;
//    private boolean redVisited = false;
//    private boolean moonVisited = false;


    private InputManager inputManager;
    private Camera3PController orbitController1;
    private Movement3PController movement3PController1;
    private RotationController solarSystemNodeController;


    public MyGame() {
        super();
    }

    public static void main(String[] args) {
        Game game = new MyGame();
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }

    @Override
    protected void update(Engine engine) {
        renderSystem = (GL4RenderSystem) engine.getRenderSystem();
        elapsedTime += engine.getElapsedTimeMillis();

        displayStringPlayerOne = "Player HUD";

        // Auto adjusting HUD
        renderSystem.setHUD(displayStringPlayerOne, renderSystem.getRenderWindow().getViewport(0).getActualLeft(), renderSystem.getRenderWindow().getViewport(0).getActualBottom() + 5);

        inputManager.update(elapsedTime);
    }

    @Override
    protected void setupCameras(SceneManager sceneManager, RenderWindow renderWindow) {
        // Creates the first players camera
        Camera camera = sceneManager.createCamera("MainCamera", Camera.Frustum.Projection.PERSPECTIVE);
        // Adds the first players camera to the top viewport.
        renderWindow.getViewport(0).setCamera(camera);

        camera.setMode('n');
        // Creates a new child node off of the root node.
        SceneNode cameraNode = sceneManager.getRootSceneNode().createChildSceneNode(camera.getName() + "Node");
        // Attaches the camera object to the camera node.
        cameraNode.attachObject(camera);
    }

    @Override
    protected void setupScene(Engine engine, SceneManager sceneManager) throws IOException {

        Configuration configuration = engine.getConfiguration();
        TextureManager textureManager = engine.getTextureManager();

        // Set up all six sides of the skybox with assets in the skybox folder
        textureManager.setBaseDirectoryPath(configuration.valueOf("assets.skyboxes.path"));
        Texture front = textureManager.getAssetByPath("front.jpeg");
        Texture back = textureManager.getAssetByPath("back.jpeg");
        Texture left = textureManager.getAssetByPath("left.jpeg");
        Texture right = textureManager.getAssetByPath("right.jpeg");
        Texture top = textureManager.getAssetByPath("top.jpeg");
        Texture bottom = textureManager.getAssetByPath("bottom.jpeg");

        // changing texture manager back to the texture path
        textureManager.setBaseDirectoryPath(configuration.valueOf("assets.textures.path"));

        // Creating the needed transform so the textures appear correctly in the skybox
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(0, front.getImage().getHeight());
        affineTransform.scale(1d, -1d);

        // Applying the transform created just before this
        front.transform(affineTransform);
        back.transform(affineTransform);
        left.transform(affineTransform);
        right.transform(affineTransform);
        top.transform(affineTransform);
        bottom.transform(affineTransform);

        // Adding all the textures to the skybox
        SkyBox skyBox = sceneManager.createSkyBox(SKYBOX_NAME);
        skyBox.setTexture(front, SkyBox.Face.FRONT);
        skyBox.setTexture(back, SkyBox.Face.BACK);
        skyBox.setTexture(left, SkyBox.Face.LEFT);
        skyBox.setTexture(right, SkyBox.Face.RIGHT);
        skyBox.setTexture(top, SkyBox.Face.TOP);
        skyBox.setTexture(bottom, SkyBox.Face.BOTTOM);
        sceneManager.setActiveSkyBox(skyBox);


        //Creates the dolphin and sets the render. Followed by the node creation and placement of the node in the world.
        // The entity is then attached to the node.
        SceneNode dolphinNode = createSceneNode(sceneManager,
                "DolphinNode", "dolphinHighPoly.obj", Vector3f.createFrom(-1.0f, 0.31f, 0.0f));


        setupOrbitCamera(engine, sceneManager);
        setupMovement(sceneManager);

        // Creates planet 1 and sets the render. Followed by the node creation and placement of the node in the world.
        // The entity is then attached to the node.
        SceneNode earthNode = createSceneNode(sceneManager,
                "EarthNode", "earth.obj", Vector3f.createFrom(-50.0f, 3.0f, -8.0f));
        earthNode.setLocalScale(1.5f, 1.5f, 1.5f);

        // Creates planet 2 and sets the render. Followed by the node creation and placement of the node in the world.
        // The entity is then attached to the node.
        SceneNode moonNode = createSceneNode(sceneManager,
                "MoonNode", "planet2.obj", Vector3f.createFrom(25.0f, 1.0f, 14.0f));
        moonNode.scale(1.0f, 1.0f, 1.0f);

        // Creates planet 3 and sets the render. Followed by the node creation and placement of the node in the world.
        // The entity is then attached to the node.
        SceneNode redPlanetNode = createSceneNode(sceneManager,
                "RedPlanetNode","planet3.obj", Vector3f.createFrom(3.0f, 2.0f, -30.0f));
        redPlanetNode.scale(2.0f, 2.0f, 2.0f);

        SceneNode solarSystemNode = sceneManager.getRootSceneNode().createChildSceneNode("SolarSystemNode");
        solarSystemNode.attachChild(earthNode);
        solarSystemNode.attachChild(redPlanetNode);
        solarSystemNode.attachChild(moonNode);

        // Create a pyramid ship manual object
        ManualObject pyramidShip = pyramidShip(engine, sceneManager);
        SceneNode pyramidShipNode = sceneManager.getRootSceneNode().createChildSceneNode("PyramidShipNode");
        pyramidShipNode.attachObject(pyramidShip);
        pyramidShipNode.setLocalPosition(5.0f, 7.0f, 8.0f);

        // Create a floor manual object, move it into place, and scale it to size
        ManualObject floor = floor(engine, sceneManager);
        SceneNode floorNode = sceneManager.getRootSceneNode().createChildSceneNode("floorNode");
        floorNode.attachObject(floor);
        floorNode.roll(Radianf.createFrom((float) Math.toRadians(180)));
        floorNode.scale(100.0f, 100.0f, 100.0f);

        // Create the second floor manual object, move it into place, and scale it to size.
        /*
        ManualObject floor2 = floor2(engine, sceneManager);
        SceneNode floor2Node = sceneManager.getRootSceneNode().createChildSceneNode("floor2Node");
        floor2Node.attachObject(floor2);
        floor2Node.roll(Radianf.createFrom((float) Math.toRadians(180)));
        floor2Node.rotate(Radianf.createFrom((float) Math.toRadians(180)), Vector3f.createFrom(0.0f,1.0f,0.0f));
        floor2Node.scale(100.0f, 100.0f, 100.0f);

         */

        // Gets the ambient light and sets its intensity for the scene.
        sceneManager.getAmbientLight().setIntensity(new Color(0.2f, 0.2f, 0.2f));

        // Create a spot light
        Light positionalLight = sceneManager.createLight("PositionalLight", Light.Type.SPOT);
        positionalLight.setAmbient(new Color(0.1f, 0.1f, 0.1f));
        positionalLight.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
        positionalLight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        positionalLight.setRange(10f);

        // Create the node for the light and attaches it to the dolphin node as a child.
        SceneNode positionalLightNode = sceneManager.getRootSceneNode().createChildSceneNode(positionalLight.getName() + "Node");
        positionalLightNode.attachObject(positionalLight);
        dolphinNode.attachChild(positionalLightNode);

        setupInputs(sceneManager); // Setup the inputs

        solarSystemNodeController = new RotationController(Vector3f.createUnitVectorY(), 0.000006f);

        sceneManager.addController(solarSystemNodeController);
    }

    @Override
    protected void setupWindow(RenderSystem renderSystem, GraphicsEnvironment graphicsEnvironment) {
        // Defines the window size of the game and make it so it is NOT in exclusive fullscreen.
        renderSystem.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
    }

    protected void setupOrbitCamera(Engine engine, SceneManager sceneManager) {
        SceneNode dolphinNode = sceneManager.getSceneNode("DolphinNode");
        SceneNode cameraNode = sceneManager.getSceneNode("MainCameraNode");
        orbitController1 = new Camera3PController(cameraNode, dolphinNode);
    }

    protected void setupMovement(SceneManager sceneManager) {
        SceneNode dolphinNode = sceneManager.getSceneNode("DolphinNode");
        movement3PController1 = new Movement3PController(dolphinNode);
    }

    protected void setupInputs(SceneManager sceneManager) {
        inputManager = new GenericInputManager();
        ArrayList controllers = inputManager.getControllers();

        Action moveForwardW = new ForwardThirdPersonAction(sceneManager.getSceneNode("DolphinNode"), orbitController1);
        Action moveBackwardS = new BackwardThirdPersonAction(sceneManager.getSceneNode("DolphinNode"), orbitController1);
        Action moveLeftA = new LeftThirdPersonAction(sceneManager.getSceneNode("DolphinNode"), orbitController1);
        Action moveRightD = new RightThirdPersonAction(sceneManager.getSceneNode("DolphinNode"), orbitController1);

        Action increaseElevation = new ThirdPersonElevationIncrease(sceneManager.getSceneNode("DolphinNode"), orbitController1);
        Action decreaseElevation = new ThirdPersonElevationDecrease(sceneManager.getSceneNode("DolphinNode"), orbitController1);

        Action orbitLeft = new ThirdPersonOrbitLeft(sceneManager.getSceneNode("DolphinNode"), orbitController1);
        Action orbitRight = new ThirdPersonOrbitRight(sceneManager.getSceneNode("DolphinNode"), orbitController1);

        Action radiasIncrease = new ThirdPersonRadiasIncrease(sceneManager.getSceneNode("DolphinNode"), orbitController1);
        Action radiasDecrease = new ThirdPersonRadiasDecrease(sceneManager.getSceneNode("DolphinNode"), orbitController1);

        Action turnLeft = new TurnLeftThirdPersonAction(sceneManager.getSceneNode("DolphinNode"));
        Action turnRight = new TurnRightThirdPersonAction(sceneManager.getSceneNode("DolphinNode"));



        for (Object controller : controllers) {
            Controller c = (Controller) controller;

            if (c.getType() == Controller.Type.KEYBOARD) {
                // Avatar movements
                inputManager.associateAction(c, Component.Identifier.Key.W, moveForwardW, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                inputManager.associateAction(c, Component.Identifier.Key.S, moveBackwardS, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                inputManager.associateAction(c, Component.Identifier.Key.A, moveLeftA, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                inputManager.associateAction(c, Component.Identifier.Key.D, moveRightD, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                // Turn the dolphin
                inputManager.associateAction(c, Component.Identifier.Key.Q, turnLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                inputManager.associateAction(c, Component.Identifier.Key.E, turnRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);


                // Camera Movements
                inputManager.associateAction(c, Component.Identifier.Key.UP, increaseElevation, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                inputManager.associateAction(c, Component.Identifier.Key.DOWN, decreaseElevation, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                inputManager.associateAction(c, Component.Identifier.Key.LEFT, orbitLeft, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                inputManager.associateAction(c, Component.Identifier.Key.RIGHT, orbitRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

                inputManager.associateAction(c, Component.Identifier.Key.Z, radiasIncrease, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                inputManager.associateAction(c, Component.Identifier.Key.X, radiasDecrease, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

            } else if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                orbitController1.setupInput(inputManager, c.getName());
                movement3PController1.setupInput(inputManager, c.getName());
            }
        }
    }

    protected SceneNode createSceneNode(SceneManager sceneManager, String nameOfNode,
                                        String nameOfOBJFile, Vector3 spawnLocation) throws IOException {

        Entity entity = sceneManager.createEntity(nameOfNode, nameOfOBJFile);
        entity.setPrimitive(Renderable.Primitive.TRIANGLES);

        SceneNode sceneNode = sceneManager.getRootSceneNode().createChildSceneNode(nameOfNode);
        sceneNode.attachObject(entity);
        sceneNode.setLocalPosition(spawnLocation);

        return sceneNode;
    }

    protected ManualObject pyramidShip(Engine engine, SceneManager sceneManager) throws IOException {

        ManualObject pyramidShip = sceneManager.createManualObject("PyramidShip");
        ManualObjectSection pyramidShipSelection = pyramidShip.createManualSection("PyramidShipSection");

        pyramidShip.setGpuShaderProgram(
                sceneManager.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices = new float[] {
                -1.0f,0.0f,1.0f,    2.0f,0.0f,0.0f,     0.0f,0.5f,0.0f,     // Left
                2.0f,0.0f,0.0f,     -1.0f,0.0f,-1.0f,    0.0f,0.5f,0.0f,    // Right
                -1.0f,0.0f,-1.0f,   -1.0f,0.0f,1.0f,    0.0f,0.5f,0.0f,     // Back
                -1.0f,0.0f,-1.0f,   2.0f,0.0f,0.0f,     -1.0f,0.0f,1.0f,    // Bottom
        };

        float[] textureCoordinates = new float[] {
                1.0f,1.0f,  0.0f,0.0f,  1.0f, 0.0f, // Left
                1.0f,1.0f,  0.0f,0.0f,  1.0f, 0.0f, // Right
                0.0f,0.0f,  1.0f,0.0f,  0.5f,1.0f,  // Back
                0.0f,0.0f,  1.0f,1.0f,  0.0f,1.0f,  // Bottom
        };

        float[] normals = new float[] {
                0.0f,1.0f,1.0f,     0.0f,1.0f,1.0f,     0.0f,1.0f,1.0f,     // Left
                1.0f, 1.0f,0.0f,    1.0f,1.0f,0.0f,     1.0f,1.0f,0.0f,     // Right
                -1.0f,1.0f,0.0f,    -1.0f,1.0f,0.0f,    -1.0f,1.0f,0.0f,    // Back
                0.0f,-1.0f,0.0f,    0.0f,-1.0f,0.0f,    0.0f,-1.0f,0.0f,    // Bottom
        };

        int[] indices = new int[] {0,1,2,3,4,5,6,7,8,9,10,11};

        FloatBuffer verticesBuffer = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer textureBuffer = BufferUtil.directFloatBuffer(textureCoordinates);
        FloatBuffer normalsBuffer = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuffer = BufferUtil.directIntBuffer(indices);

        pyramidShipSelection.setVertexBuffer(verticesBuffer);
        pyramidShipSelection.setTextureCoordsBuffer(textureBuffer);
        pyramidShipSelection.setNormalsBuffer(normalsBuffer);
        pyramidShipSelection.setIndexBuffer(indexBuffer);

        Texture texture = engine.getTextureManager().getAssetByPath("hexagons.jpeg");
        TextureState textureState = (TextureState)
                sceneManager.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);

        textureState.setTexture(texture);

        FrontFaceState faceState =  (FrontFaceState)
                sceneManager.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);

        pyramidShip.setDataSource(Renderable.DataSource.INDEX_BUFFER);
        pyramidShip.setRenderState(textureState);
        pyramidShip.setRenderState(faceState);

        return pyramidShip;
    }

    protected ManualObject floor(Engine engine, SceneManager sceneManager) throws IOException {

        ManualObject floor = sceneManager.createManualObject("floor");
        ManualObjectSection floorSection  = floor.createManualSection("floorSection");

        floor.setGpuShaderProgram(sceneManager.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices = new float[] {
                0.0f,0.0f,0.0f,     1.0f,0.0f,0.0f,     1.0f,0.0f,1.0f,     0.0f,0.0f,1.0f
        };

        float [] textureCoordinates = new float[] {
                0.0f,0.0f,      0.5f,1.0f,      1.0f,0.0f,
                0.0f,0.0f,      0.5f,1.0f,      1.0f,0.0f
        };

        float[] normals = new float[] {
                0.0f,1.0f,0.0f,    0.0f,1.0f,0.0f,    0.0f,1.0f,0.0f
        };

        int[] indices = new int[] {0,1,2,0,2,3};

        FloatBuffer verticesBuffer = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer textureBuffer = BufferUtil.directFloatBuffer(textureCoordinates);
        FloatBuffer normalsBuffer = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuffer = BufferUtil.directIntBuffer(indices);

        floorSection.setVertexBuffer(verticesBuffer);
        floorSection.setTextureCoordsBuffer(textureBuffer);
        floorSection.setNormalsBuffer(normalsBuffer);
        floorSection.setIndexBuffer(indexBuffer);

        Texture texture = engine.getTextureManager().getAssetByPath("blue.jpeg");
        TextureState textureState = (TextureState)
                sceneManager.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);

        textureState.setTexture(texture);

        FrontFaceState faceState =  (FrontFaceState)
                sceneManager.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);

        floor.setDataSource(Renderable.DataSource.INDEX_BUFFER);
        floor.setRenderState(textureState);
        floor.setRenderState(faceState);

        return floor;
    }
/*
    protected ManualObject floor2(Engine engine, SceneManager sceneManager) throws IOException {

        ManualObject floor2 = sceneManager.createManualObject("floor2");
        ManualObjectSection floorSection  = floor2.createManualSection("floor2Section");

        floor2.setGpuShaderProgram(sceneManager.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float[] vertices = new float[] {
                1.0f,0.0f,1.0f,     -1.0f,0.0f,-1.0f,   1.0f,0.0f,-1.0f

        };

        float [] textureCoordinates = new float[] {
                0.0f,0.0f,      0.5f,1.0f,      1.0f,0.0f
        };

        float[] normals = new float[] {
                0.0f,1.0f,0.0f,    0.0f,1.0f,0.0f,    0.0f,1.0f,0.0f
        };

        int[] indices = new int[] {0,1,2};

        FloatBuffer verticesBuffer = BufferUtil.directFloatBuffer(vertices);
        FloatBuffer textureBuffer = BufferUtil.directFloatBuffer(textureCoordinates);
        FloatBuffer normalsBuffer = BufferUtil.directFloatBuffer(normals);
        IntBuffer indexBuffer = BufferUtil.directIntBuffer(indices);

        floorSection.setVertexBuffer(verticesBuffer);
        floorSection.setTextureCoordsBuffer(textureBuffer);
        floorSection.setNormalsBuffer(normalsBuffer);
        floorSection.setIndexBuffer(indexBuffer);

        Texture texture = engine.getTextureManager().getAssetByPath("blue.jpeg");
        TextureState textureState = (TextureState)
                sceneManager.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);

        textureState.setTexture(texture);

        FrontFaceState faceState =  (FrontFaceState)
                sceneManager.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);

        floor2.setDataSource(Renderable.DataSource.INDEX_BUFFER);
        floor2.setRenderState(textureState);
        floor2.setRenderState(faceState);

        return floor2;
    }

 */
}


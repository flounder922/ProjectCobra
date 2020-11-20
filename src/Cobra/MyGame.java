package Cobra;

import myGameEngine.ServerDisconnectAction;
import myGameEngine.ThirdPersonCamera.*;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.networking.IGameConnection;
import ray.physics.PhysicsEngine;
import ray.physics.PhysicsEngineFactory;
import ray.physics.PhysicsObject;
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
import ray.rage.util.BufferUtil;
import ray.rage.util.Configuration;
import ray.rml.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

public class MyGame extends VariableFrameRateGame {

    // Render system and HUD variables
    private GL4RenderSystem renderSystem; // render system for the game, is defined each update.
    private float elapsedTime = 0.0f;  // Used to keep track of how long the game has been running.
    private String displayStringPlayerOne; // Used to display the information on the HUD.

    // Server connection variables
    private String serverAddress;
    private int serverPort;
    private IGameConnection.ProtocolType serverProtocol;
    private ProtocolClient protocolClient;
    private boolean isClientConnected;
    private Vector<UUID> gameObjectsToRemove;

    // Input manager variable
    private InputManager inputManager;

    // Variables for camera controller and movement of player avatar
    private Camera3PController orbitController1;
    private Movement3PController movement3PController1;

    // Skybox variables
    private static final String SKYBOX_NAME = "SkyBox";

    public final String PLAYER_AVATAR = "PlayerAvatarNode";

    // Physics related variables
    private PhysicsEngine physicsEngine;
    private PhysicsObject playerAvatarPhysicsObject, groundPlanePhysicsObject;


    public MyGame(String serverAddress, int serverPort) {
        super();
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.serverProtocol = IGameConnection.ProtocolType.UDP;
    }

    public static void main(String[] args) {
        Game game = new MyGame(args[0], Integer.parseInt(args[1]));
        
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

    private void executeScript(ScriptEngine jsEngine, String scriptFileName) {
        try {
            FileReader fileReader = new FileReader(scriptFileName);
            jsEngine.eval(fileReader);
            fileReader.close();
        }catch (FileNotFoundException e1) {
            System.out.println(scriptFileName + " not found " + e1);
        } catch (IOException e2) {
            System.out.println("IO problem with " + scriptFileName + e2);
        } catch (ScriptException e3) {
            System.out.println("ScriptException in " + scriptFileName + e3);
        } catch (NullPointerException e4) {
            System.out.println ("Null ptr exception in " + scriptFileName + e4);
        }

    }

    @Override
    protected void update(Engine engine) {
        renderSystem = (GL4RenderSystem) engine.getRenderSystem();
        elapsedTime += engine.getElapsedTimeMillis();

        Matrix4 matrix;
        physicsEngine.update(engine.getElapsedTimeMillis());

        for (SceneNode s : engine.getSceneManager().getSceneNodes()) {
            if (s.getPhysicsObject() != null) {
                matrix = Matrix4f.createFrom(toFloatArray(s.getPhysicsObject().getTransform()));
                //s.setLocalRotation(matrix.toMatrix3());
                s.setLocalPosition(matrix.value(0, 3), matrix.value(1, 3), matrix.value(2, 3));
            }
        }


        displayStringPlayerOne = "Player HUD";

        // Auto adjusting HUD
        renderSystem.setHUD(displayStringPlayerOne, renderSystem.getRenderWindow().getViewport(0).getActualLeft(),
                        renderSystem.getRenderWindow().getViewport(0).getActualBottom() + 5);

        updateVerticalPosition();
        orbitController1.updateCameraPosition();
        inputManager.update(elapsedTime);
        processNetworking(elapsedTime);

        if (isClientConnected)
            protocolClient.sendMoveMessage(getPlayerPosition());

    }


    protected void updateVerticalPosition() {
        SceneNode avatarNode = this.getEngine().getSceneManager().getSceneNode(PLAYER_AVATAR);

        SceneNode tessellationNode = this.getEngine().getSceneManager().getSceneNode("TessellationNode");

        Tessellation tessellationEntity = ((Tessellation) tessellationNode.getAttachedObject("TessellationEntity"));

        Vector3 worldAvatarPosition = avatarNode.getWorldPosition();
        Vector3 localAvatarPosition = avatarNode.getLocalPosition();

        Vector3 newAvatarPosition = Vector3f.createFrom(localAvatarPosition.x(),
                tessellationEntity.getWorldHeight(worldAvatarPosition.x(), worldAvatarPosition.z()),
                localAvatarPosition.z());

        avatarNode.setLocalPosition(newAvatarPosition);
    }

    protected void processNetworking(float elapsedTime) {
        // Process packets received by the client from the server
        if (protocolClient != null)
            protocolClient.processPackets();
        // remove ghost avatars for players who have left the game
        Iterator<UUID> iterator = gameObjectsToRemove.iterator();

        while(iterator.hasNext()) {
            getEngine().getSceneManager().destroySceneNode(iterator.next().toString());
        }
        gameObjectsToRemove.clear();
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

        ScriptEngineManager factory = new ScriptEngineManager();

        // Name of the script that is used
        String scriptName = "hello.js";

        // define the script language as being javascript
        ScriptEngine jsEngine = factory.getEngineByName("js");

        // Running the script.
        this.executeScript(jsEngine, scriptName);

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
                PLAYER_AVATAR, "Avatar.obj", Vector3f.createFrom(-1.0f, 5.0f, -1.0f));


        // Sets up the camera
        setupOrbitCamera(engine, sceneManager);
        setupMovement(sceneManager);


        // Create a pyramid ship manual object
        ManualObject pyramidShip = pyramidShip(engine, sceneManager);
        SceneNode pyramidShipNode = sceneManager.getRootSceneNode().createChildSceneNode("PyramidShipNode");
        pyramidShipNode.attachObject(pyramidShip);
        pyramidShipNode.setLocalPosition(5.0f, 7.0f, 8.0f);


        // Creating the terrain
        Tessellation tessellationEntity = sceneManager.createTessellation("TessellationEntity", 7);
        tessellationEntity.setSubdivisions(2.0f);
        tessellationEntity.setQuality(7);
        tessellationEntity.setHeightMapTiling(5);

        // Creates the node for the terrain
        SceneNode tessellationNode = sceneManager.getRootSceneNode().createChildSceneNode("TessellationNode");
        tessellationNode.attachObject(tessellationEntity);

        // Sets the scale of the terrain
        //tessellationNode.scale(100, 50, 100);
        tessellationNode.setLocalPosition(0.0f, 0.0f, 0.0f);

        // Sets the terrain height map
        tessellationEntity.setHeightMap(this.getEngine(), "terrain.jpg");

        // Sets the terrain texture and sets the tiling
        tessellationEntity.setTexture(this.getEngine(), "grass.jpg");
        tessellationEntity.setTextureTiling(75);
        tessellationEntity.getTextureState().setWrapMode(TextureState.WrapMode.REPEAT);


        // Gets the ambient light and sets its intensity for the scene.
        sceneManager.getAmbientLight().setIntensity(new Color(0.6f, 0.6f, 0.6f));


        // Create a spot light
        Light positionalLight = sceneManager.createLight("PositionalLight", Light.Type.SPOT);
        positionalLight.setAmbient(new Color(0.3f, 0.3f, 0.3f));
        positionalLight.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
        positionalLight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        positionalLight.setRange(10f);

        // Create the node for the light and attaches it to the dolphin node as a child.
        SceneNode positionalLightNode =
                sceneManager.getRootSceneNode().createChildSceneNode(positionalLight.getName() + "Node");
        positionalLightNode.attachObject(positionalLight);
        dolphinNode.attachChild(positionalLightNode);


        // Setup the physics
        initializePhysicsSystem();
        createRagePhysicsWorld(engine);


        // Setup the inputs
        setupInputs(sceneManager);


        // Setup the networking
        setupNetworking();
    }

    @Override
    protected void setupWindow(RenderSystem renderSystem, GraphicsEnvironment graphicsEnvironment) {
        // Defines the window size of the game and make it so it is NOT in exclusive fullscreen.
        renderSystem.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
    }

    protected void setupOrbitCamera(Engine engine, SceneManager sceneManager) {
        SceneNode dolphinNode = sceneManager.getSceneNode(PLAYER_AVATAR);
        SceneNode cameraNode = sceneManager.getSceneNode("MainCameraNode");
        orbitController1 = new Camera3PController(cameraNode, dolphinNode);
    }

    protected void setupMovement(SceneManager sceneManager) {
        SceneNode dolphinNode = sceneManager.getSceneNode(PLAYER_AVATAR);
        movement3PController1 = new Movement3PController(dolphinNode);
    }

    protected void setupInputs(SceneManager sceneManager) {
        inputManager = new GenericInputManager();
        ArrayList controllers = inputManager.getControllers();

        Action moveForwardW = new ForwardThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR), orbitController1);
        Action moveBackwardS = new BackwardThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR), orbitController1);
        Action moveLeftA = new LeftThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR), orbitController1);
        Action moveRightD = new RightThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR), orbitController1);

        Action increaseElevation = new ThirdPersonElevationIncrease(sceneManager.getSceneNode(PLAYER_AVATAR), orbitController1);
        Action decreaseElevation = new ThirdPersonElevationDecrease(sceneManager.getSceneNode(PLAYER_AVATAR), orbitController1);

        Action orbitLeft = new ThirdPersonOrbitLeft(sceneManager.getSceneNode(PLAYER_AVATAR), orbitController1);
        Action orbitRight = new ThirdPersonOrbitRight(sceneManager.getSceneNode(PLAYER_AVATAR), orbitController1);

        Action radiasIncrease = new ThirdPersonRadiasIncrease(sceneManager.getSceneNode(PLAYER_AVATAR), orbitController1);
        Action radiasDecrease = new ThirdPersonRadiasDecrease(sceneManager.getSceneNode(PLAYER_AVATAR), orbitController1);

        Action turnLeft = new TurnLeftThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR));
        Action turnRight = new TurnRightThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR));

        Action disconnect = new ServerDisconnectAction(protocolClient);


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

                // Other
                inputManager.associateAction(c, Component.Identifier.Key.B, disconnect, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);

            } else if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                orbitController1.setupInput(inputManager, c.getName());
                movement3PController1.setupInput(inputManager, c.getName());
            }
        }
    }

    private void setupNetworking() {

        gameObjectsToRemove = new Vector<UUID>();
        isClientConnected = false;

        try {
            protocolClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (protocolClient == null) {
            System.out.println("Missing protocol host");
        } else {
            protocolClient.sendJoinMessage();
        }
    }

    private void initializePhysicsSystem() {
        String engine = "ray.physics.JBullet.JBulletPhysicsEngine";
        float[] gravity = {0, -3.0f, 0};

        physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
        physicsEngine.initSystem();
        physicsEngine.setGravity(gravity);
    }

    private void createRagePhysicsWorld(Engine engine) {
        float mass = 4.0f;
        float up[] = {0, 1, 0};
        double[] tempVariable;

        SceneNode playerAvatarNode = engine.getSceneManager().getSceneNode(PLAYER_AVATAR);
        SceneNode groundPlaneNode = engine.getSceneManager().getSceneNode("TessellationNode");

        // Holds the transform for the player avatar
        tempVariable = toDouble(playerAvatarNode.getLocalTransform().toFloatArray());
        float[] boxSize = {1f, 1f, 1f};
        playerAvatarPhysicsObject = physicsEngine.addBoxObject(
                physicsEngine.nextUID(), mass, tempVariable, boxSize);

        playerAvatarNode.setPhysicsObject(playerAvatarPhysicsObject);
        playerAvatarPhysicsObject.setBounciness(0.0f);
        playerAvatarPhysicsObject.setFriction(0.2f);
        playerAvatarPhysicsObject.setSleepThresholds(0.8f, 0.5f);
        playerAvatarPhysicsObject.setDamping(0.6f, 0.5f);
        playerAvatarNode.scale(0.1f, 0.1f, 0.1f);

        // Now holds the transform for the ground plane node
        tempVariable = toDouble(groundPlaneNode.getLocalTransform().toFloatArray());
        groundPlanePhysicsObject = physicsEngine.addStaticPlaneObject(
                physicsEngine.nextUID(), tempVariable, up, 0.0f);

        groundPlaneNode.setPhysicsObject(groundPlanePhysicsObject);
        groundPlanePhysicsObject.setBounciness(0.0f);
        groundPlanePhysicsObject.setFriction(0.5f);
        groundPlaneNode.scale(100.0f, 20.0f, 100.0f);
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

    private float[] toFloatArray(double[] doubleArray) {
        if (doubleArray == null)
            return null;

        int n = doubleArray.length;
        float[] ret = new float[n];

        for (int i = 0; i < n; i++) {
            ret[i] = (float)doubleArray[i];
        }
        return ret;
    }

    protected SceneNode createSceneNode(SceneManager sceneManager, String name,
                                        String nameOfOBJFile, Vector3 spawnLocation) throws IOException {

        Entity entity = sceneManager.createEntity(name + "Entity", nameOfOBJFile);
        entity.setPrimitive(Renderable.Primitive.TRIANGLES);

        SceneNode sceneNode = sceneManager.getRootSceneNode().createChildSceneNode(name);
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

    public void setIsConnected(boolean isConnected) {
        this.isClientConnected = isConnected;
    }

    public Vector3 getPlayerPosition() {
        SceneNode avatarNode = getEngine().getSceneManager().getSceneNode(PLAYER_AVATAR);
        return avatarNode.getWorldPosition();
    }

    public boolean isConnected() {
        return isClientConnected;
    }
}


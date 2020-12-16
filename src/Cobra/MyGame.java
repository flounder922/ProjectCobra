package Cobra;

import myGameEngine.ServerDisconnectAction;
import myGameEngine.ThirdPersonCamera.*;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import ray.audio.*;
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
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static ray.rage.scene.SkeletalEntity.EndType.LOOP;

public class MyGame extends VariableFrameRateGame {

    private static MyGame game;
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
    private IAudioManager audioManager;

    Sound birdSound;
    Sound zombieSound;

    boolean animationPlaying = false;

    // Variables for camera controller and movement of player avatar
    public Camera3PController orbitController1;
    public Movement3PController movement3PController1;

    // Skybox variables
    private static final String SKYBOX_NAME = "SkyBox";

    public final String PLAYER_AVATAR = "PlayerAvatarNode";

    // Physics related variables
    private PhysicsEngine physicsEngine;
    private PhysicsObject playerAvatarPhysicsObject, groundPlanePhysicsObject, npcPhysicsObject;
    private PhysicsObject wallPhysicsObject1, wallPhysicsObject2, wallPhysicsObject3, wallPhysicsObject4, wallPhysicsObject5,
            wallPhysicsObject6, wallPhysicsObject7, wallPhysicsObject8, wallPhysicsObject9, wallPhysicsObject10, wallPhysicsObject11,
            wallPhysicsObject12, wallPhysicsObject13, wallPhysicsObject14, wallPhysicsObject15, wallPhysicsObject16, wallPhysicsObject17,
            wallPhysicsObject18, wallPhysicsObject19, wallPhysicsObject20, wallPhysicsObject21, wallPhysicsObject22, wallPhysicsObject23,
            wallPhysicsObject24, wallPhysicsObject25, wallPhysicsObject26, wallPhysicsObject27, wallPhysicsObject28, wallPhysicsObject29,
            wallPhysicsObject30, wallPhysicsObject31, wallPhysicsObject32, wallPhysicsObject33, wallPhysicsObject34, wallPhysicsObject35,
            wallPhysicsObject36, wallPhysicsObject37, wallPhysicsObject38, wallPhysicsObject39, wallPhysicsObject40, wallPhysicsObject41,
            wallPhysicsObject42, wallPhysicsObject43, wallPhysicsObject44, wallPhysicsObject45, wallPhysicsObject46, wallPhysicsObject47,
            wallPhysicsObject48, wallPhysicsObject49, wallPhysicsObject50, wallPhysicsObject51, wallPhysicsObject52, wallPhysicsObject53,
            wallPhysicsObject54, wallPhysicsObject55;

    private int playerHealth = 10;
    private boolean playerWon = false;


    public MyGame(String serverAddress, int serverPort) {
        super();
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.serverProtocol = IGameConnection.ProtocolType.UDP;
    }

    public static void main(String[] args) {
        game = new MyGame(args[0], Integer.parseInt(args[1]));
        
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

        SkeletalEntity playerAvatarEntity = (SkeletalEntity) engine.getSceneManager().getEntity("PlayerAvatar");
        SceneNode wallNode1 = engine.getSceneManager().getSceneNode("WallNode1");
        SceneNode wallNode52 = engine.getSceneManager().getSceneNode("WallNode52");
        SceneNode wallNode17 = engine.getSceneManager().getSceneNode("WallNode17");
        SceneNode wallNode39 = engine.getSceneManager().getSceneNode("WallNode39");

        Matrix4 matrix;
        physicsEngine.update(engine.getElapsedTimeMillis());

        for (SceneNode s : engine.getSceneManager().getSceneNodes()) {
            if (s.getPhysicsObject() != null) {
                matrix = Matrix4f.createFrom(toFloatArray(s.getPhysicsObject().getTransform()));
                //s.setLocalRotation(matrix.toMatrix3());
                s.setLocalPosition(matrix.value(0, 3), matrix.value(1, 3), matrix.value(2, 3));
            }
        }

        if (getPlayerPosition().x() - 41 > 0.5 && getPlayerPosition().z() - 43 > 0.5 || playerWon) {
            displayStringPlayerOne = "YOU WIN!!!!!";
            playerWon = true;
        } else {
            displayStringPlayerOne = "Health: " + playerHealth;
        }

        if (playerHealth <= 0) {
            SceneNode player = engine.getSceneManager().getSceneNode(PLAYER_AVATAR);
            player.setLocalPosition(Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        }

        // Auto adjusting HUD
        renderSystem.setHUD(displayStringPlayerOne, renderSystem.getRenderWindow().getViewport(0).getActualLeft(),
                        renderSystem.getRenderWindow().getViewport(0).getActualBottom() + 5);

        //updateVerticalPosition();
        orbitController1.updateCameraPosition();
        inputManager.update(elapsedTime);
        processNetworking(elapsedTime);
        playerAvatarEntity.update();

        birdSound.setLocation(wallNode1.getWorldPosition());
        Sound birdSound2 = birdSound;
        birdSound2.setLocation(wallNode17.getWorldPosition());
        Sound birdSound3 = birdSound;
        birdSound3.setLocation(wallNode52.getWorldPosition());
        Sound birdSound4 = birdSound;
        birdSound4.setLocation(wallNode39.getWorldPosition());
        setEarParameters(getEngine().getSceneManager());

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
        String scriptName = "PlayerLight.js";

        // define the script language as being javascript
        ScriptEngine jsEngine = factory.getEngineByName("js");

        jsEngine.put("sceneManager", sceneManager);
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


        // Create the skeletal entity
        SkeletalEntity playerAvatarEntity = sceneManager.createSkeletalEntity("PlayerAvatar", "Avatar.rkm", "Avatar.rks");

        // Create and attach the texture to the skeletal entity
        Texture playerAvatarTexture = sceneManager.getTextureManager().getAssetByPath("avatarTexture.jpg");
        TextureState playerAvatarTextureState = (TextureState) sceneManager.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);

        playerAvatarTextureState.setTexture(playerAvatarTexture);
        playerAvatarEntity.setRenderState(playerAvatarTextureState);

        // Attach the entity to a scene node
        SceneNode playerAvatarNode = sceneManager.getRootSceneNode().createChildSceneNode("PlayerAvatarNode");
        playerAvatarNode.attachObject(playerAvatarEntity);

        // Load the animations
        playerAvatarEntity.loadAnimation("ClapAction", "Avatar_clap.rka");
        playerAvatarEntity.loadAnimation("WaveAction", "Avatar_wave.rka");


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
        tessellationEntity.setTextureTiling(50);
        tessellationEntity.getTextureState().setWrapMode(TextureState.WrapMode.REPEAT);


        // Gets the ambient light and sets its intensity for the scene.
        sceneManager.getAmbientLight().setIntensity(new Color(0.6f, 0.6f, 0.6f));

/*
        // Create a spot light
        Light positionalLight = sceneManager.createLight("PositionalLight", Light.Type.SPOT);
        positionalLight.setAmbient(new Color(0.3f, 0.3f, 0.3f));
        positionalLight.setDiffuse(new Color(0.7f, 0.7f, 0.7f));
        positionalLight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        positionalLight.setRange(10f);

 */

        // Create the node for the light and attaches it to the dolphin node as a child.
        SceneNode positionalLightNode =
                sceneManager.getRootSceneNode().createChildSceneNode( "PositionalLightNode");
        positionalLightNode.attachObject(sceneManager.getLight("PositionalLight"));
        playerAvatarNode.attachChild(positionalLightNode);
        positionalLightNode.rotate(Radianf.createFrom((float) Math.toRadians(180)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));


        // Setup the networking
        setupNetworking();


        // Setup the inputs
        setupInputs(sceneManager);


        // Setup the physics
        initializePhysicsSystem();
        createRagePhysicsWorld(engine);

        // Handles the building of the maze.
        createMaze(sceneManager);


        // Initialize audio
        initializeSound(sceneManager);
    }

    private void createMaze(SceneManager sceneManager) throws IOException {
        SceneNode mazeNode = getEngine().getSceneManager().getRootSceneNode().createChildSceneNode("MazeNode");

        // This is every wall node in the game used to build the maze...
        SceneNode wallNode1 = createSceneNode(sceneManager, "WallNode1", "Wall.obj", Vector3f.createFrom(1.0f, 0.0f, -3.0f));
        SceneNode wallNode2 = createSceneNode(sceneManager, "WallNode2", "Wall.obj", Vector3f.createFrom(10.0f, 0.0f, -3.0f));
        SceneNode wallNode3 = createSceneNode(sceneManager, "WallNode3", "Wall.obj", Vector3f.createFrom(-4.0f, 0.0f, 2.0f));
        wallNode3.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode4 = createSceneNode(sceneManager, "WallNode4", "Wall.obj", Vector3f.createFrom(-4.0f, 0.0f, 11.0f));
        wallNode4.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode5 = createSceneNode(sceneManager, "WallNode5", "Wall.obj", Vector3f.createFrom(-4.0f, 0.0f, 20.0f));
        wallNode5.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode6 = createSceneNode(sceneManager, "WallNode6", "Wall.obj", Vector3f.createFrom(5.0f, 0.0f, 4.0f));

        SceneNode wallNode7 = createSceneNode(sceneManager, "WallNode7", "Wall.obj", Vector3f.createFrom(10.0f, 0.0f, -1.0f));
        wallNode7.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode8 = createSceneNode(sceneManager, "WallNode8", "Wall.obj", Vector3f.createFrom(14.0f, 0.0f, 4.0f));
        SceneNode wallNode9 = createSceneNode(sceneManager, "WallNode9", "Wall.obj", Vector3f.createFrom(23.0f, 0.0f, 4.0f));
        SceneNode wallNode10 = createSceneNode(sceneManager, "WallNode10", "Wall.obj", Vector3f.createFrom(31.0f, 0.0f, 4.0f));
        SceneNode wallNode11 = createSceneNode(sceneManager, "WallNode11", "Wall.obj", Vector3f.createFrom(40.0f, 0.0f, 4.0f));


        SceneNode wallNode12 = createSceneNode(sceneManager, "WallNode12", "Wall.obj", Vector3f.createFrom(-4.0f, 0.0f, 29.0f));
        wallNode12.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        SceneNode wallNode13 = createSceneNode(sceneManager, "WallNode13", "Wall.obj", Vector3f.createFrom(-4.0f, 0.0f, 38.0f));
        wallNode13.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        SceneNode wallNode14 = createSceneNode(sceneManager, "WallNode14", "Wall.obj", Vector3f.createFrom(35.0f, 0.0f, 47.0f));
        wallNode14.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode15 = createSceneNode(sceneManager, "WallNode15", "Wall.obj", Vector3f.createFrom(1.0f, 0.0f, 11.0f));
        SceneNode wallNode16 = createSceneNode(sceneManager, "WallNode16", "Wall.obj", Vector3f.createFrom(10.0f, 0.0f, 11.0f));
        SceneNode wallNode17 = createSceneNode(sceneManager, "WallNode17", "Wall.obj", Vector3f.createFrom(26.0f, 0.0f, 11.0f));
        SceneNode wallNode18 = createSceneNode(sceneManager, "WallNode18", "Wall.obj", Vector3f.createFrom(35.0f, 0.0f, 11.0f));

        SceneNode wallNode19 = createSceneNode(sceneManager, "WallNode19", "Wall.obj", Vector3f.createFrom(45.0f, 0.0f, 9.0f));
        wallNode19.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        SceneNode wallNode20 = createSceneNode(sceneManager, "WallNode20", "Wall.obj", Vector3f.createFrom(45.0f, 0.0f, 18.0f));
        wallNode20.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        SceneNode wallNode21 = createSceneNode(sceneManager, "WallNode21", "Wall.obj", Vector3f.createFrom(45.0f, 0.0f, 27.0f));
        wallNode21.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        SceneNode wallNode22 = createSceneNode(sceneManager, "WallNode22", "Wall.obj", Vector3f.createFrom(45.0f, 0.0f, 36.0f));
        wallNode22.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        SceneNode wallNode23 = createSceneNode(sceneManager, "WallNode23", "Wall.obj", Vector3f.createFrom(45.0f, 0.0f, 45.0f));
        wallNode23.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        SceneNode wallNode24 = createSceneNode(sceneManager, "WallNode24", "Wall.obj", Vector3f.createFrom(40.0f, 0.0f, 50.0f));
        //wallNode24.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));


        SceneNode wallNode25 = createSceneNode(sceneManager, "WallNode25", "Wall.obj", Vector3f.createFrom(39.6f, 0.0f, 16.0f));
        wallNode25.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode26 = createSceneNode(sceneManager, "WallNode26", "Wall.obj", Vector3f.createFrom(31.0f, 0.0f, 17.0f));

        SceneNode wallNode27 = createSceneNode(sceneManager, "WallNode27", "Wall.obj", Vector3f.createFrom(21.5f, 0.0f, 16.0f));
        wallNode27.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode28 = createSceneNode(sceneManager, "WallNode28", "Wall.obj", Vector3f.createFrom(21.5f, 0.0f, 25.0f));
        wallNode28.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));


        SceneNode wallNode29 = createSceneNode(sceneManager, "WallNode29", "Wall.obj", Vector3f.createFrom(31.0f, 0.0f, 23.0f));

        SceneNode wallNode30 = createSceneNode(sceneManager, "WallNode30", "Wall.obj", Vector3f.createFrom(39.6f, 0.0f, 25.0f));
        wallNode30.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode31 = createSceneNode(sceneManager, "WallNode31", "Wall.obj", Vector3f.createFrom(35.5f, 0.0f, 22.0f));
        wallNode31.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode32 = createSceneNode(sceneManager, "WallNode32", "Wall.obj", Vector3f.createFrom(26.2f, 0.0f, 23.0f));

        SceneNode wallNode33 = createSceneNode(sceneManager, "WallNode33", "Wall.obj", Vector3f.createFrom(31.0f, 0.0f, 26.5f));
        SceneNode wallNode34 = createSceneNode(sceneManager, "WallNode34", "Wall.obj", Vector3f.createFrom(26.2f, 0.0f, 26.5f));

        SceneNode wallNode35 = createSceneNode(sceneManager, "WallNode35", "Wall.obj", Vector3f.createFrom(14.5f, 0.0f, 16.0f));
        wallNode35.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        SceneNode wallNode36 = createSceneNode(sceneManager, "WallNode36", "Wall.obj", Vector3f.createFrom(14.5f, 0.0f, 25.0f));
        wallNode36.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode37 = createSceneNode(sceneManager, "WallNode37", "Wall.obj", Vector3f.createFrom(10.0f, 0.0f, 20.0f));
        wallNode37.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));
        SceneNode wallNode38 = createSceneNode(sceneManager, "WallNode38", "Wall.obj", Vector3f.createFrom(10.0f, 0.0f, 29.0f));
        wallNode38.rotate(Radianf.createFrom((float) Math.toRadians(90)), Vector3f.createFrom(0.0f, 1.0f, 0.0f));

        SceneNode wallNode39 = createSceneNode(sceneManager, "WallNode39", "Wall.obj", Vector3f.createFrom(4.5f, 0.0f, 15.0f));
        SceneNode wallNode40 = createSceneNode(sceneManager, "WallNode40", "Wall.obj", Vector3f.createFrom(5.5f, 0.0f,  15.0f));
        SceneNode wallNode41 = createSceneNode(sceneManager, "WallNode41", "Wall.obj", Vector3f.createFrom(1.0f, 0.0f, 20.0f));
        SceneNode wallNode42 = createSceneNode(sceneManager, "WallNode42", "Wall.obj", Vector3f.createFrom(2.0f, 0.0f, 20.0f));
        SceneNode wallNode43 = createSceneNode(sceneManager, "WallNode43", "Wall.obj", Vector3f.createFrom(4.5f, 0.0f, 25.0f));
        SceneNode wallNode44 = createSceneNode(sceneManager, "WallNode44", "Wall.obj", Vector3f.createFrom(5.5f, 0.0f,  25.0f));

        SceneNode wallNode45 = createSceneNode(sceneManager, "WallNode45", "Wall.obj", Vector3f.createFrom(14.5f, 0.0f,  34.0f));
        SceneNode wallNode46 = createSceneNode(sceneManager, "WallNode46", "Wall.obj", Vector3f.createFrom(23.0f, 0.0f,  34.0f));
        SceneNode wallNode47 = createSceneNode(sceneManager, "WallNode47", "Wall.obj", Vector3f.createFrom(32.0f, 0.0f,  34.0f));
        SceneNode wallNode48 = createSceneNode(sceneManager, "WallNode48", "Wall.obj", Vector3f.createFrom(41.0f, 0.0f,  34.0f));

        SceneNode wallNode49 = createSceneNode(sceneManager, "WallNode49", "Wall.obj", Vector3f.createFrom(1.0f, 0.0f, 30.0f));
        SceneNode wallNode50 = createSceneNode(sceneManager, "WallNode50", "Wall.obj", Vector3f.createFrom(2.0f, 0.0f, 30.0f));

        SceneNode wallNode51 = createSceneNode(sceneManager, "WallNode51", "Wall.obj", Vector3f.createFrom(1.0f, 0.0f, 42.0f));
        SceneNode wallNode52 = createSceneNode(sceneManager, "WallNode52", "Wall.obj", Vector3f.createFrom(10.0f, 0.0f, 42.0f));
        SceneNode wallNode53 = createSceneNode(sceneManager, "WallNode53", "Wall.obj", Vector3f.createFrom(19.0f, 0.0f, 42.0f));
        SceneNode wallNode54 = createSceneNode(sceneManager, "WallNode54", "Wall.obj", Vector3f.createFrom(28.0f, 0.0f, 42.0f));
        SceneNode wallNode55 = createSceneNode(sceneManager, "WallNode55", "Wall.obj", Vector3f.createFrom(37.0f, 0.0f, 42.0f));

        // End of the wall creations.




        // Adding all the physics objects to the walls so they can be passed through.
        createWallPhysicsObject(wallNode1, wallPhysicsObject1);
        createWallPhysicsObject(wallNode2, wallPhysicsObject2);
        createWallPhysicsObject(wallNode3, wallPhysicsObject3);
        createWallPhysicsObject(wallNode4, wallPhysicsObject4);
        createWallPhysicsObject(wallNode5, wallPhysicsObject5);
        createWallPhysicsObject(wallNode6, wallPhysicsObject6);
        createWallPhysicsObject(wallNode7, wallPhysicsObject7);
        createWallPhysicsObject(wallNode8, wallPhysicsObject8);
        createWallPhysicsObject(wallNode9, wallPhysicsObject9);
        createWallPhysicsObject(wallNode10, wallPhysicsObject10);
        createWallPhysicsObject(wallNode11, wallPhysicsObject11);
        createWallPhysicsObject(wallNode12, wallPhysicsObject12);
        createWallPhysicsObject(wallNode13, wallPhysicsObject13);
        createWallPhysicsObject(wallNode14, wallPhysicsObject14);
        createWallPhysicsObject(wallNode15, wallPhysicsObject15);
        createWallPhysicsObject(wallNode16, wallPhysicsObject16);
        createWallPhysicsObject(wallNode17, wallPhysicsObject17);
        createWallPhysicsObject(wallNode18, wallPhysicsObject18);
        createWallPhysicsObject(wallNode19, wallPhysicsObject19);
        createWallPhysicsObject(wallNode20, wallPhysicsObject20);
        createWallPhysicsObject(wallNode21, wallPhysicsObject21);
        createWallPhysicsObject(wallNode22, wallPhysicsObject22);
        createWallPhysicsObject(wallNode23, wallPhysicsObject23);
        createWallPhysicsObject(wallNode24, wallPhysicsObject24);
        createWallPhysicsObject(wallNode25, wallPhysicsObject25);
        createWallPhysicsObject(wallNode26, wallPhysicsObject26);
        createWallPhysicsObject(wallNode27, wallPhysicsObject27);
        createWallPhysicsObject(wallNode28, wallPhysicsObject28);
        createWallPhysicsObject(wallNode29, wallPhysicsObject29);
        createWallPhysicsObject(wallNode30, wallPhysicsObject30);
        createWallPhysicsObject(wallNode31, wallPhysicsObject31);
        createWallPhysicsObject(wallNode32, wallPhysicsObject32);
        createWallPhysicsObject(wallNode33, wallPhysicsObject33);
        createWallPhysicsObject(wallNode34, wallPhysicsObject34);
        createWallPhysicsObject(wallNode35, wallPhysicsObject35);
        createWallPhysicsObject(wallNode36, wallPhysicsObject36);
        createWallPhysicsObject(wallNode37, wallPhysicsObject37);
        createWallPhysicsObject(wallNode38, wallPhysicsObject38);
        createWallPhysicsObject(wallNode39, wallPhysicsObject39);
        createWallPhysicsObject(wallNode40, wallPhysicsObject40);
        createWallPhysicsObject(wallNode41, wallPhysicsObject41);
        createWallPhysicsObject(wallNode42, wallPhysicsObject42);
        createWallPhysicsObject(wallNode43, wallPhysicsObject43);
        createWallPhysicsObject(wallNode44, wallPhysicsObject44);
        createWallPhysicsObject(wallNode45, wallPhysicsObject45);
        createWallPhysicsObject(wallNode46, wallPhysicsObject46);
        createWallPhysicsObject(wallNode47, wallPhysicsObject47);
        createWallPhysicsObject(wallNode48, wallPhysicsObject48);
        createWallPhysicsObject(wallNode49, wallPhysicsObject49);
        createWallPhysicsObject(wallNode50, wallPhysicsObject50);
        createWallPhysicsObject(wallNode51, wallPhysicsObject51);
        createWallPhysicsObject(wallNode52, wallPhysicsObject52);
        createWallPhysicsObject(wallNode53, wallPhysicsObject53);
        createWallPhysicsObject(wallNode54, wallPhysicsObject54);
        createWallPhysicsObject(wallNode55, wallPhysicsObject55);


        // Adding all the walls to the maze node to keep a hierarchy to move the maze around easily.
        for (int i = 1; i < 56; ++i) {
            mazeNode.attachChild(getEngine().getSceneManager().getSceneNode("WallNode" + i));
        }
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

    @Override
    public void keyPressed(KeyEvent event) {

            switch (event.getKeyCode()) {
                case KeyEvent.VK_K:
                    doWave();
                    break;
                case KeyEvent.VK_L:
                    doClap();
                    break;
                case KeyEvent.VK_J:
                    stopAnimation();
                    break;
            }

        super.keyPressed(event);
    }

    protected void setupInputs(SceneManager sceneManager) {
        inputManager = new GenericInputManager();
        ArrayList controllers = inputManager.getControllers();

        Action moveForwardW = new ForwardThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR), game);
        Action moveBackwardS = new BackwardThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR), game);
        Action moveLeftA = new LeftThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR), game);
        Action moveRightD = new RightThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR), game);

        Action increaseElevation = new ThirdPersonElevationIncrease(sceneManager.getSceneNode(PLAYER_AVATAR), game);
        Action decreaseElevation = new ThirdPersonElevationDecrease(sceneManager.getSceneNode(PLAYER_AVATAR), game);

        Action orbitLeft = new ThirdPersonOrbitLeft(sceneManager.getSceneNode(PLAYER_AVATAR), game);
        Action orbitRight = new ThirdPersonOrbitRight(sceneManager.getSceneNode(PLAYER_AVATAR), game);

        Action radiasIncrease = new ThirdPersonRadiasIncrease(sceneManager.getSceneNode(PLAYER_AVATAR), game);
        Action radiasDecrease = new ThirdPersonRadiasDecrease(sceneManager.getSceneNode(PLAYER_AVATAR), game);

        Action turnLeft = new TurnLeftThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR), game);
        Action turnRight = new TurnRightThirdPersonAction(sceneManager.getSceneNode(PLAYER_AVATAR), game);

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
        float[] boxSize = {1f, 0.5f, 1f};
        playerAvatarPhysicsObject = physicsEngine.addBoxObject(
                physicsEngine.nextUID(), mass, tempVariable, boxSize);

        playerAvatarNode.setPhysicsObject(playerAvatarPhysicsObject);
        playerAvatarPhysicsObject.setBounciness(0.0f);
        playerAvatarPhysicsObject.setFriction(0.2f);
        //playerAvatarPhysicsObject.setSleepThresholds(0.8f, 0.5f);
        playerAvatarPhysicsObject.setDamping(0.69f, 0.69f);
        playerAvatarNode.scale(0.1f, 0.1f, 0.1f);

        // Now holds the transform for the ground plane node
        tempVariable = toDouble(groundPlaneNode.getLocalTransform().toFloatArray());
        groundPlanePhysicsObject = physicsEngine.addStaticPlaneObject(
                physicsEngine.nextUID(), tempVariable, up, 0.0f);

        groundPlaneNode.setPhysicsObject(groundPlanePhysicsObject);
        groundPlanePhysicsObject.setBounciness(0.0f);
        groundPlanePhysicsObject.setFriction(0.5f);
        groundPlaneNode.scale(100.0f, 10.0f, 100.0f);
    }

    public void createNPCPhysicsObject(SceneNode npcNode) {
        float mass = 4.0f;
        float up[] = {0, 1, 0};
        double[] tempVariable;

        tempVariable = toDouble(npcNode.getLocalTransform().toFloatArray());
        float[] ghostBox = {1.0f, 0.5f, 1.0f};
        npcPhysicsObject = physicsEngine.addBoxObject(
                physicsEngine.nextUID(), mass, tempVariable, ghostBox);

        npcNode.setPhysicsObject(npcPhysicsObject);
        npcPhysicsObject.setBounciness(0.0f);
        npcPhysicsObject.setFriction(0.2f);
    }

    public void createWallPhysicsObject(SceneNode wallNode, PhysicsObject wallPhysicsObject) {
        double[] tempVariable;

        tempVariable = toDouble(wallNode.getLocalTransform().toFloatArray());
        float[] wallSize = {10f, 3f, 0.1f};

        //System.out.println("Going to assign physics Box");
        try {
            wallPhysicsObject = physicsEngine.addBoxObject(physicsEngine.nextUID(),0, tempVariable, wallSize);

        } catch (NullPointerException e) {
            System.out.println(wallNode.getName() + ": " + e);
        }
        //System.out.println("Finished assigning physics Box");

        //System.out.println("Attaching physics object to node");
        wallNode.setPhysicsObject(wallPhysicsObject);
        //System.out.println("Finished attaching physics object to node");
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

    private void doWave() {
        SkeletalEntity playerAvatarEntity = (SkeletalEntity) getEngine().getSceneManager().getEntity("PlayerAvatar");

        if (!animationPlaying) {
            playerAvatarEntity.stopAnimation();
            playerAvatarEntity.playAnimation("WaveAction", 0.5f, LOOP, 0);
            animationPlaying = true;
        }
    }

    private void doClap() {
        SkeletalEntity playerAvatarEntity = (SkeletalEntity) getEngine().getSceneManager().getEntity("PlayerAvatar");

        if (!animationPlaying) {
            playerAvatarEntity.stopAnimation();
            playerAvatarEntity.playAnimation("ClapAction", 0.5f, LOOP, 0);
        }
    }

    private void stopAnimation() {
        SkeletalEntity playerAvatarEntity = (SkeletalEntity) getEngine().getSceneManager().getEntity("PlayerAvatar");
        playerAvatarEntity.stopAnimation();
        animationPlaying = false;
    }

    public void initializeSound(SceneManager sceneManager) {

        AudioResource resource1, resource2;
        audioManager = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");

        if (!audioManager.initialize()) {
            System.out.println("Audio manager failed to initialize!");
            return;
        }

        resource1 = audioManager.createAudioResource("birds.wav", AudioResourceType.AUDIO_STREAM);
        resource2 = audioManager.createAudioResource("zombie.wav", AudioResourceType.AUDIO_STREAM);

        birdSound = new Sound(resource1, SoundType.SOUND_EFFECT, 50, true);
        zombieSound = new Sound(resource2, SoundType.SOUND_EFFECT, 70, true);

        birdSound.initialize(audioManager);
        birdSound.setMaxDistance(10.0f);
        birdSound.setMinDistance(0.5f);
        birdSound.setRollOff(3.0f);

        zombieSound.initialize(audioManager);
        zombieSound.setMaxDistance(15.0f);
        zombieSound.setMinDistance(1.0f);
        zombieSound.setRollOff(5.0f);

        SceneNode wallNode = sceneManager.getSceneNode("WallNode1");

        birdSound.setLocation(wallNode.getWorldPosition());
        setEarParameters(sceneManager);

        birdSound.play();
    }

    private void setEarParameters(SceneManager sceneManager) {
        SceneNode playerNode = sceneManager.getSceneNode("PlayerAvatarNode");

        Vector3 avDirection = playerNode.getWorldForwardAxis();

        audioManager.getEar().setLocation(getPlayerPosition());
        audioManager.getEar().setOrientation(avDirection, Vector3f.createFrom(0,1,0));
    }

    public void takeDamage() {
        if (!playerWon)
            playerHealth--;
    }
}


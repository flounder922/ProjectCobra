package Cobra;

import ray.networking.client.GameConnectionClient;
import ray.rage.rendersystem.Renderable;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import java.util.Vector;

public class UDPClient extends GameConnectionClient {
    private MyGame game;
    private UUID id;
    private Vector<GhostAvatar> ghostAvatars;
    private SceneManager sceneManager;

    public UDPClient(InetAddress remAddr, int remPort, ProtocolType protocolType, MyGame game) throws IOException {
        super(remAddr, remPort, protocolType);
        this.game = game;
        this.id = UUID.randomUUID();
        this.ghostAvatars = new Vector<GhostAvatar>();
    }

    @Override
    protected void processPacket(Object message) {
        String strMessage = (String) message;
        String[] messageTokens = strMessage.split(",");

        if (messageTokens.length > 0) {

            // Receive “join”
            // Format: join, success/failure
            if (messageTokens[0].compareTo("join") == 0)
            {
                if (messageTokens[1].compareTo("success") == 0) {
                    game.setIsConnected(true);
                    sendCreateMessage(game.getPlayerPosition());
                }
                if (messageTokens[1].compareTo("failure") == 0) {
                    game.setIsConnected(false);
                }
            }
            if (messageTokens[0].compareTo("bye") == 0) // receive “bye”
            { // format: bye, remoteId
                UUID ghostID = UUID.fromString(messageTokens[1]);
                removeGhostAvatar(ghostID);
            }
            if ((messageTokens[0].compareTo("dsfr") == 0) // receive “dsfr”
                    || (messageTokens[0].compareTo("create") == 0)) { // format: create, remoteId, x,y,z or dsfr, remoteId, x,y,z
                UUID ghostID = UUID.fromString(messageTokens[1]);
                Vector3 ghostPosition = Vector3f.createFrom(
                        Float.parseFloat(messageTokens[2]),
                        Float.parseFloat(messageTokens[3]),
                        Float.parseFloat(messageTokens[4]));
                try {
                    createGhostAvatar(ghostID, ghostPosition);
                } catch (IOException e) {
                    System.out.println("error creating ghost avatar");
                }
            }
            if (messageTokens[0].compareTo("wsds") == 0) // rec. “create…”
            { // etc….. }
                if (messageTokens[0].compareTo("wsds") == 0) // rec. “wants…”
                { // etc….. }
                    if (messageTokens[0].compareTo("move") == 0) // rec. “move...”
                    { // etc….. }
                    }
                }
            }
        }
    }

    // format: join, localId

    public void sendJoinMessage() {
        try {
            sendPacket(new String("join," + id.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // format: (create, localId, x, y, z)

    public void sendCreateMessage(Vector3 pos) {
        try {
            String message = new String("create," + id.toString());
            message += "," + pos.x()+"," + pos.y() + "," + pos.z();
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendByeMessage() {
        //todo

    }

    public void sendDetailsForMessage(UUID remId, Vector3f pos) {
        //todo
    }

    public void sendMoveMessage(Vector3f pos) {
        //todo
    }

    private void createGhostAvatar(UUID ghostID, Vector3 ghostPosition) throws IOException {
       ghostAvatars.add(new GhostAvatar(ghostID, ghostPosition));
    }

    private void removeGhostAvatar(UUID ghostID) {
        ghostAvatars.remove(ghostID);
    }

    private class GhostAvatar {
        private UUID id;
        private SceneNode node;
        private Entity entity;

        public GhostAvatar(UUID id, Vector3 position) throws IOException {
            this.id = id;
            sceneManager = game.getEngine().getSceneManager();

            createGhostAvatar(id, position);

        }

        protected void createGhostAvatar(UUID ghostID, Vector3 ghostPosition) throws IOException {
            entity = sceneManager.createEntity(String.valueOf(ghostID), "dolphinHighPoly.obj");
                    // createEntity(id.toString(), "dolphinHighPoly.obj");
            entity.setPrimitive(Renderable.Primitive.TRIANGLES);

            node = sceneManager.getRootSceneNode().createChildSceneNode(id.toString());
            node.attachObject(entity);
            node.setLocalPosition(ghostPosition);
        }
    }
}

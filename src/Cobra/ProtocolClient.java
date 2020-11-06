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

public class ProtocolClient extends GameConnectionClient {
    private MyGame game;
    private UUID id;
    private Vector<GhostAvatar> ghostAvatars;

    public ProtocolClient(InetAddress remAddr, int remPort, ProtocolType protocolType, MyGame game) throws IOException {
        super(remAddr, remPort, protocolType);
        this.game = game;
        this.id = UUID.randomUUID();
        ghostAvatars = new Vector<GhostAvatar>();
    }

    @Override
    protected void processPacket(Object message) {
        String strMessage = (String) message;
        String[] messageTokens = strMessage.split(",");

        if (messageTokens.length > 0) {
            // Receive join
            // Format: join, success/failure
            if (messageTokens[0].compareTo("join") == 0) {
                if (messageTokens[1].compareTo("success") == 0) {
                    game.setIsConnected(true);
                    sendCreateMessage(game.getPlayerPosition());
                    System.out.println("Joined server successfully!!");
                }
                if (messageTokens[1].compareTo("failure") == 0) {
                    game.setIsConnected(false);
                    System.out.println("Failed to join server");
                }
            }
            if (messageTokens[0].compareTo("bye") == 0) {
                // format: bye, remoteId
                UUID ghostID = UUID.fromString(messageTokens[1]);
                removeGhostAvatar(ghostID);
            }

            if ((messageTokens[0].compareTo("dsfr") == 0) // receive dsfr
                    || (messageTokens[0].compareTo("create") == 0)) { // format: create, remoteId, x,y,z or dsfr, remoteId, x,y,z
                System.out.println("Look at me I am trying to create something");
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

            if (messageTokens[0].compareTo("move") == 0) {
                System.out.println("I am trying to move something");
                UUID ghostID = UUID.fromString(messageTokens[1]);
                Vector3 ghostPosition = Vector3f.createFrom(
                        Float.parseFloat(messageTokens[2]),
                        Float.parseFloat(messageTokens[3]),
                        Float.parseFloat(messageTokens[4]));
                moveGhostAvatar(ghostID, ghostPosition);

            }

            if (messageTokens[0].compareTo("wsds") == 0) {
                UUID remoteId = UUID.fromString(messageTokens[1]);
                sendDetailsForMessage(remoteId,
                         game.getEngine().getSceneManager().getSceneNode("DolphinNode").getWorldPosition());
            }
        }
    }

    // format: join, localId
    public void sendJoinMessage() {
        try {
            sendPacket("join," + id.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // format: (create, localId, x, y, z)
    public void sendCreateMessage(Vector3 pos) {
        try {
            String message = "create," + id.toString();
            message += "," + pos.x()+"," + pos.y() + "," + pos.z();
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendByeMessage() {
        if (game.isConnected()) {
            try {
                System.out.println("Bye for now");
                String message = "bye," + id.toString();
                sendPacket(message);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < ghostAvatars.size(); ++i) {
                game.getEngine().getSceneManager().destroySceneNode(ghostAvatars.get(i).id.toString());
            }
            ghostAvatars.clear();
            game.setIsConnected(false);
        }

    }

    // Send the details of local clients avatar
    public void sendDetailsForMessage(UUID remId, Vector3 pos) {
        try {
            String message = "dsrf," + id.toString();
            message += "," + remId;
            message += "," + pos.x() + "," + pos.y() + "," + pos.z();
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sends the new position of local clients avatar
    public void sendMoveMessage(Vector3 pos) {
        try {
            String message = "move," + id.toString();
            message += "," + pos.x() + "," + pos.y() + "," + pos.z();
            sendPacket(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Instantiates a new instance of the ghost avatar class and adds it to the ghost avatar list.
    private void createGhostAvatar(UUID ghostID, Vector3 ghostPosition) throws IOException {
       ghostAvatars.add(new GhostAvatar(ghostID, ghostPosition));
    }

    private void moveGhostAvatar(UUID ghostID, Vector3 ghostPosition) {

        for (int i = 0; i < ghostAvatars.size(); ++i) {
            if (ghostAvatars.get(i).id == ghostID) {
                ghostAvatars.get(i).moveGhost(ghostPosition);
            }
        }
    }

    private void removeGhostAvatar(UUID ghostID) {

        for (int i = 0; i < ghostAvatars.size(); ++i) {
            if (ghostAvatars.get(i).id == ghostID) {
                game.getEngine().getSceneManager().destroySceneNode(ghostID.toString());
                ghostAvatars.remove(ghostID);
            }
        }
    }

    private class GhostAvatar {
        private UUID id;
        private SceneNode node;
        private Entity entity;

        public GhostAvatar(UUID id, Vector3 position) throws IOException {
            this.id = id;
            createGhostAvatar(id, position);
        }

        protected void createGhostAvatar(UUID ghostID, Vector3 ghostPosition) throws IOException {
            entity = game.getEngine().getSceneManager().createEntity(String.valueOf(ghostID), "dolphinHighPoly.obj");
            entity.setPrimitive(Renderable.Primitive.TRIANGLES);

            node = game.getEngine().getSceneManager().getRootSceneNode().createChildSceneNode(id.toString());
            node.attachObject(entity);
            node.setLocalPosition(ghostPosition);
        }


        protected void moveGhost(Vector3 ghostPosition) {
            node.setLocalPosition(ghostPosition);
        }
    }
}

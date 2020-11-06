package myGameEngine;

import Cobra.ProtocolClient;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class ServerDisconnectAction extends AbstractInputAction {

    private ProtocolClient game;
    public ServerDisconnectAction(ProtocolClient game) {
        this.game = game;
    }

    @Override
    public void performAction(float v, Event event) {
        game.sendByeMessage();
    }
}

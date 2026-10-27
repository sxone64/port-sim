package portsim.ui.node;

import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.layout.*;
import org.jspecify.annotations.NullMarked;
import portsim.model.Position;
import portsim.model.Terminal;
import portsim.model.ship.Ship;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static portsim.model.Cell.Type.DOCK_DOWN;
import static portsim.model.Cell.Type.DOCK_UP;

// Transparent non-interactable fixed size pane that handles ship node placement and movement inside a terminal
@NullMarked
public final class TerminalNode extends Pane {
    public static final double TILE_SIZE = 64;

    private final Terminal terminal;

    private final Map<Ship, ShipNode> shipNodes = new HashMap<>();

    public TerminalNode(Terminal terminal) {
        this.terminal = terminal;

        var width = terminal.getTotalColumns() * TILE_SIZE;
        var height = terminal.getTotalRows() * TILE_SIZE;

        // Enforce fixed pane size
        setMinSize(width, height);
        setPrefSize(width, height);
        setMaxSize(width, height);

        // Allows mouse events to pass through the pane
        setPickOnBounds(false);

        for (var ship: terminal.getShips()) {
            var pos = terminal.getShipPosition(ship);
            pos.ifPresent(position -> addShip(ship, position));
        }
    }

    /*
        Attach an existing ship node to specified position.
        Used mostly to attach a ship node that's been removed from another terminal node
     */
    public void attachShip(ShipNode node, Position pos) {
        var ship = node.getShip();

        placeNodeAt(node, pos);
        shipNodes.put(ship, node);
        getChildren().add(node);
    }

    public Optional<ShipNode> removeShip(Ship ship) {
        var node = shipNodes.remove(ship);
        if (node != null) getChildren().remove(node);
        return Optional.ofNullable(node);
    }

    /*
        Public methods that cover simulation events tied to a specific terminal.

        Each method acknowledges the event through ack runnable once its UI presentation is finished,
        which resumes the ship thread for the next step
     */

    public void enterShip(Ship ship, Position pos, Runnable ack) {
        // TODO: Add fade-in slide animation
        addShip(ship, pos);
        ack.run();
    }

    public void exitShip(Ship ship, Runnable ack) {
        // TODO: Add fade-out slide animation
        removeShip(ship);
        ack.run();
    }

    public void moveShip(Ship ship, Position from, Position to, Runnable ack) {
        var node = shipNodes.get(ship);

        if (node == null)
            throw new IllegalStateException(
                    "Ship IMO %d is not part of this terminal node ID %d"
                            .formatted(ship.getImo(), terminal.getIdTerminal()));

        // Orient the ship node based on horizontal movement direction
        var orientRight = to.column() - from.column() > 0;
        if (orientRight)
            node.orientRight();
        else
            node.orientLeft();

        var transition = new TranslateTransition(ship.getStepDuration(), node);

        transition.setFromX(0);
        transition.setFromY(0);

        transition.setToX((to.column() - from.column()) * TILE_SIZE);
        transition.setToY((to.row() - from.row()) * TILE_SIZE);

        transition.setOnFinished(_ -> {
            placeNodeAt(node, to);

            node.setTranslateX(0);
            node.setTranslateY(0);

            ack.run();
        });

        transition.play();
    }

    private void addShip(Ship ship, Position pos) {
        var node = new ShipNode(ship);
        attachShip(node, pos);
    }

    // Calculate and return the center point of a cell for specified position and ship node dimensions
    public Point2D centerPointOf(ShipNode node, Position pos) {
        var point = pointOf(pos);
        var bounds = node.getBoundsInLocal();

        var offsetX = (TILE_SIZE - bounds.getWidth()) / 2;
        var offsetY = (TILE_SIZE - bounds.getHeight()) / 2;

        return point.add(offsetX, offsetY);
    }

    private void placeNodeAt(ShipNode node, Position pos) {
        var point = pointOf(pos);
        var bounds = node.getBoundsInLocal();

        var offsetX = (TILE_SIZE - bounds.getWidth()) / 2;
        var offsetY = (TILE_SIZE - bounds.getHeight()) / 2;

        node.relocate(point.getX() + offsetX, point.getY() + offsetY);

        // Ship movement orientation rules above are respected
        var cellType = terminal.getCellType(pos);
        if (cellType == DOCK_UP)
            node.orientLeft();
        else if (cellType == DOCK_DOWN)
            node.orientRight();
    }

    // Convert logical grid position into top-left 2D pixel coordinates
    private Point2D pointOf(Position pos) {
        return new Point2D(pos.column() * TILE_SIZE, pos.row() * TILE_SIZE);
    }
}

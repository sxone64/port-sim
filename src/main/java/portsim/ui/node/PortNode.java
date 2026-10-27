package portsim.ui.node;

import javafx.animation.TranslateTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import portsim.model.Port;
import portsim.model.Position;
import portsim.model.ship.Ship;
import portsim.ui.sprite.CellSprites;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static portsim.ui.node.TerminalNode.TILE_SIZE;

/*
    Non-interactable stack pane that handles port map rendering and
    ship node transfers between different terminal nodes.

    There are three layers in this stack pane:
    1) Port canvas layer - Visual representation of the whole port.

    2) Terminal nodes layer - Vertically stacked terminal nodes.
    Terminal node bounds are in sync with the canvas representation.

    3) Transfer layer - Transparent non-interactable pane used to manage ship node transfers
    between different terminal nodes
 */
@NullMarked
public final class PortNode extends StackPane {
    /*
        Ship node is removed from one terminal node, attached to this pane, moved across the pane to
        destination position and attached to destination terminal node after transfer animation is complete
     */
    private final Pane transferLayer = new Pane();

    private final Map<Integer, TerminalNode> terminalNodes = new HashMap<>();

    public PortNode(Port port) {
        var terminalLayer = new VBox();

        var gridCanvas = createGridCanvas(port);

        for (var terminal: port.terminals()) {
            var node = new TerminalNode(terminal);
            terminalNodes.put(terminal.getIdTerminal(), node);
            terminalLayer.getChildren().add(node);
        }

        transferLayer.setPickOnBounds(false);
        getChildren().addAll(gridCanvas, terminalLayer, transferLayer);
    }

    public Optional<TerminalNode> getTerminalNode(int idTerminal) {
        return Optional.ofNullable(terminalNodes.get(idTerminal));
    }

    public void transferShip(Ship ship, int idTerminalFrom, Position from,
                             int idTerminalTo, Position to, Runnable ack) {
        var fromTerminalNode = terminalNodes.get(idTerminalFrom);
        var toTerminalNode = terminalNodes.get(idTerminalTo);

        var shipNodeOptional = fromTerminalNode.removeShip(ship);

        if (shipNodeOptional.isEmpty())
            throw new IllegalStateException(
                    "Ship IMO %d needs to be part of terminal node ID %d before transfer is attempted"
                            .formatted(ship.getImo(), idTerminalFrom));

        var node = shipNodeOptional.get();

        /*
            First we get pixel coordinates of from and to positions relative to
            local bounds of terminal nodes they belong to.

            After that we relativize the coordinates to the scene that holds these terminal nodes.

            Last step is to relativize the scene coordinates to
            transfer pane local coordinates where transfer animation is then executed
         */

        var start = fromTerminalNode.localToScene(fromTerminalNode.centerPointOf(node, from));
        var end = toTerminalNode.localToScene(toTerminalNode.centerPointOf(node, to));

        var transferStart = transferLayer.sceneToLocal(start);
        var transferEnd = transferLayer.sceneToLocal(end);

        node.relocate(transferStart.getX(), transferStart.getY());
        transferLayer.getChildren().add(node);

        var transition = new TranslateTransition(ship.getStepDuration(), node);

        transition.setFromX(0);
        transition.setFromY(0);

        transition.setToX(transferEnd.getX() - transferStart.getX());
        transition.setToY(transferEnd.getY() - transferStart.getY());

        transition.setOnFinished(_ -> {
            transferLayer.getChildren().remove(node);

            node.setTranslateX(0);
            node.setTranslateY(0);

            toTerminalNode.attachShip(node, to);

            ack.run();
        });

        transition.play();
    }

    // One canvas instance for the whole port is constructed
    private Canvas createGridCanvas(Port port) {
        var terminals = port.terminals();

        var columns = terminals.isEmpty()
                ? 0 : terminals.getFirst().getTotalColumns();

        var rows = terminals.isEmpty()
                ? 0 : terminals.getFirst().getTotalRows();

        var width = columns * TILE_SIZE;
        var height = terminals.size() * rows * TILE_SIZE;

        var canvas = new Canvas(width, height);
        var context = canvas.getGraphicsContext2D();

        var cellSprites = CellSprites.getInstance();

        var rowOffset = 0;
        for (var terminal: terminals) {
            for (var row = 0; row < rows; row++) {
                for (var col = 0; col < columns; col++) {
                    var pos = new Position(row, col);
                    var type = terminal.getCellType(pos);

                    var image = cellSprites.of(type);

                    var x = col * TILE_SIZE;
                    var y = (rowOffset + row) * TILE_SIZE;

                    context.drawImage(image, x, y, TILE_SIZE, TILE_SIZE);
                }
            }

            rowOffset += rows;
        }

        return canvas;
    }
}

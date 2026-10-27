package portsim.ui.node;

import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

// Pane that handles UI visualization of port simulation through panning and zooming
@NullMarked
public final class SimulationPane extends Pane {
    private static final double ZOOM_SPEED = 1.1; // Zoom per mouse-wheel notch
    private static final double NOTCH_DELTA = 40.0; // deltaY of one mouse-wheel notch

    private final Scale scale = new Scale(1, 1, 0, 0);

    @Nullable
    private PortNode content;

    private double sceneAnchorX, sceneAnchorY;
    private double contentAnchorX, contentAnchorY;

    public SimulationPane() {
        setCursor(Cursor.OPEN_HAND);

        setupPanning();
        setupZooming();
    }

    public void setContent(PortNode node) {
        content = node;

        content.getTransforms().add(scale);
        getChildren().add(content);
    }

    private void setupPanning() {
        setOnMousePressed(event -> {
            if (content == null || !event.isPrimaryButtonDown()) return;

            sceneAnchorX = event.getSceneX();
            sceneAnchorY = event.getSceneY();

            contentAnchorX = content.getTranslateX();
            contentAnchorY = content.getTranslateY();

            setCursor(Cursor.CLOSED_HAND);
        });

        setOnMouseDragged(event -> {
            if (content == null || !event.isPrimaryButtonDown()) return;

            var deltaX = event.getSceneX() - sceneAnchorX;
            var deltaY = event.getSceneY() - sceneAnchorY;

            content.setTranslateX(contentAnchorX + deltaX);
            content.setTranslateY(contentAnchorY + deltaY);
        });

        setOnMouseReleased(_ -> setCursor(Cursor.OPEN_HAND));
    }

    // Performed relative to mouse cursor position
    private void setupZooming() {
        setOnScroll(event -> {
            if (content == null) return;

            var zoomFactor = Math.pow(ZOOM_SPEED, event.getDeltaY() / NOTCH_DELTA);

            var newScale = scale.getX() * zoomFactor;
            if (newScale == scale.getX()) return;

            // Get local content coordinates of cursor position
            var pointInContent = content.sceneToLocal(event.getSceneX(), event.getSceneY());

            // Rescale the content to top-left anchor point
            scale.setX(newScale);
            scale.setY(newScale);

            // Because of rescaling cursor position has drifted
            var pointInScene = content.localToScene(pointInContent);

            // Shift the content in opposite direction of drifting
            content.setTranslateX(content.getTranslateX() + (event.getSceneX() - pointInScene.getX()));
            content.setTranslateY(content.getTranslateY() + (event.getSceneY() - pointInScene.getY()));

            event.consume();
        });
    }
}

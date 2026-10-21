package portsim.ui.node;

import javafx.scene.image.ImageView;
import org.jspecify.annotations.NullMarked;
import portsim.model.ship.Ship;
import portsim.ui.sprite.ShipSprites;

@NullMarked
public final class ShipNode extends ImageView {
    private static final double FIT_WIDTH = 60;

    private final Ship ship;

    public ShipNode(Ship ship) {
        this.ship = ship;

        var image = ShipSprites.getInstance().of(ship.getClass());

        setImage(image);

        setFitWidth(FIT_WIDTH);
        setPreserveRatio(true);
        setSmooth(true);
    }

    public Ship getShip() {
        return ship;
    }

    public void orientRight() {
        if (getScaleX() > 0)
            flipHorizontal();
    }

    public void orientLeft() {
        if (getScaleX() < 0)
            flipHorizontal();
    }

    private void flipHorizontal() {
        setScaleX(-getScaleX());
    }
}

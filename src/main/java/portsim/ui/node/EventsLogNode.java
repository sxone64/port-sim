package portsim.ui.node;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

/*
    Specialized VBox that displays simulation event messages via Label nodes.
    Displays only a fixed number of latest event messages
 */
@NullMarked
public final class EventsLogNode extends VBox {
    private static final int DEFAULT_MAX_MESSAGES = 5;

    private final List<String> messages = new ArrayList<>();

    private int maxMessages;

    public EventsLogNode() {
        this(DEFAULT_MAX_MESSAGES);
    }

    public EventsLogNode(int maxMessages) {
        setMaxMessages(maxMessages);
    }

    // Needed in order to make maxMessages a property for FXML
    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        if (maxMessages <= 0)
            throw new IllegalArgumentException("Max number of messages must be at least one");

        this.maxMessages = maxMessages;

        int excess = messages.size() - maxMessages;
        if (excess > 0)
            messages.subList(0, excess).clear();

        var children = getChildren();

        // Max number of messages reduced
        while (children.size() > maxMessages)
            children.removeLast();

        // Max number of messages increased
        while (children.size() < maxMessages) {
            var label = new Label();

            label.setWrapText(true);
            label.setVisible(false);

            children.add(label);
        }

        refresh();
    }

    public void addMessage(String message) {
        if (messages.size() == maxMessages)
            messages.removeFirst();

        messages.add(message);
        refresh();
    }

    public void clear() {
        messages.clear();
        refresh();
    }

    private void refresh() {
        var children = getChildren();

        for (var i = 0; i < maxMessages; i++) {
            var label = (Label) children.get(i);

            var occupied = i < messages.size();
            var labelText = occupied ? messages.get(i) : "";

            label.setText(labelText);

            if (label.isVisible() != occupied)
                label.setVisible(occupied);
        }
    }
}

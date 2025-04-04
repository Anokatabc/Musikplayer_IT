/*
package org.example.musikplayer_doit.services;

import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MouseEventService(MouseEvent event) {

    // Assuming you have a Pane that spans the scene
    Pane pane = new Pane();
    Rectangle selectionRect = new Rectangle(0, 0, 0, 0);
selectionRect.set(Color.BLUE);
selectionRect.setStrokeWidth(1);
selectionRect.setFill(Color.rgb(0, 0, 255, 0.1)); // semi-transparent fill
pane.getChildren().add(selectionRect);

    final double[] start = new double[2];

pane.setOnMousePressed(event -> {
        // Record the starting point for the selection rectangle
        start[0] = event.getX();
        start[1] = event.getY();
        // Set starting position of the selection rectangle
        selectionRect.setX(start[0]);
        selectionRect.setY(start[1]);
        selectionRect.setWidth(0);
        selectionRect.setHeight(0);
    });

pane.setOnMouseDragged(event -> {
        // Calculate the drag distance from the starting point
        double offsetX = event.getX() - start[0];
        double offsetY = event.getY() - start[1];

        // If dragging left/up from the start point, adjust the rectangle's origin
        if (offsetX < 0) {
            selectionRect.setX(event.getX());
            selectionRect.setWidth(-offsetX);
        } else {
            selectionRect.setX(start[0]);
            selectionRect.setWidth(offsetX);
        }

        if (offsetY < 0) {
            selectionRect.setY(event.getY());
            selectionRect.setHeight(-offsetY);
        } else {
            selectionRect.setY(start[1]);
            selectionRect.setHeight(offsetY);
        }
    });

pane.setOnMouseReleased(event -> {
        // Finalize the selection
        System.out.println("Selection bounds: " + selectionRect.getX() + ", " +
                selectionRect.getY() + ", " + selectionRect.getWidth() + ", " + selectionRect.getHeight());
        // Optionally, you can remove or hide the rectangle after processing selection
    });

}
*/
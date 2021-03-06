/* Copyright (C) 2005-2011 Fabio Riccardi */

package com.lightcrafts.ui.browser.view;

import com.lightcrafts.platform.Platform;
import com.lightcrafts.ui.browser.model.ImageDatum;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collections;

class ImageBrowserMouseListener implements MouseInputListener {

    private AbstractImageBrowser browser;
    private ImageBrowserSelectionModel selection;
    private ImageDatumControl controller;

    ImageBrowserMouseListener(
        AbstractImageBrowser browser,
        ImageBrowserSelectionModel selection,
        ImageDatumControl controller
    ) {
        this.browser = browser;
        this.selection = selection;
        this.controller = controller;
    }

    public void mouseClicked(MouseEvent event) {
        if (controller.isControllerEvent(event)) {
            controller.handleEvent(event);
        }
        else if (event.getClickCount() == 2) {
            Point p = event.getPoint();
            int index = browser.getIndex(p);
            ImageDatum datum = browser.getImageDatum(index);
            if (datum != null) {
                browser.notifyDoubleClicked(datum);
            }
        }
    }

    public void mousePressed(MouseEvent event) {
        browser.requestFocusInWindow();

        if (controller.isControllerEvent(event)) {
            controller.handleEvent(event);
        }
        else {
            // Identify which ImageDatum got the click:
            Point p = event.getPoint();
            int index = browser.getIndex(p);
            if (index < 0) {
                selection.clearSelected();
                return;
            }
            ImageDatum datum = browser.getImageDatum(index);

            // Scroll that ImageDatum's thumbnail into full view:
            Rectangle bounds = browser.getBounds(index);
            browser.scrollRectToVisible(bounds);

            // Figure out how to update the selection state:
            if (event.isShiftDown()) {
                browser.addContinuousSelected(datum);
            }
            else if (
                ((! isMac()) && event.isControlDown()) ||
                 (isMac() && event.isMetaDown() && ! event.isPopupTrigger())) {
                // This is ctrl-click on windows/linux, command-click on
                // mac, filtering out synthetic command-clicks from multi-
                // button mice.
                if (selection.isSelected(datum)) {
                    selection.removeSelected(datum);
                }
                else {
                    selection.addSelected(datum);
                }
            }
            else {
                if (
                    event.isPopupTrigger() ||
                    (isWindows() && (event.getButton() != MouseEvent.BUTTON1))
                ) {
                    // A popup trigger on a selected ImageDatum does nothing
                    // to selection, but a popup trigger on an unselected
                    // ImageDatum selects it and unselects everything else.
                    if (! selection.isSelected(datum)) {
                        selection.setSelected(Collections.singletonList(datum));
                        selection.setLeadSelected(datum);
                    }
                }
                else if (! selection.isSelected(datum)) {
                    // All other mouse presses select the ImageDatum.
                    selection.setLeadSelected(datum);
                }
            }
        }
        if (event.isPopupTrigger()) {
            browser.showPopup(event);
        }
    }

    public void mouseReleased(MouseEvent event) {
        if (controller.isControllerEvent(event)) {
            controller.handleEvent(event);
        }
        else if (event.isPopupTrigger()) {
            browser.showPopup(event);
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent event) {
        // This means, "don't control any ImageDatum"
        controller.setIndex(-1);
    }

    public void mouseDragged(MouseEvent event) {
        if (controller.isControllerEvent(event)) {
            controller.handleEvent(event);
        }
    }

    public void mouseMoved(MouseEvent event) {
        if (controller.isControllerEvent(event)) {
            controller.handleEvent(event);
        }
    }

    private static boolean isMac() {
        return Platform.getType() == Platform.MacOSX;
    }

    private static boolean isWindows() {
        return Platform.getType() == Platform.Windows;
    }
}

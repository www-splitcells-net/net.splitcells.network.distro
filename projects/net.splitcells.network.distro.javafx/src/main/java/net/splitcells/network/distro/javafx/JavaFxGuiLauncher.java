/*
 * Copyright (c) 2021 Contributors To The `net.splitcells.*` Projects
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License v2.0 or later
 * which is available at https://www.gnu.org/licenses/old-licenses/gpl-2.0-standalone.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.javafx;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.splitcells.dem.Dem;
import net.splitcells.network.distro.Distro;

import java.util.concurrent.Semaphore;

public class JavaFxGuiLauncher extends Application {
    private static final String DEFAULT_URL = "http://localhost:8443/index.html";
    private static final String DEFAULT_STYLE = "-fx-font-size: 18;";

    public static void main(String... args) {
        Application.launch(args);
    }

    /**
     * {@link WebView#setPrefSize(double, double)} is not set to {@link Double#MAX_VALUE},
     * because big numbers can cause an exception.
     *
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        final var initSemaphore = new Semaphore(1);
        try {
            initSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            Dem.process(() -> {
                Distro.service().start();
                initSemaphore.release();
                Dem.waitIndefinitely();
            }, Distro::configuratorForUsers);
        }).start();
        try {
            initSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        primaryStage.setTitle("Splitcells Network JavaFX Distro");
        final var webView = new WebView();
        final var webEngine = webView.getEngine();
        webEngine.load(DEFAULT_URL);
        webView.setPrefSize(5000, 5000);
        //webView.set
        final var resetButton = new Button("⟳");
        resetButton.setStyle(DEFAULT_STYLE);
        final var refreshButton = new Button("↻");
        refreshButton.setStyle(DEFAULT_STYLE);
        final var loadUrlButton = new Button("⏎");
        loadUrlButton.setStyle(DEFAULT_STYLE);
        final var url = new TextField();
        url.setStyle(DEFAULT_STYLE);
        final var gridPane = new GridPane();
        gridPane.add(resetButton, 0, 0);
        gridPane.add(refreshButton, 1, 0);
        gridPane.add(loadUrlButton, 2, 0);
        gridPane.add(url, 3, 0);
        gridPane.add(webView, 0, 1, 4, 1);
        Scene scene = new Scene(gridPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

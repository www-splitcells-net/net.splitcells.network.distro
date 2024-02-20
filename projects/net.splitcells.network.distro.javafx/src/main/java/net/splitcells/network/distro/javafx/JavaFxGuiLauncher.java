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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.splitcells.dem.Dem;
import net.splitcells.network.distro.java.Distro;

import java.util.concurrent.Semaphore;

import static net.splitcells.dem.utils.ExecutionException.executionException;

/**
 * The user is prevent from opening any page online,
 * in order to avoid confusion and prevent the user from uploading data to the public unknowingly.
 */
public class JavaFxGuiLauncher extends Application {

    private static final String BASE_URL = "http://localhost:8443/";
    private static final String DEFAULT_URL = BASE_URL + "index.html";
    private static final String DEFAULT_STYLE = "-fx-font-size: 18;";

    private static final String PUBLIC_WEBSITE = "https://splitcells.net/";
    private static final String PUBLIC_WEBSITE2 = "http://splitcells.net/";

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
        final var initSemaphore = new Semaphore(0);
        final var serviceSemaphore = new Semaphore(0);
        final var backendThread = new Thread(() -> {
            Dem.process(() -> {
                try (final var service = Distro.serviceForLocalUsers()) {
                    service.start();
                    initSemaphore.release();
                    try {
                        serviceSemaphore.acquire();
                    } catch (InterruptedException e) {
                        throw executionException(e);
                    }
                }
            }, Distro::configuratorForLocalUsers);
        });
        backendThread.setDaemon(true);
        backendThread.start();
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
        final var resetButton = new Button("↻");
        resetButton.setStyle(DEFAULT_STYLE);
        resetButton.setTooltip(new Tooltip("Open starting page, in order to reset the user interface."));
        final var refreshButton = new Button("⟳");
        refreshButton.setStyle(DEFAULT_STYLE);
        refreshButton.setTooltip(new Tooltip("Refresh the current page."));
        final var loadUrlButton = new Button("⏎");
        loadUrlButton.setTooltip(new Tooltip("Open page of entered URL."));
        loadUrlButton.setStyle(DEFAULT_STYLE);
        final var previousInHistory = new Button("↢");
        previousInHistory.setStyle(DEFAULT_STYLE);
        previousInHistory.setTooltip(new Tooltip("Go backwards in page history."));
        final var nextInHistory = new Button("↣");
        nextInHistory.setStyle(DEFAULT_STYLE);
        final var url = new TextField();
        url.setStyle(DEFAULT_STYLE);
        {
            nextInHistory.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    /* TODO This does not work. "go(1)" always throws an exception,
                     * even if the user goes backward in the history before.
                     * "history.forward()" does not do anything
                     *
                     *  webEngine.getHistory().go(1);
                     * webEngine.executeScript("history.forward()");
                     * Platform.runLater(() -> {
                     *    webEngine.executeScript("history.forward()");
                     * });
                     */
                }
            });
            previousInHistory.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    webEngine.getHistory().go(-1);
                }
            });
            previousInHistory.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    webEngine.getHistory().go(-1);
                }
            });
            resetButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    webEngine.load(DEFAULT_URL);
                }
            });
            loadUrlButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    webEngine.load(containUserUrl(url.getCharacters().toString()));
                }
            });
            webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> stringWebEvent) {
                    url.setText(containUserUrl(webEngine.getLocation()));
                }
            });
            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                    newValue = containUserUrl(newValue);
                    url.setText(newValue);
                    webEngine.load(newValue);
                }
            });
            url.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent keyEvent) {
                    if (keyEvent.getCode().getCode() == 10) {
                        webEngine.load(containUserUrl(url.getCharacters().toString()));
                    }
                }
            });
        }
        final var gridPane = new GridPane();
        gridPane.add(previousInHistory, 0, 0);
        // TODO This button is disabled, as its functionality does not work: gridPane.add(nextInHistory, 1, 0);
        gridPane.add(refreshButton, 1, 0);
        gridPane.add(resetButton, 2, 0);
        gridPane.add(loadUrlButton, 3, 0);
        gridPane.add(url, 4, 0);
        gridPane.add(webView, 0, 1, 5, 1);
        Scene scene = new Scene(gridPane);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                serviceSemaphore.release();
            }
        });
    }

    private static String enhanceUserEnteredUrl(String userInput) {
        if (!userInput.startsWith("http")) {
            return "http://" + userInput;
        }
        return userInput;
    }


    private static String containUserUrl(String url) {
        url = enhanceUserEnteredUrl(url);
        if (url.startsWith(PUBLIC_WEBSITE)) {
            url = BASE_URL + url.substring(PUBLIC_WEBSITE.length());
        } else if (url.startsWith(PUBLIC_WEBSITE2)) {
            url = BASE_URL + url.substring(PUBLIC_WEBSITE2.length());
        }
        if (!url.startsWith(BASE_URL)) {
            url = DEFAULT_URL;
        }
        return url;
    }
}

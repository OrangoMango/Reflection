// File managed by WebFX (DO NOT EDIT MANUALLY)

module Reflection.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;
    requires webfx.extras.canvas.pane;
    requires webfx.platform.resource;
    requires webfx.platform.scheduler;

    // Exported packages
    exports com.orangomango.reflectiongame;
    exports com.orangomango.reflectiongame.core;
    exports com.orangomango.reflectiongame.core.inventory;
    exports com.orangomango.reflectiongame.ui;

    // Resources packages
    opens audio;
    opens images;
    opens misc;

    // Provided services
    provides javafx.application.Application with com.orangomango.reflectiongame.MainApplication;

}
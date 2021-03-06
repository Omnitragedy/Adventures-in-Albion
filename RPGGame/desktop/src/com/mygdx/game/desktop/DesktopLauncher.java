package com.mygdx.game.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.RPGGame;

public class DesktopLauncher {

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.useGL30 = true;
        config.foregroundFPS = 60;
        config.backgroundFPS = -1;

        config.width = 728;
        config.height = 450;
//        config.addIcon("RPG Icon.png", Files.FileType.Internal);

        new LwjglApplication(new RPGGame(), config);

    }
}

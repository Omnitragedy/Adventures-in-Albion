package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Screens.StartScreen;

public class RPGGame extends Game {
        public static final int WIDTH = 485;
        public static final int HEIGHT = 300;
        
	public SpriteBatch batch;
        
        public static final byte LEDGE_BIT = 1;
        public static final byte HERO_BIT = 2;
        public static final byte BARREL_BIT = 4;
        public static final byte ENEMY_BIT = 8;
        public static final byte DEAD_BIT = 16;
        
        public static final int BASE_HP = 100;
        
	
	@Override
        //creates the new game once the program is started
	public void create () {
            batch = new SpriteBatch();
            setScreen(new StartScreen(this));
	}

	@Override
        //called when the game should be rendered
	public void render () {
            super.render();
	}
}

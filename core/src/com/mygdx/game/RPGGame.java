package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.Screens.PlayScreen;

public class RPGGame extends Game {
        public static final int V_WIDTH = 400;
        public static final int V_HEIGHT = 300;
        
	public SpriteBatch batch;
        
        public static final int LEDGE_BIT = 1;
        public static final int HERO_BIT = 2;
        public static final int BARREL_BIT = 4;
        public static final int DESTROYED_BIT = 8;
        public static final int ENEMY_BIT = 16;
        public static final int DEAD_BIT = 32;
        public static final int WEAPON_BIT = 64;
        
        public static final int BASE_HP = 100;
        
	
	@Override
        //creates the new game once the program is started
	public void create () {
            batch = new SpriteBatch();
            setScreen(new PlayScreen(this));
	}

	@Override
        //called when the game should be rendered
	public void render () {
            super.render();
	}
}

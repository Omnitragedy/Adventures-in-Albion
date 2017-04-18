/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mygdx.Sprites.Enemies;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.Sprites.Hero;
import com.mygdx.game.Screens.PlayScreen;

/**
 *
 * @author Saurav
 */
public abstract class Enemy extends Sprite {
    protected World world;
    protected PlayScreen screen;
    
    public Body b2body;
    
    public Enemy(PlayScreen initScreen, String TextureRegion) {
        super(initScreen.getAtlas().findRegion(TextureRegion));
        
        this.world = initScreen.getWorld();
        this.screen = initScreen;
        
        
        world = screen.getWorld();
    }
    
    /**
     * Update the position (and appearance) of the sprite
     * @param deltaT delta time in step
     * @param HeroCopy copy of the hero object
    */
    public abstract void update(double deltaT, Hero HeroCopy);
    
    /**
     * Should be called when the enemy is hurt somehow. Each call decrements the
     * health by the same amount.
     */
    public abstract void onEnemyHit();
    
    /**
     * Should be called when the enemy is killed. This method is intended to
     * keep the enemy from moving once its health has reached zero.
     */
    public abstract void onEnemyKilled();
}

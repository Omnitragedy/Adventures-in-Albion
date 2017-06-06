/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.Sprites.Characters;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.Sprites.State;

/**
 *
 * @author sumughan1714
 */
public abstract class GenericCharacter extends Sprite {
    public GenericCharacter(TextureRegion texture) {
        super(texture);
    }
    
    /**
     * Returns the appropriate frame for the current animation based on the time
     * step provided as a parameter
     * @param deltaT delta time
     * @return the texture to be rendered
     */
    protected abstract TextureRegion getFrame(double deltaT);
    
    /**
     * Returns the current state of the character. By using this method, it can
     * be determined what action the character is currently carrying out
     * @return the current state of the character.
     */
    public abstract State getState();
    
    /**
     * Returns the Box2D physics body
     * @return the Box2D physics body
     */
    public abstract Body getBody();
}

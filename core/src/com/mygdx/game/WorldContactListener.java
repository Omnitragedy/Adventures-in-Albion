/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.mygdx.Sprites.Characters.Enemies.Grunt;
import com.mygdx.Sprites.Characters.Hero.Hero;

/**
 *
 * @author Saurav
 */
public class WorldContactListener implements ContactListener {
    
    /**
     * Called when two objects come into contact
     * @param contact union of the two collided objects
     */
    @Override
    public void beginContact(Contact contact) {
//        System.out.println("\n----Begin Contact----");
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        
//        System.out.println(fixA.getUserData());
//        System.out.println(fixB.getUserData());
        
        if((fixA.getUserData() instanceof Grunt || fixB.getUserData() instanceof Grunt)){   //done to ensure that there is no ClassCastException
            if(fixA.getUserData() instanceof Hero || fixB.getUserData() instanceof Hero) {
                Fixture HeroFix = fixA.getUserData() instanceof Hero ? fixA : fixB;         //ternary used to determine which is the hero
                Hero hero = (Hero) HeroFix.getUserData();
                if(!hero.getIsInvincible() || hero.getStateTimer() > 2) {                   //only decrements the health if the player is not invincible
                    hero.decrementHP(10);
                }
            }
        }
    }
    
    /**
     * Called when two objects break contact
     * @param contact union of the two collided objects
     */
    @Override
    public void endContact(Contact contact) {
//        System.out.println("----End Contact----");
    }
    
    
    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        
    }
}

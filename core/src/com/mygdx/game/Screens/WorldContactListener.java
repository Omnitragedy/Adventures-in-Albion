/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mygdx.game.Screens;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.mygdx.Sprites.Enemies.Enemy;
import com.mygdx.Sprites.Hero;
import com.mygdx.Sprites.State;

/**
 *
 * @author Saurav
 */
public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        System.out.println("\n----Begin Contact----");
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();
        
        System.out.println(fixA.getUserData());
        System.out.println(fixB.getUserData());
        
        if((fixA.getUserData() instanceof Enemy || fixB.getUserData() instanceof Enemy)){
            if(fixA.getUserData() instanceof Hero || fixB.getUserData() instanceof Hero) {
                Fixture HeroFix = fixA.getUserData() instanceof Hero ? fixA : fixB;
                Hero hero = (Hero) HeroFix.getUserData();
                if(!hero.getState().equals(State.INVINCIBLE) || hero.getStateTimer() > 2)
                    hero.decrementHP(10);
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        System.out.println("----End Contact----");
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        
    }
}

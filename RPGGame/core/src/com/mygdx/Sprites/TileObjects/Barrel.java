/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mygdx.Sprites.TileObjects;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.RPGGame;

/**
 *
 * @author Saurav
 */
public class Barrel extends InteractiveTile {
    public Barrel(World world, TiledMap map, Rectangle bounds) {
        super(world, map, bounds);
        fixture.setUserData(this);
        setCategoryFilter(RPGGame.BARREL_BIT);
        setMaskFilter(RPGGame.HERO_BIT |
                RPGGame.ENEMY_BIT);
    }
    
    @Override
    public void onBodyHit() {
        setCategoryFilter(RPGGame.DESTROYED_BIT);
        getCell().setTile(null);
    }
}

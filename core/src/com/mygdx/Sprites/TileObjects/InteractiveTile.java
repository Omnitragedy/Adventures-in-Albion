/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mygdx.Sprites.TileObjects;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.Screens.PlayScreen;

/**
 *
 * @author Saurav
 */
public abstract class InteractiveTile {
    protected World world;
    protected TiledMap map;
    protected Rectangle bounds;
    protected Body body;
    protected PlayScreen screen;
    protected MapObject object;

    protected Fixture fixture;

    public InteractiveTile(World world, TiledMap map, Rectangle bounds){
        this.world = world;
        this.map = map;
        this.bounds = bounds;

        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();

        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set((bounds.getX() + bounds.getWidth() / 2), (bounds.getY() + bounds.getHeight() / 2));

        body = world.createBody(bdef);

        shape.setAsBox(bounds.getWidth() / 2, bounds.getHeight() / 2);
        fdef.shape = shape;
        fixture = body.createFixture(fdef);

    }
    
    /**
     * Handles what happens when the character sprite collides with another body
     */
    public abstract void onBodyHit();
    
    public void setCategoryFilter(int filterBit) {
        Filter filter = new Filter();
        filter.categoryBits = (short) filterBit;
        filter.maskBits = fixture.getFilterData().maskBits;
        fixture.setFilterData(filter);
    }
    
    public void setMaskFilter(int maskBit) {
        Filter filter = new Filter();
        filter.categoryBits = fixture.getFilterData().categoryBits;
        filter.maskBits = (short) maskBit;
        fixture.setFilterData(filter);
    }
    
    public Cell getCell() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(6);
        return layer.getCell((int)(body.getPosition().x / 32), (int)(body.getPosition().y / 32));
    }
}

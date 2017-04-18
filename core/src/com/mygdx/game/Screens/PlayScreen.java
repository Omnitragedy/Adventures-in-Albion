/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ai.steer.behaviors.Evade;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.Sprites.Enemies.Grunt;
import com.mygdx.Sprites.Hero;
import com.mygdx.Sprites.State;
import com.mygdx.Sprites.TileObjects.Barrel;
import com.mygdx.game.RPGGame;
import com.mygdx.game.Scenes.StatusBar;
import java.util.ArrayList;

/**
 *
 * @author Saurav
 */
public class PlayScreen implements Screen {
    //Reference to the game, used to set the Screens
    private RPGGame game;
    private TextureAtlas atlas;
    
    //sets the main components of GUI
    private OrthographicCamera gamecam;
    private Viewport gamePort;
    private StatusBar status;
    
    //Vars to handle the Tiled Map
    private TmxMapLoader maploader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    
    //Box2d variables for handling physics
    private World world;
    private Box2DDebugRenderer b2dr;
    
    private Hero player;
    private ArrayList<Grunt> grunts = new ArrayList<Grunt>();
    protected Fixture fixture;
    
    
    private final int lowerBoundSpeed;
    private final int upperBoundSpeed;
    
    /**
     * This constructor takes in the new game as a parameter and sets all of the
     * game's initial states such as the camera position and game physics
     * @param game newly created game object
     */
    public PlayScreen(RPGGame game) {
        atlas = new TextureAtlas("CharacterSprites/Hero_and_Enemies.atlas");
        
        this.game = game;
        
        //creates a camera that will follow the character sprite
        gamecam = new OrthographicCamera();
        
        //creates a ViewPort that maintains the aspect ratio of the screen
        gamePort = new FitViewport(RPGGame.V_WIDTH, RPGGame.V_HEIGHT, gamecam);
        gamePort.apply();
        
        //creates the game's status bar
        status = new StatusBar(game.batch);
        
        maploader = new TmxMapLoader();
        map = maploader.load("Game Map v1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1);
        
        //positions the gamecam
        gamecam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);
        
        world = new World(new Vector2(0, 0),true);
        
        //shows the outlines of the in-game bodies
        b2dr = new Box2DDebugRenderer();
        b2dr.setDrawVelocities(true);
        
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;
        
        //sets the bodies for all of the ledges (layer 5 = ledges)
        for(MapObject object : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fdef.shape = shape;
            fdef.filter.categoryBits = RPGGame.LEDGE_BIT;
            body.createFixture(fdef);
        }
        
        //sets the bodies for all of the barrels (layer 6 = barrels)
        for(MapObject object : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)){
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            
            new Barrel(world, map, rect);
        }
        
        //Initialization of Hero object
        player = new Hero(this);
        
        //Initialization of enemy/enemies
        lowerBoundSpeed = (int) (player.getMaxLinearSpeed() - 25);
        upperBoundSpeed = (int) (player.getMaxLinearSpeed() - 25);
        
        //creates 3 initial enemies
        grunts.add(new Grunt(this, rnd(580, 590), rnd(200, 350), rnd(lowerBoundSpeed, upperBoundSpeed)));
        grunts.add(new Grunt(this, rnd(580, 590), rnd(200, 300), rnd(lowerBoundSpeed, upperBoundSpeed)));
        grunts.add(new Grunt(this, rnd(580, 590), rnd(150, 200), rnd(lowerBoundSpeed, upperBoundSpeed)));
        
        world.setContactListener(new WorldContactListener());
        
        for(int i = 0; i < grunts.size(); i++) {
            Pursue<Vector2> purseBehavior = new Pursue<Vector2>(player, grunts.get(i), 10);
            grunts.get(i).setBehavior(purseBehavior);
        }
    }
    
        public TextureAtlas getAtlas() {
        return atlas;
    }
    
    @Override
    public void show() {
        
    }
    
    /**
     * Moves the on-screen sprite in the direction of the pressed arrow key
     * @param deltaT time between step cycles
     */
    public void handleInput(double deltaT) {
        String playerStateAsString = player.getState().name();
        if(playerStateAsString.length() >= 5) {
            if((player.getState() != State.SPEAR_END) && playerStateAsString.substring(0, 5).equals("SPEAR")) {
                player.getBody().setLinearVelocity(Vector2.Zero);
            } else if(player.getState().equals(State.SPEAR_END) && !Gdx.input.isKeyPressed(Input.Keys.SPACE))
                player.setCanStartAnimation(true);
            else{
                //Impulses make the sprite move instantaneously, but forces make it move gradually

                //allows the character sprite to move up and down
                if(!playerStateAsString.substring(0,5).equals("SPEAR")) {
                    if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
                        //applies an "upward" force on the sprite if the "UP" button is pressed
                        player.getBody().setLinearVelocity(new Vector2(player.getBody().getLinearVelocity().x, 1));
                    } else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                        //applies an "downward" force on the sprite if the "DOWN" button is pressed
                        player.getBody().setLinearVelocity(new Vector2(player.getBody().getLinearVelocity().x, -1));
                    }
                }
            }
            if(!playerStateAsString.substring(0,5).equals("SPEAR")) {
                //allows the character sprite to move left and right
                if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                    //applies an "leftward" force on the sprite if the "LEFT" button is pressed
                    player.getBody().setLinearVelocity(new Vector2(-1, player.getBody().getLinearVelocity().y));
                } else if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                    //applies an "rightward" force on the sprite if the "RIGHT" button is pressed
                    player.getBody().setLinearVelocity(new Vector2(1, player.getBody().getLinearVelocity().y));
                }
            }

            //ensure that the length of the vector is always 55 N when the character moves any direction
            if(!playerStateAsString.substring(0,5).equals("SPEAR"))
                player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().setLength(55));

            //if no input is given for a certain direction, the sprite stops moving in that direction
            if((player.getBody().getLinearVelocity().x < 0 && !Gdx.input.isKeyPressed(Input.Keys.LEFT))
                    || (player.getBody().getLinearVelocity().x > 0 && !Gdx.input.isKeyPressed(Input.Keys.RIGHT))) {
                player.getBody().setLinearVelocity(new Vector2(0, player.getBody().getLinearVelocity().y));
            }
            if((player.getBody().getLinearVelocity().y < 0 && !Gdx.input.isKeyPressed(Input.Keys.DOWN))
                    || (player.getBody().getLinearVelocity().y > 0 && !Gdx.input.isKeyPressed(Input.Keys.UP))) {
                player.getBody().setLinearVelocity(new Vector2(player.getBody().getLinearVelocity().x, 0));
            }
        } else if (player.getState().equals(State.DEAD)) {
            for(Grunt g : grunts) {
                Evade<Vector2> evade = new Evade<Vector2>(player, g);
                g.setBehavior(evade);
            }
        }
    }
    
    /**
     * Calls the appropriate methods so that the components of the game all render
     * In addition, if one of the specified inputs in handleInput is used, then
     * that change is effected in the game.
     * @param deltaT 
     */
    public void update(double deltaT) {
        //handles any user input first
        handleInput(deltaT);
        
        //makes the GUI update every sixtieth of a second
        //the second and third variables set the time that the program spends on the
        //velocity and position calculations, respectively
        world.step(1/60f, 10, 5);
        
        player.update(deltaT);
        for(Grunt grunt : grunts) {
            grunt.update(deltaT, player);
        }
        
        gamecam.position.x = player.getBody().getPosition().x;
        gamecam.position.y = player.getBody().getPosition().y;
        
        gamecam.update();
        renderer.setView(gamecam);
    }
    
    @Override
    //Clears the screen and renders the new screen
    //this render method rerenders the screen at a set interval, making this the
    //a big loop that keeps the graphics in sync with the data
    
    public void render(float delta) {
        update(delta);
        
        //clears the screen and fills it black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
        
        //render the game map
        renderer.render();
        
        //render box2d debug lines
        b2dr.render(world, gamecam.combined);
        
        game.batch.setProjectionMatrix(gamecam.combined);
        game.batch.begin();
        player.draw(game.batch);
        for(Grunt g : grunts) {
            g.draw(game.batch);
        }
        game.batch.end();
        
        game.batch.setProjectionMatrix(status.stage.getCamera().combined);
        status.stage.draw();
    }
    
    /**
     * Changes the size of the window if the window is resized by the user
     * @param width
     * @param height 
     */
    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }
    
    /**
     * Returns the game map
     * @return Tiled game map 
     */
    public TiledMap getMap(){
        return map;
    }
    
    public StatusBar getStatus() {
        return status;
    }
    
    /**
     * Returns the World
     * @return the game world
     */
    public World getWorld(){
        return world;
    }

    @Override
    public void pause() {
        
    }

    @Override
    public void resume() {
        
    }

    @Override
    public void hide() {
        
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        status.dispose();
    }
    
    private int rnd(int lb, int ub) {
        return (int)(Math.random()*(ub+1))+lb;
    }
    
}

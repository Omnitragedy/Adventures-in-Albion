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
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.Sprites.Characters.Enemies.Grunt;
import com.mygdx.Sprites.Characters.Hero.Hero;
import com.mygdx.Sprites.State;
import com.mygdx.game.RPGGame;
import com.mygdx.game.WorldContactListener;
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
    
    //timers based off of the built-in step timer
    private double playtimeLeftTimer;   //used to check how much time is left
    
    //Vars to handle the Tile Map
    private TmxMapLoader maploader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    
    //Box2d variables for handling physics
    private World world;
//    private Box2DDebugRenderer b2dr;
    
    private Hero player;
    private ArrayList<Grunt> GruntsList = new ArrayList<Grunt>();
    private int enemyCount;
    
    private float timeSinceLastEnemySpawn;
    private static final int MAX_ENEMIES = 30;
    
    //Bounds for the range in between what the max speeds for the enemies could be
    private final int lowerBoundSpeed;
    private final int upperBoundSpeed;
    
    /**
     * This constructor takes in the new game as a parameter and sets all of the
     * game's initial states such as the camera position and game physics
     * @param game newly created game object
     */
    public PlayScreen(RPGGame game) {
        atlas = new TextureAtlas("CharacterSprites/Hero_and_Enemies.pack");
        
        this.game = game;
        
        //creates a camera that will follow the character sprite
        gamecam = new OrthographicCamera();
        
        //creates a ViewPort that maintains the aspect ratio of the screen
        gamePort = new FitViewport(RPGGame.WIDTH, RPGGame.HEIGHT, gamecam);
        gamePort.apply();
        
        //creates the game's status bar
        status = new StatusBar(game.batch);
        
        maploader = new TmxMapLoader();
        map = maploader.load("Game Map v1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1);
        
        //positions the gamecam
        gamecam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);
        
        world = new World(new Vector2(0, 0),true);
        
          //===========IMPORTANT TO DEVELOPMENT PROCESS===========
//        //shows the outlines of the in-game bodies
//        b2dr = new Box2DDebugRenderer();
//        b2dr.setDrawVelocities(true);
        
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;
        
        //sets the bodies for all of the ledges (layer 6 = ledges)
        for(MapObject object : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
			
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fdef.shape = shape;
            fdef.filter.categoryBits = RPGGame.LEDGE_BIT;
            body.createFixture(fdef);
        }
        
        //sets the bodies for all of the barrels (layer 7 = barrels)
        for(MapObject object : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)){
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fdef.shape = shape;
            fdef.filter.categoryBits = RPGGame.LEDGE_BIT;
            body.createFixture(fdef);
        }
        
        //Initialization of Hero object
        player = new Hero(this);
        
        //Initialization of enemy/enemies
        lowerBoundSpeed = (int) (player.getMaxLinearSpeed() - 20);
        upperBoundSpeed = (int) (player.getMaxLinearSpeed() - 15);
        
        //creates 10 initial enemies
        addEnemyNearHero(10);
        
        world.setContactListener(new WorldContactListener());
        
        for(int i = 0; i < GruntsList.size(); i++) {
            Pursue<Vector2> purseBehavior = new Pursue<Vector2>(player, GruntsList.get(i), 10);
            GruntsList.get(i).setBehavior(purseBehavior);
        }
        
        playtimeLeftTimer = 61;    //sets it so that the game can only go on for 60 seconds
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
        
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            player.setRunning();
        else
            player.setWalking();
        
        if(Gdx.input.isKeyJustPressed(Input.Keys.A))
            reduceTimeLeft();
        
        float playerMaxSpeed = player.getMaxLinearSpeed();
        
        if(playerStateAsString.length() >= 5) {
            if((player.getState() != State.SPEAR_END) && playerStateAsString.substring(0, 5).equals("SPEAR")) {
                player.getBody().setLinearVelocity(Vector2.Zero);
            }
            
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
                player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().setLength(playerMaxSpeed));

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
            for(Grunt g : GruntsList) {
                Evade<Vector2> evade = new Evade<Vector2>(player, g);
                g.setBehavior(evade);
            }
        }
    }
    
    /**
     * Basically the driver for the project's functions to run. All of the rendering
     * and actions are based on the cycle speed specified in this method. I do
     * not have to call this method explicitly as the framework calls this method
     * internally.
     * 
     * Calls the appropriate methods so that the components of the game all render
     * In addition, if one of the specified inputs in handleInput is used, then
     * that change is effected in the game.
     * @param deltaT 
     */
    public void update(double deltaT) {
        
        /*
        Specifies the length of time step that the update method should render
        the second and third variables set the time that the program spends on the
        velocity and position calculations, respectively
        */
        world.step(1/60f, 5, 2);
        
        timeSinceLastEnemySpawn += deltaT;
        playtimeLeftTimer -= deltaT;
        status.updateTimer((int) playtimeLeftTimer);
        
        //handles any user input first
        handleInput(deltaT);
        
        player.update(deltaT);
        
        /*
        Determines how many enemies should be added to the screen. First checks if the number on the screen right now
        exceeds the preset limit and if at least a certain amount of time has passes since the last spawning
        */
        if(GruntsList.size() <= MAX_ENEMIES && timeSinceLastEnemySpawn > 1.5 && !player.isDead()) {     //used to add a variable number of enemies
            int enemiesToAdd;
            if(GruntsList.size() < 5) {             //if there are less than 5 enemies on the screen, 
                enemiesToAdd = 6;
            } else if (GruntsList.size() < 10) {    //if there are less than 10 enemies on the screen 
                enemiesToAdd = 3;
            }
            else {                                  //if there are any more enemies than 10
                enemiesToAdd = 1;
            }
            
            addEnemyNearHero(enemiesToAdd);    //adds this many enemies during the cycle
            for(int i = 0; i < GruntsList.size(); i++) {
                Pursue<Vector2> purseBehavior = new Pursue<Vector2>(player, GruntsList.get(i), 10);
                GruntsList.get(i).setBehavior(purseBehavior);
            }
            timeSinceLastEnemySpawn = 0;
        }
        
        /*
        If there are any dead enemies, they are deleted from the ArrayList of enemies.
        */
        for(int i = GruntsList.size()-1; i > -1; i--) {
            if(GruntsList.get(i).getIsDead()) {
                world.destroyBody(GruntsList.get(i).getBody());
                GruntsList.remove(i);
                status.incrementEnemiesKilled(); 
            }
        }
        
        /*
        Updates all of the enemies on the screen (ie. renders them all)
        */
        for(Grunt grunt : GruntsList) {
            grunt.update(deltaT, player);
        }
        
        /*
        Centers the camera so that the player stays at the center of the screen
        */
        gamecam.position.x = player.getBody().getPosition().x;
        gamecam.position.y = player.getBody().getPosition().y;
        
        gamecam.update();
        renderer.setView(gamecam);
    }
    
    /**
     * When called, an enemy is added near the location of the player.
     * The method will add a new enemy the number of times specified in the "times" argument.
     */
    private void addEnemyNearHero(int times) {
        for(int i = 0; i < times; i++) {
            GruntsList.add(new Grunt(this, rnd((int) player.getPosition().x - 5, (int) player.getPosition().x + 5),
                    rnd((int) player.getPosition().y - 5, (int) player.getPosition().y + 5), rnd(lowerBoundSpeed, upperBoundSpeed)));
        }
    }
    
    /**
     * Clears the screen and renders the new screen
     * this render method re-renders the screen at a set interval, making this the
     * a big loop that keeps the graphics in sync with the data
     * @param deltaT 
     */
    @Override
    public void render(float deltaT) {
        update(deltaT);  //renders the components of the game
        
        
        Gdx.gl.glClearColor(0, 0, 0, 1);            //makes background black
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);   //clears the previous pixels
        
        int[] backgroundLayers = {0,1,2,3,4};       //render these layers first
        int[] foregroundLayers = {5};               //render this layer after the characters are rendered
        
        //render the game map
        renderer.render(backgroundLayers);          //render the background ground tiles
        
//        //render box2d debug lines
//        b2dr.render(world, gamecam.combined);
        
        game.batch.setProjectionMatrix(gamecam.combined);       //spills the sprites for the hero and enemies from the SpriteBatch
        game.batch.begin();
        for(Grunt g : GruntsList) {
            g.draw(game.batch);                     //render each enemy individually
        }
        player.draw(game.batch);                    //render the hero's sprite
        game.batch.end();
        
        renderer.render(foregroundLayers);         //render the foreground layers at the very end
        
        game.batch.setProjectionMatrix(status.getStage().getCamera().combined);
        status.getStage().draw();
        
        if(isGameOver()) {
            //display the game over screen once it is "game over"
            game.setScreen(new GameOverScreen(game, status.getEnemiesKilled()));
        }
    }
    
    private void reduceTimeLeft() {
        System.out.println("Y0U 4R3 A B4D H4X0R");
        playtimeLeftTimer = 6;
    }
    
    /**
     * Checks if the game is over. These are the conditions constituting a game over state
     * If only one of these conditions is true, it is considered a game over:
     * <li>The player has no health left.</li>
     * <li>The timer timer has reached zero</li>
     * 
     * This method is meant to be used to check if the game over screen should be displayed
     * @return whether or not the game has finished
     */
    private boolean isGameOver() {
        return (player.getHP() <= 0 && player.getStateTimer() > 3.5f) || playtimeLeftTimer < 0; //waits 3 seconds befor the game over screen is shown
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
    
    /**
     * Returns the StatusBar GUI
     * @return StatusBar GUI
     */
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
    }
    
    /**
     * Generates a random number in between the two numbers, the lower and upper bounds
     * @param lb lower bound
     * @param ub upper bound
     * @return a random number in between the lower bound and the upper bound
     */
    private int rnd(double lb, double ub) {
        return (int)((Math.random()*(ub+1))+lb);
    }
    
    /**
     * Increments the number of enemies by 1. Should be called when an enemy is added to the screen.
     */
    public void incrementEnemyCount() {
        enemyCount++;
    }
    
    /**
     * Decrements the number of enemies by 1. Should be called when an enemy is killed.
     */
    public void decrementEnemyCount() {
        enemyCount--;
    }
}

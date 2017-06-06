/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mygdx.Sprites.Characters.Enemies;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Evade;
import com.badlogic.gdx.ai.steer.behaviors.Pursue;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.Sprites.Characters.GenericCharacter;
import com.mygdx.Sprites.Characters.Hero.Hero;
import com.mygdx.Sprites.State;
import com.mygdx.game.RPGGame;
import com.mygdx.game.Screens.PlayScreen;

/**
 *
 * @author Saurav
 */
public class Grunt extends GenericCharacter implements Steerable<Vector2> {
    private World world;
    private PlayScreen screen;
    
    private Body enemyBody;
    
    private Hero Hero;
    
    private int hp;
    
    private float stateTimer;
    
    public State currentState;
    public State previousState;
    
    private TextureRegion Standing;
    
    private Animation WalkUp, WalkRight, WalkDown, WalkLeft;
    private static final int X_OFFSET = 0;    //835
    private static final int Y_OFFSET = 0;
    
    private boolean canHit;
    private boolean isDying;
    private boolean isDead;
    
    //the instance variables below are used for the AI calculations;
    private float orientationAngle;
    private float zeroLinearSpeedThreshold;
    private Vector2 currentVelocity;
    private float maxLinearSpeed, maxLinearAcceleration;
    private float maxAngularSpeed, maxAngularAcceleration;
    
    private final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
    private SteeringBehavior<Vector2> steeringBehavior;
    
    
    public Grunt(PlayScreen initScreen, float x, float y, float initMaxLinSpeed) {
        /*
        Textures from: http://gaurav.munjal.us/Universal-LPC-Spritesheet-Character-Generator/
        */
        super(initScreen.getAtlas().findRegion("HeroSprites"));
        
        this.world = initScreen.getWorld();
        this.screen = initScreen;
        
        
        world = screen.getWorld();
        
        hp = RPGGame.BASE_HP;
        
        orientationAngle = (float) (3 * Math.PI / 2);
        zeroLinearSpeedThreshold = 5;
        currentVelocity = new Vector2();
        maxLinearSpeed = initMaxLinSpeed;
        maxLinearAcceleration = maxLinearSpeed;
        maxAngularSpeed = Float.MAX_VALUE;
        maxAngularAcceleration = maxAngularSpeed;
        
        canHit = true;
        isDying = false;
        
        defineCharacter(x, y);
        
        
        Array<TextureRegion> frames = new Array<TextureRegion>();   //used as temporary storage for animation frames
        
        //set the "Walk Left" animation
        for(int i = 1; i < 8; i++) 
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 9 * 64 + Y_OFFSET, 64, 64));    //param: texture, x, y, width, height
        WalkLeft = new Animation(0.12f, frames);
        frames.clear();
        
        //set the "Walk Right" animation
        for(int i = 1; i < 8; i++) 
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 11 * 64 + Y_OFFSET, 64, 64));    //param: texture, x, y, width, height
        WalkRight = new Animation(0.12f, frames);
        frames.clear();
        
        Standing = new TextureRegion(getTexture(), 0 + X_OFFSET, 10 * 64+3 + Y_OFFSET, 64, 64);
        super.setBounds(0, 0, 40, 40);
        
        super.setRegion(Standing);
    }
    
    /**
     * Makes the grunt's enemyBody and sets the location of the grunt at (x,y)
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    private void defineCharacter(float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(x, y);      //spawns the Grunt at this position
        bdef.type = BodyDef.BodyType.DynamicBody;
        
        enemyBody = world.createBody(bdef);
        bdef.bullet = true;     //makes sure that the enemy body will not tunnel through another object
        
        
        FixtureDef fdef = new FixtureDef();
        PolygonShape feet = new PolygonShape();
        feet.setAsBox(8, 5, new Vector2(0, -16), 0);
        fdef.shape = feet;
        
        fdef.filter.categoryBits = RPGGame.ENEMY_BIT;
        fdef.filter.maskBits = RPGGame.LEDGE_BIT |
                RPGGame.BARREL_BIT |
                RPGGame.ENEMY_BIT |
                RPGGame.HERO_BIT;

        
        fdef.restitution = 1f;      //defines how well the enemy bounces
        enemyBody.createFixture(fdef).setUserData(this);
    }
    
    /**
     * Returns the Vector indicating linear velocity
     * @return Vector indicating linear velocity
     */
    @Override
    public Vector2 getLinearVelocity() {
        return enemyBody.getLinearVelocity();
    }

    
    @Override
    public float getAngularVelocity() {
        return enemyBody.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius() {
        return 6;
    }

    @Override
    public boolean isTagged() {
        return false;
    }

    @Override
    public void setTagged(boolean tagged) {
        
    }

    @Override
    public Vector2 getPosition() {
        return enemyBody.getPosition();
    }

    @Override
    public float getOrientation() {
        return orientationAngle;
    }

    @Override
    public void setOrientation(float orientation) {
        orientationAngle = orientation;
    }

    /** Returns the angle in radians pointing along the specified vector.
     * @param vector the vector
     * @return the angle
     */
    @Override
    public float vectorToAngle(Vector2 vector) {
        return (float)Math.atan2(-vector.x, vector.y);
    }
    
    /** Returns the unit vector in the direction of the specified angle expressed in radians.
     * @param outVector the output vector.
     * @param angle the angle in radians.
     * @return the output vector for chaining.
     */
    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = -(float)Math.sin(angle);
        outVector.y = (float)Math.cos(angle);
        return outVector;
    }
    
    @Override
    public float getZeroLinearSpeedThreshold() {
        return zeroLinearSpeedThreshold;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        zeroLinearSpeedThreshold = value;
    }

    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }
    
    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }
    
    /** Creates a new location.
	 * <p>
	 * This method is used internally to instantiate locations of the correct type parameter {@code T}. This technique keeps the API
	 * simple and makes the API easier to use with the GWT backend because avoids the use of reflection.
	 * @return the newly created location. */
    @Override
    public Location<Vector2> newLocation() {
        return (Location<Vector2>) enemyBody.getPosition();
    }
    
    /**
     * Renders the grunt and furthers the animation one predefined step cycle
     * @param deltaT delta Time
     * @param Hero copy of the Hero object
     */
    public void update(double deltaT, Hero Hero) {
        this.Hero = Hero;
        
        if(steeringBehavior != null) {
            if(this.getPosition().dst(Hero.getPosition()) < 21 && canHit
                    && (Hero.getState().name().contains("SPEAR") && !Hero.getState().equals(State.SPEAR_END))) {
                checkEnemyHit();
                canHit = false;
            } else if(this.getPosition().dst(Hero.getPosition()) > 50) {
                setBehavior(new Pursue<Vector2>(Hero, this));
                canHit = true;
            }

            // Calculate steering acceleration
            steeringBehavior.calculateSteering(steeringOutput);

            // Apply steering acceleration to move this agent
            applySteering();
        }
        
        super.setPosition(enemyBody.getPosition().x - getWidth() / 2, enemyBody.getPosition().y - getHeight() / 2);
        
        
        setRegion(getFrame(deltaT));    //sets the appropriate sprite
    }
    
    /**
     * Returns the appropriate frame for the current animation based on the time
     * step provided as a parameter
     * @param deltaT delta time
     * @return the texture to be rendered
     */
    @Override
    public TextureRegion getFrame(double deltaT) {
        currentState = getState();
        
        TextureRegion region;
        
        switch(currentState) {
            case WALK_UP:
                if(currentState != previousState)
                    Standing = new TextureRegion(getTexture(), 0 + X_OFFSET, 8 * 64 + Y_OFFSET, 64, 64);
                region = (TextureRegion) WalkUp.getKeyFrame(stateTimer, true);
                break;
            case WALK_RIGHT:
                if(currentState != previousState)
                    Standing = new TextureRegion(getTexture(), 0 + X_OFFSET, 11 * 64 + Y_OFFSET, 64, 64);
                region = (TextureRegion) WalkRight.getKeyFrame(stateTimer, true);
                break;
            case WALK_DOWN:
                if(currentState != previousState)
                    Standing = new TextureRegion(getTexture(), 0 + X_OFFSET, 10 * 64 + Y_OFFSET, 64, 64);
                region = (TextureRegion) WalkDown.getKeyFrame(stateTimer, true);
                break;
            case WALK_LEFT:
                if(currentState != previousState)
                    Standing = new TextureRegion(getTexture(), 0 + X_OFFSET, 9 * 64 + Y_OFFSET, 64, 64);
                region = (TextureRegion) WalkLeft.getKeyFrame(stateTimer, true);
                break;
            case STANDING:
            default:
                region = Standing;
                break;
        }
        
        stateTimer = currentState == previousState ? stateTimer + (float) deltaT : 0;
        previousState = currentState;
        return region;
    }
    
    /**
     * Returns the current state of the character. By using this method, it can
     * be determined what action the character is currently carrying out
     * @return the current state of the character.
     */
    @Override
    public State getState() {
        currentVelocity = enemyBody.getLinearVelocity().cpy();
        
        if(currentVelocity.isZero()) {
            return State.STANDING;
        }
        if(currentVelocity.x > 0) {
                return State.WALK_RIGHT;
            }
            else if(currentVelocity.x < 0) {
                return State.WALK_LEFT;
            }
            else if(currentVelocity.y > 0) {
                return State.WALK_UP;
            }
            else {
                return State.WALK_DOWN;
            }
        
    }
    
    /**
     * Takes the steering output received from the AI calculations to adjust the course
     * of the enemy such that it moves towards the hero.
     */
    private void applySteering () {
        Vector2 finalVector = enemyBody.getLinearVelocity().sub(steeringOutput.linear);    //subtract the steering vector from the current velocity
        finalVector.setLength(maxLinearSpeed);
        
        currentVelocity.set(finalVector);
        enemyBody.setLinearVelocity(currentVelocity);
    }
    
    /**
     * Returns the Box2D physics body
     * @return the Box2D physics body
     */
    @Override
    public Body getBody() {
        return enemyBody;
    }
    
    /**
     * Change the steering behavior of the grunt. Primarily used are pursue and evade,
     * so this method is used to switch between the two steering behaviors for the enemy.
     * @param behavior 
     */
    public void setBehavior(SteeringBehavior<Vector2> behavior) {
        steeringBehavior = behavior;
    }
    
    /**
     * Returns the enemy's current steering behavior (ie. pursue or evade)
     * @return 
     */
    public SteeringBehavior<Vector2> getBehavior() {
        return steeringBehavior;
    }
    
    /**
     * Should be called when the enemy is hurt somehow. Each call decrements the
     * health by the same amount.
     */
    public void checkEnemyHit() {
            enemyBody.setLinearVelocity(new Vector2(0, 0));
            hp -= 50;

            if(hp == 0)
                onEnemyKilled();

            setBehavior(new Evade<Vector2>(Hero, this));
    }
    
    /**
     * Returns the current hp of the grunt
     * @return hp of the grunt
     */
    public int getHP() {
        return hp;
    }
    
    /**
     * Should be called when the enemy is killed. This method is intended to
     * keep the enemy from moving once its health has reached zero.
     */
    public void onEnemyKilled() {
//        world.destroyBody(enemyBody);
        isDying = true;
        stateTimer = 0; //used to check how long the goomba has been dead
        
        setBehavior(null);  //stops velocity calculations
        enemyBody.setActive(false);    //makes it so that the grunt stops moving
        enemyBody.setAwake(false);
        
        screen.decrementEnemyCount();
    }
    
    @Override
    public void draw(Batch batch) {
        if(!isDying) {
            super.draw(batch);
        } else if(stateTimer < 0.75) { //contines the "flashing out" animation for one second
            /*
            EXPLAIN IN PRESENTATION
            The alpha values act as a type of multiplies for the other RGB values, but the alpha values are always cut down
            to a value between 0 and 1. THis means that if the alpha value goes from a greater than 1 value all the way to 0,
            at least one of the alpha values will have repeated by the time the alpha value gets to 0.
            
            Eg: start from an alpha value of 3 and go down to 0. You will have gone from full opacity to full transparency once
            you get to 2, and this will happen again between 2 and 1, and once again between 1 and 0.
            */
            super.setAlpha(4 - (4 * stateTimer));   //makes the enemy flash at a rate of 4 times per second
            
            super.draw(batch);
        } else if(stateTimer < 1.4) {
            super.setAlpha(9 - (9 * stateTimer));   //makes the enemy flash at a rate of 9 times per second
            
            super.draw(batch);
        } else
            isDead = true;
    }
    
    /**
     * Returns whether or not the enemy has died
     * @return whether or not the enemy has died
     */
    public boolean getIsDead() {
        return isDead;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.Sprites.Characters.Hero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.Sprites.Characters.GenericCharacter;
import com.mygdx.Sprites.State;
import com.mygdx.game.RPGGame;
import com.mygdx.game.Screens.PlayScreen;

/**
 *
 * @author Saurav
 */
public class Hero extends GenericCharacter implements Steerable<Vector2> {
    private World world;
    private PlayScreen screen;
    private Body HeroBody;
    
    private int hp;
    
    private State currentState;
    private State previousState;
    
    private boolean canStartAnim = true;
    private TextureRegion Standing;
    
    private static final int X_OFFSET = 835;
    private static final int Y_OFFSET = 0;
    
    private static Animation WalkUp, WalkRight, WalkDown, WalkLeft;
    private static Animation SpearUp, SpearRight, SpearDown, SpearLeft;
    private static Animation Dead;
    
    private float stateTimer;
    
    private boolean isRecoiling;
    private double recoilTimer = 0;         //used to check if enough time has elapsed for the next stab by the hero
    
    private float invincibleTimer;
    private boolean isInvincible;
    
    private int walkDir;    //1 - Up, 2 - Right, 3 - Down, 4 - Left
    
    //the instance variables below are used for the AI calculations;
    private boolean isTagged = false;
    private float orientationAngle = (float) (3 * Math.PI / 2);
    private float zeroLinearSpeedThreshold = 0;
    private float maxLinearSpeed = 55;
    private float maxLinearAcceleration = 4;
    private float maxAngularSpeed = (float) (Math.PI / 60);
    private float maxAngularAcceleration = Float.MAX_VALUE;
    
    
    public Hero(PlayScreen initScreen) {
        /*
        Textures from: http://gaurav.munjal.us/Universal-LPC-Spritesheet-Character-Generator/
        */
        super(initScreen.getAtlas().findRegion("HeroSprites"));
        
        world = initScreen.getWorld();
        this.screen = initScreen;
        
        hp = RPGGame.BASE_HP;
        
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        walkDir = 3;
        defineCharacter();
        
        Array<TextureRegion> frames = new Array<TextureRegion>();   //used as temporary storage for animation frames
        
        //set the "Walk Up" animation
        for(int i = 1; i < 9; i++) 
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 8 * 64 + Y_OFFSET, 64, 64));    //param: texture, x, y, width, height
        WalkUp = new Animation(0.12f, frames);
        frames.clear();
        
        //set the "Walk Left" animation
        for(int i = 1; i < 9; i++) 
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 9 * 64 + Y_OFFSET, 64, 64));    //param: texture, x, y, width, height
        WalkLeft = new Animation(0.12f, frames);
        frames.clear();
        
        //set the "Walk Down" animation
        for(int i = 1; i < 9; i++) 
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 10 * 64 + Y_OFFSET, 64, 64));    //param: texture, x, y, width, height
        WalkDown = new Animation(0.12f, frames);
        frames.clear();
        
        //set the "Walk Right" animation
        for(int i = 1; i < 9; i++) 
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 11 * 64 + Y_OFFSET, 64, 64));    //param: texture, x, y, width, height
        WalkRight = new Animation(0.12f, frames);
        frames.clear();
        
        //set the "Spear Up" animation
        for(int i = 0; i < 8; i++)
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 4 * 64 + Y_OFFSET, 64, 64));
        SpearUp = new Animation(0.15f, frames);
        frames.clear();
        
        //set the "Spear Left" animation
        for(int i = 0; i < 8; i++)
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 5 * 64 + Y_OFFSET, 64, 64));
        SpearLeft = new Animation(0.15f, frames);
        frames.clear();
        
        //set the "Spear Down" animation
        for(int i = 0; i < 8; i++)
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 6 * 64 + Y_OFFSET, 64, 64));
        SpearDown = new Animation(0.15f, frames);
        frames.clear();
        
        //set the "Spear Right" animation
        for(int i = 0; i < 8; i++)
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 7 * 64 + Y_OFFSET, 64, 64));
        SpearRight = new Animation(0.15f, frames);
        frames.clear();
        
        //set the "Dying" animation
        for(int i = 0; i < 6; i++)
            frames.add(new TextureRegion(getTexture(), i * 64 + X_OFFSET, 20 * 64 + 5 + Y_OFFSET, 64, 64));
        Dead = new Animation(0.15f, frames);
        frames.clear();
        
        Standing = new TextureRegion(getTexture(), 0 + X_OFFSET, 10 * 64 + 3 + Y_OFFSET, 64, 64);
        super.setBounds(0, 0, 40, 40);
        setRegion(Standing);
    }
    
    /**
     * Returns the Box2D physics body
     * @return the Box2D physics body
     */
    @Override
    public Body getBody() {
        return HeroBody;
    }
    
    /**
     * Renders the hero and furthers the animation one predefined step cycle
     * @param deltaT delta Time
     */
    public void update(double deltaT) {
        super.setPosition(HeroBody.getPosition().x - getWidth() / 2, HeroBody.getPosition().y - getHeight() / 2);
        
        setRegion(getFrame(deltaT));        //sets the appropriate sprite
        
        screen.getStatus().setHP(hp);
        if(hp <= 0) {
            for(Fixture f : HeroBody.getFixtureList()) {
                Filter deadFilter = new Filter();   //used to make sure that enemies can't hit you once you are dead
                deadFilter.categoryBits = RPGGame.DEAD_BIT;
                f.setFilterData(deadFilter);
            }
            HeroBody.setLinearVelocity(Vector2.Zero);
        }
        
        if(invincibleTimer > 2) {
            setNonInvincible();
        }
        
        if(isInvincible) {
            super.setAlpha(8 - (4 * invincibleTimer));     
        }
        if(hp <= 0 && stateTimer > 2) { //makes the player disappear after 2 seconds of being dead
            super.setAlpha(0);
        }
        
        if(isRecoiling) {
            recoilTimer += deltaT;
            if(recoilTimer > .35) {
                isRecoiling = false;
                recoilTimer = 0;
            }
        }
    }
    
    public void decrementHP(int hit) {
        if(!getIsInvincible()) {
            setInvincible();
            hp -= hit;
        }
    }
    
    /**
     * Makes the player invincible by changing the current state to invincible, resetting the
     * timer for how long the player will stay invincible, and makes the player immune to damage
     */
    private void setInvincible() {
        isInvincible = true;
        invincibleTimer = 0;
        
        for(Fixture f : HeroBody.getFixtureList()) {
            Filter InvincibleFilter = f.getFilterData();     //makes it so that the hero cannot collide with anything
            InvincibleFilter.maskBits = RPGGame.BARREL_BIT | RPGGame.LEDGE_BIT;
            
            f.setFilterData(InvincibleFilter);
        }
    }
    
    /**
     * Makes the player vulnerable to attack by enemies again.
     */
    private void setNonInvincible() {
        isInvincible = false;
        setAlpha(1);        //ensure that the hero becomes fully opaque once he is not invincible
        
        for(Fixture f : HeroBody.getFixtureList()) {
            Filter VulnerableFilter = f.getFilterData();     //makes it so that the hero can collide with enemies again
            VulnerableFilter.maskBits = RPGGame.LEDGE_BIT |
                RPGGame.BARREL_BIT |
                RPGGame.ENEMY_BIT;
            f.setFilterData(VulnerableFilter);
        }
    }
    
    /**
     * Returns the hp that the hero has left
     * @return hp left
     */
    public int getHP() {
        return hp;
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
        
        switch(getCurrentState()) {
            case DEAD:
                region = (TextureRegion) Dead.getKeyFrame(stateTimer, false);
                break;
            case SPEAR_UP:
                region = (TextureRegion) SpearUp.getKeyFrame(stateTimer, false);
                break;
            case SPEAR_RIGHT:
                region = (TextureRegion) SpearRight.getKeyFrame(stateTimer, false);
                break;
            case SPEAR_DOWN:
                region = (TextureRegion) SpearDown.getKeyFrame(stateTimer, false);
                break;
            case SPEAR_LEFT:
                region = (TextureRegion) SpearLeft.getKeyFrame(stateTimer, false);
                break;
            case WALK_UP:
                if(getCurrentState() != previousState)
                    Standing = new TextureRegion(getTexture(), 0 + X_OFFSET, 8 * 64 + Y_OFFSET, 64, 64);
                region = (TextureRegion) WalkUp.getKeyFrame(stateTimer, true);
                walkDir = 1;
                break;
            case WALK_RIGHT:
                if(getCurrentState() != previousState)
                    Standing = new TextureRegion(getTexture(), 0 + X_OFFSET, 11 * 64 + Y_OFFSET, 64, 64);
                region = (TextureRegion) WalkRight.getKeyFrame(stateTimer, true);
                walkDir = 2;
                break;
            case WALK_DOWN:
                if(getCurrentState() != previousState)
                    Standing = new TextureRegion(getTexture(), 0 + X_OFFSET, 10 * 64 + Y_OFFSET, 64, 64);
                region = (TextureRegion) WalkDown.getKeyFrame(stateTimer, true);
                walkDir = 3;
                break;
            case WALK_LEFT:
                if(getCurrentState() != previousState)
                    Standing = new TextureRegion(getTexture(), 0 + X_OFFSET, 9 * 64 + Y_OFFSET, 64, 64);
                region = (TextureRegion) WalkLeft.getKeyFrame(stateTimer, true);
                walkDir = 4;
                break;
            case SPEAR_END:
            case STANDING:
            default:
                region = Standing;
                break;
        }
        
        stateTimer = getCurrentState() == previousState ? stateTimer + (float) deltaT : 0;  //adds time to the stateTimer if the current state didn't change
        if(getIsInvincible())
            invincibleTimer += (float) deltaT;  //adds time to the invincible timer if he is invincible
        
        previousState = getCurrentState();
        
        return region;
    }
    
    /**
     * Returns the current state of the character. By using this method, it can
     * be determined what action the character is currently carrying out
     * @return the current state of the character.
     */
    @Override
    public State getState() {
        if(hp <= 0) {
            return State.DEAD;
        }
        if(!HeroBody.getLinearVelocity().isZero()) {   //condition means that the player is walking
            if(HeroBody.getLinearVelocity().x > 0) {
                walkDir = 2;
                return State.WALK_RIGHT;
            }
            else if(HeroBody.getLinearVelocity().x < 0) {
                walkDir = 4;
                return State.WALK_LEFT;
            }
            else if(HeroBody.getLinearVelocity().y > 0) {
                walkDir = 1;
                return State.WALK_UP;
            }
            else {
                walkDir = 3;
                return State.WALK_DOWN;
            }
        } else {
            if(!isRecoiling && Gdx.input.isKeyPressed(Input.Keys.SPACE)) { //starts the action of thrusting the spear
                //checking if the hero is recoiling ensures that the player does not spam the spear button
                switch(getWalkDir()) {
                    case 1:
                        return State.SPEAR_UP;
                    case 2:
                        return State.SPEAR_RIGHT;
                    case 3:
                        return State.SPEAR_DOWN;
                    case 4:
                    default:
                        return State.SPEAR_LEFT;
                }
            } else if(!isRecoiling && (Gdx.input.isKeyPressed(Input.Keys.SPACE) && previousState.equals(State.SPEAR_END)) || (getAnimation().isAnimationFinished(stateTimer))) {
                isRecoiling = true;
                return State.SPEAR_END;
            }
            else if(!previousState.equals(State.SPEAR_END) && previousState.name().contains("SPEAR")) {
                //if the spear was being thrusted, the spear thrusting animation continues
                switch(getWalkDir()) {
                    case 1:
                        return State.SPEAR_UP;
                    case 2:
                        return State.SPEAR_RIGHT;
                    case 3:
                        return State.SPEAR_DOWN;
                    case 4:
                    default:
                        return State.SPEAR_LEFT;
                }
            }
            else
                return State.STANDING;
        }
    }
    
    /**
     * Sets the Shape if the character's feet "hitbox." Also sets it so that the
     * Hero can collide with enemies and background fixtures.
     */
    private void defineCharacter() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(910, 580); //set the coordinates for the spawn point of the player
        bdef.type = BodyDef.BodyType.DynamicBody;
        HeroBody = world.createBody(bdef);
        HeroBody.setUserData(this);
        
        //The fixture definition for the player's feet (aka the place that enemies come and hit)
        FixtureDef FeetFixture = new FixtureDef();
        
        //make sprite's feet the box that is checked for collision
        PolygonShape FeetFixtureShape = new PolygonShape();
        FeetFixtureShape.setAsBox(9, 5, new Vector2(0, -16), 0);
        FeetFixture.shape = FeetFixtureShape;
        
        FeetFixture.filter.categoryBits = RPGGame.HERO_BIT;    //identity of this
        
        //sets it so that the hero can't collide with destroyed blocks
        FeetFixture.filter.maskBits = RPGGame.LEDGE_BIT | //can collide with
                RPGGame.BARREL_BIT |
                RPGGame.ENEMY_BIT;
        
        
        HeroBody.createFixture(FeetFixture).setUserData(this);
    }
    
    /**
     * Returns the amount of time the character has been in its current state
     * @return the amount of time the character has been in its current state
     */
    public float getStateTimer() {
        return stateTimer;
    }
    
    /**
     * Should be used to get the appropriate animation to be rendered for the character
     * based on its current behavior. For example, if the character is moving right,
     * the "WalkRight" animation for the character will be rendered
     * @return the appropriate animation
     */
    public Animation getAnimation() {
        switch(getCurrentState()) {
            case WALK_UP:
                return WalkUp;
            case WALK_DOWN:
                return WalkDown;
            case WALK_LEFT:
                return WalkLeft;
            case WALK_RIGHT:
                return WalkRight;
            case SPEAR_UP:
                return SpearUp;
            case SPEAR_DOWN:
                return SpearDown;
            case SPEAR_LEFT:
                return SpearLeft;
            case SPEAR_RIGHT:
                return SpearRight;
            case STANDING: 
            case SPEAR_END:
            default:
                return new Animation(1, Standing);  //doesn't matter the time because there is only one frame in the animation
        }   
    }
    
    public boolean setCanStartAnimation(boolean newVal) {
        canStartAnim = newVal;
        return canStartAnim;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return HeroBody.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return HeroBody.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius() {
        return 20;
    }

    @Override
    public boolean isTagged() {
        return isTagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        isTagged = tagged;
    }

    @Override
    public Vector2 getPosition() {
        return new Vector2(HeroBody.getPosition().x, HeroBody.getPosition().y);
    }

    @Override
    public float getOrientation() {
        return orientationAngle;
    }

    @Override
    public void setOrientation(float orientation) {
        orientationAngle = orientation;
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return (float)Math.atan2(-vector.x, vector.y);
    }

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

    @Override
    public Location<Vector2> newLocation() {
        return (Location<Vector2>) HeroBody.getPosition();
    }
    
    public float calculateOrientationFromLinearVelocity(Hero hero) {
        // If we haven't got any velocity, then we can do nothing.
        if (HeroBody.getLinearVelocity().isZero(getZeroLinearSpeedThreshold()))
            return getOrientation();

        return vectorToAngle(getLinearVelocity());
    }
    
    /**
     * Returns whether or not the enemy will be hurt after it entered the spear's range
     * @return whether or not the enemy will get hurt
     */
    public boolean isAttacking() {
        return getCurrentState().name().contains("SPEAR");
    }

    /**
     * @return the currentState
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * @return the walkDir
     */
    public int getWalkDir() {
        return walkDir;
    }

    /**
     * @return isInvincible
     */
    public boolean getIsInvincible() {
        return isInvincible;
    }
    
    public void setWalking() {
        if(maxLinearSpeed != 55) {
            maxLinearSpeed = 55;
            /*
            The next four lines are used to make the walking animation slower so as
            to give the illusion that the hero is walking
            */
            WalkUp.setFrameDuration(WalkUp.getFrameDuration() * 1.2f);
            WalkRight.setFrameDuration(WalkRight.getFrameDuration() * 1.2f);
            WalkDown.setFrameDuration(WalkDown.getFrameDuration() * 1.2f);
            WalkLeft.setFrameDuration(WalkLeft.getFrameDuration() * 1.2f);
        }
    }
    
    public void setRunning() {
        if(maxLinearSpeed != 100) {
            maxLinearSpeed = 100;
            /*
            The next four lines are used to make the walking animation faster so as
            to give the illusion that the hero is running
            */
            WalkUp.setFrameDuration(WalkUp.getFrameDuration() / 1.2f);
            WalkRight.setFrameDuration(WalkRight.getFrameDuration() / 1.2f);
            WalkDown.setFrameDuration(WalkDown.getFrameDuration() / 1.2f);
            WalkLeft.setFrameDuration(WalkLeft.getFrameDuration() / 1.2f);
        }
    }
    
    /**
     * Returns whether or not the player is dead
     * @return whether or not the player is dead
     */
    public boolean isDead() {
        return hp <= 0;
    }
}

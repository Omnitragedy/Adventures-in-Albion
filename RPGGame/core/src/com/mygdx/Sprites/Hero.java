/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.Sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.RPGGame;
import com.mygdx.game.Screens.PlayScreen;

/**
 *
 * @author Saurav
 */
public class Hero extends Sprite implements Steerable<Vector2> {
    private World world;
    private PlayScreen screen;
    private Body b2body;
    
    private EdgeShape spearEdge;
    
    private int hp;
    
    public State currentState;
    public State previousState;
    
    private boolean canStartAnim = true;
    private TextureRegion Standing;
    
    private Animation WalkUp, WalkRight, WalkDown, WalkLeft;
    private Animation SpearUp, SpearRight, SpearDown, SpearLeft;
    private Animation Dead;
    private float stateTimer;
    private int walkDir;    //1 - Up, 2 - Right, 3 - Down, 4 - Left
    
    //the instance variables below are used for the AI calculations;
    private boolean isTagged = false;
    private float orientationAngle = (float) (3 * Math.PI / 2);
    private float zeroLinearSpeedThreshold = 0;
    private float maxLinearSpeed = 55;
    private float maxLinearAcceleration = 4;
    private float maxAngularSpeed = (float) (Math.PI / 60);
    private float maxAngularAcceleration = Float.MAX_VALUE;
    private boolean independentFacing;
    private static final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
    private SteeringBehavior<Vector2> steeringBehavior;
    
    public Hero(PlayScreen initScreen) {
        super(initScreen.getAtlas().findRegion("HeroSprites"));
        
        System.out.println(super.getTexture().toString());
        
        world = initScreen.getWorld();
        this.screen = initScreen;
        
        hp = 100;
        
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        walkDir = 3;
        setHero();
        
        Array<TextureRegion> frames = new Array<TextureRegion>();   //used as temporary storage for animation frames
        
        //set the "Walk Up" animation
        for(int i = 1; i < 9; i++) 
            frames.add(new TextureRegion(getTexture(), i * 64, 8 * 64, 64, 64));    //param: texture, x, y, width, height
        WalkUp = new Animation(0.12f, frames);
        frames.clear();
        
        //set the "Walk Left" animation
        for(int i = 1; i < 9; i++) 
            frames.add(new TextureRegion(getTexture(), i * 64, 9 * 64, 64, 64));    //param: texture, x, y, width, height
        WalkLeft = new Animation(0.12f, frames);
        frames.clear();
        
        //set the "Walk Down" animation
        for(int i = 1; i < 9; i++) 
            frames.add(new TextureRegion(getTexture(), i * 64, 10 * 64, 64, 64));    //param: texture, x, y, width, height
        WalkDown = new Animation(0.12f, frames);
        frames.clear();
        
        //set the "Walk Right" animation
        for(int i = 1; i < 9; i++) 
            frames.add(new TextureRegion(getTexture(), i * 64, 11 * 64, 64, 64));    //param: texture, x, y, width, height
        WalkRight = new Animation(0.12f, frames);
        frames.clear();
        
        //set the "Spear Up" animation
        for(int i = 0; i < 9; i++)
            frames.add(new TextureRegion(getTexture(), i * 64, 4 * 64, 64, 64));
        SpearUp = new Animation(0.15f, frames);
        frames.clear();
        
        //set the "Spear Left" animation
        for(int i = 0; i < 8; i++)
            frames.add(new TextureRegion(getTexture(), i * 64, 5 * 64, 64, 64));
        SpearLeft = new Animation(0.15f, frames);
        frames.clear();
        
        //set the "Spear Down" animation
        for(int i = 0; i < 8; i++)
            frames.add(new TextureRegion(getTexture(), i * 64, 6 * 64, 64, 64));
        SpearDown = new Animation(0.15f, frames);
        frames.clear();
        
        //set the "Spear Right" animation
        for(int i = 0; i < 8; i++)
            frames.add(new TextureRegion(getTexture(), i * 64, 7 * 64, 64, 64));
        SpearRight = new Animation(0.15f, frames);
        frames.clear();
        
        //set the "Spear Right" animation
        for(int i = 0; i < 6; i++)
            frames.add(new TextureRegion(getTexture(), i * 64, 20 * 64 + 5, 64, 64));
        Dead = new Animation(0.15f, frames);
        frames.clear();
        
        Standing = new TextureRegion(getTexture(), 0, 10 * 64 + 3, 64, 64);
        super.setBounds(0, 0, 40, 40);
        setRegion(Standing);
    }
    
    
    
    public Body getBody() {
        return b2body;
    }
    
    
    public void update(double deltaT) {
        super.setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
        
        setRegion(getFrame(deltaT));        //sets the appropriate sprite
        
        screen.getStatus().setHP(hp);
        if(hp <= 0) {
            for(Fixture f : b2body.getFixtureList()) {
                Filter deadFilter = new Filter();   //used to make sure that enemies can't hit you once you are dead
                deadFilter.categoryBits = RPGGame.DEAD_BIT;
                f.setFilterData(deadFilter);
            }
            b2body.setLinearVelocity(Vector2.Zero);
        }
    }
    
    public void decrementHP(int hit) {
        hp -= hit;
        currentState = State.INVINCIBLE;
    }
    
    public int getHP() {
        return hp;
    }
    
    public TextureRegion getFrame(double deltaT) {
        currentState = getState();
        
        TextureRegion region;
        
        switch(currentState) {
            case DEAD:
                region = (TextureRegion) Dead.getKeyFrame(stateTimer, false);
                break;
            case INVINCIBLE:
                region = (TextureRegion) getWalkAnim().getKeyFrame(stateTimer, true);
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
                if(currentState != previousState)
                    Standing = new TextureRegion(getTexture(), 0, 8 * 64, 64, 64);
                region = (TextureRegion) WalkUp.getKeyFrame(stateTimer, true);
                walkDir = 1;
                break;
            case WALK_RIGHT:
                if(currentState != previousState)
                    Standing = new TextureRegion(getTexture(), 0, 11 * 64, 64, 64);
                region = (TextureRegion) WalkRight.getKeyFrame(stateTimer, true);
                walkDir = 2;
                break;
            case WALK_DOWN:
                if(currentState != previousState)
                    Standing = new TextureRegion(getTexture(), 0, 10 * 64, 64, 64);
                region = (TextureRegion) WalkDown.getKeyFrame(stateTimer, true);
                walkDir = 3;
                break;
            case WALK_LEFT:
                if(currentState != previousState)
                    Standing = new TextureRegion(getTexture(), 0, 9 * 64, 64, 64);
                region = (TextureRegion) WalkLeft.getKeyFrame(stateTimer, true);
                walkDir = 4;
                break;
            case SPEAR_END:
            case STANDING:
            default:
                region = Standing;
                break;
        }
        
        stateTimer = currentState == previousState ? stateTimer + (float) deltaT : 0;
        previousState = currentState;
        return region;
    }
    
    private Animation getWalkAnim() {
        switch(walkDir) {
            case 1:
                return WalkUp;
            case 2:
                return WalkRight;
            case 3:
                return WalkDown;
            case 4:
            default:
                return WalkLeft;
        }
    }
    
    public State getState() {
        if(hp <= 0) {
            return State.DEAD;
        }
        if(!b2body.getLinearVelocity().isZero()) {   //condition means that the player is walking
            if(b2body.getLinearVelocity().x > 0) {
                walkDir = 2;
                return State.WALK_RIGHT;
            }
            else if(b2body.getLinearVelocity().x < 0) {
                walkDir = 4;
                return State.WALK_LEFT;
            }
            else if(b2body.getLinearVelocity().y > 0) {
                walkDir = 1;
                return State.WALK_UP;
            }
            else {
                walkDir = 3;
                return State.WALK_DOWN;
            }
        } else {
            if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) { //starts the action of thrusting the spear
                switch(walkDir) {
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
            else if(!previousState.equals(State.SPEAR_END) && previousState.name().substring(walkDir).equals("SPEAR")) {
                return previousState;
            } else if((Gdx.input.isKeyPressed(Input.Keys.SPACE) && previousState.equals(State.SPEAR_END)) || (getAnimation().isAnimationFinished(stateTimer)))
                return State.SPEAR_END;
            else if(!previousState.equals(State.SPEAR_END) && previousState.name().contains("SPEAR")) {
                //if the spear was being thrusted, the spear thrusting animation continues
                switch(walkDir) {
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
    
    private void setHero() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(719, 316); //set the coordinates for the spawn point of the player
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);
        
        FixtureDef fdef = new FixtureDef();
        
        
        //make sprite's feet the box that is checked for collision
        PolygonShape feet = new PolygonShape();
        feet.setAsBox(9, 5, new Vector2(0, -16), 0);
        fdef.shape = feet;
        
        fdef.filter.categoryBits = RPGGame.HERO_BIT;    //identity of this
        
        //sets it so that the hero can't collide with destroyed blocks
        fdef.filter.maskBits = RPGGame.LEDGE_BIT | //can collide with
                RPGGame.BARREL_BIT |
                RPGGame.ENEMY_BIT;
        
        b2body.createFixture(fdef).setUserData(this);
    }
    
    public void setSpearSensor() {
        FixtureDef fdef = new FixtureDef();
        
        EdgeShape shape = new EdgeShape();
        
        //make sprite's spear EndShape to check if it hits an enemy
        switch(currentState) {
            case SPEAR_UP:
            shape.set(-5,15,5,15);
        }
        fdef.shape = shape;
        b2body.createFixture(fdef);
    }
    
    public float getStateTimer() {
        return stateTimer;
    }
    
    public Animation getAnimation() {
        switch(currentState) {
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
                return new Animation(0.00001f, Standing);
        }   
    }
    
    public boolean getCanStartAnimation() {
        return canStartAnim;
    }
    
    public boolean setCanStartAnimation(boolean newVal) {
        canStartAnim = newVal;
        return canStartAnim;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return b2body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return b2body.getAngularVelocity();
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
        return new Vector2(b2body.getPosition().x, b2body.getPosition().y);
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
        return (Location<Vector2>) b2body.getPosition();
    }
    
    public float calculateOrientationFromLinearVelocity(Hero hero) {
        // If we haven't got any velocity, then we can do nothing.
        if (b2body.getLinearVelocity().isZero(getZeroLinearSpeedThreshold()))
            return getOrientation();

        return vectorToAngle(getLinearVelocity());
    }
}

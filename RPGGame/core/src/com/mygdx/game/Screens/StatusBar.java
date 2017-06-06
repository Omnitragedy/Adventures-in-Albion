/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.game.Screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.RPGGame;

/**
 *
 * @author Saurav
 */
public class StatusBar {
    private Stage stage;
    private Viewport viewport;
    
    private int enemiesKilled;
    private int hp;
    private static final int MAX_HP = 20;   //only a max of this many bars will be displayed
    
    private Label TimeLeftNum, EnemyCountNum, hpNum;
    
    private Label TimeLeftLabel, EnemyCountLabel, hpLabel;
    
    public StatusBar(SpriteBatch sb) {
        
        hp = 100;
        
        viewport = new FitViewport(RPGGame.WIDTH, RPGGame.HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);
        
        Table table = new Table();
        table.top();
        table.setFillParent(true);
        
        BitmapFont font = new BitmapFont();
        BitmapFont healthFont = new BitmapFont();
        healthFont.setFixedWidthGlyphs("I.");       //makes sure that the size of the hp characters does not vary
        
        final float SCALE_FACTOR = 1.3f;
        
        TimeLeftLabel = new Label("Time:", new LabelStyle(font, Color.SLATE));
        TimeLeftLabel.setFontScale(SCALE_FACTOR);
        EnemyCountNum = new Label("Enemies Killed:", new LabelStyle(font, Color.SLATE));
        EnemyCountNum.setFontScale(SCALE_FACTOR);
        hpLabel = new Label("Health:", new LabelStyle(font, Color.SLATE));
        hpLabel.setFontScale(SCALE_FACTOR);
        
        TimeLeftNum = new Label("0", new LabelStyle(font, Color.SLATE));
        TimeLeftNum.setFontScale(SCALE_FACTOR);
        EnemyCountLabel = new Label("0", new LabelStyle(font, Color.SLATE));
        EnemyCountLabel.setFontScale(SCALE_FACTOR);
        hpNum = new Label("" + hp, new LabelStyle(font, Color.WHITE));
        hpNum.setFontScale(SCALE_FACTOR);
        hpNum.setColor(Color.LIME);         //must be set separate from constructor or else the inital color blends with future colors
        
        
        table.add(hpLabel).expandX();
        table.add(EnemyCountNum).expandX();
        table.add(TimeLeftLabel).expandX();
        
        table.row();
        
        table.add(hpNum).expandX();
        table.add(EnemyCountLabel).expandX();
        table.add(TimeLeftNum).expandX();
        
        
        stage.addActor(table);
    }
    
    /**
     * To be called from the PlayScreen class.
     * Sets a new HP value after some events happens to the player
     * @param newHP new HP value
     */
    public void setHP(int newHP) {
        this.hp = newHP/5;
        String hpRep = new String();
        for(int i = 0; i < hp; i++)
            hpRep += "l";   //"\u25A0" is ■;
        for(int  i = hpRep.length(); i < MAX_HP; i++)
            hpRep += ".";
        hpNum.setText(hpRep);
        
        if(hp <= 6)
            hpNum.setColor(Color.RED);
        else if(hp <= 10) {
            hpNum.setColor(0, 0, 0, 1);
            hpNum.setColor(Color.CORAL);
        }
            
    }
    
    /**
     * To be called from the PlayScreen class.
     * Increments the count of the number of enemies killed and reflects the change
     * in the GUI.
     */
    public void incrementEnemiesKilled() {
        enemiesKilled++;
        this.EnemyCountLabel.setText("" + enemiesKilled);
    }
    
    public int getEnemiesKilled() {
        return enemiesKilled;
    }
    
    /**
     * To be called from the PlayScreen class.
     * Update the GUI part displaying the
     * time left before game over.
     * @param timeLeft time left to play
     */
    public void updateTimer(int timeLeft) {
        TimeLeftNum.setText("" + timeLeft);
    }

    /**
     * @return the stage
     */
    public Stage getStage() {
        return stage;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.game.Scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.RPGGame;

/**
 *
 * @author Saurav
 */
public class StatusBar implements Disposable{
    public Stage stage;
    private Viewport viewport;
    
    private Integer worldTimer;
    private Integer hp;
    private static final int MAX_HP = 20;   //only a max of this many bars will be displayed
    
    Label countdownLabel;
    Label hpAmountLabel;
    Label timeLabel;
    Label hpLabel;
    
    public StatusBar(SpriteBatch sb) {
        worldTimer = 300;
        hp = 100;
        
        viewport = new FitViewport(RPGGame.V_WIDTH, RPGGame.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);
        
        Table table = new Table();
        table.top();
        table.setFillParent(true);
        
        timeLabel = new Label("Arrows", new Label.LabelStyle(new BitmapFont(), Color.SLATE));
        hpLabel = new Label("Hero's Health", new Label.LabelStyle(new BitmapFont(), Color.SLATE));
        
        countdownLabel = new Label(worldTimer.toString(), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        hpAmountLabel = new Label(hp.toString(), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        
        
        
        table.add(hpLabel).expandX();
        table.add(timeLabel).expandX();
        
        table.row();
        
        table.add(hpAmountLabel).expandX();
        table.add(countdownLabel).expandX();
        
        
        stage.addActor(table);
    }
    
    public void setHP(int initHp) {
        this.hp = initHp/5;
        String hpRep = new String();
        for(int i = 0; i < hp; i++)
            hpRep += "I";   //"\u25A0" or ■;
        for(int  i = hpRep.length(); i < MAX_HP; i++)
            hpRep += ".";
        hpAmountLabel.setText(hpRep);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}

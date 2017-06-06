/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.RPGGame;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author Saurav
 */
public class GameOverScreen implements Screen {
    private Viewport viewport;
    private Stage stage;
    
    
    public GameOverScreen(RPGGame game, int enemiesKilled) {
        viewport = new FitViewport(RPGGame.WIDTH, RPGGame.HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, game.batch);
        
        
        LabelStyle font = new LabelStyle(new BitmapFont(), Color.WHITE);
        
        Table table = new Table();
        table.center();
        table.setFillParent(true);
        
        Label gameOverLabel = new Label("GAME OVER!", font);
        gameOverLabel.setFontScale(2.5f);
        
        Label enemiesKilledLabel = new Label("Enemies Killed: " + enemiesKilled, font);
        table.add(gameOverLabel).expandX();
        
        table.row();
        table.add(enemiesKilledLabel).expandX();
        table.row();
        
        Label highScoreLabel;
        
        FileReader r = null;
        try {
            r = new FileReader("Top Score.txt");
        } catch (FileNotFoundException ex) {
            System.out.println("CANNOT FIND FILE");
        }
        
        Scanner f = new Scanner(r);
        
        int oldHighScore = 0;
        if(f.hasNextInt())
            oldHighScore = f.nextInt();
        
        if(enemiesKilled > oldHighScore) {
            PrintWriter pw = null;
            try {
                pw = new PrintWriter("Top Score.txt");
            } catch (FileNotFoundException ex) {
                    System.out.println("ERROR IN WRITING");
            }
            pw.print(enemiesKilled);
            pw.close();
            
            highScoreLabel = new Label("New High Score!", font);
        } else {
            highScoreLabel = new Label("High Score: " + oldHighScore, font);
        }
        
        table.add(highScoreLabel);
        
        stage.addActor(table);
    }
    
    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
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
    
}

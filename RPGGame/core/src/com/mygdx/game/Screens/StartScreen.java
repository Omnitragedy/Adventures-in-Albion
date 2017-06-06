/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.RPGGame;
import javax.swing.JOptionPane;

/**
 *
 * @author Saurav
 */
public class StartScreen implements Screen {
    
    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    
    
    public StartScreen(final RPGGame game) {
        skin = new Skin(Gdx.files.internal("GUISkin.json"));
        
        viewport = new FitViewport(RPGGame.WIDTH, RPGGame.HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, game.batch);
        
        Table table = new Table();
        table.setFillParent(true);
        table.setWidth(stage.getWidth());
        table.align(Align.center);    //layout of children
        
//        table.setPosition(0, stage.getHeight());
        
        TextButton TitleDialogSub = new TextButton("Adventures in", skin, "default");
        TitleDialogSub.getLabel().setFontScale(stage.getWidth() / (TitleDialogSub.getWidth() * 1.7f));
        
        TextButton TitleDialogMain = new TextButton("ALBION", skin, "default");
        TitleDialogMain.getLabel().setFontScale(stage.getWidth() / (TitleDialogMain.getWidth() * 1.35f));
        
        table.add(TitleDialogSub).expandX();        //add the small title
        table.row();
        table.add(TitleDialogMain).expandX();       //add the larger title
        table.row();
        
        TextButton Author = new TextButton("Created by: Saurav Sumughan\n"
                                        +  "----------------------------", skin, "default");
        Author.getLabel().setFontScale(stage.getWidth() / (Author.getWidth() * 1.6f));
        
        table.add(Author);
        table.row();
        
        
        TextButton StartButton = new TextButton("\nStart!", skin, "default");     //making the start button
        StartButton.setWidth(100);
        StartButton.setHeight(50);
        StartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("\n\nStarting!\n");
                
                dispose();
                game.setScreen(new PlayScreen(game));
            }
        });
        
        table.row();    //adds a new row
        
        TextButton HelpButton = new TextButton("Help!", skin, "default");       //making the help button
        HelpButton.setWidth(100);
        HelpButton.setHeight(50);
        HelpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                JOptionPane.showMessageDialog(null,
                        "Use the Arrow Keys to move.\n"
                + "Press the Space Bar to stab with your spear.\n"
                + "Whenever you are in a bind, hold down shift to run!\n"
                + "And remember, the goal is to kill as many of the skeletons\n"
                + "as possible before time runs out!",
                        "Welcome to Adventures in Albion!",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        table.add(StartButton);
        table.row();
        table.add(HelpButton);
        
        stage.addActor(table);
        
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(102/255f, 86/255f, 75/255f, 1);
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
        stage.dispose();
        skin.dispose();
    }
}

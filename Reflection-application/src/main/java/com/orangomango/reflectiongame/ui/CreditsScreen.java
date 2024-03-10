package com.orangomango.reflectiongame.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.input.MouseEvent;

import java.util.HashMap;

import com.orangomango.reflectiongame.AssetLoader;

import dev.webfx.platform.resource.Resource;

public class CreditsScreen extends GameScreen{
	private double moveY;

	private static final Font FONT = Font.loadFont(Resource.toUrl("/misc/font.ttf", CreditsScreen.class), 50);

	public CreditsScreen(int w, int h, HashMap<KeyCode, Boolean> keys){
		super(w, h, keys);

		Timeline loop = new Timeline(new KeyFrame(Duration.millis(25), e -> {
			this.moveY += 2;
			if (this.moveY > this.height*2){
				this.moveY = 0;
			}
		}));
		loop.setCycleCount(Animation.INDEFINITE);
		loop.play();
	}

	@Override
	public void handleMouseInput(MouseEvent e, double scale, double offsetX){
		SCREEN_SWITCHER.accept(new HomeScreen(this.width, this.height, this.keys));
	}

	@Override
	public void update(GraphicsContext gc, double scale){
		super.update(gc, scale);

		gc.save();
		gc.scale(scale, scale);
		gc.drawImage(AssetLoader.getInstance().getImage("background.jpg"), 0, 0, this.width, this.height);
		gc.setFill(Color.WHITE);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFont(FONT);
		gc.fillText("\"Reflection\" v2.0\nGame made in Java (from scratch)\nwith the JavaFX framework\n\n---\nCode: OrangoMango\nImages: OrangoMango, OkinX\nBackground image: Eder Muniz\nAudio: freesound.org\n\nMade for the Indie Dev Game Jam #3\nv1.0 Dev time: 72h\nTheme: \"One tool, Many uses\"\n\nSource code available on GitHub\nhttps://orangomango.github.io\n\nv1.0 released on February 2024 :)", this.width/2.0, this.height/2.0-this.moveY);
		gc.restore();
	}
}

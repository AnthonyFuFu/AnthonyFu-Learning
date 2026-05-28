package com.anthonyfu.designPattern.behavioral.templateMethod;

public class TemplateMethodPattern {
	public static void main(String[] args) {
		Game game = new Football();
		game.play();

		System.out.println();

		game = new Basketball();
		game.play();
	}
}

package me.Flibio.JobsLite.Utils;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

public class JobCreationMessages {
	
	private static Text PREFIX = Texts.builder("JobsLite » ").color(TextColors.AQUA).build();
	
	public static Text cancelNotification() {
		Text text = Texts.builder().append(PREFIX).build();
		text = text.builder().append(Texts.builder("If at any point you would like to cancel job creation, please type ").color(TextColors.WHITE).build()).build();
		text = text.builder().append(Texts.builder("[").color(TextColors.DARK_GRAY).build()).build();
		text = text.builder().append(Texts.builder("CANCEL").color(TextColors.RED).build()).build();
		text = text.builder().append(Texts.builder("]").color(TextColors.DARK_GRAY).build()).build();
		
		return text;
	}
	
	public static Text genericQuestion(String question, String suffix) {
		Text text = Texts.builder().append(PREFIX).build();
		text = text.builder().append(Texts.builder(question).color(TextColors.WHITE).build()).build();
		text = text.builder().append(Texts.builder("?").color(TextColors.WHITE).build()).build();
		text = text.builder().append(Texts.builder(" ").color(TextColors.GREEN).build()).build();
		
		return text;
	}
	
	public static Text genericInstruction(String instruction) {
		Text text = Texts.builder().append(PREFIX).build();
		text = text.builder().append(Texts.builder("Please ").color(TextColors.GREEN).build()).build();
		text = text.builder().append(Texts.builder(instruction).color(TextColors.WHITE).build()).build();
		
		return text;
	}
	
	public static Text genericSuccess(String message) {
		Text text = Texts.builder().append(PREFIX).build();
		text = text.builder().append(Texts.builder(message).color(TextColors.GREEN).build()).build();
		
		return text;
	}
	
	public static Text genericSuccess(String message, TextColor color) {
		Text text = Texts.builder().append(PREFIX).build();
		text = text.builder().append(Texts.builder(message).color(color).build()).build();
		
		return text;
	}
}

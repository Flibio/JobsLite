package me.Flibio.JobsLite.Utils;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;


public class TextUtils {
	
	public static Text yesOption(Consumer<CommandSource> onClick) {
		Text yes = Text.builder("[").color(TextColors.DARK_GRAY).build();
		yes = yes.toBuilder().append(Text.builder("YES").color(TextColors.GREEN).build()).build();
		yes = yes.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();
		
		yes = yes.toBuilder().onHover(TextActions.showText(Text.builder("YES!").color(TextColors.GREEN).build())).build();
		
		yes = yes.toBuilder().onClick(TextActions.executeCallback(onClick)).build();
		
		return yes;
	}
	
	public static Text noOption(Consumer<CommandSource> onClick) {
		Text no = Text.builder("[").color(TextColors.DARK_GRAY).build();
		no = no.toBuilder().append(Text.builder("NO").color(TextColors.RED).build()).build();
		no = no.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();
		
		no = no.toBuilder().onHover(TextActions.showText(Text.builder("NO!").color(TextColors.RED).build())).build();
		
		no = no.toBuilder().onClick(TextActions.executeCallback(onClick)).build();
		
		return no;
	}
	
	public static Text option(Consumer<CommandSource> onClick, TextColor color, String option) {
		Text text = Text.builder("[").color(TextColors.DARK_GRAY).build();
		text = text.toBuilder().append(Text.builder(option).color(color).build()).build();
		text = text.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();
		
		text = text.toBuilder().onHover(TextActions.showText(Text.builder(option).color(color).build())).build();
		
		text = text.toBuilder().onClick(TextActions.executeCallback(onClick)).build();
		
		return text;
	}
	
	public static Text chatMessage(String name, String jobName, TextColor jobColor, String message) {
		Text text = Text.builder("[").color(TextColors.DARK_GRAY).build();
		text = text.toBuilder().append(Text.builder(jobName).color(jobColor).build()).build();
		text = text.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();
		text = text.toBuilder().append(Text.builder(" "+name+" ").color(TextColors.YELLOW).build()).build();
		text = text.toBuilder().append(Text.builder(message).color(TextColors.WHITE).build()).build();
		
		return text;
	}
	public static Text chatMessage(String name, String jobName, int level, TextColor jobColor, String message) {
		Text text = Text.builder("[").color(TextColors.DARK_GRAY).build();
		text = text.toBuilder().append(Text.builder(jobName).color(jobColor).build()).build();
		text = text.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();
		text = text.toBuilder().append(Text.builder(" {").color(TextColors.DARK_GRAY).build()).build();
		text = text.toBuilder().append(Text.builder(level+"").color(jobColor).build()).build();
		text = text.toBuilder().append(Text.builder("}").color(TextColors.DARK_GRAY).build()).build();
		text = text.toBuilder().append(Text.builder(" "+name+" ").color(TextColors.YELLOW).build()).build();
		text = text.toBuilder().append(Text.builder(message).color(TextColors.WHITE).build()).build();
		
		return text;
	}
	
	public static Text levelUp(String name, int newLevel, String job) {
		Text text = Text.builder("Congratulations ").color(TextColors.YELLOW).build();
		text = text.toBuilder().append(Text.builder(name).color(TextColors.GREEN).build()).build();
		text = text.toBuilder().append(Text.builder(", you are now a ").color(TextColors.YELLOW).build()).build();
		text = text.toBuilder().append(Text.builder("level "+newLevel).color(TextColors.GREEN).build()).build();
		text = text.toBuilder().append(Text.builder(" "+job+"!").color(TextColors.GOLD).build()).build();
		
		return text;
	}
	
	public static Text toGo(int toGo, int nextLevel, String job) {
		Text text = Text.builder("You now need ").color(TextColors.YELLOW).build();
		text = text.toBuilder().append(Text.builder(NumberFormat.getNumberInstance(Locale.US).format(toGo)+" exp").color(TextColors.GREEN).build()).build();
		text = text.toBuilder().append(Text.builder(" to become a ").color(TextColors.YELLOW).build()).build();
		text = text.toBuilder().append(Text.builder("level "+nextLevel).color(TextColors.GREEN).build()).build();
		text = text.toBuilder().append(Text.builder(" "+job+"!").color(TextColors.GOLD).build()).build();
		
		return text;
	}
	
	public static Text maxLevel(String job) {
		Text text = Text.builder("You are now a ").color(TextColors.YELLOW).build();
		text = text.toBuilder().append(Text.builder("max-level").color(TextColors.GREEN).build()).build();
		text = text.toBuilder().append(Text.builder(" "+job+"!").color(TextColors.GOLD).build()).build();
		
		return text;
	}
	
	public static Text instruction(String action) {
		Text text = Text.builder("Please ").color(TextColors.YELLOW).build();
		text = text.toBuilder().append(Text.builder(action+":").color(TextColors.GREEN).build()).build();

		return text;
	}
	
	public static Text saved(String what) {
		Text text = Text.builder("Successfully saved ").color(TextColors.YELLOW).build();
		text = text.toBuilder().append(Text.builder(what).color(TextColors.GREEN).build()).build();
		text = text.toBuilder().append(Text.builder(" as a job!").color(TextColors.YELLOW).build()).build();

		return text;
	}
	
	public static Text success(String what, TextColor color) {
		Text text = Text.builder(what).color(color).build();

		return text;
	}
	
	public static Text error(String error) {
		return Text.builder(error).color(TextColors.RED).build();
	}
	
	public static Text line() {
		Text text = Text.builder("").build();
		
		return text;
	}
	
	public Text change(String change) {
		Text text = Text.builder(" + ").color(TextColors.YELLOW).build();
		
		text = text.toBuilder().append(Text.builder(change).color(TextColors.GREEN).build()).build();
		
		return text;
	}
	
	public Text updateAvailable(String version, String url) {
		Text text = Text.builder("JobsLite").color(TextColors.GREEN).build();
		text = text.toBuilder().append(Text.builder(" v"+version).color(TextColors.LIGHT_PURPLE).build()).build();
		text = text.toBuilder().append(Text.builder(" is now available to download! ").color(TextColors.YELLOW).build()).build();
		try {
			text = text.toBuilder().append(Text.builder(url).color(TextColors.GRAY).onClick(TextActions.openUrl(new URL(url))).build()).build();
		} catch (MalformedURLException e) {}
		
		return text;
	}
	
}

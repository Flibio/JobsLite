/*
 * This file is part of JobsLite, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 - 2016 Flibio
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.flibio.jobslite.utils;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

public class JobCreationMessages {
	
	private static Text PREFIX = Text.builder("JobsLite � ").color(TextColors.AQUA).build();
	
	public static Text cancelNotification() {
		Text text = Text.builder().append(PREFIX).build();
		text = text.toBuilder().append(Text.builder("If at any point you would like to cancel job creation, please type ").color(TextColors.WHITE).build()).build();
		text = text.toBuilder().append(Text.builder("[").color(TextColors.DARK_GRAY).build()).build();
		text = text.toBuilder().append(Text.builder("CANCEL").color(TextColors.RED).build()).build();
		text = text.toBuilder().append(Text.builder("]").color(TextColors.DARK_GRAY).build()).build();
		
		return text;
	}
	
	public static Text genericQuestion(String question, String suffix) {
		Text text = Text.builder().append(PREFIX).build();
		text = text.toBuilder().append(Text.builder(question).color(TextColors.WHITE).build()).build();
		text = text.toBuilder().append(Text.builder("?").color(TextColors.WHITE).build()).build();
		text = text.toBuilder().append(Text.builder(" ").color(TextColors.GREEN).build()).build();
		
		return text;
	}
	
	public static Text genericInstruction(String instruction) {
		Text text = Text.builder().append(PREFIX).build();
		text = text.toBuilder().append(Text.builder("Please ").color(TextColors.GREEN).build()).build();
		text = text.toBuilder().append(Text.builder(instruction).color(TextColors.WHITE).build()).build();
		
		return text;
	}
	
	public static Text genericSuccess(String message) {
		Text text = Text.builder().append(PREFIX).build();
		text = text.toBuilder().append(Text.builder(message).color(TextColors.GREEN).build()).build();
		
		return text;
	}
	
	public static Text genericSuccess(String message, TextColor color) {
		Text text = Text.builder().append(PREFIX).build();
		text = text.toBuilder().append(Text.builder(message).color(color).build()).build();
		
		return text;
	}
}

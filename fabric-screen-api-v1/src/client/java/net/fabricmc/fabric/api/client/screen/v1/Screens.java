/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.client.screen.v1;

import java.util.List;
import java.util.Objects;

import org.quiltmc.qsl.screen.api.client.QuiltScreen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.item.ItemRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Utility methods related to screens.
 *
 * @see ScreenEvents
 * @deprecated Use Quilt Screen API's {@link org.quiltmc.qsl.screen.api.client.QuiltScreen} instead.
 */
@Deprecated
@Environment(EnvType.CLIENT)
public final class Screens {
	/**
	 * Gets all of a screen's button widgets.
	 * The provided list allows for addition and removal of buttons from the screen.
	 * This method should be preferred over adding buttons directly to a screen's {@link Screen#children() child elements}.
	 *
	 * @return a list of all of a screen's buttons
	 */
	public static List<ClickableWidget> getButtons(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ((QuiltScreen) screen).getButtons();
	}

	/**
	 * Gets a screen's item renderer.
	 *
	 * @return the screen's item renderer
	 */
	public static ItemRenderer getItemRenderer(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ((QuiltScreen) screen).getItemRenderer();
	}

	/**
	 * Gets a screen's text renderer.
	 *
	 * @return the screen's text renderer.
	 */
	public static TextRenderer getTextRenderer(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ((QuiltScreen) screen).getTextRenderer();
	}

	public static MinecraftClient getClient(Screen screen) {
		Objects.requireNonNull(screen, "Screen cannot be null");

		return ((QuiltScreen) screen).getClient();
	}

	private Screens() {
	}
}

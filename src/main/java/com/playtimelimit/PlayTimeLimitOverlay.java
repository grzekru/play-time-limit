package com.playtimelimit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

@Singleton
public class PlayTimeLimitOverlay extends Overlay
{
	private static final Color FLASH_COLOR = new Color(255, 0, 0, 110);
	private static final Color PANEL_COLOR = new Color(0, 0, 0, 215);
	private static final Color MAIN_TEXT_COLOR = new Color(255, 255, 255, 250);
	private static final Color SUB_TEXT_COLOR = new Color(255, 245, 245, 250);
	private static final String WARNING_LINE_1 = "Take a break";
	private static final String WARNING_LINE_2 = "You hit today's limit";
	private static final Color BUTTON_TIMER_BG = new Color(80, 0, 0, 230);
	private static final Color BUTTON_TIMER_TEXT = new Color(255, 220, 220, 255);
	private static final int CLOCK_RIGHT_MARGIN = 2;

	private final Client client;
	private final PlayTimeLimitPlugin plugin;

	@Inject
	private PlayTimeLimitOverlay(Client client, PlayTimeLimitPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		setPriority(OverlayPriority.HIGHEST);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.shouldRenderClock())
		{
			return null;
		}

		renderBottomClock(graphics);

		if (!plugin.shouldFlashRed())
		{
			return null;
		}

		Color previous = graphics.getColor();
		Font previousFont = graphics.getFont();
		graphics.setColor(FLASH_COLOR);
		graphics.fillRect(0, 0, client.getCanvasWidth(), client.getCanvasHeight());

		graphics.setFont(previousFont.deriveFont(Font.BOLD, 44f));
		FontMetrics line1Metrics = graphics.getFontMetrics();
		int line1Width = line1Metrics.stringWidth(WARNING_LINE_1);
		int line1X = (client.getCanvasWidth() - line1Width) / 2;
		int line1Y = (client.getCanvasHeight() / 2) - 18;

		graphics.setFont(previousFont.deriveFont(Font.BOLD, 30f));
		FontMetrics line2Metrics = graphics.getFontMetrics();
		int line2Width = line2Metrics.stringWidth(WARNING_LINE_2);
		int line2X = (client.getCanvasWidth() - line2Width) / 2;
		int line2Y = line1Y + 44;

		graphics.setColor(PANEL_COLOR);
		graphics.fillRoundRect(line2X - 18, line1Y - 40, Math.max(line1Width, line2Width) + 36, 98, 12, 12);

		graphics.setFont(previousFont.deriveFont(Font.BOLD, 44f));
		graphics.setColor(MAIN_TEXT_COLOR);
		graphics.drawString(WARNING_LINE_1, line1X, line1Y);

		graphics.setFont(previousFont.deriveFont(Font.BOLD, 30f));
		graphics.setColor(SUB_TEXT_COLOR);
		graphics.drawString(WARNING_LINE_2, line2X, line2Y);

		graphics.setColor(previous);
		graphics.setFont(previousFont);
		return null;
	}

	private void renderBottomClock(Graphics2D graphics)
	{
		Color previousColor = graphics.getColor();
		Font previousFont = graphics.getFont();

		String totalText = formatHms(plugin.getClockPlayedSeconds());

		graphics.setFont(previousFont.deriveFont(Font.BOLD, 14f));
		FontMetrics textMetrics = graphics.getFontMetrics();
		int textWidth = textMetrics.stringWidth(totalText);

		int panelWidth = textWidth + 20;
		int panelHeight = 20;

		Widget buttonsWidget = client.getWidget(WidgetInfo.CHATBOX_BUTTONS);
		Widget tradeWidget = client.getWidget(WidgetInfo.CHATBOX_TAB_TRADE);
		if (buttonsWidget == null || tradeWidget == null)
		{
			graphics.setColor(previousColor);
			graphics.setFont(previousFont);
			return;
		}

		Rectangle buttonRowBounds = buttonsWidget.getBounds();
		Rectangle tradeBounds = tradeWidget.getBounds();
		int x = buttonRowBounds.x + buttonRowBounds.width - panelWidth - CLOCK_RIGHT_MARGIN;
		int y = tradeBounds.y + Math.max(0, (tradeBounds.height - panelHeight) / 2);

		graphics.setColor(BUTTON_TIMER_BG);
		graphics.fillRoundRect(x, y, panelWidth, panelHeight, 6, 6);

		graphics.setColor(BUTTON_TIMER_TEXT);
		graphics.drawString(totalText, x + (panelWidth - textWidth) / 2, y + 15);

		graphics.setColor(previousColor);
		graphics.setFont(previousFont);
	}

	private static String formatHms(long totalSeconds)
	{
		long safeSeconds = Math.max(0L, totalSeconds);
		long hours = TimeUnit.SECONDS.toHours(safeSeconds);
		long minutes = TimeUnit.SECONDS.toMinutes(safeSeconds) % 60;
		long seconds = safeSeconds % 60;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
}

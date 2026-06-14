package com.playtimelimit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

@Singleton
public class PlayTimeLimitOverlay extends Overlay
{
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
		if (!plugin.shouldFlashRed())
		{
			return null;
		}

		Color previous = graphics.getColor();
		Font previousFont = graphics.getFont();
		graphics.setColor(new Color(255, 0, 0, 110));
		graphics.fillRect(0, 0, client.getCanvasWidth(), client.getCanvasHeight());

		String warningLine1 = "STOP PLAYING NOW";
		String warningLine2 = "YOU ARE OVER TODAY'S LIMIT";

		graphics.setFont(previousFont.deriveFont(Font.BOLD, 44f));
		FontMetrics line1Metrics = graphics.getFontMetrics();
		int line1Width = line1Metrics.stringWidth(warningLine1);
		int line1X = (client.getCanvasWidth() - line1Width) / 2;
		int line1Y = (client.getCanvasHeight() / 2) - 18;

		graphics.setFont(previousFont.deriveFont(Font.BOLD, 30f));
		FontMetrics line2Metrics = graphics.getFontMetrics();
		int line2Width = line2Metrics.stringWidth(warningLine2);
		int line2X = (client.getCanvasWidth() - line2Width) / 2;
		int line2Y = line1Y + 44;

		graphics.setColor(new Color(0, 0, 0, 215));
		graphics.fillRoundRect(line2X - 18, line1Y - 40, Math.max(line1Width, line2Width) + 36, 98, 12, 12);

		graphics.setFont(previousFont.deriveFont(Font.BOLD, 44f));
		graphics.setColor(new Color(255, 255, 255, 250));
		graphics.drawString(warningLine1, line1X, line1Y);

		graphics.setFont(previousFont.deriveFont(Font.BOLD, 30f));
		graphics.setColor(new Color(255, 245, 245, 250));
		graphics.drawString(warningLine2, line2X, line2Y);

		graphics.setColor(previous);
		graphics.setFont(previousFont);
		return null;
	}
}

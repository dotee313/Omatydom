package com.dotee313.omatydom.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.world.Agent;
import com.dotee313.omatydom.core.world.Animal;
import com.dotee313.omatydom.core.world.IslandMap;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;

/** Cheap 2D rendering: flat tiles plus tiny stick bodies, no sprites or textures. */
public class IslandView extends View {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint body = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OmatydomGame game;

    public IslandView(Context context, AttributeSet attrs) {
        super(context, attrs);
        body.setStyle(Paint.Style.STROKE);
        body.setStrokeWidth(2f);
    }

    public void setGame(OmatydomGame game) {
        this.game = game;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game == null) {
            return;
        }
        IslandMap map = game.getMap();
        int columns = map.getWidth();
        int rows = map.getHeight();
        float cell = Math.min((float) getWidth() / columns, (float) getHeight() / rows);
        if (cell <= 0) {
            return;
        }
        float offsetX = (getWidth() - cell * columns) / 2f;
        float offsetY = (getHeight() - cell * rows) / 2f;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Tile tile = map.get(x, y);
                float left = offsetX + x * cell;
                float top = offsetY + y * cell;
                fill.setColor(tile.getType().getColor());
                canvas.drawRect(left, top, left + cell, top + cell, fill);
                if (tile.getStructure() != Structure.NONE) {
                    fill.setColor(tile.getStructure().getColor());
                    float inset = cell * 0.2f;
                    canvas.drawRect(left + inset, top + inset,
                            left + cell - inset, top + cell - inset, fill);
                }
            }
        }

        for (Animal animal : game.getAnimals()) {
            if (!animal.isAlive()) {
                continue;
            }
            float cx = offsetX + animal.getX() * cell + cell / 2f;
            float cy = offsetY + animal.getY() * cell + cell / 2f;
            fill.setColor(animal.getType().getColor());
            canvas.drawCircle(cx, cy, Math.max(1f, cell * 0.12f), fill);
        }

        for (Agent agent : game.getAgents()) {
            if (!agent.getCitizen().isAlive()) {
                continue;
            }
            float cx = offsetX + agent.getX() * cell + cell / 2f;
            float cy = offsetY + agent.getY() * cell + cell / 2f;
            float radius = Math.max(1.2f, cell * 0.16f);
            fill.setColor(agent.getCitizen().getRace().getColor());
            canvas.drawCircle(cx, cy - radius, radius, fill);
            body.setColor(agent.getJob().getColor());
            canvas.drawLine(cx, cy, cx, cy + radius * 2f, body);
        }
    }
}

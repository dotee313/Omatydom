package com.dotee313.omatydom.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Job;
import com.dotee313.omatydom.core.world.Agent;
import com.dotee313.omatydom.core.world.Animal;
import com.dotee313.omatydom.core.world.AnimalType;
import com.dotee313.omatydom.core.world.IslandMap;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.TileType;

/** Cheap pixel-art renderer: flat tiles, tiny pixel people, no textures or sprites. */
public class IslandView extends View {

    public interface OnThingTappedListener {
        void onAgentTapped(Citizen citizen);
        void onTileTapped(int x, int y);
    }

    private final Paint paint = new Paint();
    private OmatydomGame game;
    private OnThingTappedListener listener;
    private float cell = 1f;
    private int offsetX;
    private int offsetY;
    private int selectedCitizenId = -1;
    private float downX;
    private float downY;

    public IslandView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setGame(OmatydomGame game) {
        this.game = game;
        this.selectedCitizenId = -1;
        invalidate();
    }

    public void setOnThingTappedListener(OnThingTappedListener listener) {
        this.listener = listener;
    }

    public void selectAgent(int citizenId) {
        selectedCitizenId = citizenId;
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
        cell = Math.min((float) getWidth() / columns, (float) getHeight() / rows);
        if (cell <= 0) {
            return;
        }
        offsetX = (int) ((getWidth() - cell * columns) / 2f);
        offsetY = (int) ((getHeight() - cell * rows) / 2f);

        paint.setColor(0xFF10232B);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                drawTile(canvas, map.get(x, y), x, y);
            }
        }
        for (Animal animal : game.getAnimals()) {
            if (animal.isAlive()) {
                drawAnimal(canvas, animal);
            }
        }
        for (Agent agent : game.getAgents()) {
            if (agent.getCitizen().isAlive()) {
                drawPerson(canvas, agent);
            }
        }
    }

    private void drawTile(Canvas canvas, Tile tile, int x, int y) {
        float left = offsetX + x * cell;
        float top = offsetY + y * cell;
        TileType type = tile.getType();
        paint.setColor(type.getColor());
        canvas.drawRect(left, top, left + cell + 0.5f, top + cell + 0.5f, paint);

        float u = cell / 8f;
        int h = hash(x, y);
        switch (type) {
            case WATER:
                paint.setColor(0xFF7CB5E8);
                canvas.drawRect(left + u, top + 2 * u, left + 4 * u, top + 2 * u + u, paint);
                canvas.drawRect(left + 5 * u, top + 5 * u, left + 7 * u, top + 5 * u + u, paint);
                break;
            case SAND:
                paint.setColor(0xFFC4A96A);
                canvas.drawRect(left + 3 * u, top + 3 * u, left + 4 * u, top + 4 * u, paint);
                if ((h & 1) == 0) {
                    canvas.drawRect(left + 6 * u, top + 6 * u, left + 7 * u, top + 7 * u, paint);
                }
                break;
            case GRASS:
                paint.setColor(0xFF5F913C);
                canvas.drawRect(left + 2 * u, top + 5 * u, left + 3 * u, top + 7 * u, paint);
                if ((h & 3) == 0) {
                    canvas.drawRect(left + 5 * u, top + 3 * u, left + 6 * u, top + 6 * u, paint);
                }
                break;
            case FOREST:
                paint.setColor(0xFF6D4C41);
                canvas.drawRect(left + 3.5f * u, top + 5 * u, left + 4.5f * u, top + 8 * u, paint);
                paint.setColor(0xFF1B5E20);
                canvas.drawRect(left + u, top + 2 * u, left + 7 * u, top + 5 * u, paint);
                paint.setColor(0xFF43A047);
                canvas.drawRect(left + 2 * u, top + u, left + 6 * u, top + 3 * u, paint);
                break;
            case ORCHARD:
                paint.setColor(0xFF6D4C41);
                canvas.drawRect(left + 3.5f * u, top + 5 * u, left + 4.5f * u, top + 8 * u, paint);
                paint.setColor(0xFF2E7D32);
                canvas.drawRect(left + u, top + 2 * u, left + 7 * u, top + 5 * u, paint);
                paint.setColor((h & 1) == 0 ? 0xFFE53935 : 0xFFFFCA28);
                canvas.drawRect(left + 5 * u, top + 3 * u, left + 6 * u, top + 4 * u, paint);
                break;
            case MOUNTAIN:
                paint.setColor(0xFF616161);
                canvas.drawRect(left + u, top + 5 * u, left + 7 * u, top + 8 * u, paint);
                canvas.drawRect(left + 2 * u, top + 3 * u, left + 6 * u, top + 6 * u, paint);
                canvas.drawRect(left + 3 * u, top + u, left + 5 * u, top + 4 * u, paint);
                paint.setColor(0xFFE0E0E0);
                canvas.drawRect(left + 3 * u, top + u, left + 4 * u, top + 2 * u, paint);
                break;
            case CAVE:
                paint.setColor(0xFF3E2723);
                canvas.drawRect(left + 1.5f * u, top + 3 * u, left + 6.5f * u, top + 8 * u, paint);
                paint.setColor(0xFF0B0806);
                canvas.drawRect(left + 2.5f * u, top + 5 * u, left + 5.5f * u, top + 8 * u, paint);
                break;
            default:
                break;
        }

        if (tile.getStructure() != Structure.NONE) {
            drawStructure(canvas, tile.getStructure(), left, top, u);
        }
    }

    private void drawStructure(Canvas canvas, Structure structure, float left, float top, float u) {
        switch (structure) {
            case HUT:
                paint.setColor(0xFF8D5524);
                canvas.drawRect(left + 2 * u, top + 3 * u, left + 6 * u, top + 7 * u, paint);
                paint.setColor(0xFF5D3A1A);
                canvas.drawRect(left + u, top + 2 * u, left + 7 * u, top + 4 * u, paint);
                paint.setColor(0xFF301A0C);
                canvas.drawRect(left + 3.5f * u, top + 5 * u, left + 4.5f * u, top + 7 * u, paint);
                break;
            case TEMPLE:
                paint.setColor(0xFFE6C86A);
                canvas.drawRect(left + u, top + u, left + 7 * u, top + 3 * u, paint);
                paint.setColor(0xFFF5E6A8);
                canvas.drawRect(left + 2 * u, top + 3 * u, left + 3 * u, top + 7 * u, paint);
                canvas.drawRect(left + 5 * u, top + 3 * u, left + 6 * u, top + 7 * u, paint);
                paint.setColor(0xFF8D6E63);
                canvas.drawRect(left + u, top + 7 * u, left + 7 * u, top + 8 * u, paint);
                break;
            case TOWER:
                paint.setColor(0xFF9E9E9E);
                canvas.drawRect(left + 2 * u, top + 2 * u, left + 6 * u, top + 8 * u, paint);
                paint.setColor(0xFF616161);
                canvas.drawRect(left + 1.5f * u, top + 2 * u, left + 6.5f * u, top + 3.5f * u, paint);
                paint.setColor(0xFF263238);
                canvas.drawRect(left + 3 * u, top + 4 * u, left + 5 * u, top + 6 * u, paint);
                break;
            case DOCK:
                paint.setColor(0xFF8D6E63);
                canvas.drawRect(left, top + 3 * u, left + 8 * u, top + 4 * u, paint);
                canvas.drawRect(left + 2 * u, top + 5 * u, left + 3 * u, top + 8 * u, paint);
                canvas.drawRect(left + 6 * u, top + 5 * u, left + 7 * u, top + 8 * u, paint);
                break;
            case MINE:
                paint.setColor(0xFF5D4037);
                canvas.drawRect(left + u, top + 3 * u, left + 7 * u, top + 8 * u, paint);
                paint.setColor(0xFF0B0806);
                canvas.drawRect(left + 2 * u, top + 5 * u, left + 6 * u, top + 8 * u, paint);
                paint.setColor(0xFFB0BEC5);
                canvas.drawRect(left + 5 * u, top + 4 * u, left + 6 * u, top + 5 * u, paint);
                break;
            default:
                break;
        }
    }

    private void drawAnimal(Canvas canvas, Animal animal) {
        float px = offsetX + animal.getX() * cell + cell / 2f;
        float py = offsetY + animal.getY() * cell + cell / 2f;
        float u = Math.max(1f, cell / 10f);
        paint.setColor(animal.getType().getColor());
        if (animal.getType() == AnimalType.FISH) {
            canvas.drawRect(px - 2 * u, py - u, px + 2 * u, py + u, paint);
            canvas.drawRect(px + 2 * u, py - 2 * u, px + 3 * u, py + 2 * u, paint);
            paint.setColor(0xFF212121);
            canvas.drawRect(px - u, py - u, px, py, paint);
            return;
        }
        canvas.drawRect(px - 2 * u, py - 2 * u, px + 2 * u, py + u, paint);
        canvas.drawRect(px + 2 * u, py - u, px + 3 * u, py, paint);
        paint.setColor(0xFF3E2723);
        canvas.drawRect(px - u, py + u, px, py + 2 * u, paint);
        canvas.drawRect(px + u, py + u, px + 2 * u, py + 2 * u, paint);
    }

    private void drawPerson(Canvas canvas, Agent agent) {
        Citizen citizen = agent.getCitizen();
        float cx = offsetX + agent.getX() * cell + cell / 2f;
        float cy = offsetY + agent.getY() * cell + cell / 2f;
        float unit = Math.max(1f, cell / 8f);
        int px = Math.round(cx - 3 * unit);
        int py = Math.round(cy - 3.5f * unit);
        int u = Math.max(1, Math.round(unit));
        int skin = citizen.getRace().getColor();
        int cloth = citizen.getJob().getColor();
        int hair = citizen.getGender() == Gender.FEMALE ? 0xFF6D4C41 : 0xFF3E2723;

        // Crown for the ruler, otherwise a simple hair row.
        paint.setColor(citizen.getJob() == Job.RULER ? 0xFFFFD54F : hair);
        pixelRect(canvas, px, py, u, 1, 4, 0, 1);
        paint.setColor(skin);
        pixelRect(canvas, px, py, u, 1, 4, 1, 4);
        paint.setColor(0xFF212121);
        pixelRect(canvas, px, py, u, 1, 1, 2, 3);
        pixelRect(canvas, px, py, u, 3, 3, 2, 3);
        paint.setColor(cloth);
        pixelRect(canvas, px, py, u, 1, 4, 4, 6);
        pixelRect(canvas, px, py, u, 0, 0, 4, 5);
        pixelRect(canvas, px, py, u, 5, 5, 4, 5);
        paint.setColor(0xFF263238);
        pixelRect(canvas, px, py, u, 1, 1, 6, 7);
        pixelRect(canvas, px, py, u, 3, 3, 6, 7);

        if (citizen.getId() == selectedCitizenId) {
            paint.setColor(0xFFFFFFFF);
            canvas.drawRect(px - u, py - u, px + 6 * u + u, py - u + u, paint);
            canvas.drawRect(px - u, py + 8 * u, px + 6 * u + u, py + 8 * u + u, paint);
            canvas.drawRect(px - u, py, px, py + 8 * u, paint);
            canvas.drawRect(px + 6 * u, py, px + 6 * u + u, py + 8 * u, paint);
        }
    }

    private void pixelRect(Canvas canvas, int px, int py, int u,
                           int x0, int x1, int y0, int y1) {
        canvas.drawRect(px + x0 * u, py + y0 * u, px + (x1 + 1) * u, py + (y1 + 1) * u, paint);
    }

    private static int hash(int x, int y) {
        int value = x * 374761393 + y * 668265263;
        value = (value ^ (value >>> 13)) * 1274126177;
        return value ^ (value >>> 16);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            return true;
        }
        if (action == MotionEvent.ACTION_UP) {
            float dx = event.getX() - downX;
            float dy = event.getY() - downY;
            performClick();
            if (dx * dx + dy * dy < 144 && listener != null && game != null && cell > 0) {
                int x = (int) Math.floor((event.getX() - offsetX) / cell);
                int y = (int) Math.floor((event.getY() - offsetY) / cell);
                IslandMap map = game.getMap();
                if (map.inBounds(x, y)) {
                    Agent tapped = null;
                    for (Agent agent : game.getAgents()) {
                        if (agent.getCitizen().isAlive()
                                && agent.getX() == x && agent.getY() == y) {
                            tapped = agent;
                            break;
                        }
                    }
                    if (tapped != null) {
                        listener.onAgentTapped(tapped.getCitizen());
                    } else {
                        listener.onTileTapped(x, y);
                    }
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}

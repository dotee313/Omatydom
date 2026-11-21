package com.dotee313.omatydom.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.dotee313.omatydom.core.OmatydomGame;
import com.dotee313.omatydom.core.civ.Kingdom;
import com.dotee313.omatydom.core.civ.StatusEffect;
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

import java.util.List;

/** Cheap detailed sprite renderer: shaded low-res people, animals and map overlays. */
public class IslandView extends View {

    public enum Overlay { TERRAIN, BIOME, KINGDOM, RELIGION, JOB }

    public interface OnThingTappedListener {
        void onAgentTapped(Citizen citizen);
        void onTileTapped(int x, int y);
    }

    private final Paint paint = new Paint();
    private OmatydomGame game;
    private OnThingTappedListener listener;
    private Overlay overlay = Overlay.TERRAIN;
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

    /** Choose the translucent tile tint (or plain terrain). */
    public void setOverlay(Overlay overlay) {
        this.overlay = overlay == null ? Overlay.TERRAIN : overlay;
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

        drawOverlayTint(canvas, tile, x, y, left, top);

        if (tile.getStructure() != Structure.NONE) {
            drawStructure(canvas, tile.getStructure(), left, top, u);
        }
    }

    // ------------------------------------------------------------------
    // Overlays
    // ------------------------------------------------------------------

    private void drawOverlayTint(Canvas canvas, Tile tile, int x, int y, float left, float top) {
        int color;
        switch (overlay) {
            case BIOME:
                color = tile.getBiome() == null ? 0 : tile.getBiome().getColor();
                break;
            case KINGDOM:
                color = kingdomColorAt(x, y);
                break;
            case RELIGION:
                color = beliefColorAt(x, y);
                break;
            case JOB:
                color = jobColorAt(x, y);
                break;
            case TERRAIN:
            default:
                return;
        }
        if (color == 0) {
            return;
        }
        paint.setColor(withAlpha(color, 0x66));
        canvas.drawRect(left, top, left + cell + 0.5f, top + cell + 0.5f, paint);
    }

    /** Colour of the nearest kingdom capital, measured in Manhattan tiles. */
    private int kingdomColorAt(int x, int y) {
        if (game == null) {
            return 0;
        }
        Kingdom nearest = null;
        int best = Integer.MAX_VALUE;
        for (Kingdom kingdom : game.getKingdoms()) {
            int distance = Math.abs(kingdom.getCapitalX() - x)
                    + Math.abs(kingdom.getCapitalY() - y);
            if (distance < best) {
                best = distance;
                nearest = kingdom;
            }
        }
        return nearest == null ? 0 : opaque(nearest.getColor());
    }

    /** Faith and ideology of the closest citizen, falling back to tile noise. */
    private int beliefColorAt(int x, int y) {
        if (game == null) {
            return 0;
        }
        Agent closest = null;
        int best = 5; // only citizens within four tiles colour a belief patch
        for (Agent agent : game.getAgents()) {
            if (!agent.getCitizen().isAlive()) {
                continue;
            }
            int distance = Math.abs(agent.getX() - x) + Math.abs(agent.getY() - y);
            if (distance < best) {
                best = distance;
                closest = agent;
            }
        }
        int seed;
        if (closest == null) {
            seed = hash(x, y);
        } else {
            Citizen citizen = closest.getCitizen();
            seed = citizen.getReligion().ordinal() * 31 + citizen.getIdeology().ordinal() * 17;
        }
        return colorFromSeed(seed);
    }

    /** Work colour of a citizen standing exactly on the tile. */
    private int jobColorAt(int x, int y) {
        if (game == null) {
            return 0;
        }
        for (Agent agent : game.getAgents()) {
            if (agent.getX() == x && agent.getY() == y && agent.getCitizen().isAlive()) {
                Job job = agent.getCitizen().getJob();
                return job == null ? 0 : opaque(job.getColor());
            }
        }
        return 0;
    }

    /** Modulo that stays non-negative (Math.floorMod needs API 24). */
    private static int positiveMod(int value, int modulus) {
        int result = value % modulus;
        return result < 0 ? result + modulus : result;
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int opaque(int color) {
        return Color.argb(0xFF, Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int colorFromSeed(int seed) {
        int h = positiveMod(seed, 100000);
        int r = 70 + (h * 37) % 160;
        int g = 70 + (h / 7 * 53) % 160;
        int b = 70 + (h / 13 * 71) % 160;
        return Color.rgb(r, g, b);
    }

    // ------------------------------------------------------------------
    // Structures and creatures
    // ------------------------------------------------------------------

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
                // Every remaining catalogued structure (halls, farms, forges,
                // markets, bridges, palaces, ...) gets a coloured block and a roof.
                drawGenericStructure(canvas, structure, left, top, u);
                break;
        }
    }

    private void drawGenericStructure(Canvas canvas, Structure structure,
                                      float left, float top, float u) {
        int color = opaque(structure.getColor());
        int dark = shade(color, 0.62f);
        int light = mix(color, 0xFFFFFFFF, 0.35f);
        int shape = positiveMod(structure.ordinal(), 3);

        if (shape == 0) {
            paint.setColor(color);
            canvas.drawRect(left + u, top + 3 * u, left + 7 * u, top + 8 * u, paint);
            paint.setColor(dark);
            canvas.drawRect(left + 0.5f * u, top + 2 * u, left + 7.5f * u, top + 4 * u, paint);
            paint.setColor(light);
            canvas.drawRect(left + 2 * u, top + u, left + 6 * u, top + 3 * u, paint);
        } else if (shape == 1) {
            paint.setColor(color);
            canvas.drawRect(left + 2 * u, top + 1.5f * u, left + 6 * u, top + 8 * u, paint);
            paint.setColor(dark);
            canvas.drawRect(left + 1.5f * u, top + 2 * u, left + 6.5f * u, top + 3.5f * u, paint);
            paint.setColor(light);
            canvas.drawRect(left + 3 * u, top + u, left + 5 * u, top + 2.5f * u, paint);
        } else {
            paint.setColor(dark);
            canvas.drawRect(left, top + 6 * u, left + 8 * u, top + 8 * u, paint);
            paint.setColor(color);
            canvas.drawRect(left + 1.5f * u, top + 3 * u, left + 6.5f * u, top + 7 * u, paint);
            paint.setColor(light);
            canvas.drawRect(left + 2 * u, top + 2 * u, left + 6 * u, top + 4 * u, paint);
        }
        paint.setColor(shade(color, 0.38f));
        canvas.drawRect(left + 3.5f * u, top + 6 * u, left + 4.5f * u, top + 8 * u, paint);
    }

    private void drawAnimal(Canvas canvas, Animal animal) {
        float px = offsetX + animal.getX() * cell + cell / 2f;
        float py = offsetY + animal.getY() * cell + cell / 2f;
        float u = Math.max(1f, cell / (animal.getType().isDangerous() ? 7f : 10f));
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
        if (animal.getType().isDangerous()) {
            paint.setColor(0xFFFF1744);
            canvas.drawRect(px - 2 * u, py - 3 * u, px - u, py - 2 * u, paint);
            canvas.drawRect(px + u, py - 3 * u, px + 2 * u, py - 2 * u, paint);
            if (animal.getType() == AnimalType.DRAGON) {
                paint.setColor(animal.getType().getColor());
                canvas.drawRect(px - 4 * u, py - 3 * u, px - 2 * u, py - u, paint);
                canvas.drawRect(px + 2 * u, py - 3 * u, px + 4 * u, py - u, paint);
            }
        }
    }

    private void drawPerson(Canvas canvas, Agent agent) {
        Citizen citizen = agent.getCitizen();
        float cx = offsetX + agent.getX() * cell + cell / 2f;
        float cy = offsetY + agent.getY() * cell + cell / 2f;
        int u = Math.max(1, Math.round(Math.max(1f, cell / 13f)));
        int px = Math.round(cx - 5 * u);
        int py = Math.round(cy - 7 * u);

        int age = citizen.getAgeYears();
        int skin = mix(citizen.getRace().getColor(), 0xFFD8A47C, 0.48f);
        int skinDark = shade(skin, 0.72f);
        int skinLight = mix(skin, 0xFFFFFFFF, 0.22f);
        int cloth = citizen.getJob().getColor();
        int clothDark = shade(cloth, 0.68f);
        int hair = age > 55 ? 0xFF9E9E9E
                : (citizen.getGender() == Gender.FEMALE ? 0xFF5D4037 : 0xFF3E2723);

        // Soft ground shadow.
        paint.setColor(0x44202A30);
        canvas.drawRect(px + 2 * u, py + 13 * u, px + 8 * u, py + 14 * u, paint);

        // Boots and legs.
        paint.setColor(0xFF37474F);
        pixelRect(canvas, px, py, u, 3, 4, 11, 12);
        pixelRect(canvas, px, py, u, 6, 7, 11, 12);
        paint.setColor(0xFF263238);
        pixelRect(canvas, px, py, u, 2, 4, 13, 13);
        pixelRect(canvas, px, py, u, 6, 8, 13, 13);

        // Torso with a shaded side and belt.
        paint.setColor(cloth);
        pixelRect(canvas, px, py, u, 2, 7, 7, 10);
        paint.setColor(clothDark);
        pixelRect(canvas, px, py, u, 2, 3, 7, 10);
        pixelRect(canvas, px, py, u, 7, 8, 7, 10);
        paint.setColor(0xFF4E342E);
        pixelRect(canvas, px, py, u, 2, 7, 9, 9);

        // Neck, head and face shading.
        paint.setColor(skinDark);
        pixelRect(canvas, px, py, u, 5, 6, 6, 6);
        paint.setColor(skin);
        pixelRect(canvas, px, py, u, 3, 8, 2, 6);
        paint.setColor(skinDark);
        pixelRect(canvas, px, py, u, 1, 2, 4, 5);
        pixelRect(canvas, px, py, u, 9, 9, 4, 5);
        pixelRect(canvas, px, py, u, 8, 8, 2, 6);
        paint.setColor(skinLight);
        pixelRect(canvas, px, py, u, 4, 6, 3, 5);

        // Hair, eyes and mouth.
        paint.setColor(hair);
        pixelRect(canvas, px, py, u, 3, 8, 0, 1);
        pixelRect(canvas, px, py, u, 2, 2, 1, 3);
        if (citizen.getGender() == Gender.FEMALE) {
            pixelRect(canvas, px, py, u, 1, 1, 2, 7);
            pixelRect(canvas, px, py, u, 9, 9, 2, 7);
        }
        paint.setColor(0xFF202124);
        pixelRect(canvas, px, py, u, 4, 4, 3, 3);
        pixelRect(canvas, px, py, u, 7, 7, 3, 3);
        paint.setColor(shade(skinDark, 0.8f));
        pixelRect(canvas, px, py, u, 5, 6, 5, 5);

        // Arms, with a slight bend to avoid a stiff stick look.
        paint.setColor(skin);
        pixelRect(canvas, px, py, u, 1, 2, 7, 9);
        pixelRect(canvas, px, py, u, 8, 9, 7, 9);
        paint.setColor(skinDark);
        pixelRect(canvas, px, py, u, 1, 1, 9, 10);
        pixelRect(canvas, px, py, u, 9, 9, 9, 10);

        // A pregnant woman has a visibly rounded belly.
        if (citizen.isPregnant()) {
            paint.setColor(mix(cloth, 0xFFE57373, 0.32f));
            pixelRect(canvas, px, py, u, 3, 6, 8, 10);
        }

        drawJobTool(canvas, px, py, u, citizen.getJob());
        drawStatusSpecks(canvas, citizen, px, py, u);

        if (citizen.getId() == selectedCitizenId) {
            paint.setColor(0xFFFFFFFF);
            canvas.drawRect(px - u, py - u, px + 10 * u + u, py - u + u, paint);
            canvas.drawRect(px - u, py + 14 * u, px + 10 * u + u, py + 14 * u + u, paint);
            canvas.drawRect(px - u, py, px, py + 14 * u, paint);
            canvas.drawRect(px + 10 * u, py, px + 10 * u + u, py + 14 * u, paint);
        }
    }

    /** One floating pixel per temporary condition above the citizen's head. */
    private void drawStatusSpecks(Canvas canvas, Citizen citizen, int px, int py, int u) {
        List<StatusEffect> effects = citizen.getActiveStatuses();
        for (int i = 0; i < effects.size() && i < 4; i++) {
            paint.setColor(statusColor(effects.get(i)));
            canvas.drawRect(px + i * 2 * u, py - 3 * u,
                    px + (i * 2 + 1) * u, py - 2 * u, paint);
        }
    }

    private static int statusColor(StatusEffect effect) {
        switch (effect) {
            case ASH_FEVER: return 0xFF8D6E63;
            case BURNING: return 0xFFFF7043;
            case CAFFEINATED: return 0xFFFFF59D;
            case COUGH: return 0xFFB0BEC5;
            case ENCHANTED: return 0xFFCE93D8;
            case FROZEN: return 0xFF4FC3F7;
            case INVINCIBLE: return 0xFFF48FB1;
            case POISONED: return 0xFFAED581;
            case POWERUP: return 0xFFFF8A65;
            case RAGE: return 0xFFE53935;
            case SHIELD: return 0xFFFFD54F;
            case SLOWNESS: return 0xFF78909C;
            default: return colorFromSeed(effect.ordinal() * 97 + 13);
        }
    }

    private void drawJobTool(Canvas canvas, int px, int py, int u, Job job) {
        switch (job) {
            case RULER:
                paint.setColor(0xFFFFD54F);
                pixelRect(canvas, px, py, u, 3, 7, 0, 0);
                pixelRect(canvas, px, py, u, 4, 4, 1, 1);
                pixelRect(canvas, px, py, u, 6, 6, 1, 1);
                break;
            case WOODCUTTER:
                paint.setColor(0xFF8D6E63);
                pixelRect(canvas, px, py, u, 0, 0, 6, 11);
                paint.setColor(0xFFB0BEC5);
                pixelRect(canvas, px, py, u, 0, 2, 6, 7);
                break;
            case MINER:
                paint.setColor(0xFF8D6E63);
                pixelRect(canvas, px, py, u, 0, 0, 5, 11);
                paint.setColor(0xFF9E9E9E);
                pixelRect(canvas, px, py, u, 0, 3, 4, 5);
                break;
            case FISHER:
                paint.setColor(0xFF90A4AE);
                pixelRect(canvas, px, py, u, 0, 0, 2, 11);
                paint.setColor(0xFF4FC3F7);
                pixelRect(canvas, px, py, u, 0, 1, 12, 12);
                break;
            case HUNTER:
                paint.setColor(0xFF6D4C41);
                pixelRect(canvas, px, py, u, 9, 9, 2, 12);
                paint.setColor(0xFFD7CCC8);
                pixelRect(canvas, px, py, u, 8, 10, 1, 2);
                break;
            case PRIEST:
                paint.setColor(0xFFD7CCC8);
                pixelRect(canvas, px, py, u, 0, 0, 2, 12);
                paint.setColor(0xFFFFF59D);
                pixelRect(canvas, px, py, u, 0, 1, 1, 2);
                break;
            case BUILDER:
                paint.setColor(0xFF8D6E63);
                pixelRect(canvas, px, py, u, 0, 0, 5, 11);
                paint.setColor(0xFF9E9E9E);
                pixelRect(canvas, px, py, u, 0, 2, 4, 5);
                break;
            case GATHERER:
                paint.setColor(0xFFA1887F);
                pixelRect(canvas, px, py, u, 8, 10, 10, 12);
                paint.setColor(0xFF66BB6A);
                pixelRect(canvas, px, py, u, 9, 9, 9, 9);
                break;
            case TRADER:
                paint.setColor(0xFFFFD54F);
                pixelRect(canvas, px, py, u, 0, 1, 9, 10);
                break;
            case BLACKSMITH:
                paint.setColor(0xFF37474F);
                pixelRect(canvas, px, py, u, 0, 1, 4, 9);
                paint.setColor(0xFFFF7043);
                pixelRect(canvas, px, py, u, 0, 1, 10, 11);
                break;
            case SETTLER:
                paint.setColor(0xFF8BC34A);
                pixelRect(canvas, px, py, u, 0, 1, 2, 12);
                paint.setColor(0xFFFFF59D);
                pixelRect(canvas, px, py, u, 0, 1, 1, 1);
                break;
            case ROAD_BUILDER:
                paint.setColor(0xFFA1887F);
                pixelRect(canvas, px, py, u, 0, 2, 4, 11);
                paint.setColor(0xFF9E9E9E);
                pixelRect(canvas, px, py, u, 0, 2, 12, 12);
                break;
            case CLEANER:
                paint.setColor(0xFF4DD0E1);
                pixelRect(canvas, px, py, u, 0, 0, 5, 11);
                pixelRect(canvas, px, py, u, 0, 2, 3, 4);
                break;
            case FIREMAN:
                paint.setColor(0xFFFF7043);
                pixelRect(canvas, px, py, u, 0, 1, 3, 12);
                paint.setColor(0xFFB0BEC5);
                pixelRect(canvas, px, py, u, 0, 1, 1, 2);
                break;
            case ATTACKER:
                paint.setColor(0xFFE53935);
                pixelRect(canvas, px, py, u, 9, 9, 1, 12);
                paint.setColor(0xFFB0BEC5);
                pixelRect(canvas, px, py, u, 8, 10, 0, 1);
                break;
            case LABORER:
            default:
                break;
        }
    }

    private static int shade(int color, float factor) {
        int r = Math.min(255, Math.round(Color.red(color) * factor));
        int g = Math.min(255, Math.round(Color.green(color) * factor));
        int b = Math.min(255, Math.round(Color.blue(color) * factor));
        return Color.argb(Color.alpha(color), r, g, b);
    }

    private static int mix(int a, int b, float weightOfB) {
        float w = Math.max(0f, Math.min(1f, weightOfB));
        int r = Math.round(Color.red(a) * (1 - w) + Color.red(b) * w);
        int g = Math.round(Color.green(a) * (1 - w) + Color.green(b) * w);
        int bl = Math.round(Color.blue(a) * (1 - w) + Color.blue(b) * w);
        return Color.argb(255, r, g, bl);
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

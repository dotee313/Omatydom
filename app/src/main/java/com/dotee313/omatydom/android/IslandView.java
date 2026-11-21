package com.dotee313.omatydom.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
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
import com.dotee313.omatydom.core.world.Biome;
import com.dotee313.omatydom.core.world.IslandMap;
import com.dotee313.omatydom.core.world.Structure;
import com.dotee313.omatydom.core.world.Tile;
import com.dotee313.omatydom.core.world.TileType;
import com.dotee313.omatydom.core.world.Weather;

import java.util.ArrayList;
import java.util.List;

/**
 * Standalone, code-drawn sandbox renderer for Omatydom.
 *
 * <p>Every sprite (terrain, trees, buildings, people, animals, weather and
 * powers) is painted with {@link Canvas} and {@link Paint} primitives. No
 * external artwork, fonts or resources are used. The view keeps its own
 * camera (pixel cell size plus a world-space centre) and only draws the
 * tiles that are actually visible.</p>
 */
public final class IslandView extends View {

    // ------------------------------------------------------------------
    // Public contract
    // ------------------------------------------------------------------

    public enum Overlay { TERRAIN, BIOME, KINGDOM, RELIGION, JOB }

    public enum Brush {
        INSPECT, WATER, SAND, GRASS, FOREST, ORCHARD, MOUNTAIN, CAVE,
        HOUSE, FARM, ROAD, HUMAN, WOLF, FIRE, RAIN, METEOR
    }

    public enum Effect {
        RAIN, BLOOM, HEAL, LIGHTNING, FIRE, METEOR, WAR, BLESS, PEACE
    }

    public interface OnThingTappedListener {
        void onAgentTapped(Citizen citizen);

        void onTileTapped(int x, int y);

        /** Default no-op keeps older listeners source-compatible. */
        default void onTilePainted(int x, int y, Brush brush) {
        }
    }

    // ------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------

    private static final int MAX_EFFECTS = 24;
    private static final int TAP_SLOP_PX = 12;
    private static final float ZOOM_STEP = 1.25f;

    private final Paint paint = new Paint();
    private final Handler animatorHandler = new Handler(Looper.getMainLooper());
    private final ArrayList<Fx> effects = new ArrayList<Fx>();

    private OmatydomGame game;
    private OnThingTappedListener listener;
    private Overlay overlay = Overlay.TERRAIN;
    private Brush brush = Brush.INSPECT;
    private boolean showGrid;
    private boolean attached;
    private boolean cameraInitialized;

    private float density = 1f;
    private float minCell = 10f;
    private float maxCell = 64f;
    private float cell = 22f;
    private float cameraX;
    private float cameraY;

    private int selectedCitizenId = -1;

    // Pointer tracking. -1 means "no pointer in that slot".
    private int primaryPointerId = -1;
    private int secondaryPointerId = -1;
    private float primaryX;
    private float primaryY;
    private float downX;
    private float downY;
    private long downTime;
    private boolean pointerMoved;
    private boolean pinching;
    private float pinchStartDistance;
    private float pinchStartCell;
    private float pinchMidX;
    private float pinchMidY;
    private long lastTapTime;
    private float lastTapX;
    private float lastTapY;

    private int lastPaintX = Integer.MIN_VALUE;
    private int lastPaintY = Integer.MIN_VALUE;
    private long lastBrushFeedback;

    private final Runnable animator = new Runnable() {
        @Override
        public void run() {
            if (!attached || game == null) {
                return;
            }
            pruneEffects(System.currentTimeMillis());
            invalidate();
            animatorHandler.postDelayed(this, 90);
        }
    };

    public IslandView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = Math.max(0.75f, getResources().getDisplayMetrics().density);
        minCell = 10f * density;
        maxCell = 64f * density;
        cell = 22f * density;
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setStyle(Paint.Style.FILL);
        setClickable(true);
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    public void setGame(OmatydomGame newGame) {
        game = newGame;
        selectedCitizenId = -1;
        effects.clear();
        cameraInitialized = false;
        if (game != null) {
            centerOnMap();
        }
        invalidate();
        startAnimation();
    }

    public void setOverlay(Overlay value) {
        overlay = value == null ? Overlay.TERRAIN : value;
        invalidate();
    }

    public void setBrush(Brush value) {
        brush = value == null ? Brush.INSPECT : value;
        lastPaintX = Integer.MIN_VALUE;
        lastPaintY = Integer.MIN_VALUE;
        invalidate();
    }

    public void setGrid(boolean show) {
        showGrid = show;
        invalidate();
    }

    public void zoomIn() {
        zoomBy(ZOOM_STEP, getWidth() / 2f, getHeight() / 2f);
    }

    public void zoomOut() {
        zoomBy(1f / ZOOM_STEP, getWidth() / 2f, getHeight() / 2f);
    }

    public void centerOnMap() {
        IslandMap map = game == null ? null : game.getMap();
        if (map != null) {
            cameraX = (map.getWidth() - 1) / 2f;
            cameraY = (map.getHeight() - 1) / 2f;
        }
        cell = 22f * density;
        cameraInitialized = true;
        clampCamera();
        invalidate();
    }

    public void playEffect(Effect effect, int x, int y) {
        if (effect == null) {
            return;
        }
        Fx fx = new Fx();
        fx.kind = effect;
        fx.x = x;
        fx.y = y;
        fx.start = System.currentTimeMillis();
        fx.duration = durationOf(effect);
        fx.seed = hash(x, y);
        effects.add(fx);
        while (effects.size() > MAX_EFFECTS) {
            effects.remove(0);
        }
        invalidate();
        startAnimation();
    }

    public void selectAgent(int citizenId) {
        selectedCitizenId = citizenId;
        invalidate();
    }

    public void setOnThingTappedListener(OnThingTappedListener value) {
        listener = value;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    // ------------------------------------------------------------------
    // Animation lifecycle - never a tight loop, stops when detached
    // ------------------------------------------------------------------

    private void startAnimation() {
        if (attached && game != null) {
            animatorHandler.removeCallbacks(animator);
            animatorHandler.post(animator);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        animatorHandler.removeCallbacks(animator);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!cameraInitialized && game != null) {
            centerOnMap();
        }
    }

    // ------------------------------------------------------------------
    // Camera and coordinate helpers
    // ------------------------------------------------------------------

    private void clampCamera() {
        IslandMap map = game == null ? null : game.getMap();
        if (map == null) {
            return;
        }
        cell = clamp(cell, minCell, maxCell);
        float margin = 2f;
        cameraX = clamp(cameraX, -margin, map.getWidth() - 1 + margin);
        cameraY = clamp(cameraY, -margin, map.getHeight() - 1 + margin);
    }

    private void zoomBy(float factor, float anchorScreenX, float anchorScreenY) {
        float worldX = screenToWorldX(anchorScreenX);
        float worldY = screenToWorldY(anchorScreenY);
        float oldCell = cell;
        cell = clamp(cell * factor, minCell, maxCell);
        if (oldCell == cell) {
            return;
        }
        cameraX = worldX - (anchorScreenX - getWidth() / 2f) / cell;
        cameraY = worldY - (anchorScreenY - getHeight() / 2f) / cell;
        clampCamera();
        invalidate();
    }

    private float screenToWorldX(float px) {
        return cameraX + (px - getWidth() / 2f) / cell;
    }

    private float screenToWorldY(float py) {
        return cameraY + (py - getHeight() / 2f) / cell;
    }

    private int screenToTileX(float px) {
        return (int) Math.floor(screenToWorldX(px));
    }

    private int screenToTileY(float py) {
        return (int) Math.floor(screenToWorldY(py));
    }

    private float tileScreenX(int tx) {
        return getWidth() / 2f + (tx - cameraX) * cell;
    }

    private float tileScreenY(int ty) {
        return getHeight() / 2f + (ty - cameraY) * cell;
    }

    private static int durationOf(Effect effect) {
        switch (effect) {
            case LIGHTNING: return 620;
            case METEOR: return 1500;
            case WAR: return 900;
            case FIRE: return 1150;
            case PEACE: return 1250;
            case BLESS:
            case HEAL: return 1350;
            case BLOOM: return 1400;
            case RAIN:
            default: return 1500;
        }
    }

    private void pruneEffects(long now) {
        for (int i = effects.size() - 1; i >= 0; i--) {
            if (now - effects.get(i).start >= effects.get(i).duration) {
                effects.remove(i);
            }
        }
    }

    // ------------------------------------------------------------------
    // Touch: pointer-aware pan, paint, pinch and double-tap
    // ------------------------------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        long now = System.currentTimeMillis();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                primaryPointerId = pointerId;
                primaryX = event.getX(pointerIndex);
                primaryY = event.getY(pointerIndex);
                downX = primaryX;
                downY = primaryY;
                downTime = now;
                pointerMoved = false;
                pinching = false;
                secondaryPointerId = -1;
                lastPaintX = Integer.MIN_VALUE;
                lastPaintY = Integer.MIN_VALUE;
                if (brush != Brush.INSPECT) {
                    paintAt(screenToTileX(primaryX), screenToTileY(primaryY), now);
                }
                disallowParentIntercept();
                return true;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerId != primaryPointerId && secondaryPointerId == -1) {
                    secondaryPointerId = pointerId;
                    pinching = true;
                    pointerMoved = true;
                    pinchStartCell = cell;
                    pinchStartDistance = pointerDistance(event);
                    float mid = midpointX(event);
                    pinchMidX = mid;
                    pinchMidY = midpointY(event);
                    if (pinchStartDistance < 1f) {
                        pinchStartDistance = 1f;
                    }
                }
                disallowParentIntercept();
                return true;

            case MotionEvent.ACTION_MOVE: {
                int primaryIndex = event.findPointerIndex(primaryPointerId);
                if (primaryIndex < 0) {
                    return true;
                }
                float x = event.getX(primaryIndex);
                float y = event.getY(primaryIndex);
                if (pinching && secondaryPointerId != -1) {
                    int secondaryIndex = event.findPointerIndex(secondaryPointerId);
                    if (secondaryIndex >= 0) {
                        float distance = pointerDistance(event);
                        cell = clamp(pinchStartCell * distance / pinchStartDistance,
                                minCell, maxCell);
                        float midX = midpointX(event);
                        float midY = midpointY(event);
                        cameraX -= (midX - pinchMidX) / cell;
                        cameraY -= (midY - pinchMidY) / cell;
                        pinchMidX = midX;
                        pinchMidY = midY;
                        clampCamera();
                        invalidate();
                    }
                    primaryX = x;
                    primaryY = y;
                    return true;
                }
                float dx = x - downX;
                float dy = y - downY;
                if (dx * dx + dy * dy > TAP_SLOP_PX * TAP_SLOP_PX) {
                    pointerMoved = true;
                }
                float stepX = x - primaryX;
                float stepY = y - primaryY;
                if (brush == Brush.INSPECT) {
                    if (pointerMoved) {
                        cameraX -= stepX / cell;
                        cameraY -= stepY / cell;
                        clampCamera();
                        invalidate();
                    }
                } else if (pointerMoved) {
                    paintAt(screenToTileX(x), screenToTileY(y), now);
                }
                primaryX = x;
                primaryY = y;
                return true;
            }

            case MotionEvent.ACTION_POINTER_UP:
                if (pointerId == secondaryPointerId) {
                    secondaryPointerId = -1;
                    pinching = false;
                    pointerMoved = true;
                    downTime = 0L;
                } else if (pointerId == primaryPointerId && secondaryPointerId != -1) {
                    primaryPointerId = secondaryPointerId;
                    secondaryPointerId = -1;
                    pinching = false;
                    pointerMoved = true;
                    downTime = 0L;
                    int idx = event.findPointerIndex(primaryPointerId);
                    if (idx >= 0) {
                        primaryX = downX = event.getX(idx);
                        primaryY = downY = event.getY(idx);
                    }
                }
                return true;

            case MotionEvent.ACTION_UP: {
                boolean isPrimary = pointerId == primaryPointerId;
                float upX = event.getX(pointerIndex);
                float upY = event.getY(pointerIndex);
                if (isPrimary) {
                    finishTap(upX, upY, now);
                    performClick();
                }
                resetPointers();
                return true;
            }

            case MotionEvent.ACTION_CANCEL:
                resetPointers();
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    private void finishTap(float x, float y, long now) {
        boolean isTap = !pointerMoved && !pinching && downTime > 0
                && (now - downTime) < 700L
                && (x - downX) * (x - downX) + (y - downY) * (y - downY)
                <= TAP_SLOP_PX * TAP_SLOP_PX;
        if (isTap && now - lastTapTime < 300L
                && (x - lastTapX) * (x - lastTapX) + (y - lastTapY) * (y - lastTapY) < 1600f) {
            zoomBy(ZOOM_STEP, x, y);
            lastTapTime = 0L;
            return;
        }
        if (!isTap) {
            return;
        }
        lastTapTime = now;
        lastTapX = x;
        lastTapY = y;
        int tx = screenToTileX(x);
        int ty = screenToTileY(y);
        IslandMap map = game == null ? null : game.getMap();
        if (map == null || !map.inBounds(tx, ty)) {
            return;
        }
        if (brush == Brush.INSPECT) {
            Agent tapped = null;
            for (Agent agent : game.getAgents()) {
                if (agent.getCitizen().isAlive()
                        && agent.getX() == tx && agent.getY() == ty) {
                    tapped = agent;
                    break;
                }
            }
            if (tapped != null) {
                selectedCitizenId = tapped.getCitizen().getId();
                if (listener != null) {
                    listener.onAgentTapped(tapped.getCitizen());
                }
            } else if (listener != null) {
                listener.onTileTapped(tx, ty);
            }
            invalidate();
        } else {
            paintAt(tx, ty, now);
        }
    }

    private void paintAt(int tx, int ty, long now) {
        IslandMap map = game == null ? null : game.getMap();
        if (map == null || !map.inBounds(tx, ty) || brush == Brush.INSPECT) {
            return;
        }
        if (tx == lastPaintX && ty == lastPaintY) {
            return;
        }
        lastPaintX = tx;
        lastPaintY = ty;
        if (listener != null) {
            listener.onTilePainted(tx, ty, brush);
        }
        if (now - lastBrushFeedback > 140L
                && (brush == Brush.FIRE || brush == Brush.RAIN || brush == Brush.METEOR)) {
            lastBrushFeedback = now;
            if (brush == Brush.FIRE) {
                playEffect(Effect.FIRE, tx, ty);
            } else if (brush == Brush.RAIN) {
                playEffect(Effect.RAIN, tx, ty);
            } else {
                playEffect(Effect.METEOR, tx, ty);
            }
        }
    }

    private void resetPointers() {
        primaryPointerId = -1;
        secondaryPointerId = -1;
        pinching = false;
        pointerMoved = false;
        downTime = 0L;
        lastPaintX = Integer.MIN_VALUE;
        lastPaintY = Integer.MIN_VALUE;
    }

    private void disallowParentIntercept() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private float pointerDistance(MotionEvent event) {
        if (secondaryPointerId == -1) {
            return 1f;
        }
        int a = event.findPointerIndex(primaryPointerId);
        int b = event.findPointerIndex(secondaryPointerId);
        if (a < 0 || b < 0) {
            return 1f;
        }
        float dx = event.getX(a) - event.getX(b);
        float dy = event.getY(a) - event.getY(b);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float midpointX(MotionEvent event) {
        int a = event.findPointerIndex(primaryPointerId);
        int b = event.findPointerIndex(secondaryPointerId);
        return a >= 0 && b >= 0 ? (event.getX(a) + event.getX(b)) / 2f : primaryX;
    }

    private float midpointY(MotionEvent event) {
        int a = event.findPointerIndex(primaryPointerId);
        int b = event.findPointerIndex(secondaryPointerId);
        return a >= 0 && b >= 0 ? (event.getY(a) + event.getY(b)) / 2f : primaryY;
    }

    // ------------------------------------------------------------------
    // Draw pipeline
    // ------------------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        IslandMap map = game.getMap();
        if (map == null) {
            return;
        }
        clampCamera();
        long now = System.currentTimeMillis();
        int day = game.getWorld().getDay();
        Weather weather = game.getWorld().getWeather();
        int season = OmatydomGame.seasonOf(day);

        drawOcean(canvas, now, weather);

        float leftBound = cameraX - getWidth() / (2f * cell);
        float rightBound = cameraX + getWidth() / (2f * cell);
        float topBound = cameraY - getHeight() / (2f * cell);
        float bottomBound = cameraY + getHeight() / (2f * cell);
        int x0 = Math.max(0, (int) Math.floor(leftBound) - 1);
        int x1 = Math.min(map.getWidth() - 1, (int) Math.ceil(rightBound) + 1);
        int y0 = Math.max(0, (int) Math.floor(topBound) - 1);
        int y1 = Math.min(map.getHeight() - 1, (int) Math.ceil(bottomBound) + 1);
        float u = cell / 8f;

        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                Tile tile = map.get(x, y);
                if (tile != null) {
                    drawTile(canvas, map, tile, x, y, x0, y0, x1, y1,
                            tileScreenX(x), tileScreenY(y), u, now, weather, season);
                }
            }
        }

        if (showGrid) {
            drawGrid(canvas, x0, y0, x1, y1, u);
        }
        drawClouds(canvas, now, weather);
        drawWeatherParticles(canvas, now, weather);
        drawAtmosphere(canvas, weather, season, day, game.getDayProgress());

        for (Animal animal : game.getAnimals()) {
            if (animal.isAlive() && isOnScreen(animal.getX(), animal.getY(), 2)) {
                drawAnimal(canvas, animal, tileScreenX(animal.getX()),
                        tileScreenY(animal.getY()), u, now);
            }
        }
        for (Agent agent : game.getAgents()) {
            if (agent.getCitizen().isAlive() && isOnScreen(agent.getX(), agent.getY(), 2)) {
                drawPerson(canvas, agent, tileScreenX(agent.getX()),
                        tileScreenY(agent.getY()), now);
            }
        }
        drawEffects(canvas, now, u);
    }

    private boolean isOnScreen(int tx, int ty, int marginTiles) {
        float sx = tileScreenX(tx);
        float sy = tileScreenY(ty);
        return sx > -marginTiles * cell && sx < getWidth() + marginTiles * cell
                && sy > -marginTiles * cell && sy < getHeight() + marginTiles * cell;
    }

    private void drawOcean(Canvas c, long now, Weather weather) {
        int w = getWidth();
        int h = getHeight();
        paint.setColor(0xFF0B1F2E);
        c.drawRect(0, 0, w, h, paint);
        int bands = 7;
        for (int i = 0; i < bands; i++) {
            float y0 = i * h / (float) bands;
            float y1 = (i + 1) * h / (float) bands + 1f;
            int blue = mix(0xFF0D2434, 0xFF164260, i / (float) (bands - 1));
            paint.setColor(blue);
            c.drawRect(0, y0, w, y1, paint);
        }
        paint.setColor(withAlpha(0xFF6FB4D8, weather == Weather.STORM ? 0x14 : 0x22));
        for (int i = 0; i < 14; i++) {
            int seed = hash(i, 91);
            float px = wrap((seed & 0xffff) / 80f + (now % 150000L) / 150f, w + 60f) - 30f;
            float py = ((seed >>> 8) & 0xffff) / 65535f * h;
            c.drawRect(px, py, px + 14f + (seed & 7) * 3f, py + 1f, paint);
        }
    }

    // ------------------------------------------------------------------
    // Terrain: noisy tiles, shorelines, trees, rock and caves
    // ------------------------------------------------------------------

    private void drawTile(Canvas c, IslandMap map, Tile tile, int tx, int ty,
                          int vx0, int vy0, int vx1, int vy1,
                          float left, float top, float u, long now,
                          Weather weather, int season) {
        TileType type = tile.getType();
        Biome biome = tile.getBiome();
        int h = hash(tx, ty);

        int base = opaque(type.getColor());
        if (biome != null && type != TileType.WATER) {
            base = mix(base, biome.getColor(), 0.16f);
        }
        base = mix(base, (h & 1) == 0 ? 0xFFFFFFFF : 0xFF000000,
                0.025f + ((h >>> 9) & 15) * 0.0035f);
        if (type != TileType.WATER) {
            if (weather == Weather.DROUGHT) {
                base = mix(base, 0xFF9C7842, 0.34f);
            } else if (weather == Weather.ASH) {
                base = mix(base, 0xFF6F6B66, 0.42f);
            }
            if (season == 2) {
                base = mix(base, 0xFFC9793C, 0.12f);
            } else if (season == 3 || weather == Weather.SNOW) {
                base = mix(base, 0xFFD8EAF5, 0.16f);
            }
        }
        paint.setColor(base);
        c.drawRect(left - 0.35f, top - 0.35f, left + 8f * u + 0.5f, top + 8f * u + 0.5f, paint);

        switch (type) {
            case WATER:
                drawWater(c, map, tx, ty, left, top, u, h, now, weather);
                break;
            case SAND:
                drawSand(c, left, top, u, h);
                break;
            case GRASS:
                drawGrass(c, left, top, u, h, base, biome, weather, season);
                break;
            case FOREST:
                drawGrass(c, left, top, u, h, base, biome, weather, season);
                drawTrees(c, left, top, u, h, biome, tile.getResource(), weather, season);
                break;
            case ORCHARD:
                drawGrass(c, left, top, u, h, base, biome, weather, season);
                drawOrchard(c, left, top, u, h, tile.getResource(), weather, season);
                break;
            case MOUNTAIN:
                drawGrass(c, left, top, u, h, base, biome, weather, season);
                drawMountain(c, left, top, u, h, biome, tile.getResource(), weather, season);
                break;
            case CAVE:
                drawGrass(c, left, top, u, h, base, biome, weather, season);
                drawCave(c, left, top, u, h, tile.getResource(), weather);
                break;
            default:
                break;
        }

        drawOverlayTint(c, tile, tx, ty, left, top, u);
        if (tile.getStructure() != Structure.NONE) {
            drawStructure(c, tile.getStructure(), left, top, u, map, tx, ty, now);
        }
    }

    private boolean isWater(IslandMap map, int x, int y) {
        return map.typeAt(x, y) == TileType.WATER;
    }

    private void drawWater(Canvas c, IslandMap map, int tx, int ty,
                           float left, float top, float u, int h,
                           long now, Weather weather) {
        boolean shallow = !isWater(map, tx - 1, ty) || !isWater(map, tx + 1, ty)
                || !isWater(map, tx, ty - 1) || !isWater(map, tx, ty + 1);
        int water = shallow ? 0xFF4FA7C6 : 0xFF21618F;
        water = mix(water, (h & 1) == 0 ? 0xFFFFFFFF : 0xFF001B33, 0.06f);
        if (weather == Weather.STORM) {
            water = mix(water, 0xFF102B44, 0.28f);
        } else if (weather == Weather.SNOW) {
            water = mix(water, 0xFF8FBBD0, 0.16f);
        }
        paint.setColor(water);
        c.drawRect(left - 0.35f, top - 0.35f, left + 8f * u + 0.5f, top + 8f * u + 0.5f, paint);

        // Depth bands, never a flat cell.
        paint.setColor(withAlpha(0xFF8FD3F4, shallow ? 0x2A : 0x18));
        rect(c, left, top, u, 0, 1.2f, 8, 2.3f);
        paint.setColor(withAlpha(0xFF0C3A5E, 0x26));
        rect(c, left, top, u, 0, 4.6f, 8, 5.6f);
        paint.setColor(withAlpha(0xFFBFE9FF, 0x16));
        rect(c, left, top, u, 0, 6.8f, 8, 7.4f);

        // Animated horizontal glints.
        float phase = (now % 190000L) / 190f + (h & 7) * 0.37f;
        int wy = (int) (phase * 3f) % 7;
        int gx = (h >>> 6) & 3;
        paint.setColor(withAlpha(0xFFD9F2FF, shallow ? 0x88 : 0x55));
        rect(c, left, top, u, 0.6f + gx, wy + 0.25f, 3.2f + gx, wy + 0.65f);
        int wy2 = (wy + 3) % 7;
        paint.setColor(withAlpha(0xFF9FD8F2, 0x66));
        rect(c, left, top, u, 3.8f + ((h >>> 10) & 2), wy2 + 0.25f,
                6.6f + ((h >>> 10) & 2), wy2 + 0.6f);

        // Foam on each land-facing edge.
        int foam = 0x77 + (int) (Math.sin(now / 260f + h) * 0x18);
        paint.setColor(Color.argb(clamp255(foam), 232, 249, 255));
        if (!isWater(map, tx, ty - 1)) {
            rect(c, left, top, u, 0, 0, 8, 0.45f);
        }
        if (!isWater(map, tx, ty + 1)) {
            rect(c, left, top, u, 0, 7.55f, 8, 8);
        }
        if (!isWater(map, tx - 1, ty)) {
            rect(c, left, top, u, 0, 0, 0.45f, 8);
        }
        if (!isWater(map, tx + 1, ty)) {
            rect(c, left, top, u, 7.55f, 0, 8, 8);
        }

        // Rare lily pad or fish silhouette.
        if ((h & 63) == 0) {
            paint.setColor(0xFF4E8C3A);
            rect(c, left, top, u, 2, 3, 4.4f, 4.6f);
            paint.setColor(0xFF78B451);
            rect(c, left, top, u, 2.3f, 3.2f, 3.8f, 3.9f);
            paint.setColor(0xFFEFEBD0);
            rect(c, left, top, u, 4.6f, 3.4f, 5.1f, 4f);
        } else if ((h & 127) == 5) {
            paint.setColor(withAlpha(0xFF123F5E, 0xBB));
            float fx = 2f + ((h >>> 12) & 3);
            rect(c, left, top, u, fx, 4.2f, fx + 2.2f, 5.2f);
            rect(c, left, top, u, fx + 2.2f, 3.6f, fx + 3f, 5.8f);
        }
    }

    private void drawSand(Canvas c, float left, float top, float u, int h) {
        paint.setColor(0xFFE8CE92);
        rect(c, left, top, u, 0, 0, 8, 3.4f);
        paint.setColor(0xFFC6A667);
        rect(c, left, top, u, 0, 5.4f, 8, 8);
        paint.setColor(0xFFD8B87A);
        rect(c, left, top, u, 0, 3.4f, 8, 5.4f);
        for (int i = 0; i < 5; i++) {
            float gx = 0.4f + ((h >>> (i * 3 + 2)) & 7);
            float gy = 0.4f + ((h >>> (i * 3 + 5)) & 6);
            paint.setColor((i & 1) == 0 ? withAlpha(0xFFFFF0C4, 0xAA) : withAlpha(0xFF8B7044, 0x66));
            rect(c, left, top, u, gx, gy, gx + 0.42f, gy + 0.42f);
        }
        if ((h & 7) == 0) {
            paint.setColor(0xFFFDF3DD);
            rect(c, left, top, u, 5.2f, 5.6f, 6.2f, 6f);
            rect(c, left, top, u, 5.5f, 5.2f, 5.9f, 6.3f);
        }
    }

    private void drawGrass(Canvas c, float left, float top, float u, int h,
                           int base, Biome biome, Weather weather, int season) {
        paint.setColor(mix(base, 0xFF20461F, 0.32f));
        rect(c, left, top, u, 0.7f + ((h >>> 4) & 3), 1f + ((h >>> 7) & 3),
                2.9f + ((h >>> 4) & 3), 2.4f + ((h >>> 7) & 3));
        paint.setColor(mix(base, 0xFFC8E07A, 0.24f));
        rect(c, left, top, u, 3.5f + ((h >>> 11) & 3), 4.2f + ((h >>> 14) & 2),
                5.7f + ((h >>> 11) & 3), 5.3f + ((h >>> 14) & 2));

        paint.setColor(mix(base, 0xFF1F4B20, 0.45f));
        for (int i = 0; i < 3; i++) {
            float gx = 0.8f + ((h >>> (i * 4 + 3)) & 6);
            float gy = 5.4f + ((h >>> (i * 3 + 6)) & 2);
            rect(c, left, top, u, gx, gy, gx + 0.36f, gy + 1.1f);
            rect(c, left, top, u, gx + 0.5f, gy + 0.35f, gx + 0.86f, gy + 1.15f);
        }
        if ((h & 7) < 2) {
            int flower = (h & 16) == 0 ? 0xFFFFD54F : (h & 32) == 0 ? 0xFFF06292 : 0xFFE8F5E9;
            float fx = 2f + ((h >>> 17) & 4);
            float fy = 3f + ((h >>> 20) & 4);
            paint.setColor(flower);
            rect(c, left, top, u, fx, fy, fx + 0.6f, fy + 0.6f);
        }

        if (biome != null) {
            drawBiomeAccent(c, left, top, u, h, biome, weather, season);
        }
        if (weather == Weather.SNOW || season == 3) {
            paint.setColor(withAlpha(0xFFF4FBFF, weather == Weather.SNOW ? 0xC0 : 0x66));
            rect(c, left, top, u, 0, 0, 8, 0.9f);
            rect(c, left, top, u, 0.9f, 0.9f, 3.4f, 1.5f);
        }
    }

    private void drawBiomeAccent(Canvas c, float left, float top, float u, int h,
                                 Biome biome, Weather weather, int season) {
        switch (biome) {
            case ENCHANTED:
                paint.setColor(withAlpha(0xE0D7A2FF, 0x99));
                rect(c, left, top, u, 1.4f, 2.2f, 1.9f, 2.7f);
                rect(c, left, top, u, 5.6f, 5.1f, 6.1f, 5.6f);
                paint.setColor(withAlpha(0xFFFFFFFF, 0xAA));
                rect(c, left, top, u, 3.4f, 1.2f, 3.7f, 1.5f);
                break;
            case MUSHROOM:
                paint.setColor(0xFFF3E9E4);
                rect(c, left, top, u, 2.2f, 4.4f, 2.7f, 5.8f);
                paint.setColor(0xFFC2528F);
                rect(c, left, top, u, 1.6f, 3.6f, 3.3f, 4.5f);
                paint.setColor(0xFFFFFFFF);
                rect(c, left, top, u, 2f, 3.9f, 2.4f, 4.2f);
                break;
            case CANDY:
                paint.setColor(withAlpha(0xFFFF9EC7, 0x99));
                rect(c, left, top, u, 1.2f, 2.6f, 2.1f, 3.5f);
                paint.setColor(withAlpha(0xFFFFF3A6, 0x99));
                rect(c, left, top, u, 5.2f, 5.4f, 6.1f, 6.3f);
                break;
            case CRYSTAL:
                paint.setColor(withAlpha(0xFF9FF3FF, 0xBB));
                rect(c, left, top, u, 4f, 2f, 4.5f, 3.6f);
                rect(c, left, top, u, 3.5f, 3.2f, 5f, 3.8f);
                break;
            case INFERNAL:
                paint.setColor(withAlpha(0xFFFF6D3D, 0xAA));
                rect(c, left, top, u, 2.6f, 5.2f, 3f, 5.9f);
                rect(c, left, top, u, 5.8f, 2.2f, 6.2f, 2.8f);
                break;
            case LEMON:
                paint.setColor(withAlpha(0xFFF6E847, 0xAA));
                rect(c, left, top, u, 3f, 3f, 3.7f, 3.7f);
                rect(c, left, top, u, 6f, 5.6f, 6.6f, 6.2f);
                break;
            case SAVANNA:
                paint.setColor(withAlpha(0xFFDFC274, 0x88));
                rect(c, left, top, u, 1f, 6f, 2.4f, 6.5f);
                break;
            case PERMAFROST:
                paint.setColor(withAlpha(0xFFEAF8FF, 0x99));
                rect(c, left, top, u, 4.6f, 3.4f, 5.8f, 3.8f);
                break;
            case JUNGLE:
                paint.setColor(withAlpha(0xFF144D2C, 0x77));
                rect(c, left, top, u, 5f, 1f, 7f, 2.1f);
                break;
            case WASTELAND:
                paint.setColor(withAlpha(0xFFB9A17D, 0x88));
                rect(c, left, top, u, 2f, 2.4f, 3.6f, 2.8f);
                break;
            case CORRUPTED:
            case TUMOR:
                paint.setColor(withAlpha(0xFF4B1E5E, 0x99));
                rect(c, left, top, u, 5.2f, 2.6f, 6.4f, 3.8f);
                break;
            default:
                break;
        }
    }

    private void drawTrees(Canvas c, float left, float top, float u, int h,
                           Biome biome, int resource, Weather weather, int season) {
        int count = 1 + (h & 1);
        if (resource > 24) {
            count = 3;
        }
        for (int i = 0; i < count; i++) {
            float cx = 1.4f + ((h >>> (i * 3 + 4)) & 5);
            float cy = 6.6f - ((h >>> (i * 2 + 1)) & 2) * 0.3f;
            float size = 2.1f + ((h >>> (i + 6)) & 3) * 0.32f;
            drawTree(c, left, top, u, cx, cy, size, biome, h + i * 17, weather, season, false);
        }
    }

    private void drawOrchard(Canvas c, float left, float top, float u, int h,
                             int resource, Weather weather, int season) {
        int count = resource > 22 ? 2 : 1;
        for (int i = 0; i < count; i++) {
            float cx = 2.2f + ((h >>> (i * 4 + 3)) & 4);
            float cy = 6.4f - i * 0.3f;
            drawTree(c, left, top, u, cx, cy, 2.4f, Biome.SAVANNA,
                    h + i * 31, weather, season, true);
        }
    }

    private void drawTree(Canvas c, float left, float top, float u,
                          float cx, float cy, float s, Biome biome, int seed,
                          Weather weather, int season, boolean fruit) {
        paint.setColor(0x4A1E2018);
        rect(c, left, top, u, cx - s * 0.85f, cy - 0.15f, cx + s * 0.85f, cy + 0.4f);

        boolean snowy = weather == Weather.SNOW || season == 3 || biome == Biome.PERMAFROST;
        boolean mushroom = biome == Biome.MUSHROOM;
        boolean candy = biome == Biome.CANDY;
        boolean enchanted = biome == Biome.ENCHANTED;

        int canopy = 0xFF2E7D32;
        int canopyDark = 0xFF1B5E20;
        int canopyLight = 0xFF66BB6A;
        if (enchanted) {
            canopy = 0xFF8E5BC7;
            canopyDark = 0xFF5E348F;
            canopyLight = 0xFFC792EA;
        } else if (candy) {
            canopy = 0xFFF06292;
            canopyDark = 0xFFC2185B;
            canopyLight = 0xFFFFB3D1;
        } else if (biome == Biome.JUNGLE) {
            canopy = 0xFF237A46;
            canopyDark = 0xFF124D2B;
            canopyLight = 0xFF4CAF6A;
        } else if (biome == Biome.LEMON) {
            canopy = 0xFF9CA72F;
            canopyDark = 0xFF6B7620;
            canopyLight = 0xFFDCE85C;
        }

        if (mushroom) {
            paint.setColor(0xFFEFE4D2);
            rect(c, left, top, u, cx - 0.42f, cy - s * 1.1f, cx + 0.42f, cy);
            paint.setColor(0xFF9C3E7F);
            rect(c, left, top, u, cx - s, cy - s * 1.7f, cx + s, cy - s * 0.95f);
            paint.setColor(0xFFD96CAE);
            rect(c, left, top, u, cx - s * 0.7f, cy - s * 1.9f, cx + s * 0.7f, cy - s * 1.4f);
            paint.setColor(0xFFFFF5F0);
            rect(c, left, top, u, cx - s * 0.5f, cy - s * 1.6f, cx - s * 0.2f, cy - s * 1.35f);
            rect(c, left, top, u, cx + s * 0.25f, cy - s * 1.5f, cx + s * 0.55f, cy - s * 1.25f);
            return;
        }

        paint.setColor(0xFF5D3A1A);
        rect(c, left, top, u, cx - 0.34f, cy - s * 1.25f, cx + 0.34f, cy);
        paint.setColor(0xFF3E2712);
        rect(c, left, top, u, cx + 0.1f, cy - s * 1.25f, cx + 0.34f, cy);

        if (snowy) {
            paint.setColor(0xFF2C5B45);
            rect(c, left, top, u, cx - s, cy - s * 1.7f, cx + s, cy - s * 0.75f);
            paint.setColor(0xFF3D7057);
            rect(c, left, top, u, cx - s * 0.75f, cy - s * 2.3f, cx + s * 0.75f, cy - s * 1.45f);
            paint.setColor(0xFF4A8064);
            rect(c, left, top, u, cx - s * 0.45f, cy - s * 2.85f, cx + s * 0.45f, cy - s * 1.95f);
            paint.setColor(withAlpha(0xFFF3FBFF, 0xDD));
            rect(c, left, top, u, cx - s * 0.45f, cy - s * 2.85f, cx + s * 0.45f, cy - s * 2.55f);
            rect(c, left, top, u, cx - s * 0.75f, cy - s * 2.3f, cx + s * 0.75f, cy - s * 2.05f);
            rect(c, left, top, u, cx - s, cy - s * 1.7f, cx + s, cy - s * 1.48f);
            return;
        }

        if (biome == Biome.JUNGLE) {
            // Taller, narrower rainforest silhouette with splayed fronds.
            paint.setColor(canopyDark);
            rect(c, left, top, u, cx - s * 0.72f, cy - s * 2.6f,
                    cx + s * 0.72f, cy - s * 0.75f);
            paint.setColor(canopy);
            rect(c, left, top, u, cx - s * 0.52f, cy - s * 2.95f,
                    cx + s * 0.52f, cy - s * 0.95f);
            paint.setColor(canopyLight);
            rect(c, left, top, u, cx - s * 0.3f, cy - s * 3.15f,
                    cx + s * 0.2f, cy - s * 2.05f);
            paint.setColor(canopyDark);
            rect(c, left, top, u, cx - s, cy - s * 1.7f, cx - s * 0.45f, cy - s * 1.4f);
            rect(c, left, top, u, cx + s * 0.45f, cy - s * 1.9f, cx + s, cy - s * 1.6f);
        } else {
            paint.setColor(canopyDark);
            rect(c, left, top, u, cx - s, cy - s * 1.95f, cx + s, cy - s * 0.85f);
            paint.setColor(canopy);
            rect(c, left, top, u, cx - s * 0.82f, cy - s * 2.25f, cx + s * 0.82f, cy - s * 1.05f);
            paint.setColor(canopyLight);
            rect(c, left, top, u, cx - s * 0.5f, cy - s * 2.55f, cx + s * 0.45f, cy - s * 1.65f);
        }
        paint.setColor(withAlpha(0xFFFFFFFF, candy ? 0x88 : 0x33));
        rect(c, left, top, u, cx - s * 0.32f, cy - s * 2.42f, cx + s * 0.1f, cy - s * 2.12f);

        if (enchanted || candy) {
            paint.setColor(withAlpha(enchanted ? 0xFFE3B3FF : 0xFFFFF0A0, 0xCC));
            rect(c, left, top, u, cx + s * 0.55f, cy - s * 2.2f, cx + s * 0.8f, cy - s * 1.95f);
        }
        if (fruit) {
            int fruitColor = (seed & 1) == 0 ? 0xFFE53935 : 0xFFFFCA28;
            paint.setColor(fruitColor);
            rect(c, left, top, u, cx - s * 0.6f, cy - s * 1.6f, cx - s * 0.3f, cy - s * 1.3f);
            rect(c, left, top, u, cx + s * 0.35f, cy - s * 1.35f, cx + s * 0.65f, cy - s * 1.05f);
        }
    }

    private void drawMountain(Canvas c, float left, float top, float u, int h,
                              Biome biome, int resource, Weather weather, int season) {
        paint.setColor(0xFF4E4E4E);
        rect(c, left, top, u, 0.4f, 6.2f, 7.6f, 8);
        paint.setColor(0xFF6B6B6B);
        rect(c, left, top, u, 1.2f, 3.6f, 6.8f, 7);
        paint.setColor(0xFF858585);
        rect(c, left, top, u, 2.2f, 1.6f, 5.8f, 5.4f);
        paint.setColor(0xFFA8A8A8);
        rect(c, left, top, u, 2.6f, 1.6f, 4.1f, 4.4f);
        paint.setColor(0xFFC9CCCC);
        rect(c, left, top, u, 2.9f, 1.5f, 3.7f, 2.9f);
        paint.setColor(withAlpha(0xFFFFFFFF, weather == Weather.SNOW ? 0xEE : 0xBB));
        rect(c, left, top, u, 2.9f, 1.2f, 3.9f, 2.2f);
        rect(c, left, top, u, 2.2f, 3.4f, 3.1f, 4.1f);
        if (season == 3 || biome == Biome.PERMAFROST) {
            paint.setColor(withAlpha(0xFFEAF8FF, 0x88));
            rect(c, left, top, u, 1.3f, 3.6f, 6.7f, 4.2f);
        }
        if (resource > 0) {
            paint.setColor(biome == Biome.CRYSTAL ? 0xFF8FF0FF : 0xFFFFD54F);
            int ox = 4 + ((h >>> 4) & 2);
            rect(c, left, top, u, ox, 4.4f, ox + 0.5f, 5f);
            rect(c, left, top, u, ox - 0.3f, 4.6f, ox + 0.8f, 4.85f);
        }
        if (weather == Weather.ASH) {
            paint.setColor(withAlpha(0xFF5A3428, 0x55));
            rect(c, left, top, u, 1.2f, 3.6f, 6.8f, 7);
        }
    }

    private void drawCave(Canvas c, float left, float top, float u, int h,
                          int resource, Weather weather) {
        paint.setColor(0xFF514438);
        rect(c, left, top, u, 0.5f, 4.6f, 7.5f, 8);
        paint.setColor(0xFF6E5B49);
        rect(c, left, top, u, 1f, 2.6f, 7f, 5.2f);
        paint.setColor(0xFF8B735D);
        rect(c, left, top, u, 2f, 1.8f, 6f, 3.4f);
        paint.setColor(0xFF140F0B);
        rect(c, left, top, u, 2.1f, 4.4f, 5.9f, 8);
        rect(c, left, top, u, 3f, 3.4f, 5f, 5f);
        paint.setColor(0xFF3B2E23);
        rect(c, left, top, u, 1.2f, 6.6f, 2.2f, 7.5f);
        rect(c, left, top, u, 5.8f, 6.2f, 6.9f, 7.2f);
        if (resource > 8) {
            paint.setColor(weather == Weather.ASH ? 0xFFFF8A50 : 0xFFB3E5FC);
            rect(c, left, top, u, 4.6f, 5.4f, 5f, 5.9f);
            rect(c, left, top, u, 3.2f, 6.4f, 3.5f, 6.8f);
        }
    }

    // ------------------------------------------------------------------
    // Overlays: layered translucency and a fine pattern, never solid blocks
    // ------------------------------------------------------------------

    private void drawOverlayTint(Canvas c, Tile tile, int x, int y,
                                 float left, float top, float u) {
        if (overlay == Overlay.TERRAIN) {
            return;
        }
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
            default:
                return;
        }
        if (color == 0) {
            return;
        }
        paint.setColor(withAlpha(color, 0x18));
        rect(c, left, top, u, 0.2f, 0.2f, 7.8f, 7.8f);
        paint.setColor(withAlpha(color, 0x2E));
        rect(c, left, top, u, 1f, 1f, 7f, 7f);
        int h = hash(x + 40, y - 17);
        paint.setColor(withAlpha(color, 0x5C));
        for (int i = 0; i < 3; i++) {
            float px = 1.1f + ((h >>> (i * 4)) & 5);
            float py = 1.1f + ((h >>> (i * 4 + 2)) & 5);
            rect(c, left, top, u, px, py, px + 0.85f, py + 0.85f);
        }
    }

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

    private int beliefColorAt(int x, int y) {
        if (game == null) {
            return 0;
        }
        Agent closest = null;
        int best = 5;
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
            seed = citizen.getReligion().ordinal() * 31
                    + citizen.getIdeology().ordinal() * 17;
        }
        return colorFromSeed(seed);
    }

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

    // ------------------------------------------------------------------
    // Structures: a distinct hand-pixeled sprite for every catalogued type
    // ------------------------------------------------------------------

    private void drawStructure(Canvas c, Structure s, float left, float top,
                                float u, IslandMap map, int tx, int ty, long now) {
        paint.setColor(0x4A1C1A16);
        rect(c, left, top, u, 0.45f, 7.3f, 7.6f, 7.9f);
        switch (s) {
            case HUT: {
                paint.setColor(0xFF8D5A2B);
                rect(c, left, top, u, 1.8f, 4.2f, 6.2f, 7.3f);
                paint.setColor(0xFF6D4220);
                rect(c, left, top, u, 1.8f, 4.2f, 2.5f, 7.3f);
                paint.setColor(0xFFC8A24C);
                rect(c, left, top, u, 1f, 3.4f, 7f, 4.4f);
                paint.setColor(0xFFE0BC68);
                rect(c, left, top, u, 1.8f, 2.5f, 6.2f, 3.5f);
                paint.setColor(0xFFA87C36);
                rect(c, left, top, u, 2.6f, 1.8f, 5.4f, 2.6f);
                paint.setColor(0xFF35210F);
                rect(c, left, top, u, 3.4f, 5.2f, 4.6f, 7.3f);
                paint.setColor(0xFFFFD54F);
                rect(c, left, top, u, 5.1f, 5f, 5.7f, 5.6f);
                break;
            }
            case HALL: {
                paint.setColor(0xFFA1887F);
                rect(c, left, top, u, 1f, 3.4f, 7f, 7.4f);
                paint.setColor(0xFF7C6B64);
                rect(c, left, top, u, 1f, 3.4f, 1.8f, 7.4f);
                paint.setColor(0xFF8E3B2B);
                rect(c, left, top, u, 0.6f, 2.3f, 7.4f, 3.7f);
                paint.setColor(0xFFB5523A);
                rect(c, left, top, u, 1.4f, 1.4f, 6.6f, 2.5f);
                paint.setColor(0xFF5D3124);
                rect(c, left, top, u, 2.2f, 3.7f, 2.7f, 7.4f);
                rect(c, left, top, u, 3.7f, 3.7f, 4.2f, 7.4f);
                rect(c, left, top, u, 5.2f, 3.7f, 5.7f, 7.4f);
                paint.setColor(0xFF2E1E18);
                rect(c, left, top, u, 3.2f, 5.2f, 4.7f, 7.4f);
                paint.setColor(0xFFFFCA28);
                rect(c, left, top, u, 2.8f, 4.2f, 3.2f, 4.8f);
                rect(c, left, top, u, 4.8f, 4.2f, 5.2f, 4.8f);
                paint.setColor(0xFFD32F2F);
                rect(c, left, top, u, 4f, 0.5f, 4.4f, 1.5f);
                rect(c, left, top, u, 4.4f, 0.6f, 5.3f, 1.2f);
                break;
            }
            case TEMPLE: {
                paint.setColor(0xFFCFC5A8);
                rect(c, left, top, u, 0.4f, 7f, 7.6f, 8f);
                rect(c, left, top, u, 0.9f, 6.5f, 7.1f, 7.1f);
                paint.setColor(0xFFF0E4C0);
                for (int i = 0; i < 4; i++) {
                    float px = 1.2f + i * 1.65f;
                    rect(c, left, top, u, px, 3f, px + 0.75f, 6.7f);
                }
                paint.setColor(0xFFBBAE8A);
                rect(c, left, top, u, 1.9f, 3f, 2.3f, 6.7f);
                rect(c, left, top, u, 5.2f, 3f, 5.6f, 6.7f);
                paint.setColor(0xFFD9AE45);
                rect(c, left, top, u, 0.6f, 2f, 7.4f, 3.3f);
                paint.setColor(0xFFF2CE69);
                rect(c, left, top, u, 1.5f, 1.1f, 6.5f, 2.2f);
                paint.setColor(0xFFFFF2A8);
                rect(c, left, top, u, 3.4f, 1.5f, 4.6f, 2.6f);
                break;
            }
            case LIBRARY: {
                paint.setColor(0xFF8E77A8);
                rect(c, left, top, u, 1f, 2.6f, 7f, 7.4f);
                paint.setColor(0xFF6B5686);
                rect(c, left, top, u, 1f, 2.6f, 1.8f, 7.4f);
                paint.setColor(0xFF4E3A63);
                rect(c, left, top, u, 0.7f, 1.7f, 7.3f, 2.9f);
                paint.setColor(0xFF3E2A1E);
                rect(c, left, top, u, 2.1f, 3.4f, 5.9f, 6.1f);
                paint.setColor(0xFFF3E8D0);
                rect(c, left, top, u, 2.5f, 3.8f, 3.9f, 5.7f);
                rect(c, left, top, u, 4.1f, 3.8f, 5.5f, 5.7f);
                paint.setColor(0xFF8D6E63);
                rect(c, left, top, u, 3.9f, 3.8f, 4.1f, 5.7f);
                paint.setColor(0xFFEF5350);
                rect(c, left, top, u, 2.6f, 4.2f, 3.7f, 4.5f);
                rect(c, left, top, u, 4.3f, 4.8f, 5.4f, 5.1f);
                paint.setColor(0xFFCE93D8);
                rect(c, left, top, u, 2.4f, 1.2f, 3.2f, 1.8f);
                break;
            }
            case TOWER: {
                paint.setColor(0xFF9E9E9E);
                rect(c, left, top, u, 2.4f, 2.4f, 5.6f, 8f);
                paint.setColor(0xFF6F6F6F);
                rect(c, left, top, u, 2.4f, 2.4f, 3.1f, 8f);
                paint.setColor(0xFFBDBDBD);
                rect(c, left, top, u, 2f, 2.2f, 6f, 3f);
                paint.setColor(0xFF8A8A8A);
                rect(c, left, top, u, 2f, 1.1f, 2.9f, 2.4f);
                rect(c, left, top, u, 3.4f, 1.1f, 4.3f, 2.4f);
                rect(c, left, top, u, 4.8f, 1.1f, 5.7f, 2.4f);
                paint.setColor(0xFF263238);
                rect(c, left, top, u, 3.5f, 4f, 4.5f, 5.6f);
                paint.setColor(0xFFE53935);
                rect(c, left, top, u, 4f, 0.2f, 4.25f, 1.2f);
                rect(c, left, top, u, 4.25f, 0.3f, 5.1f, 0.85f);
                break;
            }
            case BARRACKS: {
                paint.setColor(0xFFA1887F);
                rect(c, left, top, u, 1f, 3.6f, 7f, 7.4f);
                paint.setColor(0xFF7E6B62);
                rect(c, left, top, u, 1f, 3.6f, 1.7f, 7.4f);
                paint.setColor(0xFFB71C1C);
                rect(c, left, top, u, 0.6f, 2.5f, 7.4f, 3.8f);
                paint.setColor(0xFFE53935);
                rect(c, left, top, u, 1.4f, 1.9f, 6.6f, 2.7f);
                paint.setColor(0xFF35251F);
                rect(c, left, top, u, 3.3f, 5f, 4.7f, 7.4f);
                paint.setColor(0xFFE6C86A);
                rect(c, left, top, u, 2.2f, 4.4f, 3.1f, 5.4f);
                paint.setColor(0xFF5D4037);
                rect(c, left, top, u, 6.3f, 0.5f, 6.6f, 3.4f);
                paint.setColor(0xFFD32F2F);
                rect(c, left, top, u, 6.6f, 0.8f, 7.7f, 2.2f);
                paint.setColor(0xFFFFD54F);
                rect(c, left, top, u, 6.6f, 1.3f, 7.5f, 1.55f);
                break;
            }
            case DOCK: {
                paint.setColor(0xFF6D4C2F);
                rect(c, left, top, u, 0, 2.4f, 8, 3.15f);
                rect(c, left, top, u, 0, 4.1f, 8, 4.85f);
                rect(c, left, top, u, 0, 5.8f, 8, 6.55f);
                paint.setColor(0xFFA87C4C);
                rect(c, left, top, u, 0, 3.15f, 8, 3.45f);
                rect(c, left, top, u, 0, 4.85f, 8, 5.15f);
                paint.setColor(0xFF4E342E);
                rect(c, left, top, u, 0.7f, 3f, 1.4f, 8f);
                rect(c, left, top, u, 6.6f, 3f, 7.3f, 8f);
                paint.setColor(0xFF3E2723);
                rect(c, left, top, u, 4.7f, 6.6f, 7.4f, 7.4f);
                rect(c, left, top, u, 5.2f, 7.4f, 6.9f, 7.8f);
                break;
            }
            case BRIDGE: {
                paint.setColor(0xFF4E342E);
                rect(c, left, top, u, 0, 2.1f, 8, 6f);
                paint.setColor(0xFFA1887F);
                for (int i = 0; i < 5; i++) {
                    float px = 0.3f + i * 1.55f;
                    rect(c, left, top, u, px, 2.3f, px + 1.3f, 5.8f);
                }
                paint.setColor(0xFF6D4C41);
                rect(c, left, top, u, 0, 1.6f, 8, 2.1f);
                rect(c, left, top, u, 0, 5.9f, 8, 6.4f);
                rect(c, left, top, u, 0.7f, 1.1f, 1.2f, 2.4f);
                rect(c, left, top, u, 3.7f, 1.1f, 4.2f, 2.4f);
                rect(c, left, top, u, 6.7f, 1.1f, 7.2f, 2.4f);
                break;
            }
            case MINE: {
                paint.setColor(0xFF5D4037);
                rect(c, left, top, u, 1f, 3.2f, 1.9f, 8f);
                rect(c, left, top, u, 6.1f, 3.2f, 7f, 8f);
                rect(c, left, top, u, 0.7f, 2.4f, 7.3f, 3.4f);
                paint.setColor(0xFF0B0806);
                rect(c, left, top, u, 2.1f, 4.1f, 5.9f, 8f);
                paint.setColor(0xFF6D6D6D);
                rect(c, left, top, u, 0.4f, 6.4f, 2.1f, 8f);
                paint.setColor(0xFF37474F);
                rect(c, left, top, u, 2.7f, 6.5f, 5.2f, 7.8f);
                paint.setColor(0xFF80DEEA);
                rect(c, left, top, u, 3.2f, 6.8f, 3.7f, 7.3f);
                rect(c, left, top, u, 4.3f, 6.9f, 4.7f, 7.3f);
                break;
            }
            case SMITHY: {
                paint.setColor(0xFF78909C);
                rect(c, left, top, u, 1f, 3.6f, 7f, 7.4f);
                paint.setColor(0xFF546E7A);
                rect(c, left, top, u, 0.8f, 2.6f, 7.2f, 3.8f);
                paint.setColor(0xFF4E342E);
                rect(c, left, top, u, 5.3f, 0.6f, 6.4f, 2.8f);
                paint.setColor(withAlpha(0xFFB0BEC5, 0x88));
                rect(c, left, top, u, 5.7f, 0.1f, 6.1f, 0.6f);
                rect(c, left, top, u, 5.9f, -0.4f, 6.4f, 0f);
                paint.setColor(0xFF12100E);
                rect(c, left, top, u, 1.9f, 5f, 4.4f, 7.4f);
                paint.setColor(0xFFFF7043);
                rect(c, left, top, u, 2.3f, 5.5f, 4f, 6.9f);
                paint.setColor(0xFFFFE082);
                rect(c, left, top, u, 2.8f, 6f, 3.6f, 6.7f);
                paint.setColor(0xFF37474F);
                rect(c, left, top, u, 4.9f, 6.2f, 6.5f, 6.8f);
                rect(c, left, top, u, 5.4f, 6.8f, 6f, 7.4f);
                paint.setColor(0xFFFFAB40);
                rect(c, left, top, u, 4.5f, 4.6f, 4.8f, 5f);
                break;
            }
            case FARM: {
                paint.setColor(0xFF6D4C2F);
                for (int i = 0; i < 3; i++) {
                    float ry = 2.5f + i * 1.7f;
                    rect(c, left, top, u, 0.6f, ry, 7.4f, ry + 1.15f);
                }
                paint.setColor(0xFF8D6E42);
                for (int i = 0; i < 3; i++) {
                    float ry = 2.5f + i * 1.7f;
                    rect(c, left, top, u, 0.6f, ry, 7.4f, ry + 0.35f);
                }
                paint.setColor(0xFF66BB6A);
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 4; j++) {
                        float px = 1f + j * 1.65f;
                        float py = 2.8f + i * 1.7f;
                        rect(c, left, top, u, px, py, px + 0.35f, py + 0.75f);
                        rect(c, left, top, u, px + 0.5f, py + 0.2f, px + 0.85f, py + 0.8f);
                    }
                }
                paint.setColor(0xFFA1887F);
                rect(c, left, top, u, 0.3f, 6.5f, 0.6f, 7.8f);
                rect(c, left, top, u, 7.4f, 6.5f, 7.7f, 7.8f);
                break;
            }
            case GRANARY: {
                for (int i = 0; i < 3; i++) {
                    float cx = 1.6f + i * 2.4f;
                    paint.setColor(0xFFC79A5E);
                    rect(c, left, top, u, cx - 0.95f, 3.4f, cx + 0.95f, 7.4f);
                    paint.setColor(0xFFE3BC7D);
                    rect(c, left, top, u, cx - 0.95f, 3.1f, cx + 0.95f, 4.1f);
                    paint.setColor(0xFF8D6A3F);
                    rect(c, left, top, u, cx - 0.95f, 5.1f, cx + 0.95f, 5.5f);
                    paint.setColor(0xFF6D4C2F);
                    rect(c, left, top, u, cx - 0.3f, 5.8f, cx + 0.3f, 7.4f);
                }
                paint.setColor(0xFFD7C39A);
                rect(c, left, top, u, 1.2f, 7.3f, 6.8f, 7.8f);
                break;
            }
            case WINDMILL: {
                paint.setColor(0xFFE0E0E0);
                rect(c, left, top, u, 2.4f, 3.4f, 5.6f, 8f);
                paint.setColor(0xFFBDBDBD);
                rect(c, left, top, u, 2.4f, 3.4f, 3.1f, 8f);
                paint.setColor(0xFF5D4037);
                rect(c, left, top, u, 2f, 2.6f, 6f, 3.7f);
                paint.setColor(0x804E342E);
                rect(c, left, top, u, 3.4f, 5.4f, 4.6f, 7.2f);
                float cx = left + 4f * u;
                float cy = top + 3.1f * u;
                c.save();
                c.translate(cx, cy);
                c.rotate((now % 9360L) / 26f);
                paint.setColor(0xFFEFEFEF);
                c.drawRect(0.25f * u, -0.3f * u, 3.4f * u, 0.3f * u, paint);
                c.drawRect(-3.4f * u, -0.3f * u, -0.25f * u, 0.3f * u, paint);
                c.drawRect(-0.3f * u, 0.25f * u, 0.3f * u, 3.4f * u, paint);
                c.drawRect(-0.3f * u, -3.4f * u, 0.3f * u, -0.25f * u, paint);
                paint.setColor(0xFF9E9E9E);
                c.drawRect(0.25f * u, -0.12f * u, 2.7f * u, 0.12f * u, paint);
                c.drawRect(-2.7f * u, -0.12f * u, -0.25f * u, 0.12f * u, paint);
                c.drawRect(-0.12f * u, 0.25f * u, 0.12f * u, 2.7f * u, paint);
                c.drawRect(-0.12f * u, -2.7f * u, 0.12f * u, -0.25f * u, paint);
                c.restore();
                paint.setColor(0xFFFFD54F);
                rect(c, left, top, u, 3.65f, 2.75f, 4.35f, 3.45f);
                break;
            }
            case WELL: {
                paint.setColor(0xFF6D4C2F);
                rect(c, left, top, u, 1.1f, 2.7f, 1.8f, 6.4f);
                rect(c, left, top, u, 6.2f, 2.7f, 6.9f, 6.4f);
                paint.setColor(0xFF8E3B2B);
                rect(c, left, top, u, 0.5f, 1.8f, 7.5f, 2.8f);
                paint.setColor(0xFFB5523A);
                rect(c, left, top, u, 1.4f, 1.3f, 6.6f, 1.9f);
                paint.setColor(0xFF9E9E9E);
                rect(c, left, top, u, 1.5f, 5.5f, 6.5f, 7.6f);
                paint.setColor(0xFFBDBDBD);
                rect(c, left, top, u, 1.5f, 5.5f, 6.5f, 6f);
                paint.setColor(0xFF102A38);
                rect(c, left, top, u, 2.3f, 5.8f, 5.7f, 6.9f);
                paint.setColor(0xFF8D6E63);
                rect(c, left, top, u, 3.4f, 6.3f, 4.6f, 7.2f);
                paint.setColor(0xFF4FC3F7);
                rect(c, left, top, u, 3.6f, 6.5f, 4.4f, 6.8f);
                break;
            }
            case MARKET: {
                paint.setColor(0xFF6D4C2F);
                rect(c, left, top, u, 1f, 4.4f, 1.6f, 7.6f);
                rect(c, left, top, u, 6.4f, 4.4f, 7f, 7.6f);
                paint.setColor(0xFFC8894C);
                rect(c, left, top, u, 1f, 5.2f, 7f, 6.1f);
                boolean redStripe = ((now / 900L) & 1) == 0;
                for (int i = 0; i < 6; i++) {
                    float px = 0.6f + i * 1.2f;
                    paint.setColor((i & 1) == 0
                            ? (redStripe ? 0xFFE53935 : 0xFFFFB74D)
                            : 0xFFFFF3E0);
                    rect(c, left, top, u, px, 2.1f, px + 1.2f, 3.6f);
                }
                paint.setColor(0xFFF06292);
                rect(c, left, top, u, 1.8f, 4.4f, 2.5f, 5.1f);
                paint.setColor(0xFFFFD54F);
                rect(c, left, top, u, 3.3f, 4.5f, 4f, 5.2f);
                paint.setColor(0xFF66BB6A);
                rect(c, left, top, u, 5f, 4.4f, 5.7f, 5.1f);
                break;
            }
            case ROAD: {
                boolean horizontal = isRoad(map, tx - 1, ty) || isRoad(map, tx + 1, ty)
                        || !isRoad(map, tx, ty - 1) && !isRoad(map, tx, ty + 1);
                boolean vertical = isRoad(map, tx, ty - 1) || isRoad(map, tx, ty + 1);
                paint.setColor(0xFF7C7C7C);
                if (horizontal) {
                    rect(c, left, top, u, 0, 2.6f, 8, 5.4f);
                }
                if (vertical) {
                    rect(c, left, top, u, 2.6f, 0, 5.4f, 8);
                }
                int h = hash(tx, ty);
                paint.setColor(0xFFA8A8A8);
                for (int i = 0; i < 4; i++) {
                    float px = 0.6f + ((h >>> (i * 3)) & 6);
                    float py = 3f + ((h >>> (i * 3 + 2)) & 2);
                    rect(c, left, top, u, px, py, px + 0.8f, py + 0.5f);
                }
                break;
            }
            case GARDEN: {
                paint.setColor(0xFF2E7D32);
                rect(c, left, top, u, 0.4f, 1.2f, 7.6f, 1.9f);
                rect(c, left, top, u, 0.4f, 6.2f, 7.6f, 6.9f);
                rect(c, left, top, u, 0.4f, 1.2f, 1.1f, 6.9f);
                rect(c, left, top, u, 6.9f, 1.2f, 7.6f, 6.9f);
                paint.setColor(0xFF6D4C2F);
                for (int i = 0; i < 3; i++) {
                    float fy = 2.5f + i * 1.35f;
                    rect(c, left, top, u, 1.5f, fy, 6.5f, fy + 0.5f);
                }
                int[] flowers = {0xFFEF5350, 0xFFFFD54F, 0xFFCE93D8, 0xFF4FC3F7};
                for (int i = 0; i < 8; i++) {
                    paint.setColor(flowers[i & 3]);
                    float px = 1.6f + (i % 4) * 1.35f;
                    float py = 2f + (i / 4) * 1.35f;
                    rect(c, left, top, u, px, py, px + 0.55f, py + 0.55f);
                }
                paint.setColor(0xFF9E9E9E);
                rect(c, left, top, u, 3.3f, 3.4f, 4.7f, 4.8f);
                paint.setColor(0xFF4FC3F7);
                rect(c, left, top, u, 3.6f, 3.7f, 4.4f, 4.2f);
                break;
            }
            case WAREHOUSE: {
                paint.setColor(0xFFB0BEC5);
                rect(c, left, top, u, 0.8f, 3f, 7.2f, 7.4f);
                paint.setColor(0xFF78909C);
                rect(c, left, top, u, 0.8f, 3f, 1.6f, 7.4f);
                paint.setColor(0xFF546E7A);
                rect(c, left, top, u, 0.4f, 2f, 7.6f, 3.2f);
                paint.setColor(0xFF5D4037);
                rect(c, left, top, u, 2.3f, 4.2f, 5.7f, 7.4f);
                paint.setColor(0xFF8D6E63);
                rect(c, left, top, u, 2.6f, 4.5f, 3f, 7.4f);
                rect(c, left, top, u, 5f, 4.5f, 5.4f, 7.4f);
                rect(c, left, top, u, 2.6f, 5.4f, 5.4f, 5.8f);
                paint.setColor(0xFFA1887F);
                rect(c, left, top, u, 0.2f, 6f, 1.5f, 7.5f);
                rect(c, left, top, u, 6.5f, 6.2f, 7.8f, 7.6f);
                break;
            }
            case PALACE: {
                paint.setColor(0xFFE8DFC0);
                rect(c, left, top, u, 0.3f, 6.9f, 7.7f, 8f);
                paint.setColor(0xFFF0D97A);
                rect(c, left, top, u, 1.6f, 3f, 6.4f, 7.1f);
                paint.setColor(0xFFD9B85B);
                rect(c, left, top, u, 1.6f, 3f, 2.3f, 7.1f);
                paint.setColor(0xFFD7CCC8);
                rect(c, left, top, u, 0.4f, 2f, 2f, 7f);
                rect(c, left, top, u, 6f, 2f, 7.6f, 7f);
                paint.setColor(0xFFFFD54F);
                rect(c, left, top, u, 0.3f, 1.5f, 2.1f, 2.3f);
                rect(c, left, top, u, 5.9f, 1.5f, 7.7f, 2.3f);
                paint.setColor(0xFFD9A441);
                rect(c, left, top, u, 1.9f, 2.1f, 6.1f, 3.2f);
                paint.setColor(0xFFFFE082);
                rect(c, left, top, u, 2.7f, 1.3f, 5.3f, 2.4f);
                rect(c, left, top, u, 3.3f, 0.6f, 4.7f, 1.6f);
                paint.setColor(0xFFB71C1C);
                rect(c, left, top, u, 0.9f, 2.4f, 1.2f, 3.6f);
                rect(c, left, top, u, 6.8f, 2.4f, 7.1f, 3.6f);
                rect(c, left, top, u, 1.2f, 2.5f, 1.7f, 3.1f);
                rect(c, left, top, u, 6.3f, 2.5f, 6.8f, 3.1f);
                paint.setColor(0xFF4E342E);
                rect(c, left, top, u, 3.3f, 5.1f, 4.7f, 7.1f);
                paint.setColor(withAlpha(0xFFFFFFFF, 0x99));
                rect(c, left, top, u, 3.8f, 1f, 4.1f, 1.3f);
                break;
            }
            case NONE:
            default:
                break;
        }
    }

    private boolean isRoad(IslandMap map, int x, int y) {
        Tile tile = map.get(x, y);
        return tile != null && tile.getStructure() == Structure.ROAD;
    }

    private void drawGrid(Canvas c, int x0, int y0, int x1, int y1, float u) {
        paint.setColor(0x2AFFFFFF);
        for (int x = x0; x <= x1 + 1; x++) {
            float sx = tileScreenX(x) - 0.5f;
            c.drawRect(sx, tileScreenY(y0), sx + 0.65f, tileScreenY(y1 + 1), paint);
        }
        for (int y = y0; y <= y1 + 1; y++) {
            float sy = tileScreenY(y) - 0.5f;
            c.drawRect(tileScreenX(x0), sy, tileScreenX(x1 + 1), sy + 0.65f, paint);
        }
    }

    // ------------------------------------------------------------------
    // Weather and atmosphere
    // ------------------------------------------------------------------

    private void drawClouds(Canvas c, long now, Weather weather) {
        int count = weather == Weather.STORM ? 7 : 5;
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        float span = w + 220f;
        float speed = weather == Weather.STORM ? 0.075f : 0.028f;
        float drift = (now % 100000L) * speed;
        int alpha = weather == Weather.STORM ? 0x3A : weather == Weather.ASH ? 0x2C : 0x22;
        paint.setColor(withAlpha(weather == Weather.ASH ? 0xFF6C6A68 : 0xFFFFFFFF, alpha));
        for (int i = 0; i < count; i++) {
            int seed = hash(i, 71);
            float cx = wrap(drift + i * span / count + (seed & 63), span) - 110f;
            float cy = h * (0.08f + (i % 3) * 0.16f) + ((seed >>> 8) & 31);
            float s = 8f + ((seed >>> 12) & 15) * 1.6f;
            c.drawRect(cx, cy + s * 0.35f, cx + s * 5f, cy + s * 1.25f, paint);
            c.drawRect(cx + s * 0.8f, cy, cx + s * 3.4f, cy + s * 1.1f, paint);
            c.drawRect(cx + s * 2.5f, cy + s * 0.15f, cx + s * 4.3f, cy + s, paint);
        }
    }

    private void drawWeatherParticles(Canvas c, long now, Weather weather) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || weather == Weather.CLEAR || weather == Weather.DROUGHT) {
            return;
        }
        int count = weather == Weather.STORM ? 64 : 44;
        long tick = now % 100000L;
        for (int i = 0; i < count; i++) {
            int seed = hash(i, 133);
            float speed = weather == Weather.RAIN ? 0.42f : 0.10f;
            if (weather == Weather.STORM) {
                speed = 0.62f;
            }
            float fx = wrap((seed & 0xffff) / 24f + tick * speed * 0.42f, w + 20f) - 10f;
            float fy = wrap((seed >>> 9) / 128f + tick * speed, h + 20f) - 10f;
            if (weather == Weather.RAIN || weather == Weather.STORM) {
                paint.setColor(withAlpha(0xFFBFE4FF, weather == Weather.STORM ? 0x88 : 0x66));
                c.drawRect(fx, fy, fx + 1f, fy + 6f, paint);
                c.drawRect(fx - 1f, fy + 4f, fx, fy + 6f, paint);
            } else if (weather == Weather.SNOW) {
                paint.setColor(withAlpha(0xFFFFFFFF, 0xBB));
                c.drawRect(fx, fy, fx + 2f, fy + 2f, paint);
            } else if (weather == Weather.ASH) {
                paint.setColor((i & 3) == 0 ? withAlpha(0xFFFF8A50, 0x99)
                        : withAlpha(0xFFADADAD, 0x99));
                c.drawRect(fx, fy, fx + 2f, fy + 2f, paint);
            }
        }
    }

    private void drawAtmosphere(Canvas c, Weather weather, int season,
                                int day, double progress) {
        int w = getWidth();
        int h = getHeight();
        float tod = (float) (((day % 100) + progress) % 1.0);
        float sun = 0.5f + 0.5f * (float) Math.cos((tod - 0.25f) * 2f * Math.PI);
        int night = (int) ((1f - sun) * 0x46);
        if (night > 6) {
            paint.setColor(Color.argb(night, 12, 28, 62));
            c.drawRect(0, 0, w, h, paint);
        }
        float warmth = 1f - Math.abs(sun - 0.5f) * 2f;
        if (warmth > 0.25f) {
            paint.setColor(Color.argb((int) (warmth * 0x14), 255, 168, 96));
            c.drawRect(0, 0, w, h, paint);
        }
        if (weather == Weather.STORM) {
            paint.setColor(0x300B1424);
            c.drawRect(0, 0, w, h, paint);
        } else if (weather == Weather.ASH) {
            paint.setColor(0x1C3A2C24);
            c.drawRect(0, 0, w, h, paint);
        }
        if (season == 2) {
            paint.setColor(0x0EA8502A);
            c.drawRect(0, 0, w, h, paint);
        } else if (season == 3) {
            paint.setColor(0x0E7CB4E0);
            c.drawRect(0, 0, w, h, paint);
        }
    }

    // ------------------------------------------------------------------
    // People: shaded pixel figures with job, mood and animation detail
    // ------------------------------------------------------------------

    private void drawPerson(Canvas c, Agent agent, float cx, float cy, long now) {
        Citizen citizen = agent.getCitizen();
        int u = Math.max(1, Math.round(cell / 15f));
        boolean child = citizen.getAgeYears() < 18;
        if (child) {
            u = Math.max(1, u - 1);
        }
        int bob = 0;
        if (agent.hasTarget()) {
            bob = (int) Math.round(Math.sin(now / 115.0 + (citizen.getId() & 7)) * 0.9);
        }
        int px = Math.round(cx - 5f * u);
        int py = Math.round(cy - 7f * u) + bob;

        int skin = mix(opaque(citizen.getRace().getColor()),
                opaque(citizen.getSpecies().getColor()), 0.3f);
        skin = mix(skin, 0xFFD8A47C, 0.42f);
        int skinDark = shade(skin, 0.72f);
        int skinLight = mix(skin, 0xFFFFFFFF, 0.22f);
        Job job = citizen.getJob();
        int cloth = job == null ? 0xFF9E9E9E : opaque(job.getColor());
        int clothDark = shade(cloth, 0.68f);
        int age = citizen.getAgeYears();
        int hair = age > 55 ? 0xFFBDBDBD
                : citizen.getGender() == Gender.FEMALE ? 0xFF5D4037 : 0xFF33251C;

        // Ground shadow, slightly ahead of the feet.
        paint.setColor(0x44202A30);
        rect(c, px, py, u, 2f, 13.4f, 8f, 14.1f);

        // Selection halo behind the figure.
        if (citizen.getId() == selectedCitizenId) {
            int pulse = 0x22 + (int) ((Math.sin(now / 190.0) + 1f) * 0x12);
            paint.setColor(Color.argb(pulse, 255, 236, 150));
            rect(c, px, py, u, -0.7f, -1f, 10.7f, 14f);
        }

        // Boots and legs.
        paint.setColor(0xFF37474F);
        rect(c, px, py, u, 3f, 10.6f, 4.6f, 13f);
        rect(c, px, py, u, 6f, 10.6f, 7.6f, 13f);
        paint.setColor(0xFF263238);
        rect(c, px, py, u, 2.7f, 12.7f, 4.8f, 13.5f);
        rect(c, px, py, u, 5.8f, 12.7f, 7.9f, 13.5f);

        // Torso with shaded side and belt.
        paint.setColor(cloth);
        rect(c, px, py, u, 2f, 6.6f, 8f, 11f);
        paint.setColor(clothDark);
        rect(c, px, py, u, 2f, 6.6f, 2.8f, 11f);
        rect(c, px, py, u, 7.2f, 6.6f, 8f, 11f);
        paint.setColor(0xFF4E342E);
        rect(c, px, py, u, 2f, 9.5f, 8f, 10.2f);
        paint.setColor(0xFFFFD54F);
        rect(c, px, py, u, 4.7f, 9.5f, 5.3f, 10.2f);

        // Neck and head.
        paint.setColor(skinDark);
        rect(c, px, py, u, 4.6f, 5.9f, 5.4f, 6.8f);
        paint.setColor(skin);
        rect(c, px, py, u, 3f, 2f, 7f, 6f);
        paint.setColor(skinDark);
        rect(c, px, py, u, 3f, 2f, 3.5f, 5.5f);
        rect(c, px, py, u, 6.5f, 2f, 7f, 5.5f);
        paint.setColor(skinLight);
        rect(c, px, py, u, 4f, 2.6f, 6f, 4.4f);

        // Hair, eyes, mouth.
        paint.setColor(hair);
        rect(c, px, py, u, 3f, 1.2f, 7f, 2.3f);
        rect(c, px, py, u, 3f, 2f, 3.7f, 3.6f);
        if (citizen.getGender() == Gender.FEMALE) {
            rect(c, px, py, u, 2.5f, 2f, 3.2f, 6.2f);
            rect(c, px, py, u, 6.8f, 2f, 7.5f, 6.2f);
        } else if (age > 40) {
            rect(c, px, py, u, 4.2f, 5.2f, 5.8f, 5.8f);
        }
        paint.setColor(0xFF202124);
        rect(c, px, py, u, 4f, 3.4f, 4.6f, 4f);
        rect(c, px, py, u, 5.6f, 3.4f, 6.2f, 4f);
        paint.setColor(shade(skinDark, 0.82f));
        rect(c, px, py, u, 4.6f, 5f, 5.6f, 5.3f);

        // Arms.
        paint.setColor(skin);
        rect(c, px, py, u, 1f, 7f, 2f, 10.4f);
        rect(c, px, py, u, 8f, 7f, 9f, 10.4f);
        paint.setColor(skinDark);
        rect(c, px, py, u, 1f, 9.8f, 2f, 10.6f);
        rect(c, px, py, u, 8f, 9.8f, 9f, 10.6f);

        if (citizen.isPregnant()) {
            paint.setColor(mix(cloth, 0xFFE57373, 0.32f));
            rect(c, px, py, u, 3f, 8.2f, 7f, 11f);
            paint.setColor(withAlpha(0xFFFFFFFF, 0x44));
            rect(c, px, py, u, 3.4f, 8.6f, 4.2f, 9.6f);
        }

        drawJobTool(c, px, py, u, job);
        drawStatusSpecks(c, citizen, px, py, u);

        if (citizen.getId() == selectedCitizenId) {
            int a = 0x99 + (int) ((Math.sin(now / 170.0) + 1f) * 0x33);
            paint.setColor(Color.argb(clamp255(a), 255, 245, 190));
            float t = Math.max(0.55f, u * 0.26f);
            rect(c, px, py, u, -0.7f, -1.2f, 10.7f, -1.2f + t);
            rect(c, px, py, u, -0.7f, 14f - t, 10.7f, 14f);
            rect(c, px, py, u, -0.7f, -1.2f, -0.7f + t, 14f);
            rect(c, px, py, u, 10.7f - t, -1.2f, 10.7f, 14f);
            paint.setColor(withAlpha(0xFFFFFFFF, 0xCC));
            rect(c, px, py, u, -1.3f, -1.8f, -0.6f, -1.1f);
            rect(c, px, py, u, 10.6f, -1.8f, 11.3f, -1.1f);
        }
    }

    private void drawStatusSpecks(Canvas c, Citizen citizen, int px, int py, int u) {
        List<StatusEffect> effects = citizen.getActiveStatuses();
        for (int i = 0; i < effects.size() && i < 4; i++) {
            paint.setColor(statusColor(effects.get(i)));
            rect(c, px, py, u, 0.4f + i * 2f, -3.2f, 1.7f + i * 2f, -2f);
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

    private void drawJobTool(Canvas c, int px, int py, int u, Job job) {
        if (job == null) {
            return;
        }
        switch (job) {
            case RULER:
                paint.setColor(0xFFFFD54F);
                rect(c, px, py, u, 2.6f, 1.2f, 3.2f, 2.3f);
                rect(c, px, py, u, 3.2f, 1.4f, 4.4f, 1.8f);
                paint.setColor(0xFFFFF8E1);
                rect(c, px, py, u, 3.2f, 1.5f, 4.1f, 1.7f);
                break;
            case PRIEST:
                paint.setColor(0xFFEDE7F6);
                rect(c, px, py, u, 0.2f, 6.4f, 0.9f, 11f);
                paint.setColor(0xFFFFD54F);
                rect(c, px, py, u, 0f, 6.5f, 1.1f, 7f);
                break;
            case BUILDER:
            case WOODCUTTER:
                paint.setColor(0xFF8D6E63);
                rect(c, px, py, u, 0.1f, 6.2f, 0.8f, 10.8f);
                paint.setColor(0xFFB0BEC5);
                rect(c, px, py, u, -0.3f, 6.6f, 1.2f, 7.5f);
                break;
            case MINER:
                paint.setColor(0xFF8D6E63);
                rect(c, px, py, u, 0.1f, 5.6f, 0.8f, 10.6f);
                paint.setColor(0xFF9E9E9E);
                rect(c, px, py, u, -0.4f, 5.8f, 1.3f, 6.6f);
                break;
            case GATHERER:
                paint.setColor(0xFF8BC34A);
                rect(c, px, py, u, 8.4f, 8f, 9.6f, 9.4f);
                paint.setColor(0xFFE57373);
                rect(c, px, py, u, 8.8f, 7.6f, 9.3f, 8.2f);
                break;
            case HUNTER:
            case ATTACKER:
                paint.setColor(0xFF6D4C41);
                rect(c, px, py, u, 9f, 2.4f, 9.5f, 8f);
                paint.setColor(0xFFD7CCC8);
                rect(c, px, py, u, 8.7f, 2.8f, 9.8f, 3.2f);
                break;
            case FISHER:
                paint.setColor(0xFF90A4AE);
                rect(c, px, py, u, 0.2f, 2.2f, 0.7f, 9.6f);
                paint.setColor(0xFF4FC3F7);
                rect(c, px, py, u, 0f, 9.4f, 0.9f, 10.4f);
                break;
            case TRADER:
                paint.setColor(0xFFFFD54F);
                rect(c, px, py, u, 8.5f, 7.8f, 9.6f, 9f);
                paint.setColor(0xFF8D6E63);
                rect(c, px, py, u, 8.7f, 8f, 9.4f, 8.8f);
                break;
            case BLACKSMITH:
                paint.setColor(0xFF37474F);
                rect(c, px, py, u, 8.3f, 7.6f, 9.7f, 8.6f);
                paint.setColor(0xFFFF7043);
                rect(c, px, py, u, 8.4f, 6.7f, 9f, 7.4f);
                break;
            case SETTLER:
                paint.setColor(0xFF8BC34A);
                rect(c, px, py, u, 0.1f, 3f, 0.8f, 9.8f);
                paint.setColor(0xFFFFF59D);
                rect(c, px, py, u, -0.2f, 2.5f, 1.1f, 3.4f);
                break;
            case ROAD_BUILDER:
                paint.setColor(0xFFA1887F);
                rect(c, px, py, u, 0.1f, 6.4f, 0.8f, 10.4f);
                paint.setColor(0xFF9E9E9E);
                rect(c, px, py, u, -0.4f, 9.8f, 1.3f, 10.5f);
                break;
            case CLEANER:
                paint.setColor(0xFF4DD0E1);
                rect(c, px, py, u, 0.1f, 6.4f, 0.8f, 10.6f);
                rect(c, px, py, u, -0.3f, 6.6f, 1.2f, 7.4f);
                break;
            case FIREMAN:
                paint.setColor(0xFFFF7043);
                rect(c, px, py, u, 0.1f, 6.4f, 0.8f, 10.6f);
                paint.setColor(0xFFB0BEC5);
                rect(c, px, py, u, -0.2f, 6.4f, 1.1f, 7f);
                break;
            case LABORER:
            default:
                break;
        }
    }

    // ------------------------------------------------------------------
    // Animals: eight original pixel silhouettes
    // ------------------------------------------------------------------

    private void drawAnimal(Canvas c, Animal animal, float cx, float cy,
                            float u, long now) {
        float au = Math.max(1f, u * 0.8f);
        float ox = cx - 5f * au;
        float oy = cy - 5f * au;
        AnimalType type = animal.getType();
        int base = opaque(type.getColor());

        paint.setColor(0x40202018);
        c.drawRect(ox + 1f * au, oy + 8.4f * au, ox + 8.6f * au, oy + 9.1f * au, paint);

        switch (type) {
            case DEER: {
                int body = mix(base, 0xFF5D4037, 0.45f);
                paint.setColor(0xFF4E342E);
                rect(c, ox, oy, au, 2.2f, 6.4f, 3f, 9f);
                rect(c, ox, oy, au, 4.2f, 6.4f, 5f, 9f);
                rect(c, ox, oy, au, 6f, 6.4f, 6.8f, 9f);
                rect(c, ox, oy, au, 7.2f, 6.4f, 8f, 9f);
                paint.setColor(body);
                rect(c, ox, oy, au, 1.4f, 4f, 7.4f, 6.8f);
                paint.setColor(mix(body, 0xFFFFFFFF, 0.2f));
                rect(c, ox, oy, au, 2f, 4f, 6.6f, 4.9f);
                rect(c, ox, oy, au, 6.2f, 2.3f, 7.6f, 4.6f);
                paint.setColor(0xFF7B5B45);
                rect(c, ox, oy, au, 6.8f, 1.6f, 8.8f, 3.2f);
                paint.setColor(0xFFD7CCC8);
                rect(c, ox, oy, au, 1f, 4.2f, 1.8f, 5.4f);
                paint.setColor(0xFF6D4C41);
                rect(c, ox, oy, au, 7f, 0.2f, 7.4f, 1.8f);
                rect(c, ox, oy, au, 8.2f, 0.2f, 8.6f, 1.8f);
                rect(c, ox, oy, au, 7f, 0.6f, 8f, 1f);
                rect(c, ox, oy, au, 8f, 0.6f, 8.9f, 1f);
                paint.setColor(0xFF212121);
                rect(c, ox, oy, au, 8f, 2f, 8.4f, 2.4f);
                break;
            }
            case BOAR: {
                int body = mix(base, 0xFF4E342E, 0.35f);
                paint.setColor(0xFF3E2723);
                rect(c, ox, oy, au, 2.4f, 6.6f, 3.4f, 9f);
                rect(c, ox, oy, au, 6.2f, 6.6f, 7.2f, 9f);
                paint.setColor(body);
                rect(c, ox, oy, au, 1.4f, 3.8f, 7.4f, 7f);
                paint.setColor(mix(body, 0xFFFFFFFF, 0.14f));
                rect(c, ox, oy, au, 2f, 3.8f, 6.8f, 4.7f);
                paint.setColor(0xFF5D4037);
                rect(c, ox, oy, au, 6.8f, 4.2f, 9f, 6.4f);
                paint.setColor(0xFF8D6E63);
                rect(c, ox, oy, au, 8.4f, 4.8f, 9.3f, 6f);
                paint.setColor(0xFF3E2723);
                rect(c, ox, oy, au, 2f, 2.8f, 3f, 4f);
                rect(c, ox, oy, au, 5.8f, 2.8f, 6.8f, 4f);
                paint.setColor(0xFFFFF8E1);
                rect(c, ox, oy, au, 8f, 4.4f, 8.4f, 6f);
                rect(c, ox, oy, au, 7.6f, 5.6f, 8.8f, 6f);
                paint.setColor(0xFF212121);
                rect(c, ox, oy, au, 7.4f, 4.4f, 7.8f, 4.8f);
                break;
            }
            case FISH: {
                paint.setColor(mix(base, 0xFF0288D1, 0.3f));
                rect(c, ox, oy, au, 2f, 3.8f, 7.2f, 6.4f);
                rect(c, ox, oy, au, 3.2f, 3f, 6.4f, 3.8f);
                rect(c, ox, oy, au, 3.2f, 6.4f, 6.4f, 7f);
                paint.setColor(mix(base, 0xFFB3E5FC, 0.4f));
                rect(c, ox, oy, au, 2.6f, 4.2f, 6.6f, 4.9f);
                paint.setColor(0xFF0288D1);
                rect(c, ox, oy, au, 7.2f, 3f, 8.6f, 4f);
                rect(c, ox, oy, au, 7.2f, 6.2f, 8.6f, 7.2f);
                rect(c, ox, oy, au, 8.4f, 4f, 9.3f, 6.2f);
                paint.setColor(0xFFFFF8E1);
                rect(c, ox, oy, au, 2.6f, 4.2f, 3.2f, 4.8f);
                paint.setColor(0xFF212121);
                rect(c, ox, oy, au, 2.8f, 4.4f, 3.1f, 4.7f);
                paint.setColor(withAlpha(0xFFFFFFFF, 0x66));
                rect(c, ox, oy, au, 8.6f, 2.2f, 9f, 2.6f);
                rect(c, ox, oy, au, 9.2f, 1.4f, 9.5f, 1.8f);
                break;
            }
            case WOLF: {
                int body = mix(base, 0xFF546E7A, 0.3f);
                paint.setColor(0xFF455A64);
                rect(c, ox, oy, au, 2.2f, 6.6f, 3.2f, 9f);
                rect(c, ox, oy, au, 6.2f, 6.6f, 7.2f, 9f);
                paint.setColor(body);
                rect(c, ox, oy, au, 1.4f, 4f, 7.4f, 7f);
                rect(c, ox, oy, au, 0.4f, 3.4f, 2f, 5.4f);
                paint.setColor(mix(body, 0xFFFFFFFF, 0.22f));
                rect(c, ox, oy, au, 2f, 4f, 6.6f, 4.8f);
                rect(c, ox, oy, au, 6.2f, 2.4f, 8.6f, 4.8f);
                paint.setColor(0xFF78909C);
                rect(c, ox, oy, au, 7f, 1.4f, 7.8f, 2.8f);
                rect(c, ox, oy, au, 8.2f, 1.4f, 9f, 2.8f);
                paint.setColor(0xFF37474F);
                rect(c, ox, oy, au, 8.4f, 3.6f, 9.6f, 4.4f);
                paint.setColor(0xFFFF3B30);
                rect(c, ox, oy, au, 7.8f, 3f, 8.4f, 3.6f);
                break;
            }
            case WORM: {
                for (int i = 0; i < 4; i++) {
                    int seg = mix(base, i % 2 == 0 ? 0xFF8D5A2B : 0xFFF0C080, 0.35f);
                    paint.setColor(seg);
                    float sx = 0.8f + i * 2f;
                    rect(c, ox, oy, au, sx, 4.4f + (i & 1) * 0.5f,
                            sx + 2.3f, 7.1f + (i & 1) * 0.5f);
                    paint.setColor(withAlpha(0xFF5D4037, 0x88));
                    rect(c, ox, oy, au, sx + 1f, 4.8f + (i & 1) * 0.5f,
                            sx + 1.25f, 6.8f + (i & 1) * 0.5f);
                }
                paint.setColor(mix(base, 0xFFFFFFFF, 0.2f));
                rect(c, ox, oy, au, 7.4f, 4f, 9.4f, 6f);
                paint.setColor(0xFFFF3B30);
                rect(c, ox, oy, au, 8.4f, 4.5f, 8.9f, 5f);
                break;
            }
            case GOLEM: {
                paint.setColor(0xFF7E57C2);
                rect(c, ox, oy, au, 2.4f, 3.2f, 7.2f, 8.4f);
                paint.setColor(0xFFB39DDB);
                rect(c, ox, oy, au, 2.8f, 3.2f, 5.2f, 6.2f);
                paint.setColor(0xFF9575CD);
                rect(c, ox, oy, au, 5.2f, 3.2f, 7.2f, 8.4f);
                paint.setColor(0xFFD1C4E9);
                rect(c, ox, oy, au, 3.4f, 3.8f, 4.6f, 5.4f);
                paint.setColor(0xFF7E57C2);
                rect(c, ox, oy, au, 1.2f, 4f, 2.4f, 7.4f);
                rect(c, ox, oy, au, 7.2f, 4f, 8.4f, 7.4f);
                rect(c, ox, oy, au, 3.4f, 1.6f, 6.2f, 3.4f);
                paint.setColor(0xFFE1BEE7);
                rect(c, ox, oy, au, 3.8f, 2f, 5f, 3f);
                float glow = 0xCC + (int) ((Math.sin(now / 220.0) + 1f) * 0x18);
                paint.setColor(Color.argb(clamp255((int) glow), 140, 240, 255));
                rect(c, ox, oy, au, 4.4f, 5.2f, 5.4f, 6.4f);
                paint.setColor(0xFFFF3B30);
                rect(c, ox, oy, au, 4.4f, 2.3f, 4.8f, 2.7f);
                rect(c, ox, oy, au, 5.4f, 2.3f, 5.8f, 2.7f);
                break;
            }
            case ZOMBIE: {
                int green = mix(base, 0xFF4CAF50, 0.35f);
                paint.setColor(0xFF37474F);
                rect(c, ox, oy, au, 3f, 7.4f, 4.2f, 9.4f);
                rect(c, ox, oy, au, 5.8f, 7.4f, 7f, 9.4f);
                paint.setColor(mix(green, 0xFF33691E, 0.4f));
                rect(c, ox, oy, au, 3f, 4.2f, 7f, 7.8f);
                paint.setColor(green);
                rect(c, ox, oy, au, 3.2f, 1.8f, 6.8f, 4.4f);
                paint.setColor(mix(green, 0xFFFFFFFF, 0.18f));
                rect(c, ox, oy, au, 3.6f, 2.2f, 4.6f, 3.2f);
                paint.setColor(0xFF33691E);
                rect(c, ox, oy, au, 3.2f, 3.9f, 6.8f, 4.4f);
                paint.setColor(green);
                rect(c, ox, oy, au, 1.4f, 4.4f, 3f, 5.4f);
                rect(c, ox, oy, au, 7f, 4.4f, 8.6f, 5.4f);
                paint.setColor(0xFFFF3B30);
                rect(c, ox, oy, au, 4f, 2.8f, 4.5f, 3.3f);
                rect(c, ox, oy, au, 5.6f, 2.8f, 6.1f, 3.3f);
                break;
            }
            case DRAGON: {
                int body = mix(base, 0xFFB71C1C, 0.3f);
                paint.setColor(0xFF8E1B18);
                rect(c, ox, oy, au, 3f, 5.2f, 7.2f, 8f);
                paint.setColor(body);
                rect(c, ox, oy, au, 3.4f, 4.6f, 6.8f, 6.6f);
                rect(c, ox, oy, au, 6.2f, 2.6f, 8.4f, 4.8f);
                paint.setColor(0xFFEF5350);
                rect(c, ox, oy, au, 1f, 2.8f, 3.8f, 5.4f);
                rect(c, ox, oy, au, 6.4f, 2.6f, 9.2f, 4.8f);
                paint.setColor(0xFFB71C1C);
                rect(c, ox, oy, au, 1.4f, 3.2f, 3.4f, 4.4f);
                rect(c, ox, oy, au, 6.8f, 3f, 8.8f, 4.2f);
                paint.setColor(0xFFFFF3E0);
                rect(c, ox, oy, au, 6.8f, 1.4f, 7.2f, 2.6f);
                rect(c, ox, oy, au, 8f, 1.4f, 8.4f, 2.6f);
                paint.setColor(0xFF7F0000);
                rect(c, ox, oy, au, 1f, 5.6f, 2.8f, 6.8f);
                rect(c, ox, oy, au, 2.4f, 6.4f, 3.6f, 7.4f);
                paint.setColor(0xFFFF3B30);
                rect(c, ox, oy, au, 7.6f, 3.2f, 8f, 3.6f);
                break;
            }
            default:
                paint.setColor(base);
                rect(c, ox, oy, au, 2, 3, 7, 7);
                break;
        }
    }

    // ------------------------------------------------------------------
    // Transient powers and blessings
    // ------------------------------------------------------------------

    private static final class Fx {
        Effect kind;
        int x;
        int y;
        long start;
        int duration;
        int seed;
    }

    private void drawEffects(Canvas c, long now, float u) {
        for (int i = effects.size() - 1; i >= 0; i--) {
            Fx fx = effects.get(i);
            float t = (now - fx.start) / (float) fx.duration;
            if (t < 0f || t >= 1f) {
                effects.remove(i);
                continue;
            }
            float sx = tileScreenX(fx.x);
            float sy = tileScreenY(fx.y);
            if (sx < -6f * cell || sx > getWidth() + 6f * cell
                    || sy < -6f * cell || sy > getHeight() + 6f * cell) {
                continue;
            }
            drawFx(c, fx, t, now, sx - 4f * u, sy - 4f * u, u);
        }
    }

    private void drawFx(Canvas c, Fx fx, float t, long now,
                        float ox, float oy, float u) {
        switch (fx.kind) {
            case RAIN: {
                for (int i = 0; i < 12; i++) {
                    float px = 1f + noise01(fx.seed, i, 3) * 6f;
                    float py = -3f + wrap(i * 0.83f + t * 7f, 7f);
                    paint.setColor(withAlpha(0xFFBFE9FF, 0xBB));
                    rect(c, ox, oy, u, px, py, px + 0.3f, py + 1.3f);
                }
                float r = 1f + t * 3f;
                paint.setColor(withAlpha(0xFF8FD3F4, (int) (0x99 * (1f - t))));
                c.drawRect(ox + (4f - r) * u, oy + (4f - r * 0.4f) * u,
                        ox + (4f + r) * u, oy + (4f - r * 0.4f) * u + Math.max(1f, u * 0.25f), paint);
                paint.setColor(withAlpha(0xFFFFFFFF, (int) (0x66 * (1f - t))));
                rect(c, ox, oy, u, 3.4f, 3.4f, 4.6f, 4.6f);
                break;
            }
            case FIRE: {
                float flicker = 0.8f + 0.2f * (float) Math.sin(now / 55f + fx.seed);
                if (t < 0.16f) {
                    paint.setColor(Color.argb((int) ((0.16f - t) / 0.16f * 0x44),
                            255, 190, 90));
                    rect(c, ox, oy, u, 0.5f, 0.5f, 7.5f, 7.5f);
                }
                paint.setColor(Color.argb((int) (0x55 * (1f - t) * flicker), 255, 112, 40));
                rect(c, ox, oy, u, 1.2f, 1.2f, 6.8f, 6.8f);
                for (int i = 0; i < 14; i++) {
                    float rise = wrap(i * 0.7f + t * 2.6f, 6f);
                    float px = 1.5f + noise01(fx.seed, i, 7) * 5f
                            + (float) Math.sin(now / 90f + i) * 0.3f;
                    float py = 6.4f - rise;
                    paint.setColor(i % 3 == 0 ? 0xFFFFF59D : i % 3 == 1 ? 0xFFFF8A50 : 0xFFE53935);
                    rect(c, ox, oy, u, px, py, px + 0.5f, py + 0.7f);
                }
                paint.setColor(0xFFFFD54F);
                rect(c, ox, oy, u, 3.4f, 5.2f, 4.6f, 6.6f);
                break;
            }
            case LIGHTNING: {
                float flash = t < 0.22f ? 1f - t / 0.22f : 0f;
                if (flash > 0f) {
                    paint.setColor(Color.argb((int) (flash * 0x88), 235, 245, 255));
                    c.drawRect(0, 0, getWidth(), getHeight(), paint);
                }
                float jag = (fx.seed & 7) - 3.5f;
                paint.setColor(withAlpha(0xFFFFFFFF, (int) (0xEE * (1f - t))));
                for (int i = 0; i < 7; i++) {
                    float y0 = -4f + i * 1.25f;
                    float x0 = 4f + (float) Math.sin(i * 2.1f + fx.seed) * jag * 0.5f;
                    float x1 = 4f + (float) Math.sin((i + 1) * 2.1f + fx.seed) * jag * 0.5f;
                    rect(c, ox, oy, u, Math.min(x0, x1), y0, Math.max(x0, x1) + 0.35f, y0 + 1.4f);
                }
                paint.setColor(withAlpha(0xFFB3E5FC, (int) (0xAA * (1f - t))));
                rect(c, ox, oy, u, 3.2f, 3.2f, 4.8f, 4.8f);
                break;
            }
            case METEOR: {
                if (t < 0.55f) {
                    float p = t / 0.55f;
                    float mx = -4f + p * 8f;
                    float my = -5f + p * 9f;
                    paint.setColor(withAlpha(0xFFFFAB40, 0xAA));
                    rect(c, ox, oy, u, mx - 2.6f, my - 0.5f, mx - 0.4f, my + 0.2f);
                    paint.setColor(0xFFFFE082);
                    rect(c, ox, oy, u, mx - 1.2f, my - 0.8f, mx + 0.5f, my + 0.8f);
                    paint.setColor(0xFFFFFFFF);
                    rect(c, ox, oy, u, mx - 0.5f, my - 0.5f, mx + 0.5f, my + 0.5f);
                } else {
                    float p = (t - 0.55f) / 0.45f;
                    float r = 0.6f + p * 4f;
                    paint.setColor(withAlpha(0xFF5D4037, (int) (0x99 * (1f - p))));
                    rect(c, ox, oy, u, 2.8f, 3.6f, 5.2f, 4.6f);
                    paint.setColor(withAlpha(0xFFFF8A50, (int) (0xCC * (1f - p))));
                    c.drawRect(ox + (4f - r) * u, oy + (4f - r * 0.45f) * u,
                            ox + (4f + r) * u, oy + (4f - r * 0.45f) * u + Math.max(1f, u * 0.35f), paint);
                    c.drawRect(ox + (4f - r * 0.35f) * u, oy + (4f - r) * u,
                            ox + (4f - r * 0.35f) * u + Math.max(1f, u * 0.35f),
                            oy + (4f + r) * u, paint);
                    for (int i = 0; i < 8; i++) {
                        float ang = i * 0.785f;
                        float d = r * 0.8f;
                        float px = 4f + (float) Math.cos(ang) * d;
                        float py = 4f + (float) Math.sin(ang) * d * 0.5f;
                        paint.setColor(withAlpha(0xFFFFD54F, (int) (0xBB * (1f - p))));
                        rect(c, ox, oy, u, px, py, px + 0.5f, py + 0.5f);
                    }
                }
                break;
            }
            case WAR: {
                for (int i = 0; i < 3; i++) {
                    float off = i * 1.6f;
                    paint.setColor(withAlpha(i == 1 ? 0xFFFF8A80 : 0xFFE53935,
                            (int) (0xDD * (1f - t))));
                    rect(c, ox, oy, u, 1f + off, 1.5f + t * 1.2f,
                            1.6f + off, 6.5f - t * 1.2f);
                    rect(c, ox, oy, u, 1.6f + off, 3f + t, 6.5f - off, 3.5f + t);
                }
                for (int i = 0; i < 8; i++) {
                    float px = 2f + noise01(fx.seed, i, 11) * 4f;
                    float py = 2f + wrap(i * 1.3f + t * 5f, 4f);
                    paint.setColor(withAlpha(0xFFFFD54F, (int) (0xAA * (1f - t))));
                    rect(c, ox, oy, u, px, py, px + 0.45f, py + 0.45f);
                }
                break;
            }
            case BLESS: {
                paint.setColor(Color.argb((int) (0x44 * (1f - t)), 255, 224, 130));
                rect(c, ox, oy, u, 0.5f, 0.5f, 7.5f, 7.5f);
                float r = 1f + t * 3.4f;
                paint.setColor(withAlpha(0xFFFFE082, (int) (0xAA * (1f - t))));
                c.drawRect(ox + (4f - r) * u, oy + (4f - r * 0.4f) * u,
                        ox + (4f + r) * u, oy + (4f - r * 0.4f) * u + Math.max(1f, u * 0.3f), paint);
                for (int i = 0; i < 12; i++) {
                    float px = 1f + noise01(fx.seed, i, 13) * 6f;
                    float py = 7f - wrap(i * 0.9f + t * 6f, 7f);
                    paint.setColor(i % 3 == 0 ? 0xFFFFF8E1 : 0xFFFFD54F);
                    rect(c, ox, oy, u, px, py, px + 0.5f, py + 0.7f);
                }
                break;
            }
            case HEAL: {
                paint.setColor(Color.argb((int) (0x55 * (1f - t)), 129, 240, 170));
                rect(c, ox, oy, u, 1f, 1f, 7f, 7f);
                paint.setColor(withAlpha(0xFF69F0AE, (int) (0xDD * (1f - t))));
                rect(c, ox, oy, u, 3.4f, 2f, 4.6f, 6f);
                rect(c, ox, oy, u, 2f, 3.4f, 6f, 4.6f);
                for (int i = 0; i < 10; i++) {
                    float px = 1.5f + noise01(fx.seed, i, 17) * 5f;
                    float py = 7f - wrap(i * 1.1f + t * 5f, 7f);
                    paint.setColor(0xFFB9F6CA);
                    rect(c, ox, oy, u, px, py, px + 0.45f, py + 0.6f);
                }
                break;
            }
            case BLOOM: {
                paint.setColor(withAlpha(0xFF66BB6A, (int) (0x88 * (1f - t))));
                for (int i = 0; i < 8; i++) {
                    float px = 1f + noise01(fx.seed, i, 19) * 6f;
                    float py = 7f - wrap(i * 0.8f + t * 4f, 7f);
                    rect(c, ox, oy, u, px, py, px + 0.55f, py + 0.55f);
                }
                int[] petals = {0xFFEF5350, 0xFFFFD54F, 0xFFCE93D8, 0xFFFFFFFF};
                for (int i = 0; i < 4; i++) {
                    float px = 2f + i * 1.4f;
                    float py = 4.5f + (i & 1) * 1.2f;
                    float open = Math.min(1f, t * 2.2f);
                    paint.setColor(petals[i]);
                    rect(c, ox, oy, u, px - open, py - 0.4f, px + open, py + 0.4f);
                    rect(c, ox, oy, u, px - 0.4f, py - open, px + 0.4f, py + open);
                }
                break;
            }
            case PEACE:
            default: {
                for (int ring = 0; ring < 2; ring++) {
                    float p = Math.min(1f, t * (ring == 0 ? 1.4f : 1f));
                    float r = 0.8f + p * 3.6f;
                    paint.setColor(withAlpha(0xFF80DEEA, (int) (0xBB * (1f - p))));
                    c.drawRect(ox + (4f - r) * u, oy + (4f - r * 0.45f) * u,
                            ox + (4f + r) * u, oy + (4f - r * 0.45f) * u + Math.max(1f, u * 0.28f), paint);
                    c.drawRect(ox + (4f - r * 0.3f) * u, oy + (4f - r) * u,
                            ox + (4f - r * 0.3f) * u + Math.max(1f, u * 0.28f),
                            oy + (4f + r) * u, paint);
                    c.drawRect(ox + (4f + r * 0.3f) * u, oy + (4f - r) * u,
                            ox + (4f + r * 0.3f) * u + Math.max(1f, u * 0.28f),
                            oy + (4f + r) * u, paint);
                }
                paint.setColor(withAlpha(0xFFE1F5FE, (int) (0x88 * (1f - t))));
                rect(c, ox, oy, u, 3.4f, 3.4f, 4.6f, 4.6f);
                break;
            }
        }
    }

    // ------------------------------------------------------------------
    // Small maths / pixel helpers. All deterministic, all API 21 safe.
    // ------------------------------------------------------------------

    /** Tile-local 8x8 unit rectangle: crisp, no anti-aliasing, no seams. */
    private void rect(Canvas c, float left, float top, float u,
                      float x0, float y0, float x1, float y1) {
        c.drawRect(left + x0 * u, top + y0 * u, left + x1 * u, top + y1 * u, paint);
    }

    private static int positiveMod(int value, int modulus) {
        int result = value % modulus;
        return result < 0 ? result + modulus : result;
    }

    private static float wrap(float value, float modulus) {
        float result = value % modulus;
        return result < 0f ? result + modulus : result;
    }

    private static float clamp(float value, float low, float high) {
        return value < low ? low : value > high ? high : value;
    }

    private static int clamp255(int value) {
        return value < 0 ? 0 : value > 255 ? 255 : value;
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(clamp255(alpha), Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int opaque(int color) {
        return Color.argb(0xFF, Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int shade(int color, float factor) {
        int r = Math.min(255, Math.round(Color.red(color) * factor));
        int g = Math.min(255, Math.round(Color.green(color) * factor));
        int b = Math.min(255, Math.round(Color.blue(color) * factor));
        return Color.argb(Color.alpha(color), r, g, b);
    }

    private static int mix(int a, int b, float weightOfB) {
        float w = clamp(weightOfB, 0f, 1f);
        int r = Math.round(Color.red(a) * (1f - w) + Color.red(b) * w);
        int g = Math.round(Color.green(a) * (1f - w) + Color.green(b) * w);
        int bl = Math.round(Color.blue(a) * (1f - w) + Color.blue(b) * w);
        return Color.argb(0xFF, r, g, bl);
    }

    private static int colorFromSeed(int seed) {
        int h = positiveMod(seed, 100000);
        int r = 70 + (h * 37) % 160;
        int g = 70 + (h / 7 * 53) % 160;
        int b = 70 + (h / 13 * 71) % 160;
        return Color.rgb(r, g, b);
    }

    private static int hash(int x, int y) {
        int value = x * 374761393 + y * 668265263;
        value = (value ^ (value >>> 13)) * 1274126177;
        return value ^ (value >>> 16);
    }

    private static int hash3(int x, int y, int salt) {
        return hash(x + salt * 31, y - salt * 17);
    }

    private static float noise01(int x, int y, int salt) {
        return (hash3(x, y, salt) >>> 8) / 16777216f;
    }
}





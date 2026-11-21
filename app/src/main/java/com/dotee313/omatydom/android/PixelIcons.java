package com.dotee313.omatydom.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.dotee313.omatydom.core.Decree;
import com.dotee313.omatydom.core.Power;
import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Gender;
import com.dotee313.omatydom.core.model.Job;

/** Code-only pixel art: top controls, tabs, overlays and small system badges. */
final class PixelIcons {

    enum Control {
        PAUSE, SPEED, CONTROL, ASSASSINATE, NEXT,
        ZOOM_IN, ZOOM_OUT, CENTER, GRID
    }

    /** Groups shown in the new bottom dock; kept separate from legacy nav art. */
    enum DockGroup {
        PEOPLE, BEASTS, LAWS, SETTINGS
    }

    /** Small badges for the sections of the richer citizen card. */
    enum CardSection {
        KINGDOM, FAITH, LANGUAGE, PARTNER, CLAN, SKILLS, TRAITS, SPECIALTY
    }

    private static final String[] PAUSE = {
            ".........",
            ".KK...KK.",
            ".KK...KK.",
            ".KK...KK.",
            ".KK...KK.",
            ".KK...KK.",
            ".KK...KK.",
            ".KK...KK.",
            "........."
    };

    private static final String[] SPEED = {
            ".........",
            "K....K...",
            "KK...KK..",
            "KKK..KKK.",
            "KK...KK..",
            "K....K...",
            ".........",
            ".........",
            "........."
    };

    private static final String[] CROWN = {
            ".........",
            "Y...Y...Y",
            "YY..Y..YY",
            "YYYYYYYYY",
            "YWYYYYYWY",
            "YYYYYYYYY",
            ".YYYYYYY.",
            ".YYYYYYY.",
            "........."
    };

    private static final String[] DAGGER = {
            ".........",
            ".......K.",
            "......KW.",
            ".....KW..",
            "....KW...",
            "...KW....",
            "..KK.....",
            ".K.......",
            "........."
    };

    private static final String[] POWER_RAIN = {
            "...WWW...", "..WWWWW..", ".WWWWWWW.", "WWWWWWWWW", ".........",
            "..B.B.B..", ".B.B.B.B.", "..B.B.B..", "........."
    };
    private static final String[] POWER_BLOOM = {
            "....G....", "...GGG...", "..GGGGG..", ".GGYGGGG.", "GGGYYGGG.",
            ".GGYGGGG.", "..GGGGG..", "...GGG...", "....G...."
    };
    private static final String[] POWER_HEAL = {
            "....W....", "...WWW...", "....W....", ".WWWWWWW.", "..WWWWW..",
            ".WWWWWWW.", "....W....", "...WWW...", "....W...."
    };
    private static final String[] POWER_BLESS = {
            "....Y....", "..Y.Y.Y..", "...YYY...", ".YYYYYYY.", "..YYYYY..",
            "...YYY...", "..Y.Y.Y..", ".Y..Y..Y.", "........."
    };
    private static final String[] POWER_LIGHTNING = {
            "......Y..", ".....YY..", "....YY...", "...YYY...", "..YYY....",
            "...YY....", "..YY.....", ".YY......", "Y........"
    };
    private static final String[] POWER_PLAGUE = {
            "..KKKKK..", ".KKKKKKK.", "KKWKKKWKK", "KKWKKKWKK", "KKKKKKKKK",
            ".KWWWWWK.", "KKKKKKKKK", ".K.K.K.K.", "..K...K.."
    };
    private static final String[] POWER_EARTHQUAKE = {
            "K...K...K", ".K.K.K.K.", "..KKKKK..", "KKKKKKKKK", "..K.K.K..",
            ".K.K.K.K.", "K...K...K", "..K...K..", ".K.....K."
    };
    private static final String[] POWER_METEOR = {
            "........Y", ".......YY", "......YY.", "....YYYY.", "...YYYYY.",
            "..YYYYYY.", ".YYYYYYY.", "YYYYYYYY.", "........."
    };
    private static final String[] POWER_PEACE = {
            "..WW...WW", ".WWWW.WWW", "WWWWWWWWW", ".WWWWWWW.", "..WWWWW..",
            "...WWW...", "....W....", "...WWW...", "..W...W.."
    };
    private static final String[] POWER_PROSPERITY = {
            "..YYYYY..", ".YYYWYYY.", "YYYYYYYYY", "YYYWYYYYY", "YYYYYYYYY",
            ".YYYWYYY.", "..YYYYY..", "...YYY...", "....Y...."
    };

    private static final String[] NEXT = {
            ".........",
            "..B......",
            "..BB.....",
            "..BBB....",
            "..BBBB...",
            "..BBBBB..",
            "..BBBB...",
            "..BBB....",
            "........."
    };

    private static final String[] ZOOM_IN = {
            ".........",
            "..WWW....",
            ".W...W...",
            "W..B..W..",
            "W.BBB.W..",
            "W..B..W..",
            ".W...W...",
            "..WWK....",
            "....KK..."
    };
    private static final String[] ZOOM_OUT = {
            ".........",
            "..WWW....",
            ".W...W...",
            "W..R..W..",
            "W.RRR.W..",
            "W..R..W..",
            ".W...W...",
            "..WWK....",
            "....KK..."
    };
    private static final String[] CENTER = {
            ".........",
            "...WWW...",
            "..W...W..",
            ".W..C..W.",
            "W..CCC..W",
            ".W..C..W.",
            "..W...W..",
            "...WWW...",
            "........."
    };
    private static final String[] GRID = {
            "NNNNNNNNN",
            "NWNWNWNWN",
            "NNNNNNNNN",
            "NWNWNWNWN",
            "NNNNNNNNN",
            "NWNWNWNWN",
            "NNNNNNNNN",
            "NWNWNWNWN",
            "NNNNNNNNN"
    };

    // Seven distinct bottom-tab badges.
    private static final String[] NAV_WORLD = {
            ".........", "..GGGGG..", ".GGLLLGG.", "GLLYYYLGG", "GLLYYYLGG",
            ".GLLLLLG.", "..BBBBB..", ".B.B.B.B.", "........."
    };
    private static final String[] NAV_PEOPLE = {
            ".........", "..KK.KK..", ".KKK.KKK.", "..KK.KK..", "...KKK...",
            "..WWWWW..", ".WWWWWWW.", "WWWWWWWWW", "........."
    };
    private static final String[] NAV_NATURE = {
            ".........", "......LL.", "....LLLL.", "...LLLL..", "..LLLLL..",
            ".LLLKK...", "LLK......", "K........", "........."
    };
    private static final String[] NAV_CREATURES = {
            "..R...R..", "..RR.RR..", "RRRRRRRRR", ".RRRRRRR.", "..RWKWR..",
            "..RRRRR..", "...R.R...", "..R...R..", "........."
    };
    private static final String[] NAV_KINGDOMS = {
            "Y...Y...Y", "YY..Y..YY", "YYYYYYYYY", "YWYYYYYWY", "YYYYYYYYY",
            ".NNNNNNN.", ".N.N.N.N.", ".NNNNNNN.", "........."
    };
    private static final String[] NAV_POWERS = {
            "....Y....", "...YYY...", "..YYYYY..", ".YYYYYYY.", "YYYPYYYYY",
            ".YYYYYYY.", "..YYYYY..", "...YYY...", "....Y...."
    };
    private static final String[] NAV_EVENTS = {
            ".........", ".WWWWWWW.", ".WKKKKKW.", ".WWWWWWW.", ".WKKKKKW.",
            ".WWWWWWW.", ".WKKKKKW.", ".WWWWWWW.", "........."
    };

    // Five map-overlay badges.
    private static final String[] OVER_TERRAIN = {
            ".........", "G..G..G..", "GG.GG.GG.", "GGGGGGGGG", ".GGGGGGG.",
            "..GGGGG..", "...GGG...", "....G....", "........."
    };
    private static final String[] OVER_BIOME = {
            ".........", "..LL.LL..", ".LLLLLLL.", "LLLLLLLLL", "LLLLLLLLL",
            ".LLLLLLL.", "..L...L..", ".L.....L.", "........."
    };
    private static final String[] OVER_KINGDOM = {
            ".Y.......", ".YY......", ".YYY.....", ".YYYY....", ".YYYYY...",
            ".NYYYYY..", ".NNYYYY..", ".NNNYY...", ".NNNN...."
    };
    private static final String[] OVER_RELIGION = {
            "....Y....", "..Y.Y.Y..", "...YYY...", ".YYYYYYY.", "..YYYYY..",
            "...YYY...", "..Y.Y.Y..", ".Y..Y..Y.", "........."
    };
    private static final String[] OVER_JOB = {
            ".........", ".....NN..", "....NNN..", "...NNN...", "..NNN....",
            ".KKK.....", "KK.......", "K........", "........."
    };

    private static final String[] LAW_ON = {
            ".........", "...GGG...", "..GGGGG..", ".GGWWWGG.", "GGGGGGGGG",
            "..W...W..", ".WW...WW.", "WW.....WW", "........."
    };
    private static final String[] LAW_OFF = {
            ".........", "...KKK...", "..KKKKK..", ".KKRRRKK.", "KKKKKKKKK",
            "..R...R..", ".RR...RR.", "RR.....RR", "........."
    };

    private static final String[] CLAN = {
            ".........", "..Y...Y..", "..NN.NN..", "NNNNNNNNN", ".NNNNNNN.",
            "..NNNNN..", "..N.N.N..", ".NN...NN.", "........."
    };

    // --- New grouped-dock art -------------------------------------------------
    // Three citizens in burgundy / orange / brown cloaks, not the old two-head badge.
    private static final String[] DOCK_PEOPLE = {
            "...........",
            "KKK.KKK.KKK",
            "KWK.KWK.KWK",
            "FFF.FFF.FFF",
            "KFK.KFK.KFK",
            "FWF.FWF.FWF",
            "MMM.YYY.EEE",
            "MWM.YWY.EWE",
            "MMM.YYY.EEE",
            "MWM.YWY.EWE",
            "..........."
    };

    // A wise horned beast: crystal horns, glowing eyes, burgundy mane, brown muzzle.
    private static final String[] DOCK_BEASTS = {
            "..F......F..",
            "..FF....FF..",
            "...FF..FF...",
            "KKKKKKKKKKKK",
            "KWGGGFFGGGWK",
            "KWGYGGGGYGWK",
            "KWGGGGGGGGWK",
            "MKWGGGGGGWKM",
            "MMKEEEEEEKMM",
            "MMKEFEEFEKMM",
            ".MMKEEEEEKMM",
            "..MMMMMMMM.."
    };

    // Scales of justice with a burgundy bowl and a carved wooden base.
    private static final String[] DOCK_LAWS = {
            ".....YY.....",
            ".....YY.....",
            ".....YY.....",
            "..YYYYYYYY..",
            "..G......G..",
            "..G......G..",
            "..G......G..",
            ".MMM....MMM.",
            ".MFM....MFM.",
            ".....YY.....",
            "...EEEEEE...",
            "..EEEEEEEE.."
    };

    // Cogwheel with a burgundy hub, brighter than the monochrome zoom/grid controls.
    private static final String[] DOCK_SETTINGS = {
            "...YY..YY...",
            "...YYYYYY...",
            ".YYGGGGGGYY.",
            ".YGGGGGGGGY.",
            "YYGGMMMMGGYY",
            "YYGGMFFMGGYY",
            "YYGGMMMMGGYY",
            ".YGGGGGGGGY.",
            ".YYGGGGGGYY.",
            "...YYYYYY...",
            "...YY..YY...",
            "............"
    };

    // --- New citizen-card section art ----------------------------------------
    private static final String[] CARD_FAITH = {
            "....Y....", "...YFY...", "...YYY...", "..YYFYY..", "..MMMMM..",
            ".MMMMMMM.", "..EEEEE..", ".EEEEEEE.", "........."
    };
    private static final String[] CARD_LANGUAGE = {
            "..MMMMM..", ".MWWWWWM.", "MWKWKWKWM", "MWWWWWWWM", "MWKWKWKWM",
            ".MWWWWWM.", "..MMMMM..", "...MMM...", "....M...."
    };
    private static final String[] CARD_PARTNER = {
            ".MM...YY.", "MMMM.YYYY", "MMMM.YYYY", ".MM...YY.", "..M...Y..",
            "....F....", ".........", ".........", "........."
    };
    private static final String[] CARD_SKILLS = {
            "....Y....", "....Y....", "...YYY...", "YYYYYYYYY", ".YYFYFYY.",
            "..YYYYY..", ".YY...YY.", "Y.......Y", "........."
    };
    private static final String[] CARD_TRAITS = {
            "...FFF...", "..FMMMF..", ".FMWMWMF.", "FMMMMMMMF", ".FMMMMMF.",
            "..FMMMF..", "...FMF...", "....F....", "........."
    };
    private static final String[] CARD_SPECIALTY = {
            "..MMMMM..", ".MMMMMMM.", "MMMMWMMMM", "MMMMWMMMM", ".MMMMMMM.",
            "..MMMMM..", "...MMM...", "....M....", "........."
    };

    // Decree badges, generated rather than loaded from an art pack.
    private static final String[] DECREE_FESTIVAL = {
            "...YYY...", "..YYYYY..", ".YYYYYYY.", "YYYWYYYYY", "YYYYYYYYY",
            "..YWWWY..", ".WWY.WWW.", "..W...W..", "........."
    };
    private static final String[] DECREE_TAX_CUT = {
            "..YYYYY..", ".YYYYYYY.", "YYYYYYYYY", "YYYYY....", "YYYY.....",
            "YYY......", "YY.......", "Y........", "........."
    };
    private static final String[] DECREE_TAX_RAISE = {
            "Y........", "YY.......", "YYY......", "YYYY.....", "YYYYY....",
            "YYYYYYYYY", ".YYYYYYY.", "..YYYYY..", "........."
    };
    private static final String[] DECREE_TEMPLE = {
            "..YYYYY..", ".YYYYYYY.", "YYYYYYYYY", "..YNYNY..", "..YYYYY..",
            "..YYYYY..", "..NNNNN..", ".NNNNNNN.", "........."
    };
    private static final String[] DECREE_AMNESTY = {
            ".........", "....WWW..", "..WWWWWW.", ".WWWWWWWW", "WWWWWWWWW",
            ".WWWWWWW.", "..WWWWW..", "...WWW...", "........."
    };
    private static final String[] DECREE_CONSCRIPTION = {
            ".......K.", "......KK.", ".....KK..", "....KK...", "...KK....",
            "..KK.....", ".KK......", "K........", "........."
    };
    private static final String[] DECREE_FREE_TRADE = {
            ".....W...", "....WW...", "WWWWWWWW.", ".NNNNNNN.", "..NNNNN..",
            "...NNN...", "..BBBBB..", ".B.B.B.B.", "........."
    };
    private static final String[] DECREE_CRACKDOWN = {
            "..KKKKK..", ".KKKKKKK.", "KKKKKKKKK", "KKKWWWKKK", "KKKWWWKKK",
            ".KKWWWKK.", "..KKKKK..", "...KKK...", "....K...."
    };

    private PixelIcons() {
        // utility class
    }

    static Drawable control(Context context, Control control) {
        switch (control) {
            case SPEED:
                return pattern(context, SPEED);
            case CONTROL:
                return pattern(context, CROWN);
            case ASSASSINATE:
                return pattern(context, DAGGER);
            case NEXT:
                return pattern(context, NEXT);
            case ZOOM_IN:
                return pattern(context, ZOOM_IN);
            case ZOOM_OUT:
                return pattern(context, ZOOM_OUT);
            case CENTER:
                return pattern(context, CENTER);
            case GRID:
                return pattern(context, GRID);
            case PAUSE:
            default:
                return pattern(context, PAUSE);
        }
    }

    /** One generated badge per bottom-navigation tab. */
    static Drawable nav(Context context, int index) {
        switch (index) {
            case 1: return pattern(context, NAV_PEOPLE);
            case 2: return pattern(context, NAV_NATURE);
            case 3: return pattern(context, NAV_CREATURES);
            case 4: return pattern(context, NAV_KINGDOMS);
            case 5: return pattern(context, NAV_POWERS);
            case 6: return pattern(context, NAV_EVENTS);
            case 0:
            default: return pattern(context, NAV_WORLD);
        }
    }

    /** Badges for the five map overlays. */
    static Drawable overlay(Context context, int index) {
        switch (index) {
            case 1: return pattern(context, OVER_BIOME);
            case 2: return pattern(context, OVER_KINGDOM);
            case 3: return pattern(context, OVER_RELIGION);
            case 4: return pattern(context, OVER_JOB);
            case 0:
            default: return pattern(context, OVER_TERRAIN);
        }
    }

    static Drawable lawIcon(Context context, boolean enabled) {
        return pattern(context, enabled ? LAW_ON : LAW_OFF);
    }

    static Drawable biomeSwatch(Context context, int color) {
        int[] pixels = new int[81];
        fill(pixels, Color.TRANSPARENT);
        rect(pixels, 9, 0, 0, 8, 8, shade(color, 0.55f));
        rect(pixels, 9, 1, 1, 7, 7, color);
        rect(pixels, 9, 2, 2, 4, 3, mix(color, 0xFFFFFFFF, 0.38f));
        rect(pixels, 9, 6, 5, 7, 6, shade(color, 0.72f));
        return fromPixels(context, 9, 9, pixels);
    }

    static Drawable resourceSwatch(Context context, int color) {
        int[] pixels = new int[81];
        fill(pixels, Color.TRANSPARENT);
        int light = mix(color, 0xFFFFFFFF, 0.42f);
        int dark = shade(color, 0.62f);
        rect(pixels, 9, 3, 0, 5, 1, light);
        rect(pixels, 9, 1, 2, 7, 3, color);
        rect(pixels, 9, 0, 4, 8, 5, color);
        rect(pixels, 9, 2, 6, 6, 7, dark);
        rect(pixels, 9, 3, 8, 5, 8, dark);
        return fromPixels(context, 9, 9, pixels);
    }

    /**
     * A code-drawn 12x12 badge for every sandbox brush. Each tool has its own
     * silhouette, palette and a couple of highlight pixels, so the bar reads
     * like a small pixel-art toolbar without any external assets.
     */
    static Drawable brushIcon(Context context, IslandView.Brush brush) {
        int size = 12;
        int[] px = new int[size * size];
        fill(px, Color.TRANSPARENT);
        if (brush == null) {
            brush = IslandView.Brush.INSPECT;
        }
        int ink = 0xFF202124;
        int light = 0xFFECEFF1;
        int water = 0xFF42A5F5;
        int waterLight = 0xFFB3E5FC;
        int green = 0xFF66BB6A;
        int darkGreen = 0xFF2E7D32;
        int wood = 0xFF8D6E63;
        int rock = 0xFF90A4AE;
        int fire = 0xFFFF7043;
        int gold = 0xFFFFD54F;
        int red = 0xFFEF5350;

        switch (brush) {
            case INSPECT:
                // Magnifier lens with a short wooden handle.
                rect(px, size, 2, 1, 6, 1, light);
                rect(px, size, 2, 7, 6, 7, light);
                rect(px, size, 1, 2, 1, 6, light);
                rect(px, size, 7, 2, 7, 6, light);
                rect(px, size, 3, 2, 5, 6, 0xFF26C6DA);
                rect(px, size, 3, 3, 4, 4, 0xFFFFFFFF);
                rect(px, size, 7, 7, 10, 10, wood);
                rect(px, size, 8, 8, 9, 9, ink);
                break;
            case WATER:
                // Droplet above a moving wave.
                rect(px, size, 4, 1, 7, 2, water);
                rect(px, size, 3, 2, 8, 4, water);
                rect(px, size, 2, 4, 9, 6, water);
                rect(px, size, 3, 7, 8, 7, water);
                rect(px, size, 5, 3, 6, 5, waterLight);
                rect(px, size, 1, 9, 4, 9, waterLight);
                rect(px, size, 5, 10, 8, 10, water);
                rect(px, size, 9, 9, 11, 9, waterLight);
                break;
            case SAND:
                // Two dunes under a small sun.
                rect(px, size, 8, 1, 10, 3, gold);
                rect(px, size, 0, 8, 11, 10, 0xFFD7A86E);
                rect(px, size, 1, 6, 5, 7, mix(0xFFD7A86E, 0xFFFFFFFF, 0.35f));
                rect(px, size, 6, 5, 11, 8, 0xFFD7A86E);
                rect(px, size, 2, 9, 9, 9, shade(0xFFD7A86E, 0.7f));
                break;
            case GRASS:
                // Uneven blades rooted in a strip of soil.
                rect(px, size, 2, 5, 2, 10, darkGreen);
                rect(px, size, 4, 3, 4, 10, green);
                rect(px, size, 6, 1, 6, 10, darkGreen);
                rect(px, size, 8, 4, 8, 10, green);
                rect(px, size, 10, 6, 10, 10, darkGreen);
                rect(px, size, 1, 10, 11, 11, shade(green, 0.55f));
                rect(px, size, 5, 2, 5, 3, mix(green, 0xFFFFFFFF, 0.4f));
                break;
            case FOREST:
                // Broad evergreen canopy and a straight trunk.
                rect(px, size, 5, 7, 6, 10, wood);
                rect(px, size, 2, 5, 9, 8, darkGreen);
                rect(px, size, 3, 2, 8, 5, green);
                rect(px, size, 4, 1, 7, 2, mix(green, 0xFFFFFFFF, 0.3f));
                rect(px, size, 3, 9, 8, 10, shade(wood, 0.7f));
                break;
            case ORCHARD:
                // Same orchard shape with bright fruit dots.
                rect(px, size, 5, 7, 6, 10, wood);
                rect(px, size, 2, 5, 9, 8, 0xFF388E3C);
                rect(px, size, 3, 2, 8, 5, green);
                rect(px, size, 4, 1, 7, 2, mix(green, 0xFFFFFFFF, 0.35f));
                set(px, size, 3, 5, red);
                set(px, size, 8, 4, red);
                set(px, size, 6, 7, gold);
                set(px, size, 9, 7, red);
                break;
            case MOUNTAIN:
                // Snow-capped peak in front of a low ridge.
                rect(px, size, 0, 9, 11, 11, shade(rock, 0.6f));
                rect(px, size, 1, 7, 10, 9, rock);
                rect(px, size, 3, 4, 8, 7, rock);
                rect(px, size, 5, 1, 6, 3, rock);
                rect(px, size, 4, 3, 7, 4, light);
                rect(px, size, 5, 2, 6, 3, 0xFFFFFFFF);
                break;
            case CAVE:
                // Rock arch around a very dark opening.
                rect(px, size, 1, 9, 10, 11, shade(wood, 0.8f));
                rect(px, size, 2, 3, 9, 9, wood);
                rect(px, size, 3, 4, 8, 8, ink);
                rect(px, size, 4, 5, 7, 8, 0xFF10171D);
                rect(px, size, 2, 2, 9, 2, shade(wood, 1.2f));
                set(px, size, 5, 7, gold);
                set(px, size, 6, 6, waterLight);
                break;
            case HOUSE:
                // Simple hut with pitched roof and lit window.
                rect(px, size, 2, 5, 9, 10, 0xFFB26A3B);
                rect(px, size, 2, 3, 9, 3, 0xFFD15B4A);
                rect(px, size, 4, 2, 7, 2, 0xFFD15B4A);
                rect(px, size, 1, 4, 10, 4, 0xFF8D3B2E);
                rect(px, size, 5, 7, 6, 10, wood);
                rect(px, size, 3, 6, 4, 7, waterLight);
                break;
            case FARM:
                // Three planted rows on tilled soil.
                rect(px, size, 0, 10, 11, 11, shade(wood, 0.7f));
                for (int x = 1; x <= 9; x += 4) {
                    rect(px, size, x, 3, x, 9, green);
                    rect(px, size, x + 1, 5, x + 1, 9, 0xFF9CCC65);
                    rect(px, size, x, 2, x + 1, 2, gold);
                }
                break;
            case ROAD:
                // Irregular pale stepping stones on a dark track.
                rect(px, size, 0, 4, 11, 7, shade(rock, 0.5f));
                rect(px, size, 1, 5, 3, 6, rock);
                rect(px, size, 5, 5, 7, 6, rock);
                rect(px, size, 9, 5, 11, 6, rock);
                rect(px, size, 2, 5, 2, 5, mix(rock, 0xFFFFFFFF, 0.4f));
                rect(px, size, 6, 5, 6, 5, mix(rock, 0xFFFFFFFF, 0.4f));
                break;
            case HUMAN:
                // Front-facing settler bust.
                rect(px, size, 3, 2, 8, 8, 0xFFD8A47C);
                rect(px, size, 2, 1, 9, 2, 0xFF3E2723);
                rect(px, size, 2, 3, 2, 5, 0xFF3E2723);
                set(px, size, 4, 5, ink);
                set(px, size, 7, 5, ink);
                rect(px, size, 5, 7, 6, 7, 0xFF8D5524);
                rect(px, size, 3, 9, 8, 11, 0xFF42A5F5);
                break;
            case WOLF:
                // Pointed ears, grey head and a pale muzzle.
                rect(px, size, 2, 1, 4, 3, 0xFF78909C);
                rect(px, size, 7, 1, 9, 3, 0xFF78909C);
                rect(px, size, 3, 3, 8, 9, 0xFF9E9E9E);
                rect(px, size, 5, 6, 6, 10, light);
                set(px, size, 4, 5, red);
                set(px, size, 7, 5, red);
                set(px, size, 5, 9, ink);
                break;
            case FIRE:
                // Tall flame with a bright core.
                rect(px, size, 5, 1, 6, 3, gold);
                rect(px, size, 4, 3, 7, 7, fire);
                rect(px, size, 3, 5, 8, 10, fire);
                rect(px, size, 2, 8, 9, 11, 0xFFE53935);
                rect(px, size, 5, 6, 6, 9, gold);
                break;
            case RAIN:
                // Heavy cloud with three falling streaks.
                rect(px, size, 3, 1, 7, 1, 0xFFCFD8DC);
                rect(px, size, 2, 2, 9, 5, 0xFF90A4AE);
                rect(px, size, 1, 4, 10, 6, 0xFFB0BEC5);
                rect(px, size, 2, 8, 3, 10, water);
                rect(px, size, 5, 8, 6, 10, water);
                rect(px, size, 8, 8, 9, 10, water);
                break;
            case METEOR:
                // Comet tail trailing into a burning head.
                rect(px, size, 0, 0, 4, 1, gold);
                rect(px, size, 1, 2, 5, 3, 0xFFFFA726);
                rect(px, size, 3, 4, 7, 5, fire);
                rect(px, size, 6, 6, 10, 10, 0xFFE53935);
                rect(px, size, 7, 7, 9, 9, gold);
                break;
            default:
                break;
        }
        return fromPixels(context, size, size, px);
    }

    /** Small pixel frame used behind the numbers in the resource strip. */
    static Drawable smallBadge(Context context, int color) {
        int size = 9;
        int[] px = new int[size * size];
        fill(px, Color.TRANSPARENT);
        int light = mix(color, 0xFFFFFFFF, 0.45f);
        int dark = shade(color, 0.5f);
        rect(px, size, 1, 1, 7, 7, dark);
        rect(px, size, 2, 2, 6, 6, 0xFF121D27);
        rect(px, size, 1, 1, 7, 1, light);
        rect(px, size, 1, 1, 1, 7, light);
        rect(px, size, 1, 7, 7, 7, dark);
        rect(px, size, 7, 2, 7, 7, dark);
        set(px, size, 2, 2, color);
        return fromPixels(context, size, size, px);
    }

    static Drawable disasterIcon(Context context, int color) {
        int[] pixels = new int[81];
        fill(pixels, Color.TRANSPARENT);
        int light = mix(color, 0xFFFFFFFF, 0.5f);
        rect(pixels, 9, 4, 0, 4, 2, color);
        rect(pixels, 9, 4, 6, 4, 8, color);
        rect(pixels, 9, 0, 4, 2, 4, color);
        rect(pixels, 9, 6, 4, 8, 4, color);
        rect(pixels, 9, 2, 2, 6, 6, color);
        rect(pixels, 9, 3, 3, 5, 5, light);
        rect(pixels, 9, 1, 1, 1, 1, light);
        rect(pixels, 9, 7, 7, 7, 7, light);
        return fromPixels(context, 9, 9, pixels);
    }

    static Drawable techIcon(Context context, int color) {
        int[] pixels = new int[81];
        fill(pixels, Color.TRANSPARENT);
        int dark = shade(color, 0.65f);
        int light = mix(color, 0xFFFFFFFF, 0.45f);
        rect(pixels, 9, 2, 2, 6, 6, dark);
        rect(pixels, 9, 3, 1, 5, 1, color);
        rect(pixels, 9, 3, 7, 5, 7, color);
        rect(pixels, 9, 1, 3, 1, 5, color);
        rect(pixels, 9, 7, 3, 7, 5, color);
        rect(pixels, 9, 3, 3, 5, 5, light);
        return fromPixels(context, 9, 9, pixels);
    }

    static Drawable clanIcon(Context context) {
        return pattern(context, CLAN);
    }

    static Drawable kingdomIcon(Context context) {
        return pattern(context, CROWN);
    }

    /** New grouped-dock badge selected by {@link DockGroup}. */
    static Drawable dockIcon(Context context, DockGroup group) {
        if (group == null) {
            group = DockGroup.PEOPLE;
        }
        switch (group) {
            case BEASTS: return pattern(context, DOCK_BEASTS);
            case LAWS: return pattern(context, DOCK_LAWS);
            case SETTINGS: return pattern(context, DOCK_SETTINGS);
            case PEOPLE:
            default: return pattern(context, DOCK_PEOPLE);
        }
    }

    static Drawable peopleGroupIcon(Context context) {
        return pattern(context, DOCK_PEOPLE);
    }

    static Drawable beastsGroupIcon(Context context) {
        return pattern(context, DOCK_BEASTS);
    }

    static Drawable lawsGroupIcon(Context context) {
        return pattern(context, DOCK_LAWS);
    }

    static Drawable settingsGroupIcon(Context context) {
        return pattern(context, DOCK_SETTINGS);
    }

    /**
     * Citizen-card section badge. Kingdom and clan reuse the established
     * {@link #kingdomIcon(Context)} and {@link #clanIcon(Context)} drawings.
     */
    static Drawable cardIcon(Context context, CardSection section) {
        if (section == null) {
            section = CardSection.KINGDOM;
        }
        switch (section) {
            case FAITH: return pattern(context, CARD_FAITH);
            case LANGUAGE: return pattern(context, CARD_LANGUAGE);
            case PARTNER: return pattern(context, CARD_PARTNER);
            case CLAN: return clanIcon(context);
            case SKILLS: return pattern(context, CARD_SKILLS);
            case TRAITS: return pattern(context, CARD_TRAITS);
            case SPECIALTY: return pattern(context, CARD_SPECIALTY);
            case KINGDOM:
            default: return kingdomIcon(context);
        }
    }

    static Drawable faithIcon(Context context) {
        return pattern(context, CARD_FAITH);
    }

    static Drawable languageIcon(Context context) {
        return pattern(context, CARD_LANGUAGE);
    }

    static Drawable partnerIcon(Context context) {
        return pattern(context, CARD_PARTNER);
    }

    static Drawable skillsIcon(Context context) {
        return pattern(context, CARD_SKILLS);
    }

    static Drawable traitsIcon(Context context) {
        return pattern(context, CARD_TRAITS);
    }

    static Drawable specialtyIcon(Context context) {
        return pattern(context, CARD_SPECIALTY);
    }

    static Drawable power(Context context, Power power) {
        switch (power) {
            case RAIN: return pattern(context, POWER_RAIN);
            case BLOOM: return pattern(context, POWER_BLOOM);
            case HEAL: return pattern(context, POWER_HEAL);
            case BLESS: return pattern(context, POWER_BLESS);
            case LIGHTNING: return pattern(context, POWER_LIGHTNING);
            case PLAGUE: return pattern(context, POWER_PLAGUE);
            case EARTHQUAKE: return pattern(context, POWER_EARTHQUAKE);
            case METEOR: return pattern(context, POWER_METEOR);
            case PEACE: return pattern(context, POWER_PEACE);
            case PROSPERITY:
            default: return pattern(context, POWER_PROSPERITY);
        }
    }

    /** Decree art is now generated in code too — no bundled art pack is used. */
    static Drawable decreeImage(Context context, Decree decree) {
        switch (decree) {
            case FESTIVAL: return pattern(context, DECREE_FESTIVAL);
            case TAX_CUT: return pattern(context, DECREE_TAX_CUT);
            case TAX_RAISE: return pattern(context, DECREE_TAX_RAISE);
            case BUILD_TEMPLE: return pattern(context, DECREE_TEMPLE);
            case AMNESTY: return pattern(context, DECREE_AMNESTY);
            case CONSCRIPTION: return pattern(context, DECREE_CONSCRIPTION);
            case FREE_TRADE: return pattern(context, DECREE_FREE_TRADE);
            case CRACKDOWN: return pattern(context, DECREE_CRACKDOWN);
            default: return pattern(context, DECREE_FESTIVAL);
        }
    }

    /** A 12×12 shaded bust: skin tone, hair, eyes, clothing and crown. */
    static Drawable portrait(Context context, Citizen citizen) {
        int size = 12;
        int[] pixels = new int[size * size];
        int skin = mix(citizen.getRace().getColor(), 0xFFD8A47C, 0.48f);
        int skinDark = shade(skin, 0.72f);
        int skinLight = mix(skin, 0xFFFFFFFF, 0.22f);
        int hair = citizen.getAgeYears() > 55 ? 0xFF9E9E9E
                : (citizen.getGender() == Gender.FEMALE ? 0xFF5D4037 : 0xFF3E2723);
        int hairLight = mix(hair, 0xFFFFFFFF, 0.18f);
        int cloth = citizen.getJob().getColor();
        int clothDark = shade(cloth, 0.68f);
        int eye = 0xFF202124;

        fill(pixels, 0x00000000);

        // Hair mass and ears.
        rect(pixels, size, 2, 1, 9, 2, hair);
        rect(pixels, size, 3, 0, 8, 0, hairLight);
        rect(pixels, size, 1, 4, 1, 6, skinDark);
        rect(pixels, size, 10, 4, 10, 6, skinDark);

        // Face with a light side and a shaded side.
        rect(pixels, size, 2, 3, 9, 7, skin);
        rect(pixels, size, 2, 3, 3, 7, skinDark);
        rect(pixels, size, 8, 3, 9, 7, skinDark);
        rect(pixels, size, 4, 4, 7, 6, skinLight);

        // Eyes, nose and mouth.
        set(pixels, size, 4, 4, eye);
        set(pixels, size, 7, 4, eye);
        set(pixels, size, 4, 5, 0xFFFFFFFF);
        set(pixels, size, 7, 5, 0xFFFFFFFF);
        set(pixels, size, 6, 5, skinDark);
        rect(pixels, size, 5, 6, 6, 6, shade(skinDark, 0.8f));

        // Longer hair for women.
        if (citizen.getGender() == Gender.FEMALE) {
            rect(pixels, size, 1, 3, 1, 8, hair);
            rect(pixels, size, 10, 3, 10, 8, hair);
        }

        // Neck and shoulders.
        rect(pixels, size, 5, 8, 6, 8, skinDark);
        rect(pixels, size, 2, 9, 9, 11, cloth);
        rect(pixels, size, 2, 9, 3, 11, clothDark);
        rect(pixels, size, 8, 9, 9, 11, clothDark);
        rect(pixels, size, 5, 10, 6, 10, shade(cloth, 1.25f));

        if (citizen.getJob() == Job.RULER) {
            rect(pixels, size, 2, 0, 9, 0, 0xFFFFD54F);
            rect(pixels, size, 4, 1, 4, 1, 0xFFFFD54F);
            rect(pixels, size, 7, 1, 7, 1, 0xFFFFD54F);
        }
        if (citizen.isPregnant()) {
            rect(pixels, size, 4, 10, 7, 11, mix(cloth, 0xFFE57373, 0.35f));
        }
        return fromPixels(context, size, size, pixels);
    }

    private static Drawable pattern(Context context, String[] rows) {
        int width = rows[0].length();
        int height = rows.length;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            if (rows[y].length() != width) {
                throw new IllegalStateException("Bad pixel icon row: " + rows[y]);
            }
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = colorOf(rows[y].charAt(x));
            }
        }
        return fromPixels(context, width, height, pixels);
    }

    /**
     * Icon ink is tuned for the light icon tiles: charcoal outlines, mid-grey
     * fills and burgundy/orange accents instead of the old blue-on-blue set.
     */
    private static int colorOf(char symbol) {
        switch (symbol) {
            case 'K': return 0xFF3E3B39;
            case 'W': return 0xFF8D8884;
            case 'Y': return 0xFFFCA311;
            case 'G': return 0xFF6D6A6A;
            case 'B': return 0xFF2E7DB8;
            case 'P': return 0xFF8E24AA;
            case 'N': return 0xFF6D4C41;
            case 'R': return 0xFFB01527;
            case 'L': return 0xFF3E8E41;
            case 'C': return 0xFF00838F;
            // Palette used by the new grouped-dock and citizen-card art.
            case 'M': return 0xFF5B0A19; // burgundy
            case 'E': return 0xFF504A3E; // warm brown
            case 'F': return 0xFFFFFFFF; // clean white highlight
            default: return Color.TRANSPARENT;
        }
    }

    private static void fill(int[] pixels, int color) {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = color;
        }
    }

    private static void rect(int[] pixels, int width, int x0, int y0, int x1, int y1, int color) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                set(pixels, width, x, y, color);
            }
        }
    }

    private static void set(int[] pixels, int width, int x, int y, int color) {
        if (x >= 0 && y >= 0 && x < width && y * width + x < pixels.length) {
            pixels[y * width + x] = color;
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

    private static Drawable fromPixels(Context context, int width, int height, int[] pixels) {
        Bitmap bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        drawable.getPaint().setAntiAlias(false);
        drawable.getPaint().setFilterBitmap(false);
        return drawable;
    }
}

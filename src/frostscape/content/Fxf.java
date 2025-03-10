package frostscape.content;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.Time;
import arc.util.Tmp;
import frostscape.graphics.Draww;
import frostscape.math.Math3D;
import frostscape.math.MultiInterp;
import frostscape.util.DrawUtils;
import mindustry.Vars;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import static arc.graphics.g2d.Draw.color;
import static arc.math.Angles.randLenVectors;
import static frostscape.util.WeatherUtils.windDirection;
import static mindustry.content.Fx.rand;

public class Fxf {
    public static MultiInterp
            smokeFade = new MultiInterp(new float[]{0, 0.8f}, new Interp[]{Interp.pow2, Interp.pow2Out});
    public static Effect
    chargeExplode = new Effect(200, e -> {
        Fill.circle(e.x, e.y, e.fslope() * e.fslope() * 5 + e.finpow() * 20);

        rand.setSeed(e.id);
        Lines.stroke(e.fin(Interp.slowFast) * 2);
        for(int i = 0; i < 8; i++){
            float scaling = Time.time/80 *(e.fin() + 1);
            float fin = (rand.random(1) + scaling + i * 0.15f) % 1, fout = 1 - fin;
            float angle = rand.random(360) + Mathf.floor(scaling) * 80;
            float len = 100 * Interp.pow2Out.apply(fout);
            Lines.lineAngle(e.x + Angles.trnsx(angle, len), e.y + Angles.trnsy(angle, len), angle, 12 * fin);
        }
        if(Vars.state.isPlaying() && Mathf.chance(e.fin() * 0.65f * Time.delta)) Effect.shake(e.fin() * 4.85f, 15, e.x, e.y);
    }),

    emberTrail = new Effect(40f, e -> {
        color(Liquids.slag.color, Color.white, e.fout() / 5f + Mathf.randomSeedRange(e.id, 0.12f));
        randLenVectors(e.id, 2, 1f + e.fin() * 3f, (x, y) -> {
            Fill.circle(e.x + x, e.y + y, .2f + e.fout() * 1.2f);
        });
    }),

    emberTrailHeight = new Effect(40f, e -> {
        color(Liquids.slag.color, Color.white, e.fout() / 5f + Mathf.randomSeedRange(e.id, 0.12f));
        float height = (float) e.data;
        DrawUtils.speckOffset(e.x, e.y, height, e.fin() * 40, DrawUtils.smokeWeight, Tmp.v1);
        randLenVectors(e.id, 2, 1f + e.fin() * 3f, (x, y) -> {
            Fill.circle(Tmp.v1.x + x, Tmp.v1.y + y, .2f + e.fout() * 1.2f);
        });
    });

    public static Effect

    glowEffect = new Effect(0, e -> {
        Draw.z(Layer.floor);
        Building b = Vars.world.buildWorld(e.x, e.y);
        if(b != null) return;
        TextureRegion region = (TextureRegion) e.data;
        Draw.alpha(e.fslope() * e.fslope());
        Draw.rect(region, e.x, e.y, e.rotation);
        Draw.blend(Blending.additive);
        Draw.rect(region, e.x, e.y, e.rotation);
        Draw.blend();
    }),

    sulphuricSmoke = new Effect(450, 100, e -> {
        Draw.color(Palf.sulphur);
        Draw.alpha(smokeFade.apply(e.fin()));
        float size = Mathf.randomSeed(e.id, 2, 4);
        Draw.z(Mathf.lerp(Layers.smokeLow, Layers.smokeHigh, e.fin()));

        Angles.randLenVectors(e.id + 1, (int) (Mathf.randomSeed(e.id, 3) + 1), e.fin() * 12, e.rotation, 360, (x, y) -> {
            DrawUtils.speckOffset(e.x + x, e.y + y, e.fin(), e.time, DrawUtils.smokeWeight, Tmp.v1);
            Fill.circle(Tmp.v1.x, Tmp.v1.y, size * e.fout(Interp.pow4));
        });
    }),

    steamSmoke = new Effect(85, 100, e -> {
        Draw.color(Pal.lightishGray);
        Draw.alpha(smokeFade.apply(e.fin()) * 0.8f);
        float size = Mathf.randomSeed(e.id, 2, 4);
        Draw.z(Mathf.lerp(Layer.blockOver, Layer.blockBuilding - 1, e.fin()));

        Angles.randLenVectors(e.id + 1, (int) (Mathf.randomSeed(e.id, 3) + 1), e.fin() * 12, e.rotation, 360, (x, y) -> {
            DrawUtils.speckOffset(e.x + x, e.y + y, e.fin(), e.time, DrawUtils.smokeWeight, Tmp.v1);
            Fill.circle(Tmp.v1.x, Tmp.v1.y, size * e.fout(Interp.pow4));
        });
    }),

    sulphurDrops = new Effect(55, e -> {
        Draw.color(Palf.sulphur);
        Draw.alpha(smokeFade.apply(e.fin()));
        Draw.z(Mathf.lerp(Layers.smokeLow, Layers.smokeHigh, e.fin()));

        Angles.randLenVectors(e.id + 1, (int) (Mathf.randomSeed(e.id, 3) + 1), e.fin() * 12, e.rotation, 360, (x, y) -> {
            DrawUtils.speckOffset(e.x + x, e.y + y, e.fin(), e.time, DrawUtils.smokeWeight, Tmp.v1);
            Fill.circle(Tmp.v1.x, Tmp.v1.y, e.fout() * 1);
        });
    });

    public static Effect steamEffect(float lifetime, float radius){
        return new Effect(lifetime, e -> {
            float a = e.fin();
            Fill.circle(e.x + Tmp.v1.set(windDirection()).x * a, e.y + Tmp.v1.y * a, radius * 1 - a);
        });
    };
}

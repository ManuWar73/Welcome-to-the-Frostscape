package frostscape.util;

import arc.Core;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import frostscape.math.Math3D;

public class DrawUtils {
    public static Vec2 tv;
    /*This conssiders
    -X and Y of position
    -Wind
    -Parallax
     */

    //Weights for different materials
    public static float
        smokeWeight = 0.05f,
        heavySmokeWeight = 0.15f;

    public static void speckOffset(float x, float y, float height, float time, float weight, Vec2 out){
        Vec2 w = WeatherUtils.windDirection().scl(time * 80 * Core.settings.getInt("frostscape-wind-visual-force")/100, time * 80 * Core.settings.getInt("frostscape-wind-visual-force")/100).sub(weight, weight);
        w.x = Mathf.maxZero(w.x);
        w.y = Mathf.maxZero(w.y);
        float px = Math3D.xCamOffset2D(x + w.x, height), py = Math3D.yCamOffset2D(y + w.y, height);
        out.set(x + px + w.x, y + py + w.y);
    }
}

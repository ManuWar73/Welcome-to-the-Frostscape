package frostscape.world.blocks.light;

import arc.struct.Seq;
import arc.util.Time;
import frostscape.Frostscape;
import frostscape.world.FrostscapeBlock;
import frostscape.world.FrostscapeBuilding;
import frostscape.world.UpgradesType;
import frostscape.world.light.LightBeams;
import frostscape.world.light.LightBeams.LightSource;
import frostscape.world.light.Lightc;
import frostscape.world.light.WorldShape;

public class SolarReflector extends FrostscapeBlock {

    public LightBeams.ColorData data = new LightBeams.ColorData(1, 1, 1);

    public SolarReflector(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
    }

    public static class ReflectorSource extends LightSource{
        float x, y;
        public ReflectorSource(LightBeams.ColorData color, float rotation, float x, float y) {
            super(color, rotation);
            this.x = x;
            this.y = y ;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }
    }

    public class SolarReflectorBuild extends FrostscapeBuilding implements Lightc {

        //Instantiated on building creation
        public ReflectorSource source;
        public float rotation = 0;

        @Override
        public void update() {
            super.update();
            source.rotation += Time.delta;
        }

        @Override
        public void created() {
            source = new ReflectorSource(data, rotation, x, y);
            Frostscape.lights.handle(this);
        }

        @Override
        public boolean exists() {
            return added;
        }

        @Override
        public UpgradesType type() {
            return (UpgradesType) block;
        }

        @Override
        public Seq<LightSource> getSources() {
            return Seq.with(source);
        }

        @Override
        public LightBeams.CollisionData collision(float x, float y, float rotation, int shape, int side, LightBeams.ColorData color, LightBeams.CollisionData collision) {
            return null;
        }

        @Override
        public void afterLight() {
            Lightc.super.afterLight();
        }
    }
}

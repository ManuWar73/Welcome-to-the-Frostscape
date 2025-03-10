package frostscape;

import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Cell;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.struct.Sort;
import arc.util.*;
import frostscape.content.Palf;
import frostscape.game.ScriptedSectorHandler;
import frostscape.graphics.FrostShaders;
import frostscape.mods.Compatibility;
import frostscape.ui.FrostUI;
import frostscape.ui.overlay.ScanningOverlay;
import frostscape.ui.overlay.SelectOverlay;
import frostscape.util.Sorts;
import frostscape.util.UIUtils;
import frostscape.world.environment.FloorDataHandler;
import frostscape.world.light.LightBeams;
import frostscape.world.meta.Family;
import frostscape.world.meta.LoreNote;
import frostscape.world.research.ResearchHandler;
import frostscape.world.upgrades.UpgradeHandler;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.EventType.*;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.mod.*;
import mindustry.ui.dialogs.BaseDialog;
import rhino.ImporterTopLevel;
import rhino.NativeJavaPackage;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.ui;

public class Frostscape extends Mod{

    public static NativeJavaPackage p = null;

    public static final String NAME = "hollow-frostscape";
    public static Mods.LoadedMod MOD;
    public static final float VERSION = 136.1f;
    public static String VERSION_NAME = "", LAST_VERSION_NAME = "";
    public static ScriptedSectorHandler sectors = new ScriptedSectorHandler();
    public static FloorDataHandler floors = new FloorDataHandler();
    public static ResearchHandler research = new ResearchHandler();

    public static UpgradeHandler upgrades = new UpgradeHandler();

    public static SelectOverlay selection = new SelectOverlay();
    public static ScanningOverlay scan = new ScanningOverlay();

    public static LightBeams lights = new LightBeams();

    public Frostscape(){

        Color.cyan.set(Palf.pulseChargeEnd);
        Color.sky.set(Palf.pulseChargeStart);

        Events.on(FileTreeInitEvent.class, e -> {
            Core.app.post(FrostShaders::load);
            MOD = Vars.mods.getMod(NAME);
        });

        Events.on(EventType.ClientLoadEvent.class,
                e -> {
                    loadSettings();
                    LoreNote.all.each(LoreNote::load);
                    Family.all.each(Family::load);

                    VERSION_NAME = MOD.meta.version;
                    LAST_VERSION_NAME = Core.settings.getString(NAME + "-last-version", "0.0");
                    if(!LAST_VERSION_NAME.equals(VERSION_NAME)) Time.runTask(10f, () -> {
                        BaseDialog dialog = new BaseDialog("phrog");
                        Image warning = new Image(Icon.warning);
                        Cell<Image> cell = dialog.cont.add(warning);
                        cell.pad(20f).row();

                        //Ok look I was lazy
                        AtomicBoolean warned = new AtomicBoolean(false);
                        AtomicReference<Float> rotationSpeed = new AtomicReference<>((float) 0);
                        warning.clicked(() -> {
                            Sounds.wind3.play();
                            warning.color.set(Mathf.random(1), Mathf.random(1), Mathf.random(1));
                            cell.width(30 + Mathf.random(69));
                            cell.height(30 + Mathf.random(69));
                            cell.expand();
                            warned.set(true);
                            rotationSpeed.set((rotationSpeed.get() + 1));
                            Core.settings.put(NAME + "-farted", true);
                        });

                        warning.update(() -> {
                            if (warned.get()) warning.rotation += rotationSpeed.get();
                        });

                        dialog.cont.add("[red]WARNING").padTop(50).row();
                        dialog.cont.add("[#dde6f0]Welcome to the Frostscape[] is still wip, proceed at your own [red]risk").row();
                        dialog.cont.add("[lightgray]Your last run version was [red]" + LAST_VERSION_NAME + "[],\n The mod has been updated to it's [cyan]" + VERSION_NAME).row();
                        dialog.cont.button("Understood, pushing on!", () -> {
                            dialog.hide();
                            Core.settings.put(NAME + "-last-version", VERSION_NAME);
                            Core.settings.put(NAME + "-re-installations", Core.settings.getInt(NAME + "-re-installations", 0) + 1);
                        }).size(300f, 50f);
                        dialog.show();
                    });
                }
        );

        Events.run(EventType.ContentInitEvent.class, () -> {
            loadSplash();
        });

        Events.run(EventType.WinEvent.class, () -> {
            loadSplash();
        });

        Events.run(EventType.SaveWriteEvent.class, () -> {
            lights.lights.clear();
        });

        Events.run(Trigger.update, () -> {
            if(Vars.state.isMenu()) return;
            lights.updateBeams();
            scan.update();
            if(!Vars.state.isPlaying()) return;
            selection.update();
        });

        Events.run(Trigger.draw, () -> {
            Draw.draw(Layer.overlayUI, selection::drawSelect);
            Draw.draw(Layer.overlayUI, scan::draw);
            Draw.draw(Layer.buildBeam, scan::drawScan);
            Draw.draw(Layer.light + 1, lights::draw);
        });
    }

    @Override
    public void init() {

        Vars.mods.getScripts().runConsole(
                "function buildWorldP(){return Vars.world.buildWorld(Vars.player.x, Vars.player.y)}");
        ImporterTopLevel scope = (ImporterTopLevel) Vars.mods.getScripts().scope;

        Seq<String> packages = Seq.with(
                "frostscape",
                "frostscape.content",
                "frostscape.game",
                "frostscape.graphics",
                "frostscape.math",
                "frostscape.mods",
                "frostscape.ui",
                "frostscape.util",
                "frostscape.world",
                "frostscape.world.light",
                "frostscape.world.upgrades"
        );

        packages.each(name -> {
            p = new NativeJavaPackage(name, Vars.mods.mainLoader());

            p.setParentScope(scope);

            scope.importPackage(p);
        });
    }

    public void loadContent(){
        long current = Time.millis();
        FrostContentLoader.load();
        final float time = Time.timeSinceMillis(current);

        Events.run(ClientLoadEvent.class, () -> {

            //Log content loading time in ClientLoadEvent
            Log.info(String.format("Loaded Frostscape content in: %s", time));

            long current1 = Time.millis();
            FrostUI.load();
            UIUtils.loadAdditions();

            Log.info(String.format("Loaded Frostscape ui in: %s", (Time.timeSinceMillis(current1))));

            current1 = Time.millis();
            //Run after all content has loaded
            Compatibility.handle();
            Log.info(String.format("Loaded Frostscape compat in: %s", (Time.timeSinceMillis(current1))));
        });
    }

    void loadSettings(){
        ui.settings.addCategory(Core.bundle.get("settings.frostscape-title"), NAME + "-hunter", t -> {
            t.sliderPref(Core.bundle.get("frostscape-parallax"), 100, 1, 100, 1, s -> s + "%");
            t.sliderPref(Core.bundle.get("frostscape-wind-visual-force"), 100, 0, 800, 1, s -> s + "%");
        });
    }

    void loadSplash(){

        MOD.meta.subtitle = null;

        ObjectFloatMap<String> categories = getCategories(NAME + ".splash.chances");

        categories.each(e -> {
            if(!(MOD.meta.subtitle == null)) return;

            boolean showing = Mathf.chance(e.value);
            if(showing){
                String[] subtitles = getEntries(NAME + ".splash." + e.key);
                MOD.meta.subtitle = subtitles[Mathf.random((int)Mathf.maxZero(subtitles.length - 1))];
            }
        });

        if(!(MOD.meta.subtitle == null)) return;

        String[] subtitles = getEntries(NAME + ".splash.default");
        MOD.meta.subtitle = subtitles[Mathf.random((int)Mathf.maxZero(subtitles.length - 1))];
    }

    String[] getEntries(String key){
        String packed = Core.bundle.get(key);
        int end = 0;
        String[] list = new String[]{};
        for (int i = 0; i < packed.length(); i++) {
            if(packed.charAt(i) == '|' && packed.charAt(i - 1) != '\\') {
                String entry = packed.substring(end, i);
                list = Structs.add(list, entry);
                end = i + 1;
            }
        }
        String subtitle = packed.substring(end);
        Structs.add(list, subtitle);
        return list;
    }

    ObjectFloatMap<String> getCategories(String key){
        String packed = Core.bundle.get(key);
        int end = 0;
        ObjectFloatMap<String> categories = new ObjectFloatMap<>();
        String lastName = "";
        boolean name = true;
        for (int i = 0; i < packed.length(); i++) {
            if(packed.charAt(i) == '|' && packed.charAt(i - 1) != '\\') {
                String entry = packed.substring(end, i);
                if(name) {
                    lastName = entry;
                }
                else categories.put(lastName, Strings.parseFloat(entry));

                end = i + 1;

                name = !name;
            }
        }
        String ending = packed.substring(end);
        categories.put(lastName, Strings.parseFloat(ending));
        return categories;
    }
}

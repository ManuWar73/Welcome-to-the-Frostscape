package frostscape.ui.dialog.database;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.HandCursorListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.TextField;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Time;
import frostscape.ui.FrostUI;
import frostscape.world.meta.LoreNote;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import mindustry.world.Block;

import static arc.Core.settings;
import static frostscape.util.StatUtils.getCategory;
import static mindustry.Vars.mobile;
import static mindustry.Vars.ui;

public class ScannedBlocksTable extends Table {
    private TextField search;
    private boolean showLocked = true;
    private Table all = new Table(), cont = new Table(), mock = new Table();
    public ScannedBlocksTable(){
        super();
        add(cont);
        this.all.margin(20.0F).marginTop(0.0F);
        this.cont.table((s) -> {
            s.image(Icon.zoom).padRight(8.0F);
            this.search = s.field(null, (text) -> {
                this.rebuild();
            }).growX().get();
            this.search.setMessageText("@players.search");
        }).fillX().padBottom(4.0F).row();
        this.cont.pane(this.all).scrollX(false);
        Events.run(EventType.UnlockEvent.class, this::rebuild);
    }


    public void rebuild(){
        this.all.clear();
        String text = this.search.getText();
        ObjectMap<String, Seq<Content>> map = new ObjectMap<>();
        Vars.content.blocks().each(b -> {
            if(b.synthetic() || !(showLocked || b.unlocked())) return;
            String cat = getCategory(b);
            if(map.containsKey(cat)) {
                map.get(cat).add(b);
                return;
            }
            map.put(cat, Seq.with(b));
        });

        map.each((category, seq) -> {
            Seq<Content> array = seq.select((c) -> {
                boolean valid;
                if (c instanceof UnlockableContent) {
                    UnlockableContent u = (UnlockableContent)c;
                    if ((text.isEmpty() || u.localizedName.toLowerCase().contains(text.toLowerCase()))) {
                        valid = true;
                        return valid;
                    }
                }

                valid = false;
                return valid;
            });
            if (array.size != 0) {
                addHeader(category);

                this.all.table((list) -> {
                    list.left();
                    int cols = (int) Mathf.clamp(((float) Core.graphics.getWidth() - Scl.scl(30.0F)) / Scl.scl(44.0F), 1.0F, 22.0F);
                    int count = 0;

                    for(int i = 0; i < array.size; ++i) {
                        UnlockableContent unlock = (UnlockableContent) array.get(i);
                        Image image = unlocked(unlock) ? new Image(unlock.uiIcon).setScaling(Scaling.fit) : new Image(Icon.lock, Pal.gray);

                        list.add(image).size(8 * 4).pad(3);

                        ClickListener listener = new ClickListener();
                        image.addListener(listener);
                        if(!mobile && unlocked(unlock)){
                            image.addListener(new HandCursorListener());
                            image.update(() -> image.color.lerp(!listener.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
                        }

                        if(unlocked(unlock)){
                            image.clicked(() -> {
                                if(Core.input.keyDown(KeyCode.shiftLeft) && Fonts.getUnicode(unlock.name) != 0){
                                    Core.app.setClipboardText((char)Fonts.getUnicode(unlock.name) + "");
                                    ui.showInfoFade("@copied");
                                }else{
                                    ui.content.show(unlock);
                                }
                            });
                            image.addListener(new Tooltip(t -> t.background(Tex.button).add(unlock.localizedName + (settings.getBool("console") ? "\n[gray]" + unlock.name : ""))));
                        }

                        if((++count) % cols == 0){
                            list.row();
                        }
                    }

                }).growX().left().padBottom(10.0F);
                this.all.row();
            }
        });


        Seq<LoreNote> notes = LoreNote.all.copy().select(n -> text.isEmpty() || n.localizedName.toLowerCase().contains(text.toLowerCase()));

        if(notes.size > 0){
            addHeader("@content.notes.name");
            this.all.table((list) -> {
                list.left();
                int cols = (int) Mathf.clamp(((float) Core.graphics.getWidth() - Scl.scl(30.0F)) / Scl.scl(44.0F), 1.0F, 22.0F);
                int count = 0;

                for(int i = 0; i < notes.size; ++i) {
                    LoreNote note = notes.get(i);
                    Image image = note.unlocked() ? new Image(note.icon).setScaling(Scaling.fit) : new Image(Icon.lock, Pal.gray);

                    list.add(image).size(8 * 4).pad(3);

                    ClickListener listener = new ClickListener();
                    image.addListener(listener);
                    if(!mobile && note.unlocked()){
                        image.addListener(new HandCursorListener());
                        image.update(() -> image.color.lerp(!listener.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
                    }

                    if(note.unlocked()){
                        image.clicked(() -> {
                            if(Core.input.keyDown(KeyCode.shiftLeft) && Fonts.getUnicode(note.name) != 0){
                                Core.app.setClipboardText((char)Fonts.getUnicode(note.name) + "");
                                ui.showInfoFade("@copied");
                            }else{
                                FrostUI.notes.show(note);
                            }
                        });
                        image.addListener(new Tooltip(t -> t.background(Tex.button).add(note.localizedName + (settings.getBool("console") ? "\n[gray]" + note.name : ""))));
                    }

                    if((++count) % cols == 0){
                        list.row();
                    }
                }

            }).growX().left().padBottom(10.0F);
            this.all.row();
        }



        if (this.all.getChildren().isEmpty()) {
            this.all.add("@none.found");
        }
    }

    public void addHeader(String category){
        this.all.add(category).growX().left().color(Pal.accent);
        this.all.row();
        this.all.image().growX().pad(5.0F).padLeft(0.0F).padRight(0.0F).height(3.0F).color(Pal.accent);
        this.all.row();
    }

    boolean unlocked(UnlockableContent content) {
        return !Vars.state.isCampaign() && !Vars.state.isMenu() || content.unlocked();
    }
}

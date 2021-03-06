package com.rpg.southparkavatars;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpg.southparkavatars.character.AbstractCharacter;
import com.rpg.southparkavatars.character.Character;
import com.rpg.southparkavatars.character.Skin;
import com.rpg.southparkavatars.character.clothing.AbstractClothing;
import com.rpg.southparkavatars.character.clothing.concrete.Back;
import com.rpg.southparkavatars.character.clothing.concrete.Glasses;
import com.rpg.southparkavatars.character.clothing.concrete.Hand;
import com.rpg.southparkavatars.character.clothing.concrete.Hat;
import com.rpg.southparkavatars.character.clothing.concrete.Necklace;
import com.rpg.southparkavatars.character.clothing.concrete.Pants;
import com.rpg.southparkavatars.character.clothing.concrete.Shirt;
import com.rpg.southparkavatars.character.decorator.BackDecorator;
import com.rpg.southparkavatars.character.decorator.BeardDecorator;
import com.rpg.southparkavatars.character.decorator.EyesDecorator;
import com.rpg.southparkavatars.character.decorator.GlassesDecorator;
import com.rpg.southparkavatars.character.decorator.HairDecorator;
import com.rpg.southparkavatars.character.decorator.HandAccessoryDecorator;
import com.rpg.southparkavatars.character.decorator.HandDecorator;
import com.rpg.southparkavatars.character.decorator.HatDecorator;
import com.rpg.southparkavatars.character.decorator.HeadDecorator;
import com.rpg.southparkavatars.character.decorator.MouthDecorator;
import com.rpg.southparkavatars.character.decorator.NecklaceDecorator;
import com.rpg.southparkavatars.character.decorator.PantsDecorator;
import com.rpg.southparkavatars.character.decorator.ShirtDecorator;
import com.rpg.southparkavatars.character.decorator.SkinDecorator;
import com.rpg.southparkavatars.character.head.AbstractHeadFeature;
import com.rpg.southparkavatars.character.head.HeadFeature;
import com.rpg.southparkavatars.character.head.concrete.Beard;
import com.rpg.southparkavatars.character.head.concrete.Eyes;
import com.rpg.southparkavatars.character.head.concrete.Hair;
import com.rpg.southparkavatars.character.head.concrete.Head;
import com.rpg.southparkavatars.character.head.concrete.Mouth;
import com.rpg.southparkavatars.character.voice.VoiceState;
import com.rpg.southparkavatars.memento.Caretaker;
import com.rpg.southparkavatars.memento.Memento;
import com.rpg.southparkavatars.observer.CharacterObserver;
import com.rpg.southparkavatars.task.AsyncTaskFactory;
import com.rpg.southparkavatars.task.AsyncTaskListener;
import com.rpg.southparkavatars.tool.BitmapLoader;
import com.rpg.southparkavatars.tool.CharacterPersister;
import com.rpg.southparkavatars.tool.ItemPersister;
import com.rpg.southparkavatars.view.CharacterView;
import com.rpg.southparkavatars.visitor.ClothingCoolnessVisitor;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PlayActivity extends AppCompatActivity implements AsyncTaskListener, CharacterObserver {
    private AbstractCharacter character;
    private AsyncTaskFactory asyncTaskFactory;
    private BitmapLoader bitmapLoader;

    private CharacterView characterView;
    private LinearLayout itemListLayout;
    private LinearLayout tabButtonLayout;
    private EditText nameEditText;
    private MediaPlayer mediaPlayer;

    private Caretaker caretaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        createCharacter();

        caretaker = new Caretaker();

        bitmapLoader = new BitmapLoader(getAssets(), getFilesDir());
        asyncTaskFactory = new AsyncTaskFactory(this, getAssets(), getFilesDir());

        loadBackground();

        characterView = (CharacterView) findViewById(R.id.character_view);

        itemListLayout = (LinearLayout) findViewById(R.id.item_list_layout);
        tabButtonLayout = (LinearLayout) findViewById(R.id.tab_button_layout);

        nameEditText = (EditText) findViewById(R.id.name_edit_text);

        asyncTaskFactory.createClothingLoadingTask(Shirt.class)
                .execute();

        initButtons();
        loadPersistedCharacter();

        character.attach(this);
        characterView.draw(character);
    }

    private void loadBackground() {
        asyncTaskFactory.createLoadBackgroundsTask()
                .execute();
    }

    private void createCharacter() {
        character = new HatDecorator(
                new GlassesDecorator(
                        new HairDecorator(
                                new BeardDecorator(
                                        new EyesDecorator(
                                                new MouthDecorator(
                                                        new HandAccessoryDecorator(
                                                                new HandDecorator(
                                                                        new HeadDecorator(
                                                                                new NecklaceDecorator(
                                                                                        new ShirtDecorator(
                                                                                                new PantsDecorator(
                                                                                                        new SkinDecorator(
                                                                                                                new BackDecorator(
                                                                                                                        new Character()
                                                                                                                ))))))))))))));

    }

    private void loadPersistedCharacter() {
        Intent starterIntent = getIntent();
        String serialized;
        if (starterIntent != null && (serialized = starterIntent.getStringExtra("character")) != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Character savedCharacter = mapper.readValue(serialized, Character.class);
                nameEditText.setText(savedCharacter.getName());
                character.copy(savedCharacter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClothesAsyncTaskFinished(List<AbstractClothing> clothes) {
        itemListLayout.removeAllViews();
        fillItemListWithClothes(clothes);
    }

    @Override
    public void onHeadFeaturesAsyncTaskFinished(List<AbstractHeadFeature> headFeatures) {
        itemListLayout.removeAllViews();
        fillItemListWithHeadFeatures(headFeatures);
    }

    @Override
    public void onSkinAsyncTaskFinished(List<Skin> skins) {
        itemListLayout.removeAllViews();
        fillItemListWithSkinColors(skins);
    }

    @Override
    public void onBackgroundsLoaded(List<BitmapDrawable> bitmaps) {
        Random rand = new Random();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_play);
        int i = rand.nextInt(bitmaps.size());
        layout.setBackground(bitmaps.get(i));
    }

    private void fillItemListWithClothes(final List<AbstractClothing> clothes) {
        ImageButton button = new ImageButton(this);
        button.setBackgroundResource(R.drawable.remove);
        button.setLayoutParams(new LinearLayout.LayoutParams(145, 145));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                caretaker.addMemento(character.saveToMemento());
                character.removeClothingType(clothes.get(0).getClass());
            }
        });

        itemListLayout.addView(button);

        for (final AbstractClothing clothing : clothes) {
            ImageView imageView = new ImageView(this);
            Bitmap bitmap = bitmapLoader.load(clothing.getPath());

            imageView.setImageBitmap(bitmap);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    caretaker.addMemento(character.saveToMemento());
                    character.addClothing(clothing);
                }
            });

            itemListLayout.addView(imageView);
        }
    }

    private void fillItemListWithHeadFeatures(final List<AbstractHeadFeature> features) {
        Class<? extends AbstractHeadFeature> featureClass = features.get(0).getClass();
        if (featureClass == Beard.class || featureClass == Hair.class) {
            ImageButton button = new ImageButton(this);
            button.setBackgroundResource(R.drawable.remove);
            button.setLayoutParams(new LinearLayout.LayoutParams(145, 145));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    caretaker.addMemento(character.saveToMemento());
                    character.removeHeadFeatureType(features.get(0).getClass());
                }
            });

            itemListLayout.addView(button);
        }

        for (final AbstractHeadFeature feature : features) {
            ImageView imageView = new ImageView(this);
            Bitmap bitmap = bitmapLoader.load(feature.getPath());

            imageView.setImageBitmap(bitmap);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    caretaker.addMemento(character.saveToMemento());
                    character.addHeadFeature(feature);
                }
            });
            itemListLayout.addView(imageView);
        }
    }

    private void fillItemListWithSkinColors(List<Skin> skins) {
        for (final Skin skin : skins) {
            ImageView imageView = new ImageView(this);
            Bitmap bitmap = bitmapLoader.load(skin.getPath());
            imageView.setImageBitmap(bitmap);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopPlaying();

                    String colorName = skin.getColor().toString().toLowerCase();
                    selectCharacterVoice(colorName);

                    caretaker.addMemento(character.saveToMemento());
                    character.setSkinFeatures(
                            skin,
                            new Head(HeadFeature.HEAD.getPath() + File.separator + colorName + ".png"),
                            new com.rpg.southparkavatars.character.head.concrete.Hand(HeadFeature.HAND.getPath() + File.separator + colorName + ".png")
                    );
                }

                @NonNull
                private String selectCharacterVoice(String colorName) {
                    try {
                        Class<?> voiceClass = Class.forName("com.rpg.southparkavatars.character.voice." + StringUtils.capitalize(colorName) + "VoiceState");
                        character.setState((VoiceState) voiceClass.newInstance());

                        mediaPlayer = MediaPlayer.create(PlayActivity.this, character.handleVoice());
                        mediaPlayer.start();
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return colorName;
                }
            });

            itemListLayout.addView(imageView);
        }
    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void initButtons() {
        generateHeadFeatureButtons();
        generateClothingButtons();
    }

    private void generateClothingButtons() {
        List<Class<? extends AbstractClothing>> clothingClasses = Arrays.asList(Back.class, Glasses.class, Hand.class,
                Hat.class, Necklace.class, Pants.class, Shirt.class);
        for (final Class<? extends AbstractClothing> clothingClass : clothingClasses) {
            Button button = new Button(this);
            int resourceId = getResources().getIdentifier("button_" + clothingClass.getSimpleName().toLowerCase() + "_change", "drawable", getPackageName());
            button.setBackgroundResource(resourceId);
            button.setLayoutParams(new LinearLayout.LayoutParams(180, 180));
            button.setTextSize(0);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asyncTaskFactory.createClothingLoadingTask(clothingClass)
                            .execute();
                }
            });
            button.setText(clothingClass.getSimpleName());
            tabButtonLayout.addView(button);
        }
    }

    private void generateHeadFeatureButtons() {
        List<Class<? extends AbstractHeadFeature>> featureClasses = Arrays.asList(Beard.class, Eyes.class, Hair.class, Mouth.class);
        for (final Class<? extends AbstractHeadFeature> featureClass : featureClasses) {
            Button button = new Button(this);
            int resourceId = getResources().getIdentifier("button_" + featureClass.getSimpleName().toLowerCase() + "_change", "drawable", getPackageName());
            button.setBackgroundResource(resourceId);
            button.setLayoutParams(new LinearLayout.LayoutParams(180, 180));
            button.setTextSize(0);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asyncTaskFactory.createHeadFeaturesLoadingTask(featureClass)
                            .execute();
                }
            });
            button.setText(featureClass.getSimpleName());
            tabButtonLayout.addView(button);
        }
    }

    public void onSkinButtonClick(View view) {
        asyncTaskFactory.createSkinLoadingTask()
                .execute();
    }

    public void onSaveButtonClick(View view) {
        hideKeyboard(view);

        String name = String.valueOf(nameEditText.getText());
        if (StringUtils.isEmpty(name)) {
            Snackbar.make(findViewById(R.id.activity_play),
                    "ERROR: Name cannot be empty!", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        } else {
            Snackbar.make(findViewById(R.id.activity_play),
                    "Your character has been saved!", Snackbar.LENGTH_SHORT)
                    .show();
        }

        File file = new File(getFilesDir() + File.separator + "characters.json");
        ItemPersister<Character> persister = new CharacterPersister(file);

        character.setName(name);
        persister.save(character.saveable());
        characterView.saveAsPNG(character);
    }

    private void hideKeyboard(View view) {
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public void onUndoButtonClick(View view) {
        Memento memento = caretaker.getMemento();
        character.restoreFromMemento(memento);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void update() {
        characterView.draw(character);
    }
}
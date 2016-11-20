package com.rpg.southparkavatars.character.decorator;

import com.rpg.southparkavatars.character.AbstractCharacter;
import com.rpg.southparkavatars.character.Character;
import com.rpg.southparkavatars.character.DrawableItem;
import com.rpg.southparkavatars.character.Skin;
import com.rpg.southparkavatars.character.clothing.AbstractClothing;
import com.rpg.southparkavatars.character.head.AbstractHeadFeature;
import com.rpg.southparkavatars.character.head.concrete.Hand;
import com.rpg.southparkavatars.character.head.concrete.Head;
import com.rpg.southparkavatars.observer.ItemObserver;

import java.util.List;

public abstract class CharacterDecorator implements AbstractCharacter {
    protected AbstractCharacter character;
    protected Class<?> itemClass;

    public CharacterDecorator(AbstractCharacter character, Class<?> itemClass) {
        this.character = character;
        this.itemClass = itemClass;
    }

    public Character getRawCharacter() {
        if (character.getClass() == Character.class) {
            return (Character) character;
        } else {
            return ((CharacterDecorator) character).getRawCharacter();
        }
    }

    @Override
    public void addClothing(AbstractClothing clothing) {
        character.addClothing(clothing);
    }

    @Override
    public void addHeadFeature(AbstractHeadFeature headFeature) {
        character.addHeadFeature(headFeature);
    }

    @Override
    public List<AbstractClothing> getClothes() {
        return character.getClothes();
    }

    @Override
    public List<AbstractHeadFeature> getHeadFeatures() {
        return character.getHeadFeatures();
    }

    @Override
    public List<DrawableItem> display() {
        List<DrawableItem> list = character.display();
        DrawableItem item = character.find(itemClass);
        if (item != null) {
            list.add(item);
        }
        return list;
    }

    public DrawableItem find(Class<?> classToBeFound) {
        return character.find(classToBeFound);
    }

    @Override
    public void attach(ItemObserver observer) {
        character.attach(observer);
    }

    @Override
    public void notifyAllObservers() {
        character.notifyAllObservers();
    }

    @Override
    public Skin getSkin() {
        return character.getSkin();
    }

    @Override
    public void setName(String name) {
        character.setName(name);
    }

    @Override
    public String getUuid() {
        return character.getUuid();
    }

    @Override
    public String getName() {
        return character.getName();
    }

    @Override
    public void copy(Character character) {
        this.character.copy(character);
    }

    @Override
    public void setSkinFeatures(Skin skin, Head head, Hand hand) {
        character.setSkinFeatures(skin, head, hand);
    }

    @Override
    public Head getHead() {
        return character.getHead();
    }

    @Override
    public Hand getHand() {
        return character.getHand();
    }
}
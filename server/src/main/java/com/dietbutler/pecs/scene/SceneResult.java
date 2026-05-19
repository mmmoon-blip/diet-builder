package com.dietbutler.pecs.scene;

import java.util.HashSet;
import java.util.Set;

public class SceneResult {

    private Set<String> scenes = new HashSet<>();

    public SceneResult() {}

    public void addScene(String scene) {
        scenes.add(scene);
    }

    public void addScene(SceneType sceneType) {
        scenes.add(sceneType.name());
    }

    public boolean hasScene(String sceneType) {
        return scenes.contains(sceneType);
    }

    public boolean hasAnyScene(SceneType... sceneTypes) {
        for (SceneType sceneType : sceneTypes) {
            if (scenes.contains(sceneType.name())) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getScenes() {
        return scenes;
    }

    public void setScenes(Set<String> scenes) {
        this.scenes = scenes;
    }

    @Override
    public String toString() {
        return scenes.toString();
    }
}

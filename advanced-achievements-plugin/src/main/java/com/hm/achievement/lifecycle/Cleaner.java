package com.hm.achievement.lifecycle;

import jakarta.inject.Inject;
import java.util.Set;

public class Cleaner implements Runnable {

    private final Set<Cleanable> cleanables;

    @Inject
    public Cleaner(Set<Cleanable> cleanables) {
        this.cleanables = cleanables;
    }

    @Override
    public void run() {
        cleanables.forEach(Cleanable::cleanPlayerData);
    }

}

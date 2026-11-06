package com.wishtoday.ts.simpleminer.core.blockBreaker;

import com.wishtoday.simpleservices.services.annotation.Service;
import lombok.Getter;

//I don't know why it wasn't converted into an interface… even though I wrote it.
@Service
public class ExperienceCollector {
    @Getter
    private int experience;

    public ExperienceCollector() {
        this.experience = 0;
    }

    public void initialize() {
        this.experience = 0;
    }

    public void consumeExperience(int i) {
        this.experience += i;
    }
}

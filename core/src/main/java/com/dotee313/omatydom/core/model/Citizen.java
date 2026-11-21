package com.dotee313.omatydom.core.model;

import com.dotee313.omatydom.core.GameConfig;
import com.dotee313.omatydom.core.civ.Species;
import com.dotee313.omatydom.core.civ.StatusEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** A single inhabitant of the island. Pure data plus clamped setters. */
public final class Citizen {
    private final int id;
    private final String name;
    private final Gender gender;
    private final Race race;
    private Species species;
    private final Map<StatusEffect, Integer> statusEffects = new EnumMap<>(StatusEffect.class);

    private Religion religion;
    private Ideology ideology;
    private Job job;
    private SocialClass socialClass;
    private Trait trait;
    private Idea idea;
    private int ageDays;
    private double health;
    private double happiness;
    private double wealth;
    private double education;
    private double loyalty;
    private String occupation;
    private boolean alive;

    private boolean pregnant;
    private int pregnancyStartDay = -1;
    private int lastBirthDay = -1;

    public Citizen(int id, String name, Gender gender, Race race,
                   Religion religion, Ideology ideology, int ageDays) {
        this.id = id;
        this.name = name == null ? "Nameless" : name;
        this.gender = gender == null ? Gender.MALE : gender;
        this.race = race == null ? Race.VAELI : race;
        this.species = Species.HUMAN;
        this.religion = religion == null ? Religion.NONE : religion;
        this.ideology = ideology == null ? Ideology.APATHETIC : ideology;
        this.job = Job.LABORER;
        this.socialClass = SocialClass.LABORER;
        Trait[] traits = Trait.values();
        this.trait = traits[Math.floorMod(id * 2654435761L, traits.length)];
        this.idea = Idea.SURVIVAL;
        this.ageDays = Math.max(0, ageDays);
        this.health = 75;
        this.happiness = 55;
        this.wealth = 40;
        this.education = 40;
        this.loyalty = 60;
        this.occupation = "Worker";
        this.alive = true;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Gender getGender() {
        return gender;
    }

    public Race getRace() {
        return race;
    }

    /** The four civilized peoples used by kingdoms, clans and name generation. */
    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species == null ? Species.HUMAN : species;
    }

    public Religion getReligion() {
        return religion;
    }

    public void setReligion(Religion religion) {
        this.religion = religion == null ? Religion.NONE : religion;
    }

    public Ideology getIdeology() {
        return ideology;
    }

    public void setIdeology(Ideology ideology) {
        this.ideology = ideology == null ? Ideology.APATHETIC : ideology;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job == null ? Job.LABORER : job;
    }

    public SocialClass getSocialClass() {
        return socialClass;
    }

    public void setSocialClass(SocialClass socialClass) {
        this.socialClass = socialClass == null ? SocialClass.LABORER : socialClass;
    }

    public Trait getTrait() {
        return trait;
    }

    public void setTrait(Trait trait) {
        this.trait = trait == null ? Trait.CONTENT : trait;
    }

    public Idea getIdea() {
        return idea;
    }

    public void setIdea(Idea idea) {
        this.idea = idea == null ? Idea.SURVIVAL : idea;
    }

    /** Add or refresh a temporary condition; a longer duration wins. */
    public void addStatus(StatusEffect effect) {
        if (effect == null) {
            return;
        }
        int duration = effect.getDurationDays();
        Integer current = statusEffects.get(effect);
        if (current == null || current < duration) {
            statusEffects.put(effect, duration);
        }
    }

    public boolean hasStatus(StatusEffect effect) {
        return statusEffects.containsKey(effect);
    }

    public void removeStatus(StatusEffect effect) {
        statusEffects.remove(effect);
    }

    public int getStatusDays(StatusEffect effect) {
        Integer days = statusEffects.get(effect);
        return days == null ? 0 : days;
    }

    public List<StatusEffect> getActiveStatuses() {
        return Collections.unmodifiableList(new ArrayList<>(statusEffects.keySet()));
    }

    /** Age all conditions by one day and drop the expired ones. */
    public void tickStatuses() {
        List<StatusEffect> expired = new ArrayList<>();
        for (Map.Entry<StatusEffect, Integer> entry : statusEffects.entrySet()) {
            int left = entry.getValue() - 1;
            if (left <= 0) {
                expired.add(entry.getKey());
            } else {
                entry.setValue(left);
            }
        }
        for (StatusEffect effect : expired) {
            statusEffects.remove(effect);
        }
    }

    public int getAgeDays() {
        return ageDays;
    }

    public void setAgeDays(int ageDays) {
        this.ageDays = Math.max(0, ageDays);
    }

    public int getAgeYears() {
        return ageDays / GameConfig.DAYS_PER_YEAR;
    }

    public boolean isAdult() {
        return getAgeYears() >= 18;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = clamp(health);
    }

    public double getHappiness() {
        return happiness;
    }

    public void setHappiness(double happiness) {
        this.happiness = clamp(happiness);
    }

    public double getWealth() {
        return wealth;
    }

    public void setWealth(double wealth) {
        this.wealth = clamp(wealth);
    }

    public double getEducation() {
        return education;
    }

    public void setEducation(double education) {
        this.education = clamp(education);
    }

    /** Loyalty towards the current ruler. */
    public double getLoyalty() {
        return loyalty;
    }

    public void setLoyalty(double loyalty) {
        this.loyalty = clamp(loyalty);
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation == null ? "Worker" : occupation;
    }

    public boolean isAlive() {
        return alive;
    }

    /** True while this citizen is carrying a child. Only adult women can conceive. */
    public boolean isPregnant() {
        return pregnant;
    }

    public int getPregnancyStartDay() {
        return pregnancyStartDay;
    }

    public int getLastBirthDay() {
        return lastBirthDay;
    }

    /** One pregnancy at a time, with a full year of rest afterwards. */
    public boolean canConceive(int day) {
        return alive
                && gender == Gender.FEMALE
                && isAdult()
                && getAgeYears() < 45
                && !pregnant
                && (lastBirthDay < 0 || day - lastBirthDay >= GameConfig.MIN_BIRTH_SPACING_DAYS);
    }

    public void conceive(int day) {
        if (canConceive(day)) {
            pregnant = true;
            pregnancyStartDay = day;
        }
    }

    public boolean isDue(int day) {
        return pregnant && pregnancyStartDay >= 0
                && day - pregnancyStartDay >= GameConfig.GESTATION_DAYS;
    }

    public int getGestationDay(int day) {
        if (!pregnant || pregnancyStartDay < 0) {
            return 0;
        }
        return Math.min(GameConfig.GESTATION_DAYS, Math.max(0, day - pregnancyStartDay));
    }

    /** Complete a pregnancy and start the rest period. */
    public void deliver(int day) {
        pregnant = false;
        pregnancyStartDay = -1;
        lastBirthDay = day;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
        if (!alive) {
            pregnant = false;
            pregnancyStartDay = -1;
        }
    }

    private static double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 100) {
            return 100;
        }
        return value;
    }

    @Override
    public String toString() {
        return name + " (" + race.getDisplayName() + ", " + getAgeYears() + ")";
    }
}

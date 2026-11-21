package com.dotee313.omatydom.core.model;

/** A single inhabitant of the island. Pure data plus clamped setters. */
public final class Citizen {
    private final int id;
    private final String name;
    private final Gender gender;
    private final Race race;

    private Religion religion;
    private Ideology ideology;
    private Job job;
    private SocialClass socialClass;
    private Idea idea;
    private int ageDays;
    private double health;
    private double happiness;
    private double wealth;
    private double education;
    private double loyalty;
    private String occupation;
    private boolean alive;

    public Citizen(int id, String name, Gender gender, Race race,
                   Religion religion, Ideology ideology, int ageDays) {
        this.id = id;
        this.name = name == null ? "Nameless" : name;
        this.gender = gender == null ? Gender.MALE : gender;
        this.race = race == null ? Race.VAELI : race;
        this.religion = religion == null ? Religion.NONE : religion;
        this.ideology = ideology == null ? Ideology.APATHETIC : ideology;
        this.job = Job.LABORER;
        this.socialClass = SocialClass.LABORER;
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

    public Idea getIdea() {
        return idea;
    }

    public void setIdea(Idea idea) {
        this.idea = idea == null ? Idea.SURVIVAL : idea;
    }

    public int getAgeDays() {
        return ageDays;
    }

    public void setAgeDays(int ageDays) {
        this.ageDays = Math.max(0, ageDays);
    }

    public int getAgeYears() {
        return ageDays / 365;
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

    public void setAlive(boolean alive) {
        this.alive = alive;
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

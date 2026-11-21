package com.dotee313.omatydom.core.world;

import com.dotee313.omatydom.core.model.Citizen;
import com.dotee313.omatydom.core.model.Job;

/** A citizen's physical presence on the island: position, job and current goal. */
public final class Agent {
    private final Citizen citizen;
    private int x;
    private int y;
    private Job job;
    private int targetX = -1;
    private int targetY = -1;
    private int progress;
    private Animal targetAnimal;

    public Agent(Citizen citizen, int x, int y, Job job) {
        this.citizen = citizen;
        this.x = x;
        this.y = y;
        this.job = job;
    }

    public Citizen getCitizen() {
        return citizen;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job == null ? Job.LABORER : job;
        clearTarget();
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public void setTarget(int x, int y) {
        this.targetX = x;
        this.targetY = y;
    }

    public boolean hasTarget() {
        return targetX >= 0 && targetY >= 0;
    }

    public void clearTarget() {
        this.targetX = -1;
        this.targetY = -1;
        this.targetAnimal = null;
        this.progress = 0;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Animal getTargetAnimal() {
        return targetAnimal;
    }

    public void setTargetAnimal(Animal targetAnimal) {
        this.targetAnimal = targetAnimal;
    }
}

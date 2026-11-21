package com.dotee313.omatydom.core.model;

/** The person on the throne, plus the state of their reign. */
public final class Ruler {
    private final Citizen citizen;
    private final String title;
    private final int reignStartDay;
    private double approval;
    private int decreesIssued;

    public Ruler(Citizen citizen, String title, int reignStartDay) {
        this.citizen = citizen;
        this.title = title == null ? "Ruler" : title;
        this.reignStartDay = Math.max(1, reignStartDay);
        this.approval = 60;
        this.decreesIssued = 0;
    }

    public Citizen getCitizen() {
        return citizen;
    }

    public String getTitle() {
        return title;
    }

    public int getReignStartDay() {
        return reignStartDay;
    }

    public int getReignDays(int currentDay) {
        return Math.max(0, currentDay - reignStartDay);
    }

    public double getApproval() {
        return approval;
    }

    public void setApproval(double approval) {
        this.approval = Math.max(0, Math.min(100, approval));
    }

    public int getDecreesIssued() {
        return decreesIssued;
    }

    public void setDecreesIssued(int decreesIssued) {
        this.decreesIssued = Math.max(0, decreesIssued);
    }

    @Override
    public String toString() {
        return title + " " + citizen.getName();
    }
}

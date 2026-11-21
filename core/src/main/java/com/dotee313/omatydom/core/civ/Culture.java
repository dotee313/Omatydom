package com.dotee313.omatydom.core.civ;

import java.util.LinkedHashSet;
import java.util.Set;

/** A mutable culture that accumulates knowledge and discovered technologies. */
public class Culture {
    private int id;
    private String name;
    private double knowledge;
    private Set<String> techIds;

    public Culture(int id, String name) {
        this.id = id;
        this.name = name;
        this.knowledge = 0.0;
        this.techIds = new LinkedHashSet<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(double knowledge) {
        this.knowledge = knowledge;
    }

    /** Returns the live technology-id set; mutate it directly if desired. */
    public Set<String> getTechIds() {
        return techIds;
    }

    public void setTechIds(Set<String> techIds) {
        this.techIds = techIds == null ? new LinkedHashSet<>() : techIds;
    }

    public void addTech(String techId) {
        if (techId != null) {
            techIds.add(techId);
        }
    }

    public boolean knowsTech(String techId) {
        return techId != null && techIds.contains(techId);
    }
}

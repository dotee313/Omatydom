package com.dotee313.omatydom.core.civ;

import java.util.ArrayList;
import java.util.List;

/** A mutable clan within a kingdom. */
public class Clan {
    private int id;
    private String name;
    private int kingdomId;
    private int chiefId;
    private int influence;
    private List<Integer> memberIds;

    public Clan(int id, String name, int kingdomId, int chiefId) {
        this.id = id;
        this.name = name;
        this.kingdomId = kingdomId;
        this.chiefId = chiefId;
        this.influence = 0;
        this.memberIds = new ArrayList<>();
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

    public int getKingdomId() {
        return kingdomId;
    }

    public void setKingdomId(int kingdomId) {
        this.kingdomId = kingdomId;
    }

    public int getChiefId() {
        return chiefId;
    }

    public void setChiefId(int chiefId) {
        this.chiefId = chiefId;
    }

    public int getInfluence() {
        return influence;
    }

    public void setInfluence(int influence) {
        this.influence = influence;
    }

    /** Returns the live member list; mutate it directly if desired. */
    public List<Integer> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Integer> memberIds) {
        this.memberIds = memberIds == null ? new ArrayList<>() : memberIds;
    }

    public void addMember(int memberId) {
        memberIds.add(memberId);
    }

    public void removeMember(int memberId) {
        memberIds.remove(Integer.valueOf(memberId));
    }
}

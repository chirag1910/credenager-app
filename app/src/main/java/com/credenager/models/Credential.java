package com.credenager.models;


public class Credential {
    private final String credId, groupId;
    private String identifier, value;

    public Credential(String credId, String identifier, String value, String groupId) {
        this.credId = credId;
        this.identifier = identifier;
        this.value = value;
        this.groupId = groupId;
    }

    public String getCredId() {
        return credId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public String toString() {
        return "Credential{" +
                "credId='" + credId + '\'' +
                ", identifier='" + identifier + '\'' +
                ", value='" + value + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}

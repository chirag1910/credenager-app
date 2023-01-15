package com.credenager.utils;

import com.credenager.models.Credential;
import com.credenager.models.Group;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Data {
    public static String dataString = null;
    public static HashMap<String, List<Credential>> cachedCred = new HashMap<>();
    public static List<Group> groups;
    public static List<Credential> credentials;

    public static void set(String data) throws Exception {
        groups = new ArrayList<>();
        credentials = new ArrayList<>();

        try {
            JSONArray groups = new JSONArray(data);
            for (int i = 0; i < groups.length() ; i++){
                JSONObject grpObj = groups.getJSONObject(i);

                String groupId = grpObj.getString("_id");
                String groupName = grpObj.getString("name");
                groupId = groupId.equals("null") ? null : groupId;
                groupName = groupName.equals("null") ? null : groupName;

                if (groupId != null)
                    addGroup(groupId, groupName);

                JSONArray credentials = grpObj.getJSONArray("credentials");
                for (int j = 0; j < credentials.length(); j++) {
                    JSONObject credObj = credentials.getJSONObject(j);

                    String credId = credObj.getString("_id");
                    String credIdentifier = credObj.getString("identifier");
                    String credValue = credObj.getString("value");

                    addCred(credId, credIdentifier, Crypt.decrypt(credValue, Session.USER_KEY), groupId);
                }
            }
            dataString = data;
            cachedCred = new HashMap<>();
        }
        catch (Exception e) {
            throw new Exception();
        }
    }

    public static void addGroup(String id, String name){
        Data.groups.add(new Group(id, name));
    }

    public static void updateGroup(String id, String name) {
        Group group = getGroupById(id);
        if (group != null)
            group.setName(name);
    }

    public static void deleteGroup(String id){
        Group group = getGroupById(id);
        if (group != null)
            Data.groups.remove(group);
    }

    public static void addCred(String id, String identifier, String value, String groupId){
        Data.credentials.add(new Credential(id, identifier, value, groupId));
    }


    public static void updateCred(String id, String identifier, String value) {
        Credential credential = getCredentialById(id);
        if (credential != null) {
            credential.setIdentifier(identifier);
            credential.setValue(value);
        }
    }

    public static void deleteCred(String id){
        Credential credential = getCredentialById(id);
        if (credential != null)
            Data.credentials.remove(credential);

    }

    public static Credential getCredentialById(String id) {
        for (Credential credential : Data.credentials) {
            if (credential.getCredId().equals(id))
                return credential;
        }
        return null;
    }
    public static Group getGroupById (String id){
        for (Group group : Data.groups) {
            if (group.getId().equals(id))
                return group;
        }
        return null;
    }
    public static List<Credential> getCredentialsByGroupId(String groupId) {
        List<Credential> credentials = new ArrayList<>();
        for (Credential credential : Data.credentials)
            if (credential.getGroupId() == (groupId))
                credentials.add(credential);

        return credentials;
    }

    public static List<Credential> searchCredentials(String keyword){
        List<Credential> credentials = new ArrayList<>();
        if (!keyword.isEmpty()){
            for (Credential credential : Data.credentials)
                if (credential.getIdentifier().toLowerCase().contains(keyword.toLowerCase()) || credential.getValue().toLowerCase().contains(keyword.toLowerCase()))
                    credentials.add(credential);
        }
        return credentials;
    }

    public static int getCredentialPosition(String groupId, String credId) {
        int i = 0;
        for (Credential credential : getCredentialsByGroupId(groupId)) {
            if (credential.getCredId().equals(credId)){
                return i;
            }
            i++;
        }
        return -1;
    }
}

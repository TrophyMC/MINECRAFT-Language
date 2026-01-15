package de.mecrytv.language.model;

import com.google.gson.JsonObject;
import de.mecrytv.model.ICacheModel;

public class LanguageModel implements ICacheModel {

    private String playerUUID;
    private String languageCode;
    private boolean firstJoin;

    public LanguageModel() {}

    public LanguageModel(String playerUUID, String languageCode) {
        this.playerUUID = playerUUID;
        this.languageCode = languageCode;
        this.firstJoin = true;
    }

    @Override
    public String getIdentifier() {
        return playerUUID;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("playerUUID", playerUUID);
        json.addProperty("languageCode", languageCode);
        json.addProperty("firstJoin", firstJoin);
        return json;
    }

    @Override
    public void deserialize(JsonObject json) {
        this.playerUUID = json.get("playerUUID").getAsString();
        this.languageCode = json.get("languageCode").getAsString();
        this.firstJoin = json.get("firstJoin").getAsBoolean();
    }

    public String getPlayerUUID() {
        return playerUUID;
    }
    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }
    public String getLanguageCode() {
        return languageCode;
    }
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
    public boolean isFirstJoin() {
        return firstJoin;
    }
    public void setFirstJoin(boolean firstJoin) {
        this.firstJoin = firstJoin;
    }
}

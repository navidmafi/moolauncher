package com.navidmafi.moolauncher.minecraft.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navidmafi.moolauncher.AppConstants;
import com.navidmafi.moolauncher.minecraft.Networking;
import com.navidmafi.moolauncher.minecraft.exceptions.VersionNotFoundException;

import java.io.IOException;
import java.util.Iterator;

public class VersionApi {

    public static String getVersionJson(String version) throws IOException, InterruptedException, VersionNotFoundException {
        ObjectMapper objectMapper = new ObjectMapper();
        String manifestJson = Networking.httpGetAsString(AppConstants.PISTON_URL);
        JsonNode manifestNode = objectMapper.readTree(manifestJson);

        Iterator<JsonNode> versionsIter = manifestNode.get("versions").elements();
        String versionUrl = null;
        while (versionsIter.hasNext()) {
            JsonNode v = versionsIter.next();
            if (version.equals(v.get("id").asText())) {
                versionUrl = v.get("url").asText();
                break;
            }
        }
        if (versionUrl == null) throw new VersionNotFoundException();
        return Networking.httpGetAsString(versionUrl);
    }
}

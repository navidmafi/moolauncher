package com.navidmafi.moolauncher.minecraft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navidmafi.moolauncher.minecraft.exceptions.VersionNotFoundException;
import com.navidmafi.moolauncher.AppConstants;

import java.io.IOException;
import java.util.Iterator;

public class MojangAPI {
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
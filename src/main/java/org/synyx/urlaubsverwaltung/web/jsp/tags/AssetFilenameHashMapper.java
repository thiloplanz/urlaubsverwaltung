package org.synyx.urlaubsverwaltung.web.jsp.tags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

class AssetFilenameHashMapper {

    private static final String ASSETS_MANIFEST_FILE = "WEB-INF/assets-manifest.json";

    private final ResourceLoader resourceLoader;

    AssetFilenameHashMapper(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    String getHashedAssetFilename(String assetNameWithoutHash) {

        InputStream manifestFileStream = getManifestFile();
        HashMap<String, String> assets = getAssets(manifestFileStream);

        if (!assets.containsKey(assetNameWithoutHash)) {
            throw new IllegalStateException(String.format("could not resolve given asset name=%s", assetNameWithoutHash));
        }

        return assets.get(assetNameWithoutHash);

    }

    private HashMap<String, String> getAssets(InputStream manifest) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(manifest, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("could not parse manifest json file");
        }
    }

    private InputStream getManifestFile() {
        try {
            return resourceLoader.getResource(ASSETS_MANIFEST_FILE).getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException(String.format("could not read %s", ASSETS_MANIFEST_FILE));
        }
    }
}

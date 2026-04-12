package com.edams.version.service;

import com.edams.version.entity.Version;
import com.edams.version.entity.VersionDiff;
import java.util.List;

public interface VersionService {
    Version createVersion(Version version);
    List<Version> listVersionsByAsset(String assetId, int offset, int size);
    Version getById(Long id);
    Version getLatestVersion(String assetId);
    void rollback(Long versionId);
    VersionDiff compareVersions(Long fromId, Long toId);
    void deleteVersion(Long id);
}

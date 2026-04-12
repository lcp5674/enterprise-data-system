package com.edams.version.service.impl;

import com.edams.version.entity.Version;
import com.edams.version.entity.VersionDiff;
import com.edams.version.repository.VersionMapper;
import com.edams.version.service.VersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    private final VersionMapper versionMapper;

    @Override
    public Version createVersion(Version version) {
        Version latest = versionMapper.findLatestByAsset(version.getAssetId());
        String newVersionNo = calculateNextVersion(latest != null ? latest.getVersionNo() : null);
        version.setVersionNo(newVersionNo);
        version.setStatus("ACTIVE");
        version.setCreateTime(LocalDateTime.now());
        versionMapper.insert(version);
        log.info("Version created: assetId={}, versionNo={}", version.getAssetId(), version.getVersionNo());
        return version;
    }

    @Override
    public List<Version> listVersionsByAsset(String assetId, int offset, int size) {
        return versionMapper.findByAssetId(assetId, offset, size);
    }

    @Override
    public Version getById(Long id) {
        Version version = versionMapper.findById(id);
        if (version == null) throw new RuntimeException("版本不存在: " + id);
        return version;
    }

    @Override
    public Version getLatestVersion(String assetId) {
        return versionMapper.findLatestByAsset(assetId);
    }

    @Override
    public void rollback(Long versionId) {
        Version version = getById(versionId);
        // 将当前最新版本标记为历史，把目标版本标记为current
        versionMapper.markAllHistorical(version.getAssetId());
        versionMapper.markAsCurrent(versionId);
        log.info("Version rollback: assetId={}, versionId={}", version.getAssetId(), versionId);
    }

    @Override
    public VersionDiff compareVersions(Long fromId, Long toId) {
        Version from = getById(fromId);
        Version to = getById(toId);
        VersionDiff diff = new VersionDiff();
        diff.setFromVersionId(fromId);
        diff.setToVersionId(toId);
        diff.setFromVersionNo(from.getVersionNo());
        diff.setToVersionNo(to.getVersionNo());
        diff.setDiffContent("版本差异: " + from.getVersionNo() + " -> " + to.getVersionNo());
        diff.setCreateTime(LocalDateTime.now());
        return diff;
    }

    @Override
    public void deleteVersion(Long id) {
        versionMapper.deleteById(id);
    }

    private String calculateNextVersion(String currentVersion) {
        if (currentVersion == null) return "1.0.0";
        String[] parts = currentVersion.split("\\.");
        int patch = Integer.parseInt(parts[2]) + 1;
        return parts[0] + "." + parts[1] + "." + patch;
    }
}

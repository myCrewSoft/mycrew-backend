package com.mycrewsoft.security.authz;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

/**
 * Scope collection for one PermissionCode.
 *
 * This object is useful for list queries. Services can ask AuthorizationService
 * for the current user's scopes, then pass global/dept/project flags into Mapper
 * XML so the database filters visible rows efficiently.
 */
@Getter
public class PermissionScopeSet {

    private final boolean global;
    private final Map<ScopeType, Set<String>> scopeIdsByType;

    private PermissionScopeSet(boolean global, Map<ScopeType, Set<String>> scopeIdsByType) {
        this.global = global;
        this.scopeIdsByType = scopeIdsByType;
    }

    public static PermissionScopeSet empty() {
        return new PermissionScopeSet(false, Map.of());
    }

    public static PermissionScopeSet from(
            Collection<ScopedPermission> scopedPermissions,
            PermissionCode permissionCode) {
        if (scopedPermissions == null || permissionCode == null) {
            return empty();
        }

        boolean hasGlobal = false;
        Map<ScopeType, Set<String>> collected = new EnumMap<>(ScopeType.class);

        for (ScopedPermission permission : scopedPermissions) {
            if (permission == null || !permissionCode.getCode().equals(permission.getPermCd())) {
                continue;
            }

            ScopeType scopeType = permission.getScopeType();
            if (scopeType == null) {
                continue;
            }

            if (scopeType == ScopeType.GLOBAL) {
                hasGlobal = true;
                continue;
            }

            String scopeId = normalize(permission.getScopeId());
            if (scopeId != null) {
                collected.computeIfAbsent(scopeType, ignored -> new LinkedHashSet<>()).add(scopeId);
            }
        }

        Map<ScopeType, Set<String>> immutable = new EnumMap<>(ScopeType.class);
        collected.forEach((scopeType, scopeIds) ->
                immutable.put(scopeType, Collections.unmodifiableSet(scopeIds)));

        return new PermissionScopeSet(hasGlobal, Collections.unmodifiableMap(immutable));
    }

    public boolean hasGlobal() {
        return global;
    }

    public boolean hasScope(ScopeType scopeType) {
        return !getScopeIds(scopeType).isEmpty();
    }

    public Set<String> getScopeIds(ScopeType scopeType) {
        if (scopeType == null) {
            return Set.of();
        }
        return scopeIdsByType.getOrDefault(scopeType, Set.of());
    }

    public Set<String> getDepartmentScopeIds() {
        return getScopeIds(ScopeType.DEPT);
    }

    public Set<String> getProjectScopeIds() {
        return getScopeIds(ScopeType.PROJECT);
    }

    public Set<String> getTaskScopeIds() {
        return getScopeIds(ScopeType.TASK);
    }

    public Set<String> getSelfScopeIds() {
        return getScopeIds(ScopeType.SELF);
    }

    public boolean isEmpty() {
        return !global && scopeIdsByType.values().stream().allMatch(Set::isEmpty);
    }

    private static String normalize(String scopeId) {
        if (scopeId == null || scopeId.isBlank()) {
            return null;
        }
        return scopeId.trim();
    }
}

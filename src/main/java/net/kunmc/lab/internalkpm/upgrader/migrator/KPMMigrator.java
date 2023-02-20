package net.kunmc.lab.internalkpm.upgrader.migrator;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.kunmc.lab.internalkpm.upgrader.migrator.migrators.OldDBWiper;
import net.kunmc.lab.internalkpm.upgrader.migrator.migrators.TokenMigrator;
import org.kunlab.kpm.interfaces.KPMRegistry;
import net.kunmc.lab.internalkpm.upgrader.migrator.migrators.V2ConfigMigrator;
import org.kunlab.kpm.versioning.Version;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class KPMMigrator
{
    @Getter
    @Unmodifiable
    private static final List<KPMMigrateAction> MIGRATE_ACTIONS;

    static
    {
        MIGRATE_ACTIONS = new ArrayList<>();
        MIGRATE_ACTIONS.add(new OldDBWiper());
        MIGRATE_ACTIONS.add(new TokenMigrator());
        MIGRATE_ACTIONS.add(new V2ConfigMigrator());
    }

    public static void doMigrate(@NotNull KPMRegistry daemon, @NotNull Path kpmDataFolder,
                                 @NotNull Version fromVersion, @NotNull Version toVersion)
    {
        for (KPMMigrateAction action : MIGRATE_ACTIONS)
        {
            if (isMigrateNeeded(action, fromVersion, toVersion))
            {
                daemon.getLogger().info("必要な移行処理を実行します: " + action.getClass().getSimpleName());
                action.migrate(daemon, kpmDataFolder);
            }
        }
    }

    private static boolean isMigrateNeeded(@NotNull KPMMigrateAction action,
                                           @NotNull Version currentVersion, @NotNull Version targetVersion)
    {
        String needMigrateVersionRange = action.getNeedMigrateVersionRange();
        String needMigrateNewVersionRange = action.getNeedMigrateNewVersionRange();

        if (needMigrateVersionRange != null)
        {
            Pair<Version, Version> range = parseVersionRange(needMigrateVersionRange);
            if (!isInVersionRange(currentVersion, range.getLeft(), range.getRight()))
                return false;
        }

        if (needMigrateNewVersionRange != null)
        {
            Pair<Version, Version> range = parseVersionRange(needMigrateNewVersionRange);
            return isInVersionRange(targetVersion, range.getLeft(), range.getRight());
        }

        return true;
    }

    private static Pair<Version, Version> parseVersionRange(String rangeString)
    {
        final String separator = "\\.\\.\\.";
        String[] range = rangeString.split(separator);

        if (range.length > 2)
            throw new IllegalArgumentException("Invalid version range: " + rangeString);

        String lowerBound = StringUtils.isEmpty(range[0]) ? "" : range[0];
        String upperBound = range.length == 2 ? range[1]: "";
        if (lowerBound.isEmpty() && upperBound.isEmpty())
            throw new IllegalArgumentException("Invalid version range: " + rangeString);

        Version defFrom = Version.ofNullable(lowerBound);
        Version defTo = Version.ofNullable(upperBound);

        if (defFrom == null && defTo == null)
            throw new IllegalArgumentException("Invalid version range: " + rangeString);

        return new Pair<>(defFrom, defTo);
    }

    private static boolean isInVersionRange(@NotNull Version target, @Nullable Version from, @Nullable Version to)
    {
        assert !(from == null && to == null);

        if (from != null && to != null)
            return target.isNewerThanOrEqualTo(from) && target.isOlderThanOrEqualTo(to);
        else if (from != null)
            return target.isNewerThanOrEqualTo(from);
        else
            return target.isOlderThanOrEqualTo(to);
    }
}

package net.kunmc.lab.internalkpm.upgrader.migrator;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.kunmc.lab.internalkpm.upgrader.migrator.migrators.OldDBWiper;
import net.kunmc.lab.kpm.interfaces.KPMRegistry;
import net.kunmc.lab.internalkpm.upgrader.migrator.migrators.V2ConfigMigrator;
import net.kunmc.lab.kpm.versioning.Version;
import org.jetbrains.annotations.NotNull;
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
        MIGRATE_ACTIONS.add(new V2ConfigMigrator());
    }

    public static void doMigrate(@NotNull KPMRegistry daemon, @NotNull Path kpmDataFolder,
                                 @NotNull Version fromVersion, @NotNull Version toVersion)
    {
        for (KPMMigrateAction action : MIGRATE_ACTIONS)
        {
            if (action.isMigrateNeeded(fromVersion, toVersion))
            {
                daemon.getLogger().info("必要な移行処理を実行します: " + action.getClass().getSimpleName());
                action.migrate(daemon, kpmDataFolder);
            }
        }
    }
}

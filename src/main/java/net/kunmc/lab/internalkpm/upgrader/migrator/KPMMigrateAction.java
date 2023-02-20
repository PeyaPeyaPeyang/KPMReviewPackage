package net.kunmc.lab.internalkpm.upgrader.migrator;

import org.kunlab.kpm.interfaces.KPMRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public interface KPMMigrateAction
{
    void migrate(@NotNull KPMRegistry daemon, @NotNull Path kpmDataFolder);

    @Nullable
    default String getNeedMigrateVersionRange() { return null;}

    @Nullable
    default String getNeedMigrateNewVersionRange()
    {
        return null;
    }
}

package net.kunmc.lab.internalkpm.upgrader.migrator.migrators;

import net.kunmc.lab.internalkpm.upgrader.migrator.KPMMigrateAction;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class OldDBWiper implements KPMMigrateAction
{
    private static Path normalizePath(Path base, String path)
    {
        Path result;
        if (Files.exists(result = base.resolve(path)))
            return result;
        if (path.startsWith("/"))
            result = base.resolve(path.substring(1));
        return result;
    }

    @Override
    public void migrate(@NotNull KPMRegistry daemon, @NotNull Path kpmDataFolder)
    {
        daemon.getLogger().info("Wiping old database files...");

        Path configPath = kpmDataFolder.resolve("config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configPath.toFile());

        Path dependPath = normalizePath(kpmDataFolder, config.getString("dependPath", "depend.db"));
        Path resolvePath = normalizePath(kpmDataFolder, config.getString("resolvePath", "resolve.db"));
        Path databaseDir = kpmDataFolder.resolve("database");

        try
        {
            Files.deleteIfExists(dependPath);
            Files.deleteIfExists(resolvePath);

            if (Files.exists(databaseDir) && Files.isDirectory(databaseDir))
            {
                try (Stream<Path> files = Files.walk(databaseDir))
                {
                    files.filter(Files::isRegularFile)
                            .forEach(path -> {
                                try
                                {
                                    Files.delete(path);
                                }
                                catch (IOException e)
                                {
                                    throw new UncheckedIOException(e);
                                }
                            });
                }

                Files.delete(databaseDir);
            }
        }
        catch (IOException e)
        {
            daemon.getLogger().warning("Failed to delete old database files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getNeedMigrateVersionRange()
    {
        return "...v2.9.9";
    }
}

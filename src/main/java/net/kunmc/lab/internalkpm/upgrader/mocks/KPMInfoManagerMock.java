package net.kunmc.lab.internalkpm.upgrader.mocks;

import org.kunlab.kpm.interfaces.kpminfo.KPMInfoManager;
import org.kunlab.kpm.kpminfo.KPMInformationFile;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class KPMInfoManagerMock implements KPMInfoManager
{
    @Override
    public @Nullable KPMInformationFile loadInfo(@NotNull Path path, @NotNull PluginDescriptionFile descriptionFile)
    {
        return null;
    }

    @Override
    public @Nullable KPMInformationFile getInfo(@NotNull Plugin plugin)
    {
        return null;
    }

    @Override
    public @Nullable KPMInformationFile getInfo(@NotNull String pluginName)
    {
        return null;
    }

    @Override
    public @Nullable KPMInformationFile getOrLoadInfo(@NotNull Plugin plugin)
    {
        return null;
    }

    @Override
    public boolean hasInfo(@NotNull Plugin plugin)
    {
        return false;
    }

    @Override
    public boolean hasInfo(@NotNull Path pluginFile)
    {
        return false;
    }

    @Override
    public void removeInfo(@NotNull Plugin plugin)
    {

    }
}

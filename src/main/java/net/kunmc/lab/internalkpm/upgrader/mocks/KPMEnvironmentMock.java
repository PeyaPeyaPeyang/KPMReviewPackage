package net.kunmc.lab.internalkpm.upgrader.mocks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kunlab.kpm.interfaces.KPMEnvironment;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@AllArgsConstructor
@Getter
public class KPMEnvironmentMock implements KPMEnvironment
{
    private final Plugin currentKPM;
    private final Path tokenPath;
    private final Path tokenKeyPath;

    @Override
    public Plugin getPlugin()
    {
        return this.currentKPM;
    }

    @Override
    public Path getDataDirPath()
    {
        return this.currentKPM.getDataFolder().toPath();
    }

    @Override
    public Logger getLogger()
    {
        return Logger.getLogger("KPMUpgrader");
    }

    @Override
    public Path getMetadataDBPath()
    {
        return null;
    }

    @Override
    public Path getAliasesDBPath()
    {
        return null;
    }

    @Override
    public List<String> getOrganizations()
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> getExcludes()
    {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getSources()
    {
        return Collections.emptyMap();
    }

    @Override
    public String getHTTPUserAgent()
    {
        return null;
    }

    @Override
    public int getHTTPTimeout()
    {
        return 0;
    }

    @Override
    public int getHTTPMaxRedirects()
    {
        return 0;
    }
}

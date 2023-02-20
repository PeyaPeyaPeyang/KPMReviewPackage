package net.kunmc.lab.internalkpm.upgrader;

import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.interfaces.resolver.result.ErrorResult;
import org.kunlab.kpm.interfaces.resolver.result.MultiResult;
import org.kunlab.kpm.interfaces.resolver.result.ResolveResult;
import org.kunlab.kpm.interfaces.resolver.result.SuccessResult;
import org.kunlab.kpm.versioning.Version;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LegacySupport
{
    private static final String KPM_REPO = "https://github.com/TeamKUN/TeamKUNPluginManager";
    private static final Path OLD_KPM_TOKEN = Paths.get("kpm.vault");

    public static boolean isLegacyMajor(Plugin oldKPM)
    {
        return Version.of(oldKPM.getDescription().getVersion())
                .isOlderThan(Version.of("2.8.9")); // This version doesn't exist(for v3-pre support)
    }

    @Nullable
    public static String retrieveLegacyToken()
    {
        if (!Files.exists(OLD_KPM_TOKEN))
            return null;

        try(BufferedReader reader = Files.newBufferedReader(OLD_KPM_TOKEN))
        {
            return reader.readLine();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Nullable
    public static String fetchLatestVersion(KPMRegistry reg)
    {
        ResolveResult resolve = reg.getPluginResolver().resolve(KPM_REPO);
        if (resolve instanceof ErrorResult)
            return null;
        else if (resolve instanceof MultiResult)
            resolve = resolve.getResolver().autoPickOnePlugin((MultiResult) resolve);

        assert resolve instanceof SuccessResult;

        return ((SuccessResult) resolve).getVersion();
    }
}

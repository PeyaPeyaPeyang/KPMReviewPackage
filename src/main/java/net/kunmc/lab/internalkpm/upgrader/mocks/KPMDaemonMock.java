package net.kunmc.lab.internalkpm.upgrader.mocks;

import lombok.Getter;
import net.kunmc.lab.internalkpm.upgrader.LegacySupport;
import org.kunlab.kpm.TokenStore;
import org.kunlab.kpm.http.Requests;
import org.kunlab.kpm.interfaces.KPMEnvironment;
import org.kunlab.kpm.interfaces.KPMRegistry;
import org.kunlab.kpm.interfaces.alias.AliasProvider;
import org.kunlab.kpm.interfaces.hook.HookExecutor;
import org.kunlab.kpm.interfaces.installer.InstallManager;
import org.kunlab.kpm.interfaces.installer.loader.PluginLoader;
import org.kunlab.kpm.interfaces.kpminfo.KPMInfoManager;
import org.kunlab.kpm.interfaces.meta.PluginMetaManager;
import org.kunlab.kpm.interfaces.resolver.PluginResolver;
import org.kunlab.kpm.resolver.PluginResolverImpl;
import org.kunlab.kpm.resolver.impl.github.GitHubURLResolver;
import org.kunlab.kpm.task.PluginLoaderImpl;
import org.kunlab.kpm.utils.ServerConditionChecker;
import org.kunlab.kpm.versioning.Version;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Logger;

@Getter
public class KPMDaemonMock implements KPMRegistry
{
    private final KPMEnvironment environment;
    private final Logger logger;
    private final PluginResolver pluginResolver;
    private final TokenStore tokenStore;
    private final PluginLoader loader;

    public KPMDaemonMock(@NotNull KPMEnvironment env)
    {
        this.environment = env;

        this.logger = env.getLogger();
        this.pluginResolver = new PluginResolverImpl();
        this.tokenStore = new TokenStore(env.getTokenPath(), env.getTokenKeyPath());
        this.loader = new PluginLoaderImpl(this);
        this.init();
    }

    private void init()
    {
        this.getPluginResolver().addResolver(new GitHubURLResolver(), "$");

        Requests.getExtraHeaders().put(
                "User-Agent",
                "TeamKUNPluginManager+Upgrader/" + this.getVersion()
        );
        Requests.setTokenStore(this.getTokenStore());

        try
        {
            if (!this.getTokenStore().loadToken())
            {
                String tokenEnv = System.getenv("TOKEN");

                if (tokenEnv == null || tokenEnv.isEmpty())
                {
                    String oldToken;
                    if ((oldToken = LegacySupport.retrieveLegacyToken()) != null)
                        this.getTokenStore().storeToken(oldToken, false);
                    else
                        throw new IllegalStateException("Token is not set.");
                }

                this.getTokenStore().fromEnv();
            }
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Version getVersion()
    {
        return Version.of("0.0.0");
    }

    @Override
    public KPMEnvironment getEnvironment()
    {
        return this.environment;
    }

    @Override
    public AliasProvider getAliasProvider()
    {
        return null;
    }

    @Override
    public PluginMetaManager getPluginMetaManager()
    {
        return new PluginMetaManagerMock();
    }

    @Override
    public KPMInfoManager getKpmInfoManager()
    {
        return new KPMInfoManagerMock();
    }

    @Override
    public HookExecutor getHookExecutor()
    {
        return new HookExecutorMock(this);
    }

    @Override
    public InstallManager getInstallManager()
    {
        return null;
    }

    @Override
    public PluginLoader getPluginLoader()
    {
        return this.loader;
    }

    @Override
    public ServerConditionChecker getServerConditionChecker()
    {
        return null;
    }

    @Override
    public void shutdown()
    {

    }
}

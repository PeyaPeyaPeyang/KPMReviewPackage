package net.kunmc.lab.internalkpm.upgrader;

import net.kunmc.lab.internalkpm.upgrader.migrator.KPMMigrator;
import net.kunmc.lab.internalkpm.upgrader.mocks.KPMDaemonMock;
import net.kunmc.lab.internalkpm.upgrader.mocks.KPMEnvironmentMock;
import net.kunmc.lab.kpm.installer.impls.install.InstallArgument;
import net.kunmc.lab.kpm.installer.impls.install.InstallTasks;
import net.kunmc.lab.kpm.installer.impls.install.PluginInstaller;
import net.kunmc.lab.kpm.installer.impls.uninstall.PluginUninstaller;
import net.kunmc.lab.kpm.installer.impls.uninstall.UnInstallTasks;
import net.kunmc.lab.kpm.installer.impls.uninstall.UninstallArgument;
import net.kunmc.lab.kpm.interfaces.KPMRegistry;
import net.kunmc.lab.kpm.interfaces.installer.InstallResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ErrorResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.ResolveResult;
import net.kunmc.lab.kpm.interfaces.resolver.result.SuccessResult;
import net.kunmc.lab.kpm.signal.SignalHandleManager;
import net.kunmc.lab.kpm.versioning.Version;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class UpgradeImpl
{
    private static final String KPM_OWNER = "TeamKUN";
    private static final String KPM_NAME = "TeamKunPluginManager";

    private final KPMReviewPackage plugin;
    private final Logger logger;
    private Plugin currentKPM;
    private final Version currentKPMVersion;

    private KPMRegistry registry;

    public UpgradeImpl(KPMReviewPackage plugin)
    {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        if ((this.currentKPM = plugin.getServer().getPluginManager().getPlugin(KPM_NAME)) == null)
        {
            this.logger.severe("KPM がこのサーバーにインストールされていません。");
            this.logger.info("KPMUpgrader は、 KPM による自動インストールでのみ使用できます。");
            this.destructSelf();
            this.currentKPMVersion = null;
            this.registry = null;
            return;
        }

        if ((this.currentKPMVersion = Version.ofNullable(this.currentKPM.getDescription().getVersion())) == null)
        {
            this.logger.severe("KPM のバージョンの取得に失敗しました。");
            this.destructSelf();
        }
    }

    public void initDaemon()
    {
        Path kpmDataFolder = this.currentKPM.getDataFolder().toPath();
        this.registry = new KPMDaemonMock(
                new KPMEnvironmentMock(
                        this.currentKPM,
                        kpmDataFolder.resolve("token.dat"),
                        kpmDataFolder.resolve("token_key.dat")
                )
        );

        this.runUpgrade("v3.0.0-pre9");
    }

    private void destructSelf(boolean showMessage)
    {
        if (showMessage)
            this.logger.info("お使いの KPM は、自動アッグレートに対応していないません。手動で KPM をアッグレートしてください。");

        if (this.currentKPM == null)
        {
            this.logger.info("KPMReviewPackage をサーバーから手動で削除してください。");
            return;
        }

        String destructCommand = "kpm rm KPMReviewPackage";

        this.plugin.getServer().dispatchCommand(
                this.plugin.getServer().getConsoleSender(),
                destructCommand
        );
    }

    private void destructSelf()
    {
        this.destructSelf(true);
    }

    private SuccessResult resolveKPM(String version)
    {
        this.logger.info("最新の KPM を解決しています。");

        String query = "$>https://github.com/" + KPM_OWNER + "/" + KPM_NAME + "/releases/tag/" + version;

        ResolveResult resolveResult = this.registry.getPluginResolver().resolve(query);

        if (resolveResult instanceof SuccessResult)
        {
            this.logger.info("KPM を解決しました：" + ((SuccessResult) resolveResult).getVersion());
            return (SuccessResult) resolveResult;
        }

        assert resolveResult instanceof ErrorResult;
        ErrorResult errorResult = (ErrorResult) resolveResult;

        this.logger.severe("KPM の取得に失敗しました：" + errorResult.getMessage());
        this.destructSelf();
        return null;
    }

    private boolean removeCurrentKPM()
    {
        this.logger.info("現在の KPM を削除しています。");

        SignalHandleManager signalHandleManager = new SignalHandleManager();

        try
        {
            InstallResult<UnInstallTasks> uninstallResult = new PluginUninstaller(this.registry, signalHandleManager)
                    .run(UninstallArgument.builder(this.currentKPM)
                            .autoConfirm(true)
                            .forceUninstall(true)
                            .build()
                    );

            if (uninstallResult.isSuccess())
            {
                this.logger.info("KPM の削除に成功しました。");
                return true;
            }

            this.logger.warning("KPM の削除は " + uninstallResult.getProgress().getCurrentTask() + " で失敗しました。");
            return false;
        }
        catch (IOException e)
        {
            this.logger.severe("アンインストーラの初期化に失敗しました。");
            e.printStackTrace();
            return false;
        }
    }

    private boolean installNewKPM(SuccessResult resolveResult)
    {
        this.logger.info("新しい KPM をインストールしています。");

        SignalHandleManager signalHandleManager = new SignalHandleManager();

        try
        {
            InstallResult<InstallTasks> installResult = new PluginInstaller(this.registry, signalHandleManager)
                    .run(InstallArgument.builder(resolveResult)
                            .onyLocate(true)
                            .build()
                    );

            if (installResult.isSuccess())
            {
                this.logger.info("KPM のインストールに成功しました。");
                return true;
            }

            this.logger.warning("KPM のインストールは " + installResult.getProgress().getCurrentTask() + " で失敗しました。");

            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkAlreadyLatestInstalled(String version)
    {
        if (this.currentKPMVersion == null)
            return false;

        boolean isLatest = this.currentKPMVersion.isNewerThanOrEqualTo(Version.of(version));
        if (isLatest)
        {
            this.logger.warning("KPM は最新です。");
            this.destructSelf(false);
        }

        return isLatest;
    }


    public void runUpgrade(String version)
    {
        this.logger.info("KPM をアッグレートしています ...");

        SuccessResult result = this.resolveKPM(version);
        if (result == null)
            return;

        if (this.checkAlreadyLatestInstalled(result.getVersion()))
            return;

        try
        {
            Files.createDirectory(this.currentKPM.getDataFolder().toPath().resolve(".caches"));
        }
        catch (IOException e)
        {
            this.logger.warning("キャッシュディレクトリの作成に失敗しました。");
            e.printStackTrace();
            return;
        }

        if (!this.removeCurrentKPM())
        {
            this.destructSelf();
            return;
        }

        this.logger.info("データを移行しています ...");

        assert result.getVersion() != null;
        Version toVersion = Version.ofNullable(result.getVersion());

        assert toVersion != null;
        KPMMigrator.doMigrate(this.registry, this.currentKPM.getDataFolder().toPath(), this.currentKPMVersion, toVersion);

         Runner.runLater(() -> {
             if (!this.installNewKPM(result))
             {
                 this.destructSelf();
                 return;
             }
            this.logger.info("サーバをリロードしています ...");
            this.plugin.getServer().reload();

            this.logger.info("KPM のアッグレートが完了しました。");

            this.currentKPM = Bukkit.getPluginManager().getPlugin("TeamKunPluginManager");
            this.destructSelf(false);
        }, 21L);  // Because KPM removes itself after 20 ticks.
    }
}

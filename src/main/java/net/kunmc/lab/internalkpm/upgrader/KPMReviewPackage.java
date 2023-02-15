package net.kunmc.lab.internalkpm.upgrader;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class KPMReviewPackage extends JavaPlugin
{
    private final UpgradeImpl impl;

    public KPMReviewPackage()
    {
        this.impl = new UpgradeImpl(this);
    }

    @Override
    public void onEnable()
    {
        if (Bukkit.getPluginManager().getPlugin("TeamKunPluginManager") == null)
        {
            this.getLogger().severe("TeamKunPluginManager がインストールされていません。このプラグインを削除してください。");
            this.getLogger().severe("The server doesn't have TeamKunPluginManager. Please remove this plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.impl.initDaemon();
    }

}

package net.kunmc.lab.internalkpm.upgrader;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public final class KPMReviewPackage extends JavaPlugin implements CommandExecutor
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

    private boolean commandError(CommandSender sender)
    {
        sender.sendMessage("You cannot use this command.");
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @Nonnull @NotNull String[] args)
    {
        if (!(sender instanceof ConsoleCommandSender))
            return this.commandError(sender);

        if (args.length == 0)
            return this.commandError(sender);

        String toVersion = args[0];

        this.impl.runUpgrade(toVersion);
        return true;
    }
}

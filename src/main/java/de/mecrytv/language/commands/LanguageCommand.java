package de.mecrytv.language.commands;

import de.mecrytv.language.inventory.LanguageInv;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LanguageCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        new LanguageInv().open(player);

        return true;
    }
}

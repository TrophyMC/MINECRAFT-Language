package de.mecrytv.language.commands;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.language.Language;
import de.mecrytv.language.inventory.LanguageInv;
import de.mecrytv.language.model.LanguageModel;
import de.mecrytv.language.utils.TranslationUtils;
import de.mecrytv.languageapi.profile.ILanguageProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LanguageCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("language.admin")) {
                TranslationUtils.sendTranslation(sender, "commands.command_no_permission");
                return true;
            }

            reloadAllProfiles(sender);
            return true;
        }

        if (!(sender instanceof Player player)) {
            TranslationUtils.sendTranslation(sender, "commands.only_players");
            return true;
        }

        new LanguageInv().open(player);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("language.admin")) {
                completions.add("reload");
            }

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private void reloadAllProfiles(CommandSender sender) {
        Language plugin = Language.getInstance();

        plugin.getLanguageAPI().reloadAll();
        TranslationUtils.sendTranslation(sender, "commands.reload.files_success");

        int playercount = Bukkit.getOnlinePlayers().size();
        TranslationUtils.sendTranslation(sender, "commands.reload.profiles_start", "{count}", String.valueOf(playercount));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String uuid = onlinePlayer.getUniqueId().toString();

            DatabaseAPI.<LanguageModel>get("language", uuid).thenAccept(model -> {
                LanguageModel langModel = (model == null)
                        ? new LanguageModel(uuid, "en_US")
                        : model;

                ILanguageProfile profile = plugin.getLanguageAPI().getProfile(onlinePlayer.getUniqueId(), langModel.getLanguageCode());
                profile.setLanguageCode(langModel.getLanguageCode());
            });
        }

        TranslationUtils.sendTranslation(sender, "commands.reload.profiles_success");
    }
}
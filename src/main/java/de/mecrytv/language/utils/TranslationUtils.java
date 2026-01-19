package de.mecrytv.language.utils;

import de.mecrytv.language.Language;
import de.mecrytv.languageapi.LanguageAPI;
import de.mecrytv.languageapi.profile.ILanguageProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TranslationUtils {


    private static String getLang(CommandSender sender) {
        if (sender instanceof Player player) {
            // Holt das Profil aus dem gemeinsamen Cache
            ILanguageProfile profile = Language.getInstance().getLanguageAPI()
                    .getProfile(player.getUniqueId(), "en_US");
            return profile.getLanguageCode();
        }
        return "en_US";
    }


    public static void sendTranslation(CommandSender sender, String configKey, String... replacements) {
        String langCode = getLang(sender);
        String message = getFinalMessage(langCode, configKey);

        Component finalMsg = Language.getInstance().getPrefix()
                .append(MiniMessage.miniMessage().deserialize(applyReplacements(message, replacements)));

        sender.sendMessage(finalMsg);
    }


    public static Component sendGUITranslation(Player player, String configKey, String... replacements) {
        String langCode = getLang(player);
        String message = getFinalMessage(langCode, configKey);

        message = message.replaceFirst("(?i)(?:<[^>]*>)*Dynamic\\s*", "").trim();

        return MiniMessage.miniMessage().deserialize(applyReplacements(message, replacements));
    }


    private static String getFinalMessage(String langCode, String configKey) {
        LanguageAPI api = Language.getInstance().getLanguageAPI();
        String message = api.getTranslation(langCode, configKey);

        if ((message == null || message.isEmpty() || message.contains("Missing Lang")) && !langCode.equals("en_US")) {
            message = api.getTranslation("en_US", configKey);
        }

        return (message == null || message.contains("Missing Lang")) ? configKey : message;
    }

    private static String applyReplacements(String message, String... replacements) {
        if (replacements != null && replacements.length > 1) {
            for (int i = 0; i < replacements.length; i += 2) {
                String target = replacements[i];
                String value = replacements[i + 1];
                if (target != null && value != null) {
                    message = message.replace(target, value);
                }
            }
        }
        return message;
    }
}
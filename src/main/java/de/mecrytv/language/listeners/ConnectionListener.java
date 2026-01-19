package de.mecrytv.language.listeners;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.language.Language;
import de.mecrytv.language.model.LanguageModel;
import de.mecrytv.languageapi.profile.ILanguageProfile;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.time.Duration;
import java.util.UUID;

public class ConnectionListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Language plugin = Language.getInstance();

        DatabaseAPI.<LanguageModel>get("language", playerUUID.toString()).thenAccept(model -> {

            if (!player.isOnline()) return;

            LanguageModel currentLanguage = (model == null)
                    ? new LanguageModel(playerUUID.toString(), "en_US")
                    : model;

            ILanguageProfile profile = plugin.getLanguageAPI().getProfile(playerUUID, currentLanguage.getLanguageCode());
            profile.setLanguageCode(currentLanguage.getLanguageCode());

            if (currentLanguage.isFirstJoin()) {
                triggerFirstJoinEffects(player, currentLanguage);
                currentLanguage.setFirstJoin(false);
                DatabaseAPI.set("language", currentLanguage);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Language.getInstance().getLanguageAPI().removeProfile(event.getPlayer().getUniqueId());
    }

    private void triggerFirstJoinEffects(Player player, LanguageModel model) {
        Language plugin = Language.getInstance();

        Bukkit.getScheduler().runTask(plugin, () -> {
            String lang = model.getLanguageCode();
            MiniMessage mm = MiniMessage.miniMessage();

            String mainTitleRaw = plugin.getLanguageAPI().getTranslation(lang, "listeners.firstjoin.join_title");
            String subTitleRaw = plugin.getLanguageAPI().getTranslation(lang, "listeners.firstjoin.join_subtitle");

            player.showTitle(Title.title(
                    mm.deserialize(mainTitleRaw),
                    mm.deserialize(subTitleRaw),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(4000), Duration.ofMillis(1000))
            ));

            spawnFirework(player);
        });
    }

    private void spawnFirework(Player player) {
        Firework fw = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.addEffect(FireworkEffect.builder()
                .withColor(Color.ORANGE, Color.YELLOW)
                .withFade(Color.WHITE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .flicker(true)
                .build());
        fwm.setPower(1);
        fw.setFireworkMeta(fwm);
    }
}
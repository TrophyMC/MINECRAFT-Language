package de.mecrytv.language.inventory;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.language.Language;
import de.mecrytv.language.model.LanguageModel;
import de.mecrytv.language.utils.TranslationUtils;
import de.mecrytv.languageapi.profile.ILanguageProfile;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LanguageInv {

    private final HeadDatabaseAPI headDatabaseAPI = new HeadDatabaseAPI();

    public void open(Player player) {
        UUID uuid = player.getUniqueId();
        Language plugin = Language.getInstance();

        DatabaseAPI.<LanguageModel>get("language", uuid.toString()).thenAccept(model -> {
            LanguageModel finalModel = (model == null) ? new LanguageModel(uuid.toString(), "en_US") : model;
            String currentLang = finalModel.getLanguageCode();

            Bukkit.getScheduler().runTask(plugin, () -> {
                Component title = TranslationUtils.sendGUITranslation(currentLang, "gui.language.title");

                Gui gui = Gui.gui()
                        .title(title)
                        .rows(3)
                        .disableAllInteractions()
                        .create();

                ItemStack borderPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                borderPane.editMeta(meta -> meta.displayName(Component.empty()));
                gui.getFiller().fillBorder(ItemBuilder.from(borderPane).asGuiItem());

                plugin.getLanguageAPI().getConfigs().keySet().forEach(langCode -> {
                    gui.addItem(createLanguageHead(player, langCode, currentLang));
                });

                gui.open(player);
            });
        });
    }

    private GuiItem createLanguageHead(Player player, String langCode, String currentLang) {
        Language plugin = Language.getInstance();

        String headId = plugin.getConfiguration().getString("languageHeadIds." + langCode);
        ItemStack headItem = headDatabaseAPI.getItemHead(headId);
        if (headItem == null) headItem = new ItemStack(Material.PAPER);

        boolean isActive = langCode.equals(currentLang);

        Component displayName = TranslationUtils.sendGUITranslation(currentLang, "gui.language.lang_name_" + langCode);
        Component description = TranslationUtils.sendGUITranslation(currentLang, "gui.language.lang_desc_" + langCode);

        ItemBuilder builder = ItemBuilder.from(headItem).name(displayName);

        List<Component> lore = new ArrayList<>();
        lore.add(description);
        lore.add(Component.empty());

        if (isActive) {
            builder.glow(true);
            lore.add(TranslationUtils.sendGUITranslation(currentLang, "gui.language.status_active"));
        } else {
            lore.add(TranslationUtils.sendGUITranslation(currentLang, "gui.language.status_selectable"));
        }

        builder.lore(lore);

        return builder.asGuiItem(event -> {
            if (isActive) return;
            updateLanguage(player, langCode);
            player.closeInventory();
        });
    }

    private void updateLanguage(Player player, String newLang) {
        Language plugin = Language.getInstance();
        UUID uuid = player.getUniqueId();

        ILanguageProfile profile = plugin.getLanguageAPI().getProfile(uuid, "en_US");
        String oldLang = profile.getLanguageCode();

        profile.setLanguageCode(newLang);

        DatabaseAPI.<LanguageModel>get("language", uuid.toString()).thenAccept(model -> {
            LanguageModel m = (model == null) ? new LanguageModel(uuid.toString(), newLang) : model;
            m.setLanguageCode(newLang);
            m.setFirstJoin(false);
            DatabaseAPI.<LanguageModel>set("language", m);
        });

        TranslationUtils.sendTranslation(player, newLang, "messages.language_changed",
                "{oldLang}", oldLang,
                "{newLang}", newLang
        );
    }
}
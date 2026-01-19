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

        ILanguageProfile profile = plugin.getLanguageAPI().getProfile(uuid, "en_US");
        String currentLang = profile.getLanguageCode();

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
    }

    private GuiItem createLanguageHead(Player player, String langCode, String currentLang) {
        Language plugin = Language.getInstance();

        String headId = plugin.getConfiguration().getString("languageHeadIds." + langCode);
        ItemStack headItem = headDatabaseAPI.getItemHead(headId);
        if (headItem == null) headItem = new ItemStack(Material.PAPER);

        boolean isActive = langCode.equalsIgnoreCase(currentLang);

        Component displayName = TranslationUtils.sendGUITranslation(currentLang, "gui.language.lang_name_" + langCode);
        Component description = TranslationUtils.sendGUITranslation(currentLang, "gui.language.lang_desc_" + langCode);

        headItem.editMeta(meta -> {
            meta.displayName(displayName);
            List<Component> lore = new ArrayList<>();
            lore.add(description);
            lore.add(Component.empty());

            if (isActive) {
                lore.add(TranslationUtils.sendGUITranslation(currentLang, "gui.language.status_active"));
            } else {
                lore.add(TranslationUtils.sendGUITranslation(currentLang, "gui.language.status_selectable"));
            }
            meta.lore(lore);
        });

        ItemBuilder builder = ItemBuilder.from(headItem);
        if (isActive) builder.glow(true);

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
        String oldLangCode = profile.getLanguageCode();
        profile.setLanguageCode(newLang);

        DatabaseAPI.<LanguageModel>get("language", uuid.toString()).thenAccept(model -> {
            LanguageModel m = (model == null) ? new LanguageModel(uuid.toString(), newLang) : model;
            m.setLanguageCode(newLang);
            m.setFirstJoin(false);

            DatabaseAPI.set("language", m);
        });

        String oldLangName = plugin.getLanguageAPI().getTranslation(newLang, "gui.language.lang_name_" + oldLangCode);
        String newLangName = plugin.getLanguageAPI().getTranslation(newLang, "gui.language.lang_name_" + newLang);

        TranslationUtils.sendTranslation(player, newLang, "messages.language_changed",
                "{oldLang}", oldLangName,
                "{newLang}", newLangName
        );
    }
}
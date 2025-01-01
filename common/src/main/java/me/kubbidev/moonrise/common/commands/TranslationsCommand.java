package me.kubbidev.moonrise.common.commands;

import me.kubbidev.moonrise.common.command.abstraction.SingleCommand;
import me.kubbidev.moonrise.common.command.access.CommandPermission;
import me.kubbidev.moonrise.common.command.spec.CommandSpec;
import me.kubbidev.moonrise.common.command.tabcomplete.CompletionSupplier;
import me.kubbidev.moonrise.common.command.tabcomplete.TabCompleter;
import me.kubbidev.moonrise.common.command.util.ArgumentList;
import me.kubbidev.moonrise.common.http.UnsuccessfulRequestException;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.locale.TranslationManager;
import me.kubbidev.moonrise.common.locale.TranslationRepository;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.util.Predicates;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TranslationsCommand extends SingleCommand {
    public TranslationsCommand() {
        super(CommandSpec.TRANSLATIONS, "Translations", CommandPermission.TRANSLATIONS, Predicates.notInRange(0, 1));
    }

    @Override
    public void execute(MoonRisePlugin plugin, Sender sender, ArgumentList args, String label) {
        Message.TRANSLATIONS_SEARCHING.send(sender);

        List<TranslationRepository.LanguageInfo> availableTranslations;
        try {
            availableTranslations = plugin.getTranslationRepository().getAvailableLanguages();
        } catch (IOException | UnsuccessfulRequestException e) {
            Message.TRANSLATIONS_SEARCHING_ERROR.send(sender);
            plugin.getLogger().warn("Unable to obtain a list of available translations", e);
            return;
        }

        if (!args.isEmpty() && args.getFirst().equalsIgnoreCase("install")) {
            Message.TRANSLATIONS_INSTALLING.send(sender);
            plugin.getTranslationRepository().downloadAndInstallTranslations(availableTranslations, sender, true);
            Message.TRANSLATIONS_INSTALL_COMPLETE.send(sender);
            return;
        }

        Message.INSTALLED_TRANSLATIONS.send(sender, plugin.getTranslationManager().getInstalledLocales().stream().map(Locale::toLanguageTag).sorted().collect(Collectors.toList()));
        Message.AVAILABLE_TRANSLATIONS_HEADER.send(sender);

        availableTranslations.stream()
                .sorted(Comparator.comparing(language -> language.locale().toLanguageTag()))
                .forEach(language -> Message.AVAILABLE_TRANSLATIONS_ENTRY.send(sender,
                        language.locale().toLanguageTag(),
                        TranslationManager.localeDisplayName(language.locale()),
                        language.progress(),
                        language.contributors()
                ));

        sender.sendMessage(Message.prefixed(Component.empty()));
        Message.TRANSLATIONS_DOWNLOAD_PROMPT.send(sender, label);
    }

    @Override
    public List<String> tabComplete(MoonRisePlugin plugin, Sender sender, ArgumentList args) {
        return TabCompleter.create().at(0, CompletionSupplier.startsWith("install")).complete(args);
    }
}

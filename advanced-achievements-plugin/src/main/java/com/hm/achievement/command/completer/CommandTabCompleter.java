package com.hm.achievement.command.completer;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.command.executable.AbstractCommand;
import com.hm.achievement.command.executable.CommandSpec;
import com.hm.achievement.command.executable.DeleteCommand;
import com.hm.achievement.command.executable.EasterEggCommand;
import com.hm.achievement.command.executable.ResetCommand;
import com.hm.achievement.config.AchievementMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.apache.commons.lang3.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

/**
 * Class in charge of handling auto-completion for achievements and categories when using /aach check, /aach reset,
 * /aach give or /aach delete commands.
 *
 * @author Pyves
 */
public class CommandTabCompleter implements TabCompleter {

    private final AchievementMap achievementMap;
    private final Set<CommandSpec> commandSpecs;

    @Inject
    public CommandTabCompleter(AchievementMap achievementMap, @NonNull Set<AbstractCommand> commands) {
        this.achievementMap = achievementMap;
        this.commandSpecs = commands.stream().filter(c -> !(c instanceof EasterEggCommand)).map(c -> c.getClass().getAnnotation(CommandSpec.class)).collect(Collectors.toSet());
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String @NonNull [] args) {
        if (shouldReturnPlayerList(command, args)) {
            return null; // Complete with players.
        }

        String aachCommand = args[0];
        Collection<String> options = Collections.emptyList();
        if (args.length == 2 && "reset".equalsIgnoreCase(aachCommand)) {
            options = new HashSet<>(achievementMap.getCategorySubcategories());
            options.add(ResetCommand.WILDCARD);
        } else if (args.length == 2 && "give".equalsIgnoreCase(aachCommand)) {
            options = achievementMap.getSubcategoriesForCategory(CommandAchievements.COMMANDS);
        } else if (args.length == 2 && "check".equalsIgnoreCase(aachCommand)) {
            options = achievementMap.getAllNames();
        } else if (args.length == 2 && "delete".equalsIgnoreCase(aachCommand)) {
            options = new HashSet<>(achievementMap.getAllNames());
            options.add(DeleteCommand.WILDCARD);
        } else if (args.length == 2 && "inspect".equalsIgnoreCase(aachCommand)) {
            options = achievementMap.getAllSanitisedDisplayNames();
        } else if (args.length == 2 && "add".equalsIgnoreCase(aachCommand)) {
            options = Collections.singleton("1");
        } else if (args.length == 3 && "add".equalsIgnoreCase(aachCommand)) {
            options = achievementMap.getCategorySubcategories();
        } else if (args.length == 2 && "grant".equalsIgnoreCase(aachCommand)) {
            options = achievementMap.getAllNames();
        } else if (args.length == 3 && "grant".equalsIgnoreCase(aachCommand)) {
            return null;
        } else if (args.length == 1) {
            options = commandSpecs.stream().filter(cs -> cs.permission().isEmpty() || sender.hasPermission("achievement." + cs.permission())).map(CommandSpec::name).collect(Collectors.toSet());
        }
        return getPartialList(options, args[args.length - 1]);
    }

    /**
     * Returns a partial list based on the input set. Members of the returned list must start with what the player has
     * types so far.
     *
     * @param options
     * @param prefix
     * @return a list containing elements matching the prefix.
     */
    private List<String> getPartialList(@NonNull Collection<String> options, String prefix) {
        // Find matching options
        // Replace spaces with an Open Box character to prevent completing wrong word. Prevented Behaviour:
        // T -> Tamer -> Teleport Man -> Teleport The Avener -> Teleport The Smelter
        // Sort matching elements by alphabetical order.
        return options.stream().filter(s1 -> s1.toLowerCase().startsWith(prefix.toLowerCase())).map(s -> s.replace(' ', '␣')).sorted().collect(Collectors.toList());
    }

    private boolean shouldReturnPlayerList(@NonNull Command command, String[] args) {
        return !"aach".equals(command.getName()) || (args.length == 3 && Stream.of("give", "reset", "check", "delete", "grant").anyMatch(s -> Strings.CI.equals(args[0], s))) || (args.length == 4 && Strings.CI.equals(args[0], "add"));
    }
}

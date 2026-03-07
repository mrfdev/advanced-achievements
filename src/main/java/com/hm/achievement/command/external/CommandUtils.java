package com.hm.achievement.command.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CommandUtils {

    /*
      The CommandUtils class is used for selector support in commands (@p, @a etc.) Originally made by ZombieStriker
      https://github.com/ZombieStriker/PsudoCommands/blob/master/src/me/zombie_striker/psudocommands/CommandUtils.java
     */

    /**
     * Use this if you are unsure if a player provided the "@a" tag. This will allow multiple entities to be retrieved.
     * <p>
     * This can return a null variable if no tags are included, or if a value for a tag does not exist (I.e. if the tag
     * [type=___] contains an entity that does not exist in the specified world)
     * <p>
     * The may also be empty or null values at the end of the array. Once a null value has been reached, you do not need
     * to loop through any of the higher indexes
     * <p>
     * Currently supports the tags:
     *
     * @param arg    the argument that we are testing for
     * @param sender the sender of the command
     * @return The entities that match the criteria
     * @p , @a , @e , @r
     * <p>
     * Currently supports the selectors: [type=] [r=] [rm=] [c=] [w=] [m=] [name=] [l=] [lm=] [h=] [hm=] [rx=] [rxm=]
     * [ry=] [rym=] [team=] [score_---=] [score_---_min=] [x] [y] [z] [limit=] [x_rotation] [y_rotation]
     * <p>
     * All selectors can be inverted.
     */
    public static Entity @Nullable [] getTargets(CommandSender sender, String arg) {
        Entity[] ents;
        Location loc = null;
        if (sender instanceof Player) {
            loc = ((Player) sender).getLocation();
        } else if (sender instanceof BlockCommandSender) {
            // Center of block.
            loc = ((BlockCommandSender) sender).getBlock().getLocation().add(0.5, 0, 0.5);
        } else if (sender instanceof CommandMinecart) {
            loc = ((CommandMinecart) sender).getLocation();
        }
        String[] tags = getTags(arg);

        // prefab fix
        for (String s : tags) {
            if (hasTag(SelectorType.X, s)) {
                if (loc != null) {
                    loc.setX(getInt(s));
                }
            } else if (hasTag(SelectorType.Y, s)) {
                if (loc != null) {
                    loc.setY(getInt(s));
                }
            } else if (hasTag(SelectorType.Z, s)) {
                if (loc != null) {
                    loc.setZ(getInt(s));
                }
            }
        }

        if (arg.startsWith("@s")) {
            ents = new Entity[1];
            if (sender instanceof Player) {
                boolean good = true;
                for (String tag : tags) {
                    if (canBeAccepted(tag, (Entity) sender, loc)) {
                        good = false;
                        break;
                    }
                }
                if (good) {
                    ents[0] = (Entity) sender;
                }
            } else {
                return null;
            }
            return ents;
        } else if (arg.startsWith("@a")) {
            // ents = new Entity[maxEnts];
            List<Entity> listOfValidEntities = new ArrayList<>();
            int C = getLimit(arg);

            boolean usePlayers = true;
            for (String tag : tags) {
                if (hasTag(SelectorType.TYPE, tag)) {
                    usePlayers = false;
                    break;
                }
            }
            List<Entity> ea = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (!usePlayers) {
                ea.clear();
                for (World w : getAcceptedWorldsFullString(loc, arg)) {
                    ea.addAll(w.getEntities());
                }
            }
            for (Entity e : ea) {
                if (listOfValidEntities.size() >= C) break;
                boolean isValid = true;
                for (String tag : tags) {
                    if (canBeAccepted(tag, e, loc)) {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    listOfValidEntities.add(e);
                }
            }

            ents = listOfValidEntities.toArray(new Entity[0]);

        } else if (arg.startsWith("@p")) {
            ents = new Entity[1];
            double closestInt = Double.MAX_VALUE;
            Entity closest = null;

            for (World w : getAcceptedWorldsFullString(loc, arg)) {
                for (Player e : w.getPlayers()) {
                    if (e == sender) continue;
                    Location temp = loc;
                    if (temp == null) temp = e.getWorld().getSpawnLocation();
                    double distance = e.getLocation().distanceSquared(temp);
                    if (closestInt > distance) {
                        boolean good = true;
                        for (String tag : tags) {
                            if (canBeAccepted(tag, e, temp)) {
                                good = false;
                                break;
                            }
                        }
                        if (good) {
                            closestInt = distance;
                            closest = e;
                        }
                    }
                }
            }
            ents[0] = closest;
        } else if (arg.startsWith("@e")) {
            List<Entity> entities = new ArrayList<>();
            int C = getLimit(arg);
            for (World w : getAcceptedWorldsFullString(loc, arg)) {
                for (Entity e : w.getEntities()) {
                    if (entities.size() > C) break;
                    if (e == sender) continue;
                    boolean valid = true;
                    for (String tag : tags) {
                        if (canBeAccepted(tag, e, loc)) {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) {
                        entities.add(e);
                    }
                }
            }
            ents = entities.toArray(new Entity[0]);
        } else if (arg.startsWith("@r")) {
            Random r = ThreadLocalRandom.current();
            ents = new Entity[1];

            List<Entity> validEntities = new ArrayList<>();
            for (World w : getAcceptedWorldsFullString(loc, arg)) {
                if (hasTag(SelectorType.TYPE, arg)) {
                    for (Entity e : w.getEntities()) {
                        boolean good = true;
                        for (String tag : tags) {
                            if (canBeAccepted(tag, e, loc)) {
                                good = false;
                                break;
                            }
                        }
                        if (good) validEntities.add(e);
                    }
                } else {
                    for (Entity e : Bukkit.getOnlinePlayers()) {
                        boolean good = true;
                        for (String tag : tags) {
                            if (canBeAccepted(tag, e, loc)) {
                                good = false;
                                break;
                            }
                        }
                        if (good) validEntities.add(e);
                    }
                }
            }
            ents[0] = validEntities.get(r.nextInt(validEntities.size()));
        } else {
            ents = new Entity[]{Bukkit.getPlayer(arg)};
        }
        return ents;
    }

    private static boolean canBeAccepted(String arg, Entity e, Location loc) {
        if (hasTag(SelectorType.X_ROTATION, arg) && isWithinYaw(arg, e)) return false;
        if (hasTag(SelectorType.Y_ROTATION, arg) && isWithinPitch(arg, e)) return false;
        if (hasTag(SelectorType.TYPE, arg) && isType(arg, e)) return false;
        if (hasTag(SelectorType.NAME, arg) && isName(arg, e)) return false;
        if (hasTag(SelectorType.TEAM, arg) && isTeam(arg, e)) return false;
        if (hasTag(SelectorType.SCORE_FULL, arg) && isScore(arg, e)) return false;
        if (hasTag(SelectorType.SCORE_MIN, arg) && isScoreMin(arg, e)) return false;
        if (hasTag(SelectorType.SCORE_13, arg) && isScoreWithin(arg, e)) return false;
        if (hasTag(SelectorType.DISTANCE, arg) && isWithinDistance(arg, loc, e)) return false;
        if (hasTag(SelectorType.LEVEL, arg) && isWithinLevel(arg, e)) return false;
        if (hasTag(SelectorType.TAG, arg) && isHasTags(arg, e)) return false;
        if (hasTag(SelectorType.RYM, arg) && isRYM(arg, e)) return false;
        if (hasTag(SelectorType.RXM, arg) && isRXM(arg, e)) return false;
        if (hasTag(SelectorType.HM, arg) && isHM(arg, e)) return false;
        if (hasTag(SelectorType.RY, arg) && isRY(arg, e)) return false;
        if (hasTag(SelectorType.RX, arg) && isRX(arg, e)) return false;
        if (hasTag(SelectorType.RM, arg) && isRM(arg, loc, e)) return false;
        if (hasTag(SelectorType.LMax, arg) && isLM(arg, e)) return false;
        if (hasTag(SelectorType.L, arg) && isL(arg, e)) return false;
        if (hasTag(SelectorType.m, arg) && isM(arg, e)) return false;
        if (hasTag(SelectorType.H, arg) && isH(arg, e)) return false;
        if (hasTag(SelectorType.World, arg) && isW(arg)) return false;
        if (hasTag(SelectorType.R, arg) && isR(arg, loc, e)) return false;
        if (hasTag(SelectorType.X, arg)) return false;
        if (hasTag(SelectorType.Y, arg)) return false;
        return !hasTag(SelectorType.Z, arg);
    }

    private static String @NonNull [] getTags(@NonNull String arg) {
        if (!arg.contains("[")) return new String[0];
        String tags = arg.split("\\[")[1].split("]")[0];
        return tags.split(",");
    }

    private static int getLimit(String arg) {
        if (hasTag(SelectorType.LIMIT, arg)) for (String s : getTags(arg)) {
            if (hasTag(SelectorType.LIMIT, s)) {
                return getInt(s);
            }
        }
        if (hasTag(SelectorType.C, arg)) for (String s : getTags(arg)) {
            if (hasTag(SelectorType.C, s)) {
                return getInt(s);
            }
        }
        return Integer.MAX_VALUE;
    }

    private static @NonNull String getType(String arg) {
        if (hasTag(SelectorType.TYPE, arg)) return arg.toLowerCase().split("=")[1].replace("!", "");
        return "Player";
    }

    private static String getName(@NonNull String arg) {
        String reparg = arg.replace(" ", "_");
        return reparg.replace("!", "").split("=")[1];
    }

    private static World getW(String arg) {
        return Bukkit.getWorld(getString(arg));
    }

    private static @NonNull String getScoreMinName(@NonNull String arg) {
        return arg.split("=")[0].substring(0, arg.split("=")[0].length() - 1 - 4).replace("score_", "");
    }

    private static @NonNull String getScoreName(@NonNull String arg) {
        return arg.split("=")[0].replace("score_", "");
    }

    private static String getTeam(@NonNull String arg) {
        return arg.toLowerCase().replace("!", "").split("=")[1];
    }

    private static int getValueAsInteger(@NonNull String arg) {
        return Integer.parseInt(arg.replace("!", "").split("=")[1]);
    }

    private static @Nullable GameMode getM(@NonNull String arg) {
        String[] split = arg.replace("!", "").toLowerCase().split("=");
        String returnType = split[1];
        if (returnType.equalsIgnoreCase("0") || returnType.equalsIgnoreCase("s") || returnType.equalsIgnoreCase("survival")) return GameMode.SURVIVAL;
        if (returnType.equalsIgnoreCase("1") || returnType.equalsIgnoreCase("c") || returnType.equalsIgnoreCase("creative")) return GameMode.CREATIVE;
        if (returnType.equalsIgnoreCase("2") || returnType.equalsIgnoreCase("a") || returnType.equalsIgnoreCase("adventure")) return GameMode.ADVENTURE;
        if (returnType.equalsIgnoreCase("3") || returnType.equalsIgnoreCase("sp") || returnType.equalsIgnoreCase("spectator")) return GameMode.SPECTATOR;
        return null;
    }

    private static @NonNull List<World> getAcceptedWorldsFullString(Location loc, String fullString) {
        String string = null;
        for (String tag : getTags(fullString)) {
            if (hasTag(SelectorType.World, tag)) {
                string = tag;
                break;
            }
        }
        if (string == null) {
            List<World> worlds = new ArrayList<>();
            if (loc == null || loc.getWorld() == null) {
                worlds.addAll(Bukkit.getWorlds());
            } else {
                worlds.add(loc.getWorld());
            }
            return worlds;
        }
        return getAcceptedWorlds(string);
    }

    private static @NonNull List<World> getAcceptedWorlds(String string) {
        List<World> worlds = new ArrayList<>(Bukkit.getWorlds());
        if (isInverted(string)) {
            worlds.remove(getW(string));
        } else {
            worlds.clear();
            worlds.add(getW(string));
        }
        return worlds;
    }

    private static boolean isTeam(String arg, Entity e) {
        if (!(e instanceof Player)) return false;
        for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            if ((t.getName().equalsIgnoreCase(getTeam(arg)) != isInverted(arg))) {
                if ((t.getEntries().contains(e.getName()) != isInverted(arg))) return true;
            }
        }
        return false;
    }

    private static boolean isWithinPitch(String arg, @NonNull Entity e) {
        return isWithinDoubleValue(isInverted(arg), arg, e.getLocation().getPitch());
    }

    private static boolean isWithinYaw(String arg, @NonNull Entity e) {
        return isWithinDoubleValue(isInverted(arg), arg, e.getLocation().getYaw());
    }

    private static boolean isWithinDistance(@NonNull String arg, @NonNull Location start, @NonNull Entity e) {
        double distanceMin = 0;
        double distanceMax = Double.MAX_VALUE;
        String distance = arg.split("=")[1];
        if (e.getLocation().getWorld() != start.getWorld()) return false;
        if (distance.contains("..")) {
            String[] temp = distance.split("\\.\\.");
            if (!temp[0].isEmpty()) {
                distanceMin = Integer.parseInt(temp[0]);
            }
            if (temp.length > 1 && !temp[1].isEmpty()) {
                distanceMax = Double.parseDouble(temp[1]);
            }
            double actDis = start.distanceSquared(e.getLocation());
            return actDis <= distanceMax * distanceMax && distanceMin * distanceMin <= actDis;
        } else {
            int mult = Integer.parseInt(distance);
            mult *= mult;
            return ((int) start.distanceSquared(e.getLocation())) == mult;
        }
    }

    private static boolean isWithinLevel(String arg, Entity e) {
        if (!(e instanceof Player)) return false;
        double distanceMin = 0;
        double distanceMax = Double.MAX_VALUE;
        String distance = arg.split("=")[1];
        if (distance.contains("..")) {
            String[] temp = distance.split("..");
            if (!temp[0].isEmpty()) {
                distanceMin = Integer.parseInt(temp[0]);
            }
            if (temp[1] != null && !temp[1].isEmpty()) {
                distanceMax = Double.parseDouble(temp[1]);
            }
            double actDis = ((Player) e).getExpToLevel();
            return actDis <= distanceMax * distanceMax && distanceMin * distanceMin <= actDis;
        } else {
            return ((Player) e).getExpToLevel() == Integer.parseInt(distance);
        }
    }

    private static boolean isScore(String arg, Entity e) {
        if (!(e instanceof Player)) return false;
        for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
            if (o.getName().equalsIgnoreCase(getScoreName(arg))) {
                if ((o.getScore(e.getName()).getScore() <= getValueAsInteger(arg) != isInverted(arg))) return true;
            }
        }
        return false;
    }

    private static boolean isScoreWithin(String arg, Entity e) {
        if (!(e instanceof Player)) return false;
        String[] scores = arg.split("\\{")[1].split("}")[0].split(",");
        for (String score : scores) {
            String[] s = score.split("=");
            String name = s[0];

            for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
                if (o.getName().equalsIgnoreCase(name)) {
                    if (!isWithinDoubleValue(isInverted(arg), s[1], o.getScore(e.getName()).getScore())) return false;
                }
            }
        }
        return true;

    }

    private static boolean isHasTags(String arg, Entity e) {
        if (!(e instanceof Player)) return false;
        return isInverted(arg) != e.getScoreboardTags().contains(getString(arg));

    }

    private static boolean isScoreMin(String arg, Entity e) {
        if (!(e instanceof Player)) return false;
        for (Objective o : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
            if (o.getName().equalsIgnoreCase(getScoreMinName(arg))) {
                if ((o.getScore(e.getName()).getScore() >= getValueAsInteger(arg) != isInverted(arg))) return true;
            }
        }
        return false;
    }

    private static boolean isRM(String arg, @NonNull Location loc, @NonNull Entity e) {
        if (loc.getWorld() != e.getWorld()) return false;
        return isGreaterThan(arg, loc.distance(e.getLocation()));
    }

    private static boolean isR(String arg, @NonNull Location loc, @NonNull Entity e) {
        if (loc.getWorld() != e.getWorld()) return false;
        return isLessThan(arg, loc.distance(e.getLocation()));

    }

    private static boolean isRXM(String arg, @NonNull Entity e) {
        return isLessThan(arg, e.getLocation().getYaw());
    }

    private static boolean isRX(String arg, @NonNull Entity e) {
        return isGreaterThan(arg, e.getLocation().getYaw());
    }

    private static boolean isRYM(String arg, @NonNull Entity e) {
        return isLessThan(arg, e.getLocation().getPitch());
    }

    private static boolean isRY(String arg, @NonNull Entity e) {
        return isGreaterThan(arg, e.getLocation().getPitch());
    }

    @SuppressWarnings("SameReturnValue")
    private static boolean isL(String arg, Entity e) {
        if (e instanceof Player) {
            isLessThan(arg, ((Player) e).getTotalExperience());
        }
        return false;
    }

    private static boolean isLM(String arg, Entity e) {
        if (e instanceof Player) {
            return isGreaterThan(arg, ((Player) e).getTotalExperience());
        }
        return false;
    }

    private static boolean isH(String arg, Entity e) {
        if (e instanceof Damageable) return isGreaterThan(arg, ((Damageable) e).getHealth());
        return false;
    }

    private static boolean isHM(String arg, Entity e) {
        if (e instanceof Damageable) return isLessThan(arg, ((Damageable) e).getHealth());
        return false;
    }

    private static boolean isM(String arg, Entity e) {
        if (getM(arg) == null) return true;
        if (e instanceof HumanEntity) {
            return isInverted(arg) != (getM(arg) == ((HumanEntity) e).getGameMode());
        }
        return false;
    }

    private static boolean isW(String arg) {
        if (getW(arg) == null) {
            return true;
        } else return isInverted(arg) != getAcceptedWorlds(arg).contains(getW(arg));
    }

    private static boolean isName(String arg, Entity e) {
        if (getName(arg) == null) return true;
        return isInverted(arg) == (e.customName() == null) && isInverted(arg) != (getName(arg).equals(Objects.requireNonNull(PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(e.customName()))).replace(" ", "_")) || (e instanceof Player && e.getName().replace(" ", "_").equalsIgnoreCase(getName(arg))));
    }

    private static boolean isType(String arg, @NonNull Entity e) {
        boolean invert = isInverted(arg);
        String type = getType(arg);
        return invert != e.getType().name().equalsIgnoreCase(type);

    }

    private static boolean isInverted(@NonNull String arg) {
        return arg.toLowerCase().split("!").length != 1;
    }

    private static int getInt(@NonNull String arg) {
        return Integer.parseInt(arg.split("=")[1]);
    }

    public static @NonNull String getString(@NonNull String arg) {
        return arg.split("=")[1].replaceAll("!", "");
    }

    private static boolean isLessThan(String arg, double value) {
        boolean inverted = isInverted(arg);
        double mult = Double.parseDouble(arg.split("=")[1]);
        return (value < mult) != inverted;
    }

    private static boolean isGreaterThan(String arg, double value) {
        boolean inverted = isInverted(arg);
        double mult = Double.parseDouble(arg.split("=")[1]);
        return (value > mult) != inverted;
    }

    private static boolean isWithinDoubleValue(boolean inverted, @NonNull String arg, double value) {
        double min = -Double.MAX_VALUE;
        double max = Double.MAX_VALUE;
        if (arg.contains("..")) {
            String[] temp = arg.split("\\.\\.");
            if (!temp[0].isEmpty()) {
                min = Integer.parseInt(temp[0]);
            }
            if (temp.length > 1 && !temp[1].isEmpty()) {
                max = Double.parseDouble(temp[1]);
            }
            return (value <= max * max && min * min <= value) != inverted;
        } else {
            double mult = Double.parseDouble(arg);
            return (value == mult) != inverted;
        }
    }

    private static boolean hasTag(@NonNull SelectorType type, @NonNull String arg) {
        return arg.toLowerCase().startsWith(type.getName());
    }

    enum SelectorType {

        LEVEL("level="), DISTANCE("distance="), TYPE("type="), NAME("name="), TEAM("team="), LMax("lm="), L("l="), World("w="), m("m="), C("c="), HM("hm="), H("h="), RM("rm="), RYM("rym="), RX("rx="), SCORE_FULL("score="), SCORE_MIN("score_min"), SCORE_13("scores="), R("r="), RXM("rxm="), RY("ry="), TAG("tag="), X("x="), Y("y="), Z("z="), LIMIT("limit="), Y_ROTATION("y_rotation"), X_ROTATION("x_rotation");

        final String name;

        SelectorType(String s) {
            this.name = s;
        }

        public String getName() {
            return name;
        }
    }
}

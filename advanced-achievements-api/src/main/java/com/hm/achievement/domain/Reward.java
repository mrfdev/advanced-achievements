package com.hm.achievement.domain;

import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public record Reward(List<String> listTexts, List<Component> chatTexts, Consumer<Player> rewarder) {
}

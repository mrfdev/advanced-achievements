package com.hm.achievement.domain;

import java.util.List;
import java.util.function.Consumer;
import org.bukkit.entity.Player;

public record Reward(List<String> listTexts, List<String> chatTexts, Consumer<Player> rewarder) {


}

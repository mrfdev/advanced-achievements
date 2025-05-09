package com.hm.achievement.command.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Utility for paginating command messages.
 * <p>
 * Ignores length of given items, user of this utility needs to make sure the items are not too long to wrap in the chat
 * box.
 * <p>
 * Wrapping in the chat box is difficult to calculate since the Minecraft font is not monospaced so 'w' and 'i' are
 * different width, as well as unicode characters which are their own special category.
 *
 * @author Rsl1122
 */
public class SupplierCommandPagination extends CommandPagination {

	private final List<Supplier<String>> toPaginate;
	private final YamlConfiguration langConfig;
	private final int perPage;
	private final int size;
	private final int maxPage;

	public SupplierCommandPagination(@NotNull List<Supplier<String>> toPaginate, int perPage, YamlConfiguration langConfig) {
		super(new ArrayList<>(), perPage, langConfig);
		this.toPaginate = toPaginate;
		size = toPaginate.size();
		this.perPage = perPage;
		this.langConfig = langConfig;
		int leftovers = size % perPage;
		// One command window can fit 20 lines, we're leaving 2 for header and footer.
		maxPage = (size - leftovers) / perPage + (leftovers > 0 ? 1 : 0);
	}

	public boolean isEmpty() {
		return toPaginate.isEmpty();
	}

	@Override
	public void sendPage(int page, @NotNull Consumer<String> to) {
		int pageToSend = Math.min(page, maxPage);

		String header = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(StringUtils.replaceEach(langConfig.getString("pagination-header"), new String[]{"PAGE", "MAX"},
                        new String[]{Integer.toString(pageToSend), Integer.toString(maxPage)})));
		String footer = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(langConfig.getString("pagination-footer")));

		to.accept(header);

		int index = pageToSend - 1;
		// Handling case where empty list is given to CommandPagination
		int pageStart = index > 0 ? (index * perPage) : 0;
		int nextPageStart = pageToSend * perPage;

		for (int i = pageStart; i < Math.min(nextPageStart, size); i++) {
			to.accept(toPaginate.get(i).get());
		}

		to.accept(footer);
	}
}

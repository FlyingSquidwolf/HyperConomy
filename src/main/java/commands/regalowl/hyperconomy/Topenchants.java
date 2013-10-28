package regalowl.hyperconomy;

import java.util.SortedMap;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Topenchants {
	Topenchants(String args[], Player player, CommandSender sender, String playerecon) {
		HyperConomy hc = HyperConomy.hc;
		HyperEconomy he = hc.getEconomyManager().getEconomy(playerecon);
		EconomyManager em = hc.getEconomyManager();
		LanguageFile L = hc.getLanguageFile();
		try {
			boolean requireShop = hc.getConfig().getBoolean("config.limit-info-commands-to-shops");
			if (args.length > 1) {
				sender.sendMessage(L.get("TOPENCHANTS_INVALID"));
				return;
			}
			String nameshop = "";
			if (player != null) {
				if (em.inAnyShop(player)) {
					nameshop = em.getShop(player).getName();
				} 				
				if (requireShop && em.getShop(player) == null && !player.hasPermission("hyperconomy.admin")) {
					sender.sendMessage(L.get("REQUIRE_SHOP_FOR_INFO"));
					return;
				}
			}
			int page;
			if (args.length == 0) {
				page = 1;
			} else {
				page = Integer.parseInt(args[0]);
			}
			SortedMap<Double, String> enchantstocks = new TreeMap<Double, String>();
			for (HyperObject ho:he.getHyperObjects()) {
				if (!(ho instanceof HyperEnchant)) {continue;}
				if (ho instanceof PlayerShopObject) {
					PlayerShopObject pso = (PlayerShopObject)ho;
					if (pso.getStatus() == HyperObjectStatus.NONE) {
						if (!pso.getShop().isAllowed(em.getHyperPlayer(player))) {
							continue;
						}
					}
				}
				boolean unavailable = false;
				if (nameshop != "") {
					if (!em.getShop(nameshop).has(ho.getName())) {
						unavailable = true;
					}
				}
				if (!unavailable) {
					double samount = he.getHyperObject(ho.getName(), em.getShop(player)).getStock();
					if (samount > 0) {
						while (enchantstocks.containsKey(samount * 100)) {
							samount = samount + .0000001;
						}
						enchantstocks.put(samount * 100, ho.getName());
					}
				}
			}
			int numberpage = page * 10;
			int count = 0;
			int le = enchantstocks.size();
			double maxpages = le / 10;
			maxpages = Math.ceil(maxpages);
			int maxpi = (int) maxpages + 1;
			sender.sendMessage(L.f(L.get("PAGE_NUMBER"), page, maxpi));
			try {
				while (count < numberpage) {
					double lk = enchantstocks.lastKey();
					if (count > ((page * 10) - 11)) {
						sender.sendMessage(ChatColor.WHITE + enchantstocks.get(lk) + ChatColor.WHITE + ": " + ChatColor.AQUA + "" + Math.floor(lk)/100);
					}
					enchantstocks.remove(lk);
					count++;
				}
			} catch (Exception e) {
				sender.sendMessage(L.get("YOU_HAVE_REACHED_THE_END"));
			}
		} catch (Exception e) {
			sender.sendMessage(L.get("TOPENCHANTS_INVALID"));
		}
	}
}

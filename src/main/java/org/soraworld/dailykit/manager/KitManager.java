package org.soraworld.dailykit.manager;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.soraworld.hocon.node.Setting;
import org.soraworld.violet.data.DataAPI;
import org.soraworld.violet.inject.MainManager;
import org.soraworld.violet.manager.VManager;
import org.soraworld.violet.plugin.SpigotPlugin;
import org.soraworld.violet.util.ChatColor;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Himmelt
 */
@MainManager
public class KitManager extends VManager {

    @Setting(comment = "comment.giveKitCommand")
    private String giveKitCommand = "give ${player} ${name} 1";
    @Setting(comment = "comment.giveSpecialCommand")
    private String giveSpecialCommand = "give ${player} ${name} 1";
    @Setting(comment = "comment.kitPrices")
    private final HashMap<String, Integer> kitPrices = new HashMap<>();
    @Setting(comment = "comment.kitRewards")
    private HashMap<String, Integer> kitRewards = new HashMap<>();
    @Setting(comment = "comment.specialPrices")
    private final HashMap<String, Integer> specialPrices = new HashMap<>();

    private static final String KIT_REWARDS_KEY = "dailykit.rewards";
    private static final String LAST_BUYDAY_KEY = "dailykit.lastbuyday";

    private PlayerPointsAPI pointsApi = null;

    public KitManager(SpigotPlugin plugin, Path path) {
        super(plugin, path);
    }

    @Override
    public void afterLoad() {
        try {
            PlayerPoints playerPoints = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
            if (playerPoints != null && playerPoints.isEnabled()) {
                pointsApi = playerPoints.getAPI();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public ChatColor defChatColor() {
        return ChatColor.YELLOW;
    }

    public void tryBuyKit(Player player, String name) {
        UUID uuid = player.getUniqueId();
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        HashMap<String,Integer> lastBuyMap = DataAPI.getStore(uuid,LAST_BUYDAY_KEY,new HashMap<>(),HashMap.class);
        int lastBuyDay = lastBuyMap.getOrDefault(name, 0);
        if (today != lastBuyDay) {
            String command = giveKitCommand.replaceAll("\\$\\{player}", player.getName()).replaceAll("\\$\\{name}", name);
            int price = kitPrices.getOrDefault(name, 0);
            if (pointsApi != null && pointsApi.take(uuid, price)) {
                int reward = DataAPI.getStoreInt(uuid, KIT_REWARDS_KEY, 0);
                DataAPI.setStore(uuid, KIT_REWARDS_KEY, reward + kitRewards.getOrDefault(name, 0));
                lastBuyMap.put(name,today);
                // DataAPI.setStore(uuid, LAST_BUYDAY_KEY, lastBuyMap);
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
            } else {
                sendKey(player, "notEnoughPoints");
            }
        } else {
            sendKey(player, "alreadyBuyToday",name);
        }
    }

    public void tryRedeemSpecial(Player player, String name) {
        String command = giveSpecialCommand.replaceAll("\\$\\{player}", player.getName()).replaceAll("\\$\\{name}", name);
        int price = specialPrices.getOrDefault(name, 0);
        int reward = DataAPI.getStoreInt(player.getUniqueId(), KIT_REWARDS_KEY, 0);
        if (reward >= price) {
            DataAPI.setStore(player.getUniqueId(), KIT_REWARDS_KEY, reward - price);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            sendKey(player, "notEnoughReward");
        }
    }

    public void showInfo(Player player) {
        sendKey(player, "infoRewards", DataAPI.getStoreInt(player.getUniqueId(), KIT_REWARDS_KEY, 0));
    }
}

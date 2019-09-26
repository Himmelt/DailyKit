package org.soraworld.dailykit.command;

import org.bukkit.entity.Player;
import org.soraworld.dailykit.manager.KitManager;
import org.soraworld.violet.command.Sub;
import org.soraworld.violet.command.SubExecutor;
import org.soraworld.violet.inject.Command;
import org.soraworld.violet.inject.Inject;

/**
 * @author Himmelt
 */
@Command(name = "dailykit", usage = "/dailykit buy|redeem|info")
public final class CommandDailyKit {

    @Inject
    private KitManager manager;

    @Sub(usage = "/dailykit buy <kit_name>")
    public final SubExecutor<Player> buy = (cmd, player, args) -> {
        if (args.notEmpty()) {
            manager.tryBuyKit(player, args.first());
        } else {
            cmd.sendUsage(player);
        }
    };

    @Sub(usage = "/dailykit redeem <special_name>")
    public final SubExecutor<Player> redeem = (cmd, player, args) -> {
        if (args.notEmpty()) {
            manager.tryRedeemSpecial(player, args.first());
        } else {
            cmd.sendUsage(player);
        }
    };

    @Sub(usage = "/dailykit info")
    public final SubExecutor<Player> info = (cmd, player, args) -> {
        manager.showInfo(player);
    };
}

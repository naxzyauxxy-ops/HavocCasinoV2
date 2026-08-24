package net.havoccasino;

import net.havoccasino.command.JackpotCommand;
import net.havoccasino.command.CasinoMenuCommand;
import net.havoccasino.command.CratesCommand;
import net.havoccasino.command.GamblingRoomCommand;
import net.havoccasino.command.MinesCommand;
import net.havoccasino.command.HavocCasinoCommand;
import net.havoccasino.command.SlotsCommand;
import net.havoccasino.config.CasinoConfig;
import net.havoccasino.economy.CurrencyService;
import net.havoccasino.economy.VaultHook;
import net.havoccasino.game.CrateManager;
import net.havoccasino.game.GameService;
import net.havoccasino.game.JackpotManager;
import net.havoccasino.game.SlotMachine;
import net.havoccasino.gui.GuiListener;
import net.havoccasino.hook.HavocExpansion;
import net.havoccasino.hook.WagersExpansion;
import net.havoccasino.region.SelectionService;
import net.havoccasino.region.WandItem;
import net.havoccasino.region.WandListener;
import net.havoccasino.room.RoomManager;
import net.havoccasino.schematic.SchematicService;
import net.havoccasino.message.Messages;
import net.havoccasino.settings.PlayerSettings;
import net.havoccasino.util.Msg;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * HavocCasino entry point. Wires the currency layer, games and commands together.
 */
public final class HavocCasino extends JavaPlugin {

    private CasinoConfig casinoConfig;
    private VaultHook vaultHook;
    private CurrencyService currencyService;
    private SlotMachine slotMachine;
    private JackpotManager jackpotManager;
    private CrateManager crateManager;
    private WandItem wandItem;
    private SelectionService selectionService;
    private SchematicService schematicService;
    private RoomManager roomManager;
    private GameService gameService;
    private PlayerSettings playerSettings;
    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.casinoConfig = new CasinoConfig(this);
        this.casinoConfig.reload();

        this.playerSettings = new PlayerSettings(this);
        this.playerSettings.load();
        Msg.init(this.casinoConfig, this.playerSettings);

        this.messages = new Messages(this);
        this.messages.load();

        this.vaultHook = new VaultHook(this);
        this.vaultHook.setup();
        if (!this.vaultHook.isEnabled()) {
            getLogger().severe("Vault economy not found. HavocCasino needs Vault plus an "
                    + "economy plugin (e.g. EssentialsX). Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.currencyService = new CurrencyService(this.vaultHook, this.casinoConfig);
        this.slotMachine = new SlotMachine(this.casinoConfig);
        this.jackpotManager = new JackpotManager(this, this.casinoConfig, this.currencyService);
        this.jackpotManager.load();

        this.crateManager = new CrateManager(this);
        this.crateManager.load();

        this.wandItem = new WandItem(this);
        this.selectionService = new SelectionService();
        this.schematicService = new SchematicService(this);
        this.roomManager = new RoomManager(this);
        this.roomManager.load();

        this.gameService = new GameService(this);

        registerCommand("slots", new SlotsCommand(this));
        registerCommand("mines", new MinesCommand(this));
        registerCommand("jackpot", new JackpotCommand(this));
        CratesCommand cratesCommand = new CratesCommand(this);
        registerCommand("gcrate", cratesCommand);
        if (getCommand("gcrate") != null) {
            getCommand("gcrate").setTabCompleter(cratesCommand);
        }
        registerCommand("gamblingroom", new GamblingRoomCommand(this));
        registerCommand("casinomenu", new CasinoMenuCommand(this));
        HavocCasinoCommand admin = new HavocCasinoCommand(this);
        registerCommand("havoccasino", admin);
        if (getCommand("havoccasino") != null) {
            getCommand("havoccasino").setTabCompleter(admin);
        }

        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(new WandListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                HavocExpansion.register(this);
                WagersExpansion.register(this);
                getLogger().info("PlaceholderAPI found — placeholders registered.");
            } catch (Throwable t) {
                getLogger().warning("Failed to register PlaceholderAPI expansion: " + t.getMessage());
            }
        }

        getLogger().info("HavocCasino enabled. Using Vault economy.");
    }

    @Override
    public void onDisable() {
        if (jackpotManager != null) {
            jackpotManager.save();
        }
        if (playerSettings != null) {
            playerSettings.save();
        }
        if (roomManager != null) {
            roomManager.save();
        }
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
        } else {
            getLogger().warning("Command '" + name + "' missing from plugin.yml.");
        }
    }

    public CasinoConfig casinoConfig() {
        return casinoConfig;
    }

    public CurrencyService currencyService() {
        return currencyService;
    }

    public SlotMachine slotMachine() {
        return slotMachine;
    }

    public JackpotManager jackpotManager() {
        return jackpotManager;
    }

    public CrateManager crateManager() {
        return crateManager;
    }

    public WandItem wandItem() {
        return wandItem;
    }

    public SelectionService selections() {
        return selectionService;
    }

    public SchematicService schematics() {
        return schematicService;
    }

    public RoomManager roomManager() {
        return roomManager;
    }

    public GameService games() {
        return gameService;
    }

    public PlayerSettings playerSettings() {
        return playerSettings;
    }

    public Messages messages() {
        return messages;
    }
}

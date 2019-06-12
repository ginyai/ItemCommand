package dev.ginyai.itemcommand;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.TypeTokens;

import java.util.Optional;

import static dev.ginyai.itemcommand.ItemCommandKeys.*;

@Plugin(
        id = "itemcommand",
        name = "ItemCommand",
        version = "@version@",
        authors = {"GiNYAi"}
)
public class ItemCommandPlugin {

    @Inject
    private Logger logger;
    @Inject
    private PluginContainer container;

    @Listener
    public void onRegisterKey(GameRegistryEvent.Register<Key<?>> event) {
        ITEM_COMMAND = Key.builder()
                .type(new TypeToken<Value<ItemCommand>>() {})
                .id("itemcommand:item_command")
                .name("Item Command")
                .query(DataQuery.of("ItemCommand"))
                .build();
        event.register(ITEM_COMMAND);
        COMMAND_SOURCE = Key.builder()
                .type(new TypeToken<Value<EnumCommandSource>>() {})
                .id("itemcommand:command_source")
                .name("Command Source")
                .query(DataQuery.of("CommandSource"))
                .build();
        event.register(COMMAND_SOURCE);
        COMMAND_STRING = Key.builder()
                .type(TypeTokens.STRING_VALUE_TOKEN)
                .id("itemcommand:command_string")
                .name("Command String")
                .query(DataQuery.of("CommandString"))
                .build();
        event.register(COMMAND_STRING);
    }

    @Listener
    public void onDataRegistration(GameRegistryEvent.Register<DataRegistration<?, ?>> event) {
        Sponge.getDataManager().registerBuilder(ItemCommand.class, new ItemCommand.Builder());
        Sponge.getDataManager().registerTranslator(EnumCommandSource.class, new EnumTranslator<>(EnumCommandSource.class));
        DataRegistration.<ItemCommandData, ItemCommandData.Immutable>builder()
                .dataClass(ItemCommandData.class)
                .immutableClass(ItemCommandData.Immutable.class)
                .builder(new ItemCommandData.Builder())
                .dataName("Item Command")
                .manipulatorId("item_command")
                .buildAndRegister(container);
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event) {
        CommandSpec set = CommandSpec.builder()
                .permission("itemcommand.command.set")
                .arguments(GenericArguments.seq(
                        new ArgItemInHand(),
                        GenericArguments.enumValue(Text.of("source"), EnumCommandSource.class),
                        GenericArguments.remainingJoinedStrings(Text.of("command"))
                )).executor((src, args) -> {
                    Player player = (Player) src;
                    EnumCommandSource commandSource = args.<EnumCommandSource>getOne("source").get();
                    String commandString = args.<String>getOne("command").get();
                    ItemStack itemStack = args.<ItemStack>getOne("item").get();
                    HandType handType = args.<HandType>getOne("hand").get();
                    DataTransactionResult dataTransactionResult = itemStack.offer(new ItemCommandData(new ItemCommand(commandSource, commandString)));
                    if (dataTransactionResult.isSuccessful()) {
                        player.setItemInHand(handType, itemStack);
                        player.sendMessage(Text.of(TextColors.GREEN, "Succeed"));
                        return CommandResult.success();
                    } else {
                        player.sendMessage(Text.builder("Failed").color(TextColors.RED).onHover(TextActions.showText(Text.of(dataTransactionResult))).build());
                        return CommandResult.empty();
                    }
                }).build();
        CommandSpec info = CommandSpec.builder()
                .permission("itemcommand.command.info")
                .arguments(new ArgItemInHand())
                .executor((src, args) -> {
                    Player player = (Player) src;
                    ItemStack itemStack = args.<ItemStack>getOne("item").get();
                    HandType handType = args.<HandType>getOne("hand").get();
                    Optional<ItemCommand> optionalItemCommand = itemStack.get(ITEM_COMMAND);
                    if (optionalItemCommand.isPresent()) {
                        ItemCommand itemCommand = optionalItemCommand.get();
                        player.sendMessage(itemCommand.info());
                    } else {
                        player.sendMessage(Text.of("Empty."));
                    }
                    return CommandResult.success();
                }).build();
        CommandSpec clear = CommandSpec.builder()
                .permission("itemcommand.command.clear")
                .arguments(new ArgItemInHand())
                .executor((src, args) -> {
                    Player player = (Player) src;
                    ItemStack itemStack = args.<ItemStack>getOne("item").get();
                    HandType handType = args.<HandType>getOne("hand").get();
                    DataTransactionResult dataTransactionResult = itemStack.remove(ITEM_COMMAND);
                    if (dataTransactionResult.isSuccessful()) {
                        player.setItemInHand(handType, itemStack);
                        player.sendMessage(Text.of(TextColors.GREEN, "Succeed"));
                        return CommandResult.success();
                    } else {
                        player.sendMessage(Text.builder("Failed").color(TextColors.RED).onHover(TextActions.showText(Text.of(dataTransactionResult))).build());
                        return CommandResult.empty();
                    }
                }).build();
        CommandSpec main = CommandSpec.builder()
                .child(set, "set")
                .child(info, "info")
                .child(clear, "clear")
                .permission("itemcommand.command.root")
                .build();
        Sponge.getCommandManager().register(this, main, "itemcommand", "ic");
    }

    @Listener(order = Order.LAST)
    public void onUseItem(InteractItemEvent.Secondary event, @First Player player, @Getter("getHandType") HandType handType) {
        ItemStack copy = player.getItemInHand(handType).map(ItemStack::copy).orElse(ItemStack.empty());
        copy.get(ITEM_COMMAND).ifPresent(itemCommand -> {
            if (itemCommand.takeItem(player.getItemInHand(handType).orElse(ItemStack.empty()))) {
                CommandResult result = itemCommand.runCommand(player);
                if (result.getSuccessCount().orElse(0) <= 0) {
                    player.setItemInHand(handType, copy);
                }
            }
        });
    }
}

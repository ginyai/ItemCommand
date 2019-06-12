package dev.ginyai.itemcommand;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static dev.ginyai.itemcommand.ItemCommandKeys.COMMAND_SOURCE;
import static dev.ginyai.itemcommand.ItemCommandKeys.COMMAND_STRING;

@NonnullByDefault
public class ItemCommand implements DataSerializable, ValueContainer<ItemCommand> {

    public static final int CONTENT_VERSION = 1;
    private EnumCommandSource commandSource;
    private String commandString;
    public ItemCommand(EnumCommandSource commandSource, String commandString) {
        this.commandSource = Objects.requireNonNull(commandSource);
        this.commandString = Objects.requireNonNull(commandString);
    }

    public static ItemCommand empty() {
        return new ItemCommand(EnumCommandSource.PLAYER, "");
    }

    public EnumCommandSource getCommandSource() {
        return commandSource;
    }


    public Value<EnumCommandSource> getCommandSourceValue() {
        return Sponge.getRegistry().getValueFactory().createValue(COMMAND_SOURCE, commandSource);
    }

    public String getCommandString() {
        return commandString;
    }

    public Value<String> getCommandStringValue() {
        return Sponge.getRegistry().getValueFactory().createValue(COMMAND_STRING, commandString);
    }

    public Text info() {
        return Text.of(TextColors.GRAY, "command-source:", TextColors.GOLD, commandSource.toString(), Text.NEW_LINE,
                TextColors.GRAY, "command-string:", TextColors.GOLD, commandString);
    }

    public boolean takeItem(ItemStack itemStack) {
        if (itemStack.getQuantity() >= 1) {
            itemStack.setQuantity(itemStack.getQuantity() - 1);
            return true;
        } else {
            return false;
        }
    }

    public CommandResult runCommand(Player itemUser) {
        CommandSource source;
        switch (commandSource) {
            case CONSOLE:
                source = Sponge.getServer().getConsole();
                break;
            case PLAYER:
                source = itemUser;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return Sponge.getCommandManager().process(source, commandString.replaceAll("%player%", itemUser.getName()));
    }

    public ItemCommand fill(ItemCommand other, MergeFunction overlap) {
//        ItemControl fin = overlap.merge(this,other);
        commandSource = other.commandSource;
        commandString = other.commandString;
        return this;
    }

    @Override
    public int getContentVersion() {
        return CONTENT_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(COMMAND_SOURCE.getQuery(), commandSource)
                .set(COMMAND_STRING.getQuery(), commandString);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<E> get(Key<? extends BaseValue<E>> key) {
        if (key.equals(COMMAND_SOURCE)) {
            return Optional.of((E) commandSource);
        } else if (key.equals(COMMAND_STRING)) {
            return Optional.of((E) commandString);
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(Key<V> key) {
        if (key.equals(COMMAND_SOURCE)) {
            return Optional.of((V) getCommandSourceValue());
        } else if (key.equals(COMMAND_STRING)) {
            return Optional.of((V) getCommandStringValue());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public ItemCommand copy() {
        return new ItemCommand(commandSource, commandString);
    }

    @Override
    public Set<Key<?>> getKeys() {
        return ImmutableSet.of(COMMAND_SOURCE, COMMAND_STRING);
    }

    @Override
    public Set<ImmutableValue<?>> getValues() {
        return ImmutableSet.of(getCommandSourceValue().asImmutable(), getCommandStringValue().asImmutable());
    }

    public static class Builder extends AbstractDataBuilder<ItemCommand> {

        public Builder() {
            super(ItemCommand.class, CONTENT_VERSION);
        }

        @Override
        protected Optional<ItemCommand> buildContent(DataView container) throws InvalidDataException {
            if (container.contains(ItemCommandKeys.COMMAND_SOURCE.getQuery()) && container.contains(ItemCommandKeys.COMMAND_STRING.getQuery())) {
                EnumCommandSource commandSource = container.getObject(COMMAND_SOURCE.getQuery(), EnumCommandSource.class).orElseThrow(InvalidDataException::new);
                String commandString = container.getString(COMMAND_STRING.getQuery()).orElseThrow(InvalidDataException::new);
                return Optional.of(new ItemCommand(commandSource, commandString));
            } else {
                return Optional.empty();
            }
        }
    }
}

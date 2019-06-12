package dev.ginyai.itemcommand;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@NonnullByDefault
public class ArgItemInHand extends CommandElement {
    protected ArgItemInHand() {
        super(Text.of("item"));
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        if (!(source instanceof Player)) {
            throw args.createError(Text.of("Only player can use this command"));
        }
        Player player = (Player) source;
        Optional<ItemStack> itemStackInMainHand = player.getItemInHand(HandTypes.MAIN_HAND);
        if (itemStackInMainHand.isPresent() && !itemStackInMainHand.get().isEmpty()) {
            context.putArg("hand", HandTypes.MAIN_HAND);
            context.putArg("item", itemStackInMainHand.get());
            return;
        }
        Optional<ItemStack> itemStackInOffHand = player.getItemInHand(HandTypes.OFF_HAND);
        if (itemStackInOffHand.isPresent() && !itemStackInOffHand.get().isEmpty()) {
            context.putArg("hand", HandTypes.OFF_HAND);
            context.putArg("item", itemStackInOffHand.get());
            return;
        }
        throw args.createError(Text.of("Cannot find any item in hands"));
    }

    @Deprecated
    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.emptyList();
    }
}

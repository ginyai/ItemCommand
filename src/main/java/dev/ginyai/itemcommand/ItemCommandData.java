package dev.ginyai.itemcommand;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

import static dev.ginyai.itemcommand.ItemCommandKeys.ITEM_COMMAND;

@NonnullByDefault
public class ItemCommandData extends AbstractSingleData<ItemCommand, ItemCommandData, ItemCommandData.Immutable> {

    public static final int CONTENT_VERSION = 1;

    protected ItemCommandData(ItemCommand value) {
        super(value, ITEM_COMMAND);
    }

    @Override
    protected Value<?> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(ITEM_COMMAND, getValue());
    }

    @Override
    public Optional<ItemCommandData> fill(DataHolder dataHolder, MergeFunction overlap) {
        Optional<ItemCommandData> optionalData = dataHolder.get(ItemCommandData.class);
        optionalData.ifPresent(itemControlData -> getValue().fill(itemControlData.getValue(), overlap));
        return Optional.of(this);
    }

    @Override
    public Optional<ItemCommandData> from(DataContainer container) {
        return from((DataView) container);
    }

    public Optional<ItemCommandData> from(DataView view) {
        if (view.contains(ITEM_COMMAND.getQuery())) {
            this.setValue(view.getSerializable(ITEM_COMMAND.getQuery(), ItemCommand.class).get());
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public ItemCommandData copy() {
        return new ItemCommandData(getValue());
    }

    @Override
    public Immutable asImmutable() {
        return new Immutable(getValue());
    }

    @Override
    public int getContentVersion() {
        return CONTENT_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(ITEM_COMMAND.getQuery(), getValue());
    }

    public static class Immutable extends AbstractImmutableSingleData<ItemCommand, Immutable, ItemCommandData> {

        protected Immutable(ItemCommand value) {
            super(value, ITEM_COMMAND);
        }

        @Override
        protected ImmutableValue<?> getValueGetter() {
            return Sponge.getRegistry().getValueFactory().createValue(ITEM_COMMAND, getValue()).asImmutable();
        }

        @Override
        public ItemCommandData asMutable() {
            return new ItemCommandData(getValue());
        }

        @Override
        public int getContentVersion() {
            return CONTENT_VERSION;
        }

        @Override
        public DataContainer toContainer() {
            return super.toContainer()
                    .set(ITEM_COMMAND.getQuery(), getValue());
        }
    }

    public static class Builder extends AbstractDataBuilder<ItemCommandData> implements DataManipulatorBuilder<ItemCommandData, ItemCommandData.Immutable> {
        public Builder() {
            super(ItemCommandData.class, CONTENT_VERSION);
        }

        @Override
        public ItemCommandData create() {
            return new ItemCommandData(ItemCommand.empty());
        }

        @Override
        public Optional<ItemCommandData> createFrom(DataHolder dataHolder) {
            return create().fill(dataHolder);
        }

        @Override
        protected Optional<ItemCommandData> buildContent(DataView container) throws InvalidDataException {
            if (container.contains(ITEM_COMMAND.getQuery())) {
                ItemCommand command = container.getSerializable(ITEM_COMMAND.getQuery(), ItemCommand.class).get();
                return Optional.of(new ItemCommandData(command));
            } else {
                return Optional.empty();
            }
        }
    }
}

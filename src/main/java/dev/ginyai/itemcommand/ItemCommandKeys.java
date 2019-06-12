package dev.ginyai.itemcommand;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

public class ItemCommandKeys {
    public static Key<Value<ItemCommand>> ITEM_COMMAND = DummyObjectProvider.createExtendedFor(Key.class, "ITEM_COMMAND");
    public static Key<Value<EnumCommandSource>> COMMAND_SOURCE = DummyObjectProvider.createExtendedFor(Key.class, "COMMAND_SOURCE");
    public static Key<Value<String>> COMMAND_STRING = DummyObjectProvider.createExtendedFor(Key.class, "COMMAND_STRING");
}

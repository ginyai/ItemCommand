package dev.ginyai.itemcommand;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class EnumTranslator<T extends Enum> implements DataTranslator<T> {

    private final Class<T> theEnum;
    private final DataQuery dataQuery;

    public EnumTranslator(Class<T> theEnum) {
        this.theEnum = theEnum;
        dataQuery = DataQuery.of(theEnum.getSimpleName());
    }

    @Override
    public TypeToken<T> getToken() {
        return TypeToken.of(theEnum);
    }

    @Override
    public T translate(DataView view) throws InvalidDataException {
        return theEnum.getEnumConstants()[view.getInt(dataQuery).orElseThrow(InvalidDataException::new)];
    }

    @Override
    public DataContainer translate(T obj) throws InvalidDataException {
        return DataContainer.createNew().set(DataQuery.of(theEnum.getSimpleName()), obj.ordinal());
    }

    @Override
    public String getId() {
        return "itemcommand:" + theEnum.getSimpleName().toLowerCase();
    }

    @Override
    public String getName() {
        return theEnum.getSimpleName() + "Translator";
    }
}

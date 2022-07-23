package account.jpaConverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class ListToString implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        attribute = attribute.stream().sorted().collect(Collectors.toList());
            return attribute.toString().replaceAll("[\\[\\]]", "").toUpperCase();
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return Arrays.stream(dbData.split(",")).collect(Collectors.toList());
    }
}

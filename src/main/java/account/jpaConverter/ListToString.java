package account.jpaConverter;

import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListToString implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if(attribute == null || attribute.isEmpty()) {
            return null;
        }
        attribute = attribute.stream().sorted().collect(Collectors.toList());
            return attribute.toString().replaceAll("[\\[\\]]", "").toUpperCase();
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if(dbData == null || dbData.isEmpty()) {
            return null;
        }
        return Arrays.stream(dbData.split(",")).collect(Collectors.toList());
    }
}

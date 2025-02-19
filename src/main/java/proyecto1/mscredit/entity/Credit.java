package proyecto1.mscredit.entity;

import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "credits")
public class Credit {
    @BsonId
    private String id;
    private String customerId; // Relación con Cliente
    private Double amount;
    private String creditType; // "personal", "hipotecario"
    private Double interestRate;
    private Double creditLimit;
    private Double currentDebt;
    private Double availableLimit;
}

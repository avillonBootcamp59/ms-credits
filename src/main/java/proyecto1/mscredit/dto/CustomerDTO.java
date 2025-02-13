package proyecto1.mscredit.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private String id;
    private String name;
    private String type; // Personal o Empresarial
    private String numberDocument; // DNI o RUC
    private String email;
}

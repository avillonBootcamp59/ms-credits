package com.bank.pe.mscredit.dto;

import lombok.Data;

import java.util.List;

@Data
public class CustomerResponse {
    private List<CustomerDTO> data;
    private Metadata metadata;
}

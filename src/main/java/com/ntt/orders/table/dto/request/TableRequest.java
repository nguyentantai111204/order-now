package com.ntt.orders.table.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TableRequest {
    @NotBlank(message = "Số bàn không được trống")
    private String tableNumber;

    private String qrCodeUrl;

    @NotBlank(message = "Trạng thái bàn không được trống")
    @Pattern(regexp = "AVAILABLE|OCCUPIED|RESERVED", message = "Trạng thái bàn phải là AVAILABLE, OCCUPIED hoặc RESERVED")
    private String tableStatus;

    @NotBlank(message = "Trạng thái không được trống")
    @Pattern(regexp = "ACTIVE|INACTIVE|DELETED", message = "Trạng thái phải là ACTIVE, INACTIVE hoặc DELETED")
    private String status;
}

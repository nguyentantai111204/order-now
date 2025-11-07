package com.ntt.orders.table.dto.response;

import com.ntt.orders.shared.common.enums.TableStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableResponse {
    private String id;
    private String tableNumber;
    private String tableStatus;
    private String status;
    private String qrCodeUrl;
}

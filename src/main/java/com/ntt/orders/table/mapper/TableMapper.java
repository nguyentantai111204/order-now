package com.ntt.orders.table.mapper;

import com.ntt.orders.shared.common.enums.BaseStatus;
import com.ntt.orders.shared.common.enums.TableStatus;
import com.ntt.orders.table.dto.request.TableRequest;
import com.ntt.orders.table.dto.response.TableResponse;
import com.ntt.orders.table.entity.DinningTable;
import org.springframework.stereotype.Component;

@Component
public class TableMapper {
    public DinningTable toEntity(TableRequest request){
        return DinningTable.builder()
                .tableNumber(request.getTableNumber())
                .tableStatus(TableStatus.valueOf(request.getTableStatus().toUpperCase()))
                .qrCodeUrl(request.getQrCodeUrl())
                .status(BaseStatus.valueOf(request.getStatus().toUpperCase()))
                .build();
    }
    public TableResponse fromEntity(DinningTable table){
        return TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .qrCodeUrl(table.getQrCodeUrl())
                .tableStatus(table.getTableStatus().name())
                .status(table.getStatus().name())
                .build();
    }

}

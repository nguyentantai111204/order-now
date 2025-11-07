package com.ntt.orders.table.service;

import com.google.zxing.WriterException;
import com.ntt.orders.shared.common.dto.PageResponse;
import com.ntt.orders.shared.common.response.ApiResponse;
import com.ntt.orders.table.dto.request.TableRequest;
import com.ntt.orders.table.dto.response.TableResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

public interface TableService {
    ApiResponse<TableResponse> createTable(TableRequest dto) throws IOException, WriterException;
    PageResponse<TableResponse> getAllTables(String status, String search, int page, int take);
    TableResponse getTableById(String id);
    ApiResponse<Void> deleteTable(String id);
    ApiResponse<TableResponse> updateTable(String id,TableRequest dto) throws IOException;
}

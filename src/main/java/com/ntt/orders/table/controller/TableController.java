package com.ntt.orders.table.controller;

import com.google.zxing.WriterException;
import com.ntt.orders.shared.common.dto.PageResponse;
import com.ntt.orders.shared.common.response.ApiResponse;
import com.ntt.orders.table.dto.request.TableRequest;
import com.ntt.orders.table.dto.response.TableResponse;
import com.ntt.orders.table.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @PostMapping
    public ResponseEntity<ApiResponse<TableResponse>> createTable(
            @Valid @ModelAttribute TableRequest request) throws IOException, WriterException {
        ApiResponse<TableResponse> response = tableService.createTable(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TableResponse>> updateTable(
            @PathVariable String id,
            @Valid @ModelAttribute TableRequest request) throws IOException {
        ApiResponse<TableResponse> response = tableService.updateTable(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<TableResponse>> getAllTables(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int take
    ) {
        PageResponse<TableResponse> response = tableService.getAllTables(status, search, page, take);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableResponse> getTableById(@PathVariable String id) {
        TableResponse response = tableService.getTableById(id);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable String id) {
        ApiResponse<Void> response = tableService.deleteTable(id);
        return ResponseEntity.ok(response);
    }
}

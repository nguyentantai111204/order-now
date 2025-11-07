package com.ntt.orders.table.service.Impl;

import com.cloudinary.Api;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ntt.orders.shared.common.dto.PageResponse;
import com.ntt.orders.shared.common.enums.BaseStatus;
import com.ntt.orders.shared.common.enums.TableStatus;
import com.ntt.orders.shared.common.exception.ResourceNotFoundException;
import com.ntt.orders.shared.common.response.ApiResponse;
import com.ntt.orders.shared.common.response.ResponseCode;
import com.ntt.orders.shared.service.CloudinaryService;
import com.ntt.orders.table.dto.request.TableRequest;
import com.ntt.orders.table.dto.response.TableResponse;
import com.ntt.orders.table.entity.DinningTable;
import com.ntt.orders.table.mapper.TableMapper;
import com.ntt.orders.table.repository.TableRepository;
import com.ntt.orders.table.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {
    private final TableRepository tableRepository;
    private final TableMapper tableMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public ApiResponse<TableResponse> createTable(TableRequest dto) throws IOException, WriterException {
        if (tableRepository.existsByTableNumber(dto.getTableNumber())) {
            return ApiResponse.error("Số bàn " + dto.getTableNumber() + " đã tồn tại", ResponseCode.DUPLICATE_ENTRY, null);
        }

        String qrCode = generateQRCodeImage(dto.getTableNumber());

        String uploadedUrl = cloudinaryService.uploadString(qrCode);

        DinningTable table = tableMapper.toEntity(dto);
        table.setQrCodeUrl(uploadedUrl);
        tableRepository.save(table);
        return ApiResponse.created(tableMapper.fromEntity(table));
    }

    @Override
    public PageResponse<TableResponse> getAllTables(String status, String search, int page, int take){
        Pageable pageable = PageRequest.of(page, take, Sort.by("createdAt").descending());
        Specification<DinningTable> spec = Specification.where(null);

        if (status != null && !status.isEmpty()) {
            BaseStatus baseStatus = BaseStatus.valueOf(status.toUpperCase());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), baseStatus));
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("tableNumber")), "%" + search.toLowerCase() + "%")
            );
        }

        Page<TableResponse> tablePage = tableRepository.findAll(spec, pageable)
                .map(tableMapper::fromEntity);

        return new PageResponse<>(
                tablePage.getContent(),
                tablePage.getNumber(),
                tablePage.getSize(),
                tablePage.getTotalElements(),
                tablePage.getTotalPages(),
                tablePage.isLast(),
                tablePage.isFirst(),
                tablePage.getNumberOfElements()
        );
    }

    @Override
    public TableResponse getTableById(String id){
       DinningTable table = tableRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy table id: "+id));
       return tableMapper.fromEntity(table);
    }

    @Override
    public ApiResponse<Void> deleteTable(String id){
        if (!tableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy table id: " + id);
        }
        tableRepository.deleteById(id);
        return ApiResponse.deleted();
    }

    @Override
    public ApiResponse<TableResponse> updateTable(String id, TableRequest dto) throws IOException {
        DinningTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy table id: " + id));

        Optional<DinningTable> existingTable = tableRepository.findByTableNumber(dto.getTableNumber());
        if (existingTable.isPresent() && !existingTable.get().getId().equals(id)) {
            return ApiResponse.error(
                    "Số bàn " + dto.getTableNumber() + " đã tồn tại",
                    ResponseCode.DUPLICATE_ENTRY,
                    null
            );
        }

        table.setTableNumber(dto.getTableNumber());
        table.setTableStatus(TableStatus.valueOf(dto.getTableStatus().toUpperCase()));
        table.setStatus(BaseStatus.valueOf(dto.getStatus().toUpperCase()));

        DinningTable updated = tableRepository.save(table);

        return ApiResponse.updated(tableMapper.fromEntity(updated));
    }


    private String generateQRCodeImage(String key) throws IOException, WriterException {
        String dirPath = "qrcodes";
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();

        String filePath = dirPath + "/table_" + key + ".png";
        int width = 250;
        int height = 250;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(key, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

        return filePath;
    }

}

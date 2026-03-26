package com.example.backend;

import com.example.backend.dto.request.ThongBaoExcelDto;
import com.example.backend.enums.PhamVi;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public class TestExcelLogic {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        // Simulate Row 2 of User Test: "Sai định dạng ID" with ID "ABC"
        ThongBaoExcelDto dto = new ThongBaoExcelDto();
        dto.setRowNumber(2);
        dto.setTieuDe("Sai định dạng ID");
        
        List<String> errors = new ArrayList<>();
        String phanLoaiStr = "ABC";
        try {
            Integer.parseInt(phanLoaiStr);
        } catch (NumberFormatException e) {
            errors.add("Phân loại ID phải là số nguyên");
        }
        
        dto.setErrors(errors);
        dto.setIsValid(errors.isEmpty());

        System.out.println("DTO Model:");
        System.out.println(" - isValid: " + dto.getIsValid());
        System.out.println(" - errors: " + dto.getErrors());
        
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
        System.out.println("\nJSON Serialized:");
        System.out.println(json);
    }
}

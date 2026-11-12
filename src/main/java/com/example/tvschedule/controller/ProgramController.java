package com.example.tvschedule.controller;

import com.example.tvschedule.dto.ApiResponse;
import com.example.tvschedule.entity.Program;
import com.example.tvschedule.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/programs")
@CrossOrigin(origins = "*")
public class ProgramController {

    @Autowired
    private ProgramRepository programRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Program>>> getAllPrograms() {
        List<Program> programs = programRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(programs, "프로그램 목록 조회 성공"));
    }

    @GetMapping("/{programId}")
    public ResponseEntity<ApiResponse<Program>> getProgramById(@PathVariable Long programId) {
        Optional<Program> program = programRepository.findById(programId);
        if (program.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(program.get(), "프로그램 조회 성공"));
        } else {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("PROGRAM_NOT_FOUND", "프로그램을 찾을 수 없습니다"));
        }
    }

    // (프로그램 생성 API는 교육용으로 제거됨)

    // (프로그램 수정 API는 교육용으로 제거됨)

    // (프로그램 삭제 API는 교육용으로 제거됨)
}

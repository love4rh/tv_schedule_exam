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

    @PostMapping
    public ResponseEntity<ApiResponse<Program>> createProgram(@RequestBody Program program) {
        Program savedProgram = programRepository.save(program);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(savedProgram, "프로그램이 생성되었습니다"));
    }

    @PutMapping("/{programId}")
    public ResponseEntity<ApiResponse<Program>> updateProgram(@PathVariable String programId, @RequestBody Program program) {
        if (!programRepository.existsById(programId)) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("PROGRAM_NOT_FOUND", "프로그램을 찾을 수 없습니다"));
        }
        
        program.setProgramId(programId);
        Program updatedProgram = programRepository.save(program);
        return ResponseEntity.ok(ApiResponse.success(updatedProgram, "프로그램이 수정되었습니다"));
    }

    @DeleteMapping("/{programId}")
    public ResponseEntity<ApiResponse<String>> deleteProgram(@PathVariable String programId) {
        if (!programRepository.existsById(programId)) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("PROGRAM_NOT_FOUND", "프로그램을 찾을 수 없습니다"));
        }
        
        programRepository.deleteById(programId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", "프로그램이 삭제되었습니다"));
    }
}

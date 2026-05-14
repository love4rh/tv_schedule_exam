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
public class PracProgramController {

    @Autowired
    private ProgramRepository programRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Program>>> getAllPrograms() {
        // TODO: programRepository의 findAll 함수를 사용하여 전체 프로그램 목록을 조회하고
        //       ApiResponse.success()로 감싸서 반환하세요.
        //       메시지: "프로그램 목록 조회 성공"
        return null;
    }

    @GetMapping("/{programId}")
    public ResponseEntity<ApiResponse<Program>> getProgramById(@PathVariable String programId) {
        // TODO: programId로 프로그램을 조회하세요.
        //       - 존재하면: 200 OK + ApiResponse.success()
        //       - 존재하지 않으면: 404 + ApiResponse.error("PROGRAM_NOT_FOUND", "프로그램을 찾을 수 없습니다")
        return null;
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
        // TODO: 프로그램 삭제를 구현하세요.
        //       1. programId로 존재 여부를 확인하세요.
        //       2. 존재하지 않으면 404 에러를 반환하세요.
        //       3. 존재하면 삭제 후 200 OK + "프로그램이 삭제되었습니다" 메시지를 반환하세요.
        return null;
    }
}

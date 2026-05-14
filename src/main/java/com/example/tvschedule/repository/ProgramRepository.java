package com.example.tvschedule.repository;

import com.example.tvschedule.entity.Program;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class ProgramRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Program> programRowMapper = new RowMapper<Program>() {
        @Override
        public Program mapRow(ResultSet rs, int rowNum) throws SQLException {
            Program program = new Program();
            program.setProgramId(rs.getString("program_id"));
            program.setProgramName(rs.getString("title"));
            program.setGenre(rs.getString("genre"));
            program.setDescription(rs.getString("summary"));
            return program;
        }
    };

    public List<Program> findAll() {
        String sql = "SELECT program_id, title, genre, summary FROM Program";
        return jdbcTemplate.query(sql, programRowMapper);
    }

    public Optional<Program> findById(Long programId) {
        String sql = "SELECT program_id, title, genre, summary FROM Program WHERE program_id = ?";
        List<Program> programs = jdbcTemplate.query(sql, programRowMapper, programId);
        return programs.isEmpty() ? Optional.empty() : Optional.of(programs.get(0));
    }

    public Program save(Program program) {
        if (program.getProgramId() == null) {
            return insert(program);
        } else {
            update(program);
            return program;
        }
    }

    private Program insert(Program program) {
        String sql = "INSERT INTO programs (title, genre, summary) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, program.getProgramName());
            ps.setString(2, program.getGenre());
            ps.setString(3, program.getDescription());
            return ps;
        }, keyHolder);
        
        program.setProgramId(keyHolder.getKey().toString());
        return program;
    }

    private void update(Program program) {
        String sql = "UPDATE programs SET title = ?, genre = ?, summary = ? WHERE program_id = ?";
        jdbcTemplate.update(sql, program.getProgramName(), program.getGenre(), program.getDescription(), program.getProgramId());
    }

    public void deleteById(String programId) {
        String sql = "DELETE FROM Program WHERE program_id = ?";
        jdbcTemplate.update(sql, programId);
    }

    public boolean existsById(String programId) {
        String sql = "SELECT COUNT(*) FROM Program WHERE program_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, programId);
        return count != null && count > 0;
    }
}

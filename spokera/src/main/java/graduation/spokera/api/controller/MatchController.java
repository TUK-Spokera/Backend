package graduation.spokera.api.controller;

import graduation.spokera.api.dto.MatchResponseDTO;
import graduation.spokera.api.model.Match;
import graduation.spokera.api.dto.MatchRequestDTO;
import graduation.spokera.api.service.MatchService;
import graduation.spokera.api.model.User;
import graduation.spokera.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;
    private final UserRepository userRepository;

    public MatchController(MatchService matchService, UserRepository userRepository) {
        this.matchService = matchService;
        this.userRepository = userRepository;
    }

    @PostMapping("/request")
    public ResponseEntity<MatchResponseDTO> requestMatch(@RequestBody MatchRequestDTO matchRequestDto) {
        Optional<User> userOpt = userRepository.findByUsername(matchRequestDto.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        MatchResponseDTO matchResponseDTO = matchService.findOrCreateMatch(matchRequestDto, userOpt.get());
        return ResponseEntity.ok(matchResponseDTO);
    }

    @GetMapping("/wait-list")
    public ResponseEntity<List<Match>> responseWaitingMatches(){
        List<Match> matches = matchService.getWaitingMatches();

        return ResponseEntity.ok(matches);

    }
}
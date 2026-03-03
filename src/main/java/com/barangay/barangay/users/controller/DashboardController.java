package com.barangay.barangay.users.controller;

import com.barangay.barangay.users.dto.ActivityOverviewDTO;
import com.barangay.barangay.users.dto.DashboardStats;
import com.barangay.barangay.users.dto.RecentSystemAction;
import com.barangay.barangay.users.model.User;
import com.barangay.barangay.users.repository.UserRepository;
import com.barangay.barangay.users.service.RootDashboardService;
import com.barangay.barangay.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Controller
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final RootDashboardService rootDashboardService;


    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getRootStats(@RequestParam UUID actorId) {

        //check if user is root admin
        User user = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("user not found."));

        if(!user.getRole().getRoleName().equals("ROOT_ADMIN")){
            throw new RuntimeException("Only root admin can access.");
        }

        return ResponseEntity.ok(rootDashboardService.getDashboardStats());
    }


    @GetMapping("/activity-overview")
    public ResponseEntity<ActivityOverviewDTO> getActivityOverview(@RequestParam UUID actorId) {
        User user = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("user not found."));

        if(!user.getRole().getRoleName().equals("ROOT_ADMIN")){
            throw new RuntimeException("Only root admin can access.");
        }
        return ResponseEntity.ok(rootDashboardService.getActivityOverview());
    }


    @GetMapping("/recent-actions")
    public ResponseEntity<List<RecentSystemAction>> getRecentActions(@RequestParam UUID actorId) {
        User user = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("user not found."));

        if(!user.getRole().getRoleName().equals("ROOT_ADMIN")){
            throw new RuntimeException("Only root admin can access.");
        }

        return ResponseEntity.ok(rootDashboardService.getRecentActions());
    }
}

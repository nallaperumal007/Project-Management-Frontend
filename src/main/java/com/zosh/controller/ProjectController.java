package com.zosh.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zosh.exception.ChatException;
import com.zosh.exception.ProjectException;
import com.zosh.exception.UserException;
import com.zosh.model.Chat;
import com.zosh.model.Project;
import com.zosh.model.User;
import com.zosh.request.TokenValidationRequest;
import com.zosh.response.MessageResponse;
import com.zosh.service.EmailService;
import com.zosh.service.ProjectService;
import com.zosh.service.UserService;
import com.zosh.util.TokenGenerator;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;
    
     @Autowired
    private EmailService emailService;

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() throws ProjectException {
        List<Project> projects = projectService.getAllProjects();
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long projectId) throws ProjectException {
        Project project = projectService.getProjectById(projectId);
        return project != null ?
                new ResponseEntity<>(project, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project, @RequestHeader("Authorization") String token) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        project.setOwner(user);
        Project createdProject = projectService.createProject(project, user.getId());
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Project> updateProject( @RequestBody Project updatedProject,@PathVariable Long projectId, @RequestHeader("Authorization") String token) throws UserException, ProjectException {
        User user = userService.findUserProfileByJwt(token);
        Project updated = projectService.updateProject(updatedProject,projectId);
        return updated != null ?
                new ResponseEntity<>(updated, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<MessageResponse> deleteProject(@PathVariable Long projectId, @RequestHeader("Authorization") String token) throws UserException {
        User user = userService.findUserProfileByJwt(token);
        
        MessageResponse response =new MessageResponse(projectService.deleteProject(projectId, user.getId()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Project>> getProjectsByOwner(@PathVariable Long userId, @RequestHeader("Authorization") String token) throws ProjectException {
        try {
            User owner = userService.findUserById(userId);
            List<Project> projects = projectService.getProjectsByOwner(owner);
            return new ResponseEntity<>(projects, HttpStatus.OK);
        } catch (UserException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity< List<Project>> searchProjects(@RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) String tag) throws ProjectException {
            List<Project> projects = projectService.searchProjects(keyword, category, tag);
            return ResponseEntity.ok(projects);
    }
    
    @PostMapping("/{userId}/add-to-project/{projectId}")
    public ResponseEntity<MessageResponse> addUserToProject(@PathVariable Long userId, @PathVariable Long projectId) throws UserException, ProjectException {
        projectService.addUserToProject(projectId, userId);
        MessageResponse response =new MessageResponse("User added to the project successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{projectId}/chat")
    public ResponseEntity<Chat> getChatByProjectId(@PathVariable Long projectId) throws ProjectException, ChatException {
        Chat chat = projectService.getChatByProjectId(projectId);
        return chat != null ? ResponseEntity.ok(chat) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{projectId}/users")
    public ResponseEntity<List<User>> getUsersByProjectId(@PathVariable Long projectId) throws ProjectException {
        List<User> users = projectService.getUsersByProjectId(projectId);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/invite")
    public ResponseEntity<String> inviteToProject(@RequestParam String userEmail, @RequestParam Long projectId) {
        String token = TokenGenerator.generateToken(userEmail,projectId,64);
        try {
            emailService.sendEmailWithToken(userEmail, token);
            return ResponseEntity.ok("User invited to the project successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send email or invite user to the project");
        }
    }
    
    @PostMapping("/invite/validate")
    public ResponseEntity<String> validateTokenAndAddToTeam(@RequestBody TokenValidationRequest request) throws UserException, ProjectException {
        projectService.addUserToProjectTeam(request.getToken(), request.getProjectId(), request.getUserEmail());
        return ResponseEntity.ok("User added to the project team successfully");
    }

    
}





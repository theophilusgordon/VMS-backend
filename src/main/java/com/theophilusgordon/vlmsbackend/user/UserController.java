package com.theophilusgordon.vlmsbackend.user;

import com.theophilusgordon.vlmsbackend.constants.AuthConstants;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "Invite a user", description = "Invite a user as host")
    @PreAuthorize(AuthConstants.ADMIN_AUTHORIZATION)
    @PostMapping("/invite")
    public ResponseEntity<UserInviteResponse> inviteUser(@RequestBody @Valid UserInviteRequest request) throws MessagingException {
        return ResponseEntity.ok(userService.inviteUser(request));
    }

    @Operation(summary = "Request password change", description = "Request password change for user")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid PasswordRequestResetRequest request) throws MessagingException {
        userService.requestResetPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reset password", description = "Reset password for user")
    @PatchMapping("/reset-password/{id}")
    public ResponseEntity<Void> resetPassword(@PathVariable String id, @RequestBody @Valid PasswordResetRequest request) throws MessagingException {
        userService.resetPassword(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update user", description = "Update user information")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody @Valid UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "Change password", description = "Change password for user")
    @PatchMapping
    public ResponseEntity<Void> changePassword(
            @RequestBody PasswordChangeRequest request,
            Principal connectedUser
    ) {
        userService.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get connected user", description = "Get connected user")
    @GetMapping("/me")
    public ResponseEntity<User> getConnectedUser(Principal connectedUser) {
        User user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get user", description = "Get user by ID")
    @PreAuthorize(AuthConstants.ADMIN_AUTHORIZATION)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @Operation(summary = "Get all users", description = "Get all users paginated. Default page is 1 and default size is 10")
    @GetMapping
    public ResponseEntity<Page<User>> getUsers(Pageable pageable) {
        Page<User> users = userService.getUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get hosts", description = "Get all hosts paginated. Default page is 1 and default size is 10")
    @PreAuthorize(AuthConstants.ADMIN_AUTHORIZATION)
    @GetMapping("/hosts")
    public ResponseEntity<Page<User>> getHosts(Pageable pageable) {
        Page<User> hosts = userService.getHosts(pageable);
        return ResponseEntity.ok(hosts);
    }

    @Operation(summary = "Search hosts", description = "Search hosts by full name")
    @PreAuthorize(AuthConstants.ADMIN_AUTHORIZATION)
    @GetMapping("/hosts/search")
    public ResponseEntity<Page<User>> searchHosts(
            Pageable pageable,
            @RequestParam String query
    ) {
        Page<User> hosts = userService.searchHosts(query, pageable);
        return ResponseEntity.ok(hosts);
    }

    @Operation(summary = "Delete user", description = "Delete user by ID")
    @PreAuthorize(AuthConstants.ADMIN_AUTHORIZATION)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
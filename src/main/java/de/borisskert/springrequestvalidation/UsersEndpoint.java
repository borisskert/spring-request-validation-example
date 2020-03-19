package de.borisskert.springrequestvalidation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UsersEndpoint {

    private final UserService service;

    @Autowired
    public UsersEndpoint(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return service.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = "username")
    public ResponseEntity<?> findByUsername(@RequestParam String username) {
        return service.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid User user) {
        String createdId = service.create(user);

        return ResponseEntity.created(URI.create("/api/users/" + createdId))
                .build();
    }

    @PutMapping("/{id}")
    public void insert(@PathVariable String id, @RequestBody @Valid User user) {
        service.insert(id, user);
    }
}

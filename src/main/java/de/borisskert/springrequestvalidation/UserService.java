package de.borisskert.springrequestvalidation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> getUserById(@PathVariable String id) {
        return repository.getById(id);
    }

    public Optional<User> findByUsername(@RequestParam String username) {
        return repository.findByUsername(username);
    }

    public String create(@RequestBody @Valid User user) {
        String id = createNewId();
        repository.save(id, user);

        return id;
    }

    public void insert(@PathVariable String id, @RequestBody @Valid User user) {
        repository.save(id, user);
    }

    private String createNewId() {
        return UUID.randomUUID().toString();
    }
}

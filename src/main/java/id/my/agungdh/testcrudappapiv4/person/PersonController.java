package id.my.agungdh.testcrudappapiv4.person;

import id.my.agungdh.testcrudappapiv4.person.dto.PersonRequest;
import id.my.agungdh.testcrudappapiv4.person.dto.PersonResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonRequest request) {
        PersonResponse response = personService.create(request);
        return ResponseEntity.created(URI.create("/api/persons/" + response.uuid())).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<PersonResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(personService.list(pageable));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PersonResponse> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(personService.getByUuid(uuid));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<PersonResponse> updateByUuid(@PathVariable UUID uuid, @Valid @RequestBody PersonRequest request) {
        return ResponseEntity.ok(personService.updateByUuid(uuid, request));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteByUuid(@PathVariable UUID uuid) {
        personService.deleteByUuid(uuid);
        return ResponseEntity.noContent().build();
    }
}

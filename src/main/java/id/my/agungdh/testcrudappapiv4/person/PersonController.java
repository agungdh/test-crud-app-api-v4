package id.my.agungdh.testcrudappapiv4.person;

import id.my.agungdh.testcrudappapiv4.common.CursorPageResponse;
import id.my.agungdh.testcrudappapiv4.common.CursorPagination;
import id.my.agungdh.testcrudappapiv4.person.dto.PersonRequest;
import id.my.agungdh.testcrudappapiv4.person.dto.PersonResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Satu controller person: view + data.
 *
 * <p>{@code GET /person} → halaman HTML (Thymeleaf).
 * {@code /api/person...} → JSON (method ditandai {@code @ResponseBody}).
 */
@Controller
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    /** View: GET /person (server-render 20 data pertama). */
    @GetMapping("/person")
    public String view(Model model) {
        var page = personService.list(null, 20, null);
        model.addAttribute("persons", page.content());
        model.addAttribute("hasNext", page.hasNext());
        model.addAttribute("nextCursor", page.nextCursor());
        return "pages/persons";
    }

    @PostMapping("/api/person")
    @ResponseBody
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonRequest request) {
        PersonResponse response = personService.create(request);
        return ResponseEntity.created(URI.create("/api/person/" + response.uuid())).body(response);
    }

    @GetMapping("/api/person")
    @ResponseBody
    public ResponseEntity<CursorPageResponse<PersonResponse>> list(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = CursorPagination.DEFAULT_SIZE_VALUE) int size,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(personService.list(cursor, size, sort));
    }

    @GetMapping("/api/person/{uuid}")
    @ResponseBody
    public ResponseEntity<PersonResponse> getByUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(personService.getByUuid(uuid));
    }

    @PutMapping("/api/person/{uuid}")
    @ResponseBody
    public ResponseEntity<PersonResponse> updateByUuid(@PathVariable UUID uuid, @Valid @RequestBody PersonRequest request) {
        return ResponseEntity.ok(personService.updateByUuid(uuid, request));
    }

    @DeleteMapping("/api/person/{uuid}")
    @ResponseBody
    public ResponseEntity<Void> deleteByUuid(@PathVariable UUID uuid) {
        personService.deleteByUuid(uuid);
        return ResponseEntity.noContent().build();
    }
}

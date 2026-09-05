package id.my.agungdh.testcrudappapiv4.person;

import id.my.agungdh.testcrudappapiv4.common.CursorPageResponse;
import id.my.agungdh.testcrudappapiv4.person.dto.PersonRequest;
import id.my.agungdh.testcrudappapiv4.person.dto.PersonResponse;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonCursorRepository personCursorRepository;
    private final PersonMapper personMapper;

    private static final Set<String> SORT_FIELDS = Set.of("id", "name", "birthDate", "createdAt");

    @Transactional
    public PersonResponse create(PersonRequest request) {
        Person person = personMapper.toEntity(request);
        return personMapper.toResponse(personRepository.save(person));
    }

    @Transactional(readOnly = true)
    public PersonResponse getByUuid(UUID uuid) {
        return personMapper.toResponse(findLiveByUuid(uuid));
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PersonResponse> list(UUID cursor, int size, String sort) {
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be between 1 and 100");
        }
        String field = "id";
        boolean desc = true;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            field = parts[0].trim();
            if (!SORT_FIELDS.contains(field)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid sort field: " + field + " (allowed: id, name, birthDate, createdAt)");
            }
            if (parts.length > 1) {
                String direction = parts[1].trim().toLowerCase();
                if (direction.equals("asc")) {
                    desc = false;
                } else if (!direction.equals("desc")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid sort direction: " + parts[1].trim() + " (allowed: asc, desc)");
                }
            }
        }
        Person cursorEntity = null;
        if (cursor != null) {
            cursorEntity = personRepository.findByUuid(cursor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor: " + cursor));
        }
        List<Person> rows = personCursorRepository.findNext(field, desc, cursorEntity, size + 1);
        boolean hasNext = rows.size() > size;
        List<Person> page = hasNext ? rows.subList(0, size) : rows;
        UUID nextCursor = hasNext && !page.isEmpty() ? page.get(page.size() - 1).getUuid() : null;
        List<PersonResponse> content = page.stream().map(personMapper::toResponse).toList();
        return new CursorPageResponse<>(content, nextCursor, hasNext, size);
    }

    @Transactional
    public PersonResponse updateByUuid(UUID uuid, PersonRequest request) {
        Person person = findLiveByUuid(uuid);
        personMapper.updateEntity(request, person);
        return personMapper.toResponse(personRepository.save(person));
    }

    @Transactional
    public void deleteByUuid(UUID uuid) {
        Person person = findLiveByUuid(uuid);
        person.softDelete(null);
        personRepository.save(person);
    }

    private Person findLiveByUuid(UUID uuid) {
        return personRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Person not found: " + uuid));
    }
}

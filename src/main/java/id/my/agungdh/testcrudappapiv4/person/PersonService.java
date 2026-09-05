package id.my.agungdh.testcrudappapiv4.person;

import id.my.agungdh.testcrudappapiv4.person.dto.PersonRequest;
import id.my.agungdh.testcrudappapiv4.person.dto.PersonResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

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
    public Page<PersonResponse> list(Pageable pageable) {
        return personRepository.findAll(pageable).map(personMapper::toResponse);
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

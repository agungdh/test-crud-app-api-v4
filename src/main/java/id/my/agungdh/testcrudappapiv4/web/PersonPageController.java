package id.my.agungdh.testcrudappapiv4.web;

import id.my.agungdh.testcrudappapiv4.common.CursorPageResponse;
import id.my.agungdh.testcrudappapiv4.common.CursorPagination;
import id.my.agungdh.testcrudappapiv4.person.PersonService;
import id.my.agungdh.testcrudappapiv4.person.dto.PersonResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Satu controller person untuk web: view + data.
 *
 * <p>{@code GET /} → halaman HTML (Thymeleaf).
 * {@code GET /datas} → JSON (buat fetch/AJAX dari halaman).
 * REST API utama tetap di {@code /api/persons}.
 */
@Controller
@RequiredArgsConstructor
public class PersonPageController {

    private final PersonService personService;

    /** View: GET / (server-render 20 data pertama). */
    @GetMapping("/")
    public String view(Model model) {
        var page = personService.list(null, 20, null);
        model.addAttribute("persons", page.content());
        model.addAttribute("hasNext", page.hasNext());
        model.addAttribute("nextCursor", page.nextCursor());
        return "pages/persons";
    }

    /** Data: GET /datas?cursor=…&size=…&sort=… (JSON). */
    @GetMapping("/datas")
    @ResponseBody
    public CursorPageResponse<PersonResponse> datas(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = CursorPagination.DEFAULT_SIZE_VALUE) int size,
            @RequestParam(required = false) String sort) {
        return personService.list(cursor, size, sort);
    }
}

package id.my.agungdh.testcrudappapiv4.person;

import id.my.agungdh.testcrudappapiv4.TestCrudAppApiV4Application;
import id.my.agungdh.testcrudappapiv4.person.dto.PersonRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Standalone dummy-data seeder, dijalankan via
 * {@code ./gradlew seedPersons [-Pcount=N]} (default 1000).
 *
 * <p>Bukan bean / endpoint aplikasi — punya {@code main} sendiri, boot context
 * headless ({@code WebApplicationType.NONE}, tidak bentrok port 8080),
 * mapping entity tetap lewat MapStruct, insert batch 500 via
 * {@code saveAll}. Re-run aman (append, tidak ada truncate).
 */
public class PersonSeeder {

    private static final int BATCH = 500;

    private static final String[] FIRST = {
        "Budi", "Siti", "Agus", "Dewi", "Rina", "Joko", "Putri", "Hendra", "Maya", "Rizky",
        "Fitri", "Dedi", "Nina", "Yoga", "Wulan", "Fajar", "Intan", "Bagus", "Sari", "Eko",
        "Dian", "Rudi", "Ratna", "Bayu", "Nadia", "Ilham", "Tania", "Galih", "Kirana", "Lestari"
    };
    private static final String[] LAST = {
        "Santoso", "Wijaya", "Pratama", "Nugroho", "Saputra", "Hidayat", "Kusuma", "Rahmawati",
        "Setiawan", "Anggraini", "Firmansyah", "Maharani", "Gunawan", "Puspita", "Ramadhan",
        "Utami", "Hartono", "Laksmana", "Pangestu", "Wulandari"
    };
    private static final String[] STREETS = {
        "Jl. Merdeka", "Jl. Sudirman", "Jl. Gatot Subroto", "Jl. Diponegoro", "Jl. Ahmad Yani",
        "Jl. Pahlawan", "Jl. Veteran", "Jl. Kartini", "Jl. Imam Bonjol", "Jl. Hayam Wuruk"
    };
    private static final String[] CITIES = {
        "Jakarta", "Bandung", "Surabaya", "Semarang", "Yogyakarta",
        "Medan", "Makassar", "Denpasar", "Balikpapan", "Palembang"
    };

    public static void main(String[] args) {
        int count = 1000;
        for (String arg : args) {
            if (arg.startsWith("--count=")) {
                count = Integer.parseInt(arg.substring("--count=".length()));
            }
        }
        if (count < 1) {
            throw new IllegalArgumentException("--count must be >= 1, got " + count);
        }
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(TestCrudAppApiV4Application.class)
                .web(WebApplicationType.NONE)
                .run(args)) {
            PersonRepository repository = ctx.getBean(PersonRepository.class);
            PersonMapper mapper = ctx.getBean(PersonMapper.class);
            List<Person> batch = new ArrayList<>(BATCH);
            for (int i = 1; i <= count; i++) {
                batch.add(mapper.toEntity(randomRequest()));
                if (batch.size() == BATCH || i == count) {
                    repository.saveAll(batch);
                    batch.clear();
                    System.out.println("Seeded " + i + "/" + count + " persons");
                }
            }
            System.out.println("Done seeding " + count + " persons");
        }
    }

    private static PersonRequest randomRequest() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        String name = FIRST[rnd.nextInt(FIRST.length)] + " " + LAST[rnd.nextInt(LAST.length)];
        String address = STREETS[rnd.nextInt(STREETS.length)] + " No. " + (1 + rnd.nextInt(200))
                + ", " + CITIES[rnd.nextInt(CITIES.length)];
        // Umur 18–75 tahun, selalu past (lolos @Past).
        LocalDate birthDate =
                LocalDate.now().minusYears(18 + rnd.nextInt(58)).minusDays(rnd.nextInt(365));
        return new PersonRequest(name, address, birthDate, rnd.nextBoolean());
    }
}

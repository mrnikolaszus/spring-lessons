package org.nickz.spring.integration.database.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nickz.spring.database.entity.Role;
import org.nickz.spring.database.entity.User;
import org.nickz.spring.database.repository.UserRepository;
import org.nickz.spring.dto.PersonalInfo;
import org.nickz.spring.dto.UserFilter;
import org.nickz.spring.integration.IntegrationTestBase;
import org.nickz.spring.integration.annotation.IT;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;



@RequiredArgsConstructor
class UserRepositoryTest extends IntegrationTestBase {

    private final UserRepository userRepository;

    @Test
    void chickBatch(){
        var users = userRepository.findAll();
        userRepository.updateCompanyAndRole(users);
        System.err.println(users);
    }

    @Test
    void checkJdbcTemplate(){
        var users = userRepository.findAllByCompanyIdAndRole(1, Role.USER);
        org.assertj.core.api.Assertions.assertThat(users).hasSize(1);
        System.err.println(users);
    }

    @Test
    void checkAuditing(){
        var ivan = userRepository.findById(1L).get();
        ivan.setBirthDate(ivan.getBirthDate().plusYears(1L));
        userRepository.flush();
        System.out.println(ivan);
    }

    @Test
    void checkCustomImplementation(){
        UserFilter filter = new UserFilter(
                null, "%ov%", LocalDate.now()
        );
        var allByFilter = userRepository.findAllByFilter(filter);
        System.out.println(allByFilter);

    }
    @Test
    void checkProjections(){
        var users = userRepository.findAllByCompanyId(1);
        org.assertj.core.api.Assertions.assertThat(users).hasSize(2);

    }



    @Test
    void checkPageable(){
        var pageable = PageRequest.of(0, 2, Sort.by("id"));
        var slice = userRepository.findAllBy(pageable);

        slice.forEach(user -> System.err.println(user.getCompany().getName()));

        while(slice.hasNext()){
            slice = userRepository.findAllBy(slice.nextPageable());
            slice.forEach(user -> System.err.println(user.getCompany().getName()));
        }
    }

    @Test void checkSort(){
        var sortBy = Sort.sort(User.class);
        var sort = sortBy.by(User::getFirstname)
                .and(sortBy.by(User::getLastname));

        var sortById = Sort.by("firstname").and(Sort.by("lastname"));
        var users3 = userRepository.findTop3ByBirthDateBefore(LocalDate.now(), sort);
        org.assertj.core.api.Assertions.assertThat(users3).hasSize(3);

    }

    @Test
    void checkFirstTop(){
        var topUser = userRepository.findTopByOrderByIdDesc();
        assertTrue(topUser.isPresent());
        topUser.ifPresent(user -> assertEquals(5L, user.getId()));
    }

    @Test
    void checkQueries(){
        var users = userRepository.findAllBy("a", "ov");
        org.assertj.core.api.Assertions.assertThat(users).hasSize(3);
    }

    @Test
    void checkUpdate(){
        var ivan = userRepository.getById(1L);
        assertSame(Role.ADMIN, ivan.getRole());
        ivan.setBirthDate(LocalDate.now());


        var resultCount = userRepository.updateRole(Role.USER, 1L, 5L);
        assertEquals(2, resultCount);

        ivan.getCompany().getName();

        var ivan2 = userRepository.getById(1L);
        assertSame(Role.USER, ivan2.getRole());
    }

}
package com.clearsolutions.usermanager.service.impl;

import com.clearsolutions.usermanager.dto.DateRange;
import com.clearsolutions.usermanager.exceptions.custom.EntityAlreadyExistsException;
import com.clearsolutions.usermanager.exceptions.custom.EntityNotFoundException;
import com.clearsolutions.usermanager.model.User;
import com.clearsolutions.usermanager.repository.UserRepository;
import com.clearsolutions.usermanager.service.UserService;
import com.clearsolutions.usermanager.testutils.FakeDataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(classes = UserServiceImpl.class, webEnvironment = NONE)
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    public static final String USER_WITH_ID_NOT_FOUND = "User with ID: %d was not found!";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_PAGE_NUMBER = 0;

    private static final Pageable DEFAULT_PAGE_REQUEST
            = PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE, Sort.unsorted());

    @Test
    void testGetExistingUserById_ShouldNotThrowAnyException() {
        // Prepare
        long id = 1L;
        var user = FakeDataGenerator.userBuilder().build();

        when(userRepository.findById(id)).thenReturn(Optional.ofNullable(user));

        // Execute & Verify
        assertDoesNotThrow(() -> userService.getById(id));
    }

    @Test
    void testGetNotExistingUserById_ShouldThrowEntityNotFoundException() {
        // Prepare
        long userId = 1L;
        var errorMessage = String.format(USER_WITH_ID_NOT_FOUND, userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(errorMessage, EntityNotFoundException.class, () -> userService.getById(userId));
    }

    @Test
    void testFindUsersByBirthDateRange_ShouldReturnListOfUsers() {
        // Prepare
        var from = LocalDate.of(1990, 1, 1);
        var to = LocalDate.of(2000, 1, 1);
        var dateRange = new DateRange(from, to);
        var users = FakeDataGenerator.getUsers();
        var expectedUsersPage = new PageImpl<>(users, DEFAULT_PAGE_REQUEST, users.size());

        when(userRepository.findUserByBirthDateBetween(from, to, DEFAULT_PAGE_REQUEST)).thenReturn(expectedUsersPage);

        // Act
        var actualUsersPage = userService.findUsersByBirthDateRange(dateRange, DEFAULT_PAGE_REQUEST);

        // Assert
        assertEquals(expectedUsersPage.getContent().size(), actualUsersPage.getContent().size());
    }

    @Test
    void testCreateNotExistingUser_ShouldReturnSavedPublication() {
        // Prepare
        var user = FakeDataGenerator.userBuilder().build();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);

        // Execute & Verify
        assertDoesNotThrow(() -> userService.create(user));
    }

    @Test
    void testCreateAlreadyExistingUser_ShouldThrowEntityAlreadyExistsException() {
        // Prepare
        var user = FakeDataGenerator.userBuilder().build();

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        // Execute & Verify
        assertThrows("User with email: " + user.getEmail() + " already exists!",
                EntityAlreadyExistsException.class, () -> userService.create(user));
    }

    @Test
    void testUpdateExistingUser_ShouldNotThrowAnyException() {
        // Prepare
        long userId = 1L;
        var user = FakeDataGenerator.userBuilder().build();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Execute & Verify
        assertDoesNotThrow(() -> userService.update(userId, user));
    }

    @Test
    void testUpdateNotExistingUser_ShouldThrowEntityNotFoundException() {
        // Prepare
        long userId = 1L;
        var errorMessage = String.format(USER_WITH_ID_NOT_FOUND, userId);
        var user = FakeDataGenerator.userBuilder().build();
        user.setId(userId);

        when(userRepository.existsById(userId)).thenReturn(false);

        // Execute & Verify
        assertThrows(errorMessage, EntityNotFoundException.class, () -> userService.update(userId, user));
    }

    @Test
    void testDeleteExistingUserById_ShouldNotThrowAnyException() {
        // Prepare
        long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        // Execute & Verify
        assertDoesNotThrow(() -> userService.deleteById(userId));
    }

    @Test
    void testDeleteNotExistingUserById_ShouldThrowEntityNotFoundException() {
        // Prepare
        long userId = 1L;
        var errorMessage = String.format(USER_WITH_ID_NOT_FOUND, userId);

        when(userRepository.existsById(userId)).thenReturn(false);

        // Execute & Verify
        assertThrows(errorMessage, EntityNotFoundException.class, () -> userService.deleteById(userId));
    }

    @Test
    void testUpdateFirstNameOfExistingUser_ShouldReturnUpdatedUser() {
        // Prepare
        long userId = 1L;
        var newFirstName = "John";
        var existingUser = FakeDataGenerator.userBuilder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        var updatedUser = userService.updateFirstName(userId, newFirstName);

        // Assert
        assertEquals(newFirstName, updatedUser.getFirstName());
    }

    @Test
    void testUpdateFirstNameOfNotExistingUser_ShouldThrowEntityNotFoundException() {
        // Prepare
        long userId = 1L;
        var newFirstName = "John";
        var errorMessage = String.format(USER_WITH_ID_NOT_FOUND, userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(errorMessage, EntityNotFoundException.class, () -> userService.updateFirstName(userId, newFirstName));
    }

    @Test
    void testUpdateLastNameOfExistingUser_ShouldReturnUpdatedUser() {
        // Prepare
        long userId = 1L;
        var newLastName = "Doe";
        var existingUser = FakeDataGenerator.userBuilder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        var updatedUser = userService.updateLastName(userId, newLastName);

        // Assert
        assertEquals(newLastName, updatedUser.getLastName());
    }

    @Test
    void testUpdateLastNameOfNotExistingUser_ShouldThrowEntityNotFoundException() {
        // Prepare
        long userId = 1L;
        var newLastName = "Doe";
        var errorMessage = String.format(USER_WITH_ID_NOT_FOUND, userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(errorMessage, EntityNotFoundException.class, () -> userService.updateLastName(userId, newLastName));
    }

    @Test
    void testUpdatePhoneOfExistingUser_ShouldReturnUpdatedUser() {
        // Prepare
        long userId = 1L;
        var newPhone = "111-222-333";
        var existingUser = FakeDataGenerator.userBuilder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        var updatedUser = userService.updatePhone(userId, newPhone);

        // Assert
        assertEquals(newPhone, updatedUser.getPhone());
    }

    @Test
    void testUpdatePhoneOfNotExistingUser_ShouldThrowEntityNotFoundException() {
        // Prepare
        long userId = 1L;
        var newPhone = "111-222-333";
        var errorMessage = String.format(USER_WITH_ID_NOT_FOUND, userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(errorMessage, EntityNotFoundException.class, () -> userService.updatePhone(userId, newPhone));
    }

    @Test
    void testUpdateAddressOfExistingUser_ShouldReturnUpdatedUser() {
        // Prepare
        long userId = 1L;
        var newAddress = "Liberty Street";
        var existingUser = FakeDataGenerator.userBuilder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        var updatedUser = userService.updateAddress(userId, newAddress);

        // Assert
        assertEquals(newAddress, updatedUser.getAddress());
    }

    @Test
    void testUpdateAddressOfNotExistingUser_ShouldThrowEntityNotFoundException() {
        // Prepare
        long userId = 1L;
        var newAddress = "Liberty Street";
        var errorMessage = String.format(USER_WITH_ID_NOT_FOUND, userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(errorMessage, EntityNotFoundException.class, () -> userService.updateAddress(userId, newAddress));
    }

    @Test
    void testUpdateBirthdateOfExistingUser_ShouldReturnUpdatedUser() {
        // Prepare
        long userId = 1L;
        var newBirthDate = LocalDate.of(2000, 1, 1);
        var existingUser = FakeDataGenerator.userBuilder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        var updatedUser = userService.updateBirthdate(userId, newBirthDate);

        // Assert
        assertEquals(newBirthDate, updatedUser.getBirthDate());
    }

    @Test
    void testUpdateBirthdateOfNotExistingUser_ShouldThrowEntityNotFoundException() {
        // Prepare
        long userId = 1L;
        var newBirthDate = LocalDate.of(2000, 1, 1);
        var errorMessage = String.format(USER_WITH_ID_NOT_FOUND, userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(errorMessage, EntityNotFoundException.class, () -> userService.updateBirthdate(userId, newBirthDate));
    }

    @Test
    void testUpdateEmailOfExistingUser_ShouldReturnUpdatedUser() {
        // Prepare
        long userId = 1L;
        var newEmail = "john@example.com";
        var existingUser = FakeDataGenerator.userBuilder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        var updatedUser = userService.updateEmail(userId, newEmail);

        // Assert
        assertEquals(newEmail, updatedUser.getEmail());
    }

    @Test
    void testUpdateEmailOfNotExistingUser_ShouldThrowEntityNotFoundException() {
        // Prepare
        long userId = 1L;
        var newEmail = "john@example.com";
        var errorMessage = String.format(USER_WITH_ID_NOT_FOUND, userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(errorMessage, EntityNotFoundException.class, () -> userService.updateEmail(userId, newEmail));
    }

    @Test
    void testUpdateEmailOfExistingUser_WhenEmailIsNotUnique_ShouldThrowEntityAlreadyExistsException() {
        // Prepare
        long userId = 1L;
        var newEmail = "john@example.com";
        var existingUser = FakeDataGenerator.userBuilder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(newEmail))
                .thenThrow(new EntityAlreadyExistsException(User.class.getSimpleName(), "Email: " + newEmail));

        // Execute & Verify
        assertThrows("User with Email: " + newEmail + " 1 was not found!",
                EntityAlreadyExistsException.class, () -> userService.updateEmail(userId, newEmail));
    }
}

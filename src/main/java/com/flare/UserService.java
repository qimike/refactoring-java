package com.flare;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Business logic for managing users. Persistence is delegated to the
 * repositories, so this class only owns the domain rules (validation and
 * credit-limit assignment).
 */
public class UserService {
    private static final int MINIMUM_AGE = 21;
    private static final int USER_CACHE_CAPACITY = 1024;

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CreditLimitPolicy creditLimitPolicy;

    public UserService() {
        this(defaultUserRepository(), new ClientRepository(), new CreditLimitPolicy());
    }

    private static UserRepository defaultUserRepository() {
        LRUCache<List<User>> cache = LRUCacheProvider.createLRUCache(new CacheLimits(USER_CACHE_CAPACITY));
        return new CachingUserRepository(new JsonUserRepository(), cache);
    }

    public UserService(UserRepository userRepository,
            ClientRepository clientRepository,
            CreditLimitPolicy creditLimitPolicy) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.creditLimitPolicy = creditLimitPolicy;
    }

    public CompletableFuture<Boolean> addUser(String firstname, String surname, String email,
            LocalDate dateOfBirth, String clientId) {
        return CompletableFuture.supplyAsync(() -> {
            if (firstname == null || surname == null || email == null) {
                return false;
            }
            if (userRepository.findByEmail(email).join().isPresent()) {
                return false;
            }
            if (!isOfEligibleAge(dateOfBirth)) {
                return false;
            }

            Client client = clientRepository.getById(clientId).join();
            if (client == null) {
                System.err.println("Client not found");
                return false;
            }

            User user = createUser(firstname, surname, email, dateOfBirth, client);
            creditLimitPolicy.applyTo(user, client);

            return userRepository.add(user).join();
        });
    }

    public CompletableFuture<Boolean> updateUser(User user) {
        return userRepository.update(user);
    }

    public CompletableFuture<List<User>> getAllUsers() {
        return userRepository.findAll();
    }

    public CompletableFuture<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email).thenApply(user -> user.orElse(null));
    }

    private boolean isOfEligibleAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears() >= MINIMUM_AGE;
    }

    private User createUser(String firstname, String surname, String email,
            LocalDate dateOfBirth, Client client) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setClient(client);
        user.setDateOfBirth(dateOfBirth);
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setSurname(surname);
        return user;
    }
}

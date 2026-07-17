package ec.edu.espe.lab6_ci.repository;

import ec.edu.espe.lab6_ci.model.Wallet;

import java.util.Optional;

public interface WalletRepositiry {
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(String id);
    boolean existsByOwnerEmail(String ownerEmail);
}

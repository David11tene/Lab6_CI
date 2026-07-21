package ec.edu.espe.lab6_ci.service;

import ec.edu.espe.lab6_ci.dto.WalletResponse;
import ec.edu.espe.lab6_ci.model.Wallet;
import ec.edu.espe.lab6_ci.repository.WalletRepository;

import java.util.Optional;

public class WalletService {
    private final WalletRepository walletRepository;
    private final RiskClient riskClient;

    public WalletService(WalletRepository walletRepository, RiskClient riskClient){
        this.walletRepository = walletRepository;
        this.riskClient = riskClient;
    }

    //Metodo para crear billetera
    public WalletResponse createWallet(String ownerEmail, double initialBalance){
        //Vaidaciones
        if(ownerEmail == null || !ownerEmail.contains("@")){
            throw new IllegalArgumentException("Invalid owner email");
        }
        if(initialBalance<0){
            throw new IllegalArgumentException("Invalid balance must " +
                    "be greater than 0");
        }
        //Regla de negocion: el usuario bloqueado
        if (riskClient.isBlocked(ownerEmail)){
            throw new IllegalStateException("User blocked");
        }
        //Regla de negocio: no duplicar billerar por email
        if(walletRepository.existsByOwnerEmail(ownerEmail)){
            throw new IllegalStateException("Wallet already exists");
        }

        Wallet wallet = new Wallet(ownerEmail, initialBalance);
        Wallet saved = walletRepository.save(wallet);

        return  new WalletResponse(saved.getBalance(), saved.getId());
    }
    //Depositar dinero
    public double deposit(String walletId, double amount){
        if(amount<0){
            throw new IllegalArgumentException("Deposit amount must be > 0");
        }
        Optional<Wallet> found = walletRepository.findById(walletId);
        if(found.isEmpty()){
            throw new IllegalArgumentException("Wallet not found");
        }

        Wallet wallet = found.get();
        wallet.deposit(amount);

        //Persistencia los 800
        walletRepository.save(wallet);
        return wallet.getBalance();
    }

    public double withdraw(String walletId, double amount){
        if(amount<0){
            throw new IllegalArgumentException("Withdraw amount must be > 0");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        if(wallet.getBalance()<amount){
            throw new IllegalStateException("Insufficient Funds");
        }

        wallet.withdraw(amount);
        walletRepository.save(wallet);
        return wallet.getBalance();
    }
}

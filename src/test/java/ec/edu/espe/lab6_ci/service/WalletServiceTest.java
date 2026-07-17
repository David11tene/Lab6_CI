package ec.edu.espe.lab6_ci.service;

import ec.edu.espe.lab6_ci.dto.WalletResponse;
import ec.edu.espe.lab6_ci.model.Wallet;
import ec.edu.espe.lab6_ci.repository.WalletRepositiry;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.test.util.AssertionErrors;

import java.util.Optional;
import java.util.regex.Matcher;

public class WalletServiceTest {
    private WalletService walletService;
    private RiskClient riskClient;
    private WalletRepositiry walletRepositiry;

    //Arrange de todas las pruebas
    @BeforeEach
    public void setUp() {
        walletRepositiry = Mockito.mock(WalletRepositiry.class);
        riskClient = Mockito.mock(RiskClient.class);
        walletService = new WalletService(walletRepositiry, riskClient);
    }

    @Test
    void createWallet_validData_shouldSaveAndReturnResponse(){
        //Arrange
        String email ="david.teneguznay@gmail.com";
        double balance = 150.00;

        Mockito.when(riskClient.isBlocked(email)).thenReturn(false);

        Mockito.when(walletRepositiry.existsByOwnerEmail(email)).thenReturn(false);

        Mockito.when(walletRepositiry.save(ArgumentMatchers.any(Wallet.class)))
                        .thenAnswer(i -> i.getArguments()[0]);

        //Act
        WalletResponse response = walletService.createWallet(email, balance);

        //Assert
        AssertionErrors.assertNotNull("El Id del wallet no debe ser nulo", response.getWalletId());
        Assertions.assertEquals(150,response.getBalance());

        Mockito.verify(riskClient).isBlocked(email);
        Mockito.verify(walletRepositiry).existsByOwnerEmail(email);
        Mockito.verify(walletRepositiry).save(ArgumentMatchers.any(Wallet.class));
    }

    @Test
    void createWallet_invalidData_shouldThrowException_andNotCallDependencies(){
        //Arrange
        String invalidEmail ="david.teneguznay-gmail.com";
        double balance = 15.00;

        //Act+Assert
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                walletService.createWallet(invalidEmail, balance));

        //No debe llamar a ninguna de las depencias
        Mockito.verifyNoInteractions(walletRepositiry,riskClient);

    }

    @Test
    void deposit_walletNotFound_shouldThrowException(){
        //Arrange
        String walletId="No-existe";

        Mockito.when(walletRepositiry.findById(walletId)).thenReturn(Optional.empty());
        //Act+Assert
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, ()
                -> walletService.deposit(walletId,50));
        Assertions.assertEquals("Wallet not found", ex.getMessage());
        Mockito.verify(walletRepositiry).findById(walletId);
        Mockito.verify(walletRepositiry, Mockito.never()).save(ArgumentMatchers.any(Wallet.class));
    }

    @Test
    void deposit_shouldUpdateBalance_usingCaptor(){
        //Arrange
        Wallet wallet = new Wallet("david.teneguznay@gmail.com",100.00);
        String walletId=wallet.getId();

        Mockito.when(walletRepositiry.findById(walletId)).thenReturn(Optional.of(wallet));
        Mockito.when(walletRepositiry.save(ArgumentMatchers.any(Wallet.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);

        //Act
        double newBalance = walletService.deposit(walletId,30.00);

        Mockito.verify(walletRepositiry).save(captor.capture());
        Wallet saved = captor.getValue();

        Assertions.assertEquals(newBalance,saved.getBalance());

    }

}

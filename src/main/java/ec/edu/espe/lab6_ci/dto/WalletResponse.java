package ec.edu.espe.lab6_ci.dto;

public class WalletResponse {
    private String walletId;
    private double balance;

    public WalletResponse(double balance, String walletId) {
        this.balance = balance;
        this.walletId = walletId;
    }

    public String getWalletId() {
        return walletId;
    }

    public double getBalance() {
        return balance;
    }
}

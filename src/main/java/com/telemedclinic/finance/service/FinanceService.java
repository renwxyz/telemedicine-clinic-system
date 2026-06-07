package com.telemedclinic.finance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telemedclinic.finance.entity.Wallet;
import com.telemedclinic.finance.repository.WalletRepository;
import com.telemedclinic.order.entity.Order;

import java.util.Optional;

@Service
public class FinanceService {

    @Autowired
    private WalletRepository walletRepository;

    @Transactional
    public void releaseFundsToPharmacy(Order order) {
        if (order.getPharmacyId() == null) {
            System.err.println("Cannot release funds: Order " + order.getOrderId() + " does not have a valid pharmacyId.");
            return;
        }

        // Calculate payout (10% cut)
        double payout = order.getTotalAmount() * 0.90;

        Optional<Wallet> walletOpt = walletRepository.findByPharmacyId(order.getPharmacyId());
        Wallet wallet;
        if (walletOpt.isPresent()) {
            wallet = walletOpt.get();
        } else {
            wallet = new Wallet(order.getPharmacyId());
        }

        wallet.addBalance(payout);
        walletRepository.save(wallet);

        System.out.println("Funds released: Rp " + payout + " to Pharmacy ID " + order.getPharmacyId() + " for Order " + order.getOrderId());
    }
}

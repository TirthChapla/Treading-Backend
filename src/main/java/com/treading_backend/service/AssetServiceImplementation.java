package com.treading_backend.service;


import com.treading_backend.model.Asset;
import com.treading_backend.model.Coin;
import com.treading_backend.model.User;
import com.treading_backend.repository.AssetsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AssetServiceImplementation implements AssetService {

    private final AssetsRepository assetRepository;

    // ✅ constructor injection thi repo set kari lidhu
    @Autowired
    public AssetServiceImplementation(AssetsRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    // ✅ new asset create karvani service (used after buying coin)
    @Override
    public Asset createAsset(User user, Coin coin, double quantity) {
        Asset asset = new Asset();

        asset.setQuantity(quantity); // 👉 ketla coin buy karyaa
        asset.setBuyPrice(coin.getCurrentPrice()); // 👉 aa rate par lidha
        asset.setCoin(coin); // 👉 coin set kariyu (BTC, ETH etc.)
        asset.setUser(user); // 👉 user ni entry add kari

        return assetRepository.save(asset); // ❤️ save to DB
    }

    // 🔍 assetId thi asset lavvani method
    public Asset getAssetById(Long assetId) {
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
    }

    // ✅ same user hoy and assetId hoy to fetch
    @Override
    public Asset getAssetByUserAndId(Long userId, Long assetId) {
        return assetRepository.findByIdAndUserId(assetId, userId);
    }

    // ✅ ek user na badha assets lavva
    @Override
    public List<Asset> getUsersAssets(Long userId) {
        return assetRepository.findByUserId(userId);
    }

    // ❤️ update asset quantity – mostly sell/buy time call thay
    @Override
    public Asset updateAsset(Long assetId, double quantity) throws Exception {
        Asset oldAsset = getAssetById(assetId); // 👉 pela existing asset lavye

        if (oldAsset == null) {
            throw new Exception("Asset not found...");
        }

        // ✅ old qty ma add kari didhi navi qty
        oldAsset.setQuantity(quantity + oldAsset.getQuantity());

        return assetRepository.save(oldAsset); // ❤️ update and save
    }

    // 👉 check karvu ke coin user ni paashe che ke nai
    @Override
    public Asset findAssetByUserIdAndCoinId(Long userId, String coinId) throws Exception {
        return assetRepository.findByUserIdAndCoinId(userId, coinId);
    }

    // ❌ asset delete karvanu (jya quantity bahuj ochhi hoy etc.)
    public void deleteAsset(Long assetId) {
        assetRepository.deleteById(assetId); // ✅ delete kari didhu
    }
}

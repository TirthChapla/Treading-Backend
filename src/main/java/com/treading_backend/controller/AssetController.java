package com.treading_backend.controller;

import com.treading_backend.exception.UserException;
import com.treading_backend.model.Asset;
import com.treading_backend.model.User;
import com.treading_backend.service.AssetService;
import com.treading_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    @Autowired
    private UserService userService;

    // ✅ constructor thi assetService inject karavi didhu
    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }


    // ✅ single asset lavva mate (ID pr base)
    @GetMapping("/{assetId}")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long assetId) {
        // 👉 assetId thi DB mathi lavyo
        Asset asset = assetService.getAssetById(assetId);

        // ❤️ return with 200 OK
        return ResponseEntity.ok().body(asset);
    }

    // ✅ coinId + jwt (user) thi unique asset lavvani
    @GetMapping("/coin/{coinId}/user")
    public ResponseEntity<Asset> getAssetByUserIdAndCoinId(
            @PathVariable String coinId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        // 👉 token mathi user find karie
        User user = userService.findUserProfileByJwt(jwt);

        // 👉 user + coinId pr base asset lavyo
        Asset asset = assetService.findAssetByUserIdAndCoinId(user.getId(), coinId);

        return ResponseEntity.ok().body(asset); // ✅ return asset
    }

    // ❤️ ek user na badha assets lavva (portfolio jvu)
    @GetMapping()
    public ResponseEntity<List<Asset>> getAssetsForUser(
            @RequestHeader("Authorization") String jwt
    ) throws UserException {

        // 👉 jwt thi user fetch
        User user = userService.findUserProfileByJwt(jwt);

        // ✅ asset list lavvani by userId
        List<Asset> assets = assetService.getUsersAssets(user.getId());

        return ResponseEntity.ok().body(assets); // ❤️ send full list
    }
}

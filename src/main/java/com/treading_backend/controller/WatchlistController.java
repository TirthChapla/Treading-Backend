package com.treading_backend.controller;

import com.treading_backend.exception.UserException;
import com.treading_backend.model.Coin;
import com.treading_backend.model.User;
import com.treading_backend.model.Watchlist;
import com.treading_backend.service.CoinService;
import com.treading_backend.service.UserService;
import com.treading_backend.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserService userService;

    @Autowired
    private CoinService coinService;

    // ✅ constructor inject karine services set kari
    @Autowired
    public WatchlistController(WatchlistService watchlistService,
                               UserService userService) {
        this.watchlistService = watchlistService;
        this.userService = userService;
    }


    // ✅ user ni watchlist return karse (JWT thi user find thase)
    @GetMapping("/user")
    public ResponseEntity<Watchlist> getUserWatchlist(
            @RequestHeader("Authorization") String jwt) throws Exception {

        // 👉 pela user find kariye token mathi
        User user = userService.findUserProfileByJwt(jwt);

        // 👉 ena userId thi watchlist lavie
        Watchlist watchlist = watchlistService.findUserWatchlist(user.getId());

        return ResponseEntity.ok(watchlist); // ✅ found, return
    }

    // ✅ first time user watchlist create karse
    @PostMapping("/create")
    public ResponseEntity<Watchlist> createWatchlist(
            @RequestHeader("Authorization") String jwt) throws UserException {

        // 👉 user auth
        User user = userService.findUserProfileByJwt(jwt);

        // ❤️ new watchlist banavi didhi
        Watchlist createdWatchlist = watchlistService.createWatchList(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdWatchlist); // ✅ 201 response
    }

    // ✅ watchlist ID thi ek specific watchlist lavie
    @GetMapping("/{watchlistId}")
    public ResponseEntity<Watchlist> getWatchlistById(
            @PathVariable Long watchlistId) throws Exception {

        // 👉 direct DB mathi find kariye id thi
        Watchlist watchlist = watchlistService.findById(watchlistId);

        return ResponseEntity.ok(watchlist); // ✅ send kari didhi
    }

    // ✅ coin add/remove thase from watchlist (toggle jvu logic che)
    @PatchMapping("/add/coin/{coinId}")
    public ResponseEntity<Coin> addItemToWatchlist(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String coinId) throws Exception {

        // 👉 user ne auth kariye pela
        User user = userService.findUserProfileByJwt(jwt);

        // 👉 coin find kariye from DB
        Coin coin = coinService.findById(coinId);

        // ❤️ if already che to remove thase, nai to add thase
        Coin addedCoin = watchlistService.addItemToWatchlist(coin, user);

        return ResponseEntity.ok(addedCoin); // ✅ final coin return
    }
}

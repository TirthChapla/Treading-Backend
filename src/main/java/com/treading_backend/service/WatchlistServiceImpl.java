package com.treading_backend.service;

import com.treading_backend.model.Coin;
import com.treading_backend.model.User;
import com.treading_backend.model.Watchlist;
import com.treading_backend.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WatchlistServiceImpl implements WatchlistService {

    @Autowired
    private WatchlistRepository watchlistRepository;

    // ✅ User ID thi watchlist find karvani
    @Override
    public Watchlist findUserWatchlist(Long userId) throws Exception {
        Watchlist watchlist = watchlistRepository.findByUserId(userId); // 👉 DB thi fetch kariyu

        if (watchlist == null) {
            throw new Exception("watch not found"); // ❌ user watchlist madej nai
        }

        return watchlist; // ✅ found, return kari didho
    }

    // ✅ navi watchlist banavvani for a new user
    @Override
    public Watchlist createWatchList(User user) {
        Watchlist watchlist = new Watchlist();

        // 👉 user set karvo important che
        watchlist.setUser(user);

        return watchlistRepository.save(watchlist); // ❤️ save into DB
    }

    // ✅ watchlist find karvi ID thi
    @Override
    public Watchlist findById(Long id) throws Exception {
        Optional<Watchlist> optionalWatchlist = watchlistRepository.findById(id);

        if (optionalWatchlist.isEmpty()) {
            throw new Exception("watch list not found"); // ❌ invalid id hoy to
        }

        return optionalWatchlist.get(); // ✅ found, aapu watchlist
    }

    // ✅ coin watchlist ma add/remove karvani
    @Override
    public Coin addItemToWatchlist(Coin coin, User user) throws Exception {
        // 👉 pela user ni watchlist lavie
        Watchlist watchlist = findUserWatchlist(user.getId());

        // 👉 already hoy to remove kari devu (toggle logic)
        if (watchlist.getCoins().contains(coin)) {
            watchlist.getCoins().remove(coin); // ❌ remove thase
        } else {
            watchlist.getCoins().add(coin); // ✅ add thase
        }

        watchlistRepository.save(watchlist); // ❤️ update DB
        return coin; // ✅ final coin return kariye
    }
}

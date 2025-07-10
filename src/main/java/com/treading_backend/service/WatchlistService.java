package com.treading_backend.service;

import com.treading_backend.model.Coin;
import com.treading_backend.model.User;
import com.treading_backend.model.Watchlist;

public interface WatchlistService {

    Watchlist findUserWatchlist(Long userId) throws Exception;

    Watchlist createWatchList(User user);

    Watchlist findById(Long id) throws Exception;

    Coin addItemToWatchlist(Coin coin,User user) throws Exception;
}

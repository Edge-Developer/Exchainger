package com.exchainger.exchainger.Model;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by OPEYEMI OLORUNLEKE on 9/20/2017.
 */

public class Profile {
    private int coins = 50;
    private String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    private boolean isSubscriber = false ;
    private final boolean isActive = true;

    public Profile() {
    }

    public Profile(int points, String email, boolean isSubscriber) {
        this.coins = points;
        this.email = email;
        this.isSubscriber = isSubscriber;
    }

    public int getCoins() {
        return coins;
    }

    public String getEmail() {
        return email;
    }

    public boolean getIsSubscriber() {
        return isSubscriber;
    }

    public boolean getIsActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "coins=" + coins +
                ", email='" + email + '\'' +
                ", isSubscriber=" + isSubscriber +
                ", isActive=" + isActive +
                '}';
    }
}

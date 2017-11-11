package com.exchainger.exchainger.Activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.exchainger.exchainger.Fragments.BuyFragment;
import com.exchainger.exchainger.Fragments.SellFragment;
import com.exchainger.exchainger.Fragments.TransactionFragment;
import com.exchainger.exchainger.Model.ChatMessage;
import com.exchainger.exchainger.Model.Profile;
import com.exchainger.exchainger.Model.TransactionRequest;
import com.exchainger.exchainger.R;
import com.exchainger.exchainger.Singleton;
import com.exchainger.exchainger.TinyDB;
import com.exchainger.exchainger.Transaction;
import com.exchainger.exchainger.TransactionDialog;
import com.exchainger.exchainger.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.exchainger.exchainger.Model.Constants.BUYER_ID;
import static com.exchainger.exchainger.Model.Constants.FIREBASE_CHILD_BUY;
import static com.exchainger.exchainger.Model.Constants.FIREBASE_CHILD_CHATS;
import static com.exchainger.exchainger.Model.Constants.FIREBASE_CHILD_SELL;
import static com.exchainger.exchainger.Model.Constants.FIREBASE_CHILD_TRANSACTIONS;
import static com.exchainger.exchainger.Model.Constants.FIREBASE_CHILD_TRANSACTION_CHATS;
import static com.exchainger.exchainger.Model.Constants.FIREBASE_CHILD_TRANSACTION_INFO;
import static com.exchainger.exchainger.Model.Constants.FIREBASE_CHILD_USERS;
import static com.exchainger.exchainger.Model.Constants.SELLER_ID;

public class MainActivity extends AppCompatActivity implements SellFragment.SellItemListener
        , BuyFragment.BuyItemListener
        , TransactionDialog.DialogListener
        , TransactionFragment.TransactionItem
        , NavigationView.OnNavigationItemSelectedListener
        , RewardedVideoAdListener {
    public static final int ADD_TRANS_REQUEST_CODE = 2;
    public final static String IS_SELLING_KEY_RESULT = "is.selling.key.result";
    public final static String IS_GIFT_CARD_KEY_RESULT = "is.gift.card.key.result";
    public final static String IS_IN_PARTS_KEY_RESULT = "is.in.parts.key.result";
    public final static String EX_RATE_KEY_RESULT = "ex.rate.key.result";
    public final static String DOLLAR_PRICE_KEY_RESULT = "dollar.price.key.result";
    public final static String ITEM_NAME_KEY_RESULT = "item.name.key.result";
    public static final String DIALOG_FRAGMENT_TAG = "dialog.fragment.tag";
    private static final int SIGN_IN_REQUEST_CODE = 1;
    private final String TAG = "MainActivity";
    private final String IS_SENT_KEY = "is.sent.key";
    private final String LAST_TAB_VISITED = "last.tab.visited";
    private ActivityMainBinding mMainBinding;
    private TinyDB isVerificationEmailSent, lastTabVisited;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mUserDatabaseRef;
    private CoordinatorLayout mCoordinatorLayout;
    private FirebaseDatabase mDatabase;
    private TransactionRequest transRequest;
    private String nodeKey;
    private String TRANSACTION_USER_ID; // Transaction User Id, to be able to identify who's the buyer and seller
    private RewardedVideoAd mRewardedVideoAd;
    private FirebaseUser mCurrentUser;
    private int currentTabPosition;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private InterstitialAd mInterstitialAd;
    private FirebaseUser temporaryUser;

    private TextView navHeaderEmailTxtView;
    private TextView navHeaderDisplayNameTxtView;
    private TextView navHeaderCoinsTxtView;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AdView mAdView;
    private TabLayout.OnTabSelectedListener mTabSelectListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab LayoutTab) {
            currentTabPosition = LayoutTab.getPosition();
            mFirebaseAnalytics.setCurrentScreen(MainActivity.this, LayoutTab.getText().toString(), LayoutTab.getText().toString());
            viewPager.setCurrentItem(currentTabPosition);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab LayoutTab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab LayoutTab) {

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Toolbar toolbar = mMainBinding.toolbar;
        setSupportActionBar(toolbar);

        isVerificationEmailSent = new TinyDB(this);
        lastTabVisited = new TinyDB(this);
        currentTabPosition = lastTabVisited.getInt(LAST_TAB_VISITED);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        viewPager = mMainBinding.viewPager;
        tabLayout = mMainBinding.tabLayout;

        mCoordinatorLayout = mMainBinding.coordinatorLayout;
        mDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();

        mUserDatabaseRef = firebaseDatabase.getReference().child(FIREBASE_CHILD_USERS);

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new SellFragment();
                    case 1:
                        return new BuyFragment();
                    case 2:
                        return new TransactionFragment();
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return tabLayout.getTabCount();
            }
        });

        tabLayout.addOnTabSelectedListener(mTabSelectListener);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        TabLayout.Tab tab = tabLayout.getTabAt(currentTabPosition);
        if (tab != null) {
            tab.select();
        }

        viewPager.setCurrentItem(currentTabPosition);

        DrawerLayout drawer = mMainBinding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navView = mMainBinding.navView;
        View navHeader = LayoutInflater.from(this).inflate(R.layout.nav_header_main, null);
        navHeaderEmailTxtView = navHeader.findViewById(R.id.email_address);
        navHeaderDisplayNameTxtView = navHeader.findViewById(R.id.display_name);
        navHeaderCoinsTxtView = navHeader.findViewById(R.id.coins);

        if (mCurrentUser != null)
            setUserDetails(mCurrentUser.getEmail(), mCurrentUser.getDisplayName(), getString(R.string.x_coins));
        else
            setUserDetails("annonymous", "Welcome", "Kindly Register or Sign In");

        navView.addHeaderView(navHeader);
        navView.setNavigationItemSelectedListener(this);
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
                final FirebaseUser user = auth.getCurrentUser();
                mCurrentUser = null;
                temporaryUser = null;
                navView.getMenu().findItem(R.id.nav_resend_email).setVisible(false);

                if (user == null) {
                    setUserDetails("annonymous", "Welcome", "Kindly Register or Sign In");
                    navView.getMenu().findItem(R.id.nav_sign_in).setTitle("Register/Sign In");
                } else {
                    if (user.isEmailVerified()) {
                        mCurrentUser = user;
                        //FirebaseAuth.getInstance().getCurrentUser().reload();
                        navView.getMenu().findItem(R.id.nav_sign_in).setTitle("Sign Out");
                        setUserDetails(mCurrentUser.getEmail(), mCurrentUser.getDisplayName(), getString(R.string.x_coins));
                    } else {
                        temporaryUser = user;
                        mDatabase.getReference(FIREBASE_CHILD_USERS).child(user.getUid()).setValue(new Profile());
                        setUserDetails(user.getEmail(), user.getDisplayName(), "Verify Your Email");
                        navView.getMenu().findItem(R.id.nav_resend_email).setVisible(true);
                        if (!isVerificationEmailSent.getBoolean(IS_SENT_KEY)) {
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        isVerificationEmailSent.putBoolean(IS_SENT_KEY, true);
                                        showIndefiniteSnackBar("✔✔ Verification Email Successfully Sent to " + user.getEmail()
                                                + " . Verify your account ASAP!");

                                    }
                                }
                            });
                        }
                    }

                }
            }
        };

        mAdView = findViewById(R.id.adView);
        //mAdView.loadAd(new AdRequest.Builder().build());  //don't load the Ad just yet
    }

    private void setUserDetails(String email, String name, String string) {
        navHeaderEmailTxtView.setText(email);
        navHeaderDisplayNameTxtView.setText(name);
        navHeaderCoinsTxtView.setText(string);
    }

    private void lauchSignInScreen() {
        List<AuthUI.IdpConfig> providers = new ArrayList<>();
        providers.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
        providers.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(),
                SIGN_IN_REQUEST_CODE);
    }

    private void loadRewardedVideoAd() {
        //mRewardedVideoAd.loadAd(getString(R.string.rewarded_video_ad_unit_id), new AdRequest.Builder().build());
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastTabVisited.putInt(LAST_TAB_VISITED, currentTabPosition);
        mRewardedVideoAd.pause(this);
        mAdView.pause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
        mRewardedVideoAd.resume(this);
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdView.destroy();
        mRewardedVideoAd.destroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (mCurrentUser != null)
                    showSnackBar(getString(R.string.sucess_login));
                else if (temporaryUser != null)
                    showIndefiniteSnackBar("Kindly Verify Your Email Address, Then Sign In Again!");
            } else
                Toast.makeText(MainActivity.this, "We couldn't sign you in. Please try again later.", Toast.LENGTH_LONG).show();
        } else if (requestCode == ADD_TRANS_REQUEST_CODE) {
            if (data != null) {
                boolean isSelling = data.getBooleanExtra(IS_SELLING_KEY_RESULT, false);
                boolean isGiftCard = data.getBooleanExtra(IS_GIFT_CARD_KEY_RESULT, false);
                boolean isInParts = data.getBooleanExtra(IS_IN_PARTS_KEY_RESULT, false);
                final int exRate = data.getIntExtra(EX_RATE_KEY_RESULT, 0);
                final int dollPrice = data.getIntExtra(DOLLAR_PRICE_KEY_RESULT, 0);
                final String itemName = data.getStringExtra(ITEM_NAME_KEY_RESULT);
                TransactionRequest request = new TransactionRequest(isSelling, isGiftCard, exRate, dollPrice, isInParts, itemName);
                final String CHILD;
                if (isSelling) CHILD = FIREBASE_CHILD_SELL;
                else CHILD = FIREBASE_CHILD_BUY;

                final DatabaseReference pushedTransactionReference = mDatabase.getReference(CHILD).push();
                pushedTransactionReference.setValue(request)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                showSnackBar("Successfully Added!");
                                String key = pushedTransactionReference.getKey();
                                Transaction transaction = new Transaction(
                                        true
                                        , CHILD + "ing " + itemName
                                        , new Date().getTime()
                                        , dollPrice
                                        , exRate
                                        , key
                                );

                                mDatabase.getReference(FIREBASE_CHILD_USERS)
                                        .child(mCurrentUser.getUid())
                                        .child(FIREBASE_CHILD_TRANSACTIONS)
                                        .child(key)
                                        .setValue(transaction);
                            }
                        });
            }
        }
    }

    private void showSnackBar(String msg) {
        Snackbar.make(mCoordinatorLayout, msg, Snackbar.LENGTH_LONG).show();
    }

    private void showIndefiniteSnackBar(String msg) {
        final Snackbar snackbar = Snackbar.make(mCoordinatorLayout, msg, Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.setAction(getString(android.R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }


    @Override
    public void onSellItemClicked(TransactionRequest request, String transactionKey) {
        displayAd();
        if (isUserNull())
            return;

        this.transRequest = request;
        this.nodeKey = transactionKey;
        TRANSACTION_USER_ID = BUYER_ID;
        boolean isInParts = request.getIsInParts();
        String itemNameNvalue = request.getItemNameAndValue();
        int eRate = request.getExchangePrice();
        int dollarPrice = request.getDollarPrice();
        int price = eRate * dollarPrice;

        launchDialog("Buy " + itemNameNvalue, isInParts, eRate, dollarPrice, price, false);
    }

    @Override
    public void onBuyItemClicked(TransactionRequest request, String transactionKey) {
        displayAd();
        if (isUserNull()) {
            return;
        }
        this.transRequest = request;
        this.nodeKey = transactionKey;
        TRANSACTION_USER_ID = SELLER_ID;
        String itemNameNvalue = request.getItemNameAndValue();
        int eRate = request.getExchangePrice();
        int dollarPrice = request.getDollarPrice();
        int price = eRate * dollarPrice;
        launchDialog("Sell " + itemNameNvalue, false, eRate, dollarPrice, price, true);
    }

    @Override
    public void clickedOkDialog(String message) {
        displayAd();
        Transaction transaction = new Transaction(
                true
                , transRequest.getTranactionType()
                , new Date().getTime()
                , transRequest.getDollarPrice()
                , transRequest.getExchangePrice()
                , nodeKey
        );

        mDatabase.getReference(FIREBASE_CHILD_USERS)
                .child(mCurrentUser.getUid())
                .child(FIREBASE_CHILD_TRANSACTIONS)
                .child(nodeKey)
                .setValue(transaction);

        DatabaseReference reference2chat = mDatabase.getReference(FIREBASE_CHILD_TRANSACTION_CHATS).child(nodeKey);
        reference2chat.child(FIREBASE_CHILD_TRANSACTION_INFO).setValue(transaction);
        reference2chat.child(FIREBASE_CHILD_CHATS).push().setValue(new ChatMessage(message, mCurrentUser.getDisplayName(), null));
        mDatabase.getReference(FIREBASE_CHILD_SELL).child(nodeKey).setValue(null);
        openChatActivity(nodeKey, TRANSACTION_USER_ID);
    }

    @Override
    public void onTransItemClick(String key, String transactionUserId) {
        displayAd();
        openChatActivity(key, transactionUserId);
    }

    private boolean isUserNull() {
        if (mCurrentUser == null) {
            showSnackBar(getString(R.string.register_or_sign_in));
            return true;
        }
        return false;
    }

    private void openChatActivity(String key, String transUserId) {
        Intent intent = ChatActivity.newIntent(this, key, transUserId);
        startActivity(intent);
    }

    private void launchDialog(String dialogTitle, boolean isInParts, int exchangeRate, int quantity, int price, boolean isSelling) {
        TransactionDialog prompt = TransactionDialog.newInstance(dialogTitle, isInParts, exchangeRate, quantity, price, isSelling);
        FragmentManager manager = getSupportFragmentManager();
        prompt.show(manager, DIALOG_FRAGMENT_TAG);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = mMainBinding.drawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        displayAd();
        int id = item.getItemId();
        if (id == R.id.nav_get_free_coins) {
            if (mCurrentUser == null) {
                showSnackBar(getString(R.string.register_or_sign_in));
            } else {
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                } else {
                    showSnackBar(getString(R.string.error_cant_load_video));
                }
            }
        } else if (id == R.id.nav_sign_in) {
            if (mCurrentUser == null)
                lauchSignInScreen();
            else {
                AuthUI.getInstance().signOut(this);
                showSnackBar("Successfully Signed Out!");
            }
        } else if (id == R.id.nav_resend_email) {
            if (temporaryUser != null)
                temporaryUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            isVerificationEmailSent.putBoolean(IS_SENT_KEY, true);
                            showIndefiniteSnackBar(
                                    "✔✔ Verification Email Successfully Sent to "
                                            + temporaryUser.getEmail()
                                            + " Verify your account ASAP!"
                            );
                        } else {
                            showIndefiniteSnackBar("Verification Email Not Sent. Try Again Later!");
                        }
                    }
                });
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void displayAd() {
        if (Singleton.getInstance().showAd()) {
            /*if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                requestNewInterstitial();
            }*/
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
    }

    @Override
    public void onRewarded(RewardItem item) {
        item.getAmount();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.filter_request:
                return true;
            case R.id.add_request:
                if (mCurrentUser == null)
                    showSnackBar(getString(R.string.register_or_sign_in));
                else {
                    boolean isSell;
                    switch (currentTabPosition) {
                        case 0:
                        case 1:
                            isSell = (currentTabPosition == 0);
                            Intent intent = AddRequestActivity.getIntent(MainActivity.this, isSell);
                            startActivityForResult(intent, ADD_TRANS_REQUEST_CODE);
                            break;
                        case 2:
                            break;
                    }
                }
                return true;
            default:
                return false;
        }
    }
}
package au.com.wallaceit.voicemail.activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.MessagingException;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.Account.SortType;
import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.R;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.activity.misc.AudioPlayerDialog;
import au.com.wallaceit.voicemail.activity.misc.GreetingRecorderDialog;
import au.com.wallaceit.voicemail.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;
import au.com.wallaceit.voicemail.activity.setup.AccountSettings;
import au.com.wallaceit.voicemail.activity.setup.Prefs;
import au.com.wallaceit.voicemail.controller.MessagingController;
import au.com.wallaceit.voicemail.fragment.MessageListFragment;
import au.com.wallaceit.voicemail.fragment.MessageListFragment.MessageListFragmentListener;
import au.com.wallaceit.voicemail.helper.Utility;
import au.com.wallaceit.voicemail.helper.VoicemailAttachmentHelper;
import au.com.wallaceit.voicemail.helper.VvmContacts;
import au.com.wallaceit.voicemail.mailstore.LocalFolder;
import au.com.wallaceit.voicemail.mailstore.LocalMessage;
import au.com.wallaceit.voicemail.mailstore.StorageManager;
import au.com.wallaceit.voicemail.search.LocalSearch;
import au.com.wallaceit.voicemail.search.SearchAccount;
import au.com.wallaceit.voicemail.search.SearchSpecification;
import au.com.wallaceit.voicemail.search.SearchSpecification.Attribute;
import au.com.wallaceit.voicemail.search.SearchSpecification.SearchCondition;
import au.com.wallaceit.voicemail.search.SearchSpecification.SearchField;
import au.com.wallaceit.voicemail.view.ViewSwitcher;
import au.com.wallaceit.voicemail.view.ViewSwitcher.OnSwitchCompleteListener;
import de.cketti.library.changelog.ChangeLog;


/**
 * MessageList is the primary user interface for the program. This Activity
 * shows a list of messages.
 * From this Activity the user can perform all standard message operations.
 */
public class MessageList extends K9Activity implements MessageListFragmentListener,
        OnBackStackChangedListener, OnSwipeGestureListener, OnSwitchCompleteListener {

    // for this activity
    private static final String EXTRA_SEARCH = "search";
    private static final String EXTRA_NO_THREADING = "no_threading";

    private static final String ACTION_SHORTCUT = "shortcut";
    private static final String EXTRA_SPECIAL_FOLDER = "special_folder";

    private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";

    private static final String EXTRA_BACK_TO_INBOX = "back_to_inbox";

    // used for remote search
    public static final String EXTRA_SEARCH_ACCOUNT = "au.com.wallaceit.voicemail.search_account";
    private static final String EXTRA_SEARCH_FOLDER = "au.com.wallaceit.voicemail.search_folder";

    private static final String STATE_DISPLAY_MODE = "displayMode";
    private static final String STATE_MESSAGE_LIST_WAS_DISPLAYED = "messageListWasDisplayed";

    public static void actionDisplayGreetings(Context context, Account account) {
        LocalSearch search = new LocalSearch("Greetings");
        search.addAccountUuid(account.getUuid());
        search.addAllowedFolder("Greetings");
        Intent intent = new Intent(context, au.com.wallaceit.voicemail.activity.MessageList.class);
        intent.putExtra(EXTRA_SEARCH, search);
        intent.putExtra(EXTRA_NO_THREADING, true);
        intent.putExtra(EXTRA_BACK_TO_INBOX, true);
        context.startActivity(intent);
    }

    public static void actionDisplaySearch(Context context, SearchSpecification search,
            boolean noThreading, boolean newTask) {
        actionDisplaySearch(context, search, noThreading, newTask, true);
    }

    public static void actionDisplaySearch(Context context, SearchSpecification search,
            boolean noThreading, boolean newTask, boolean clearTop) {
        context.startActivity(
                intentDisplaySearch(context, search, noThreading, newTask, clearTop));
    }

    public static Intent intentDisplaySearch(Context context, SearchSpecification search,
            boolean noThreading, boolean newTask, boolean clearTop) {
        Intent intent = new Intent(context, au.com.wallaceit.voicemail.activity.MessageList.class);
        intent.putExtra(EXTRA_SEARCH, search);
        intent.putExtra(EXTRA_NO_THREADING, noThreading);

        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        return intent;
    }

    public static Intent shortcutIntent(Context context, String specialFolder) {
        Intent intent = new Intent(context, au.com.wallaceit.voicemail.activity.MessageList.class);
        intent.setAction(ACTION_SHORTCUT);
        intent.putExtra(EXTRA_SPECIAL_FOLDER, specialFolder);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    public static Intent actionDisplayMessageIntent(Context context,
            MessageReference messageReference) {
        Intent intent = new Intent(context, au.com.wallaceit.voicemail.activity.MessageList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);
        return intent;
    }

    private StorageManager.StorageListener mStorageListener = new StorageListenerImplementation();

    private ActionBar mActionBar;
    private View mActionBarMessageList;
    private TextView mActionBarTitle;
    private TextView mActionBarSubTitle;
    private TextView mActionBarUnread;
    private Menu mMenu;

    private ViewGroup mMessageViewContainer;
    private View mMessageViewPlaceHolder;

    private MessageListFragment mMessageListFragment;
    private int mFirstBackStackId = -1;

    private Account mAccount;
    private String mFolderName;
    private boolean isGreetingsFolder = false;
    private boolean backToInbox = false;
    private LocalSearch mSearch;
    private boolean mSingleFolderMode;
    private boolean mSingleAccountMode;

    private ProgressBar mActionBarProgress;
    private MenuItem mMenuButtonCheckMail;
    private View mActionButtonIndeterminateProgress;

    /**
     * {@code true} if the message list should be displayed as flat list (i.e. no threading)
     * regardless whether or not message threading was enabled in the settings. This is used for
     * filtered views, e.g. when only displaying the unread messages in a folder.
     */
    private boolean mNoThreading;

    private MessageReference mMessageReference;

    /**
     * {@code true} when the message list was displayed once. This is used in
     * {@link #onBackPressed()} to decide whether to go from the message view to the message list or
     * finish the activity.
     */
    private boolean mMessageListWasDisplayed = false;
    private ViewSwitcher mViewSwitcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UpgradeDatabases.actionUpgradeDatabases(this, getIntent())) {
            finish();
            return;
        }

        setContentView(R.layout.message_list);
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.container);
        mViewSwitcher.setFirstInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
        mViewSwitcher.setFirstOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
        mViewSwitcher.setSecondInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
        mViewSwitcher.setSecondOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
        mViewSwitcher.setOnSwitchCompleteListener(this);

        initializeActionBar();

        // Enable gesture detection for MessageLists
        setupGestureDetector(this);

        if (!decodeExtras(getIntent())) {
            return;
        }

        findFragments();
        initializeDisplayMode(savedInstanceState);
        initializeLayout();
        initializeFragments();
        displayViews();

        ChangeLog cl = new ChangeLog(this);
        if (cl.isFirstRun()) {
            cl.getLogDialog().show();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        if (mFirstBackStackId >= 0) {
            getFragmentManager().popBackStackImmediate(mFirstBackStackId,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mFirstBackStackId = -1;
        }
        removeMessageListFragment();

        mMessageReference = null;
        mSearch = null;
        mFolderName = null;
        isGreetingsFolder = false;
        backToInbox = false;

        if (!decodeExtras(intent)) {
            return;
        }

        initializeDisplayMode(null);
        initializeFragments();
        displayViews();
    }

    /**
     * Get references to existing fragments if the activity was restarted.
     */
    private void findFragments() {
        FragmentManager fragmentManager = getFragmentManager();
        mMessageListFragment = (MessageListFragment) fragmentManager.findFragmentById(R.id.message_list_container);
    }

    /**
     * Create fragment instances if necessary.
     *
     * @see #findFragments()
     */
    private void initializeFragments() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        boolean hasMessageListFragment = (mMessageListFragment != null);

        if (!hasMessageListFragment) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            mMessageListFragment = MessageListFragment.newInstance(mSearch, false,
                    (VisualVoicemail.isThreadedViewEnabled() && !mNoThreading));
            ft.add(R.id.message_list_container, mMessageListFragment);
            ft.commit();
        }
    }

    /**
     * Set the initial display mode (message list, message view, or split view).
     *
     * <p><strong>Note:</strong>
     * This method has to be called after {@link #findFragments()} because the result depends on
     * the availability of a {@link //MessageViewFragment} instance.
     * </p>
     *
     * @param savedInstanceState
     *         The saved instance state that was passed to the activity as argument to
     *         {@link #onCreate(Bundle)}. May be {@code null}.
     */
    private void initializeDisplayMode(Bundle savedInstanceState) {

    }

    private void initializeLayout() {
        mMessageViewContainer = (ViewGroup) findViewById(R.id.message_view_container);
        mMessageViewPlaceHolder = getLayoutInflater().inflate(R.layout.empty_message_view, null);
    }

    private void displayViews() {
        showMessageList();
    }

    private boolean decodeExtras(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action) && intent.getData() != null) {
            Uri uri = intent.getData();
            List<String> segmentList = uri.getPathSegments();

            String accountId = segmentList.get(0);
            Collection<Account> accounts = Preferences.getPreferences(this).getAvailableAccounts();
            for (Account account : accounts) {
                if (String.valueOf(account.getAccountNumber()).equals(accountId)) {
                    String folderName = segmentList.get(1);
                    String messageUid = segmentList.get(2);
                    mMessageReference = new MessageReference(account.getUuid(), folderName, messageUid, null);
                    break;
                }
            }
        } else if (ACTION_SHORTCUT.equals(action)) {
            // Handle shortcut intents
            String specialFolder = intent.getStringExtra(EXTRA_SPECIAL_FOLDER);
            if (SearchAccount.UNIFIED_INBOX.equals(specialFolder)) {
                mSearch = SearchAccount.createUnifiedInboxAccount(this).getRelatedSearch();
            } else if (SearchAccount.ALL_MESSAGES.equals(specialFolder)) {
                mSearch = SearchAccount.createAllMessagesAccount(this).getRelatedSearch();
            }
        } else if (intent.getStringExtra(SearchManager.QUERY) != null) {
            // check if this intent comes from the system search ( remote )
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                //Query was received from Search Dialog
                String query = intent.getStringExtra(SearchManager.QUERY).trim();

                mSearch = new LocalSearch(getString(R.string.search_results));
                mSearch.setManualSearch(true);
                mNoThreading = true;

                mSearch.or(new SearchCondition(SearchField.SENDER, Attribute.CONTAINS, query));
                mSearch.or(new SearchCondition(SearchField.SUBJECT, Attribute.CONTAINS, query));
                mSearch.or(new SearchCondition(SearchField.MESSAGE_CONTENTS, Attribute.CONTAINS, query));

                Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
                if (appData != null) {
                    mSearch.addAccountUuid(appData.getString(EXTRA_SEARCH_ACCOUNT));
                    // searches started from a folder list activity will provide an account, but no folder
                    if (appData.getString(EXTRA_SEARCH_FOLDER) != null) {
                        mSearch.addAllowedFolder(appData.getString(EXTRA_SEARCH_FOLDER));
                    }
                } else {
                    mSearch.addAccountUuid(LocalSearch.ALL_ACCOUNTS);
                }
            }
        } else {
            // regular LocalSearch object was passed
            mSearch = intent.getParcelableExtra(EXTRA_SEARCH);
            mNoThreading = intent.getBooleanExtra(EXTRA_NO_THREADING, false);
        }

        if (mMessageReference == null) {
            mMessageReference = intent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);
        }

        if (mMessageReference != null) {
            mSearch = new LocalSearch();
            mSearch.addAccountUuid(mMessageReference.getAccountUuid());
            mSearch.addAllowedFolder(mMessageReference.getFolderName());
        }

        if (mSearch == null) {
            // We've most likely been started by an old unread widget
            String accountUuid = intent.getStringExtra("account");
            String folderName = intent.getStringExtra("folder");

            mSearch = new LocalSearch(folderName);
            mSearch.addAccountUuid((accountUuid == null) ? "invalid" : accountUuid);
            if (folderName != null) {
                mSearch.addAllowedFolder(folderName);
            }
        }

        Preferences prefs = Preferences.getPreferences(getApplicationContext());

        String[] accountUuids = mSearch.getAccountUuids();
        if (mSearch.searchAllAccounts()) {
            List<Account> accounts = prefs.getAccounts();
            mSingleAccountMode = (accounts.size() == 1);
            if (mSingleAccountMode) {
                mAccount = accounts.get(0);
            }
        } else {
            mSingleAccountMode = (accountUuids.length == 1);
            if (mSingleAccountMode) {
                mAccount = prefs.getAccount(accountUuids[0]);
            }
        }
        mSingleFolderMode = mSingleAccountMode && (mSearch.getFolderNames().size() == 1);

        if (mSingleAccountMode && (mAccount == null || !mAccount.isAvailable(this))) {
            Log.i(VisualVoicemail.LOG_TAG, "not opening MessageList of unavailable account");
            onAccountUnavailable();
            return false;
        }

        if (mSingleFolderMode) {
            mFolderName = mSearch.getFolderNames().get(0);
            if (mFolderName.equals("Greetings")){
                isGreetingsFolder = true;
                backToInbox = intent.getBooleanExtra(EXTRA_BACK_TO_INBOX, false);
            }
        }

        // now we know if we are in single account mode and need a subtitle
        mActionBarSubTitle.setVisibility((!mSingleFolderMode) ? View.GONE : View.VISIBLE);

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();

        StorageManager.getInstance(getApplication()).removeListener(mStorageListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!(this instanceof Search)) {
            //necessary b/c no guarantee Search.onStop will be called before MessageList.onResume
            //when returning from search results
            Search.setActive(false);
        }

        if (mAccount != null && !mAccount.isAvailable(this)) {
            onAccountUnavailable();
            return;
        }
        StorageManager.getInstance(getApplication()).addListener(mStorageListener);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_MESSAGE_LIST_WAS_DISPLAYED, mMessageListWasDisplayed);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mMessageListWasDisplayed = savedInstanceState.getBoolean(STATE_MESSAGE_LIST_WAS_DISPLAYED);
    }

    private void initializeActionBar() {
        mActionBar = getActionBar();

        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.actionbar_custom);

        View customView = mActionBar.getCustomView();
        mActionBarMessageList = customView.findViewById(R.id.actionbar_message_list);
        mActionBarTitle = (TextView) customView.findViewById(R.id.actionbar_title_first);
        mActionBarSubTitle = (TextView) customView.findViewById(R.id.actionbar_title_sub);
        mActionBarUnread = (TextView) customView.findViewById(R.id.actionbar_unread_count);
        mActionBarProgress = (ProgressBar) customView.findViewById(R.id.actionbar_progress);
        mActionButtonIndeterminateProgress =
                getLayoutInflater().inflate(R.layout.actionbar_indeterminate_progress_actionview, null);

        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean ret = false;
        if (KeyEvent.ACTION_DOWN == event.getAction()) {
            ret = onCustomKeyDown(event.getKeyCode(), event);
        }
        if (!ret) {
            ret = super.dispatchKeyEvent(event);
        }
        return ret;
    }

    @Override
    public void onBackPressed() {
        if (backToInbox){
            LocalSearch search = new LocalSearch("INBOX");
            search.addAccountUuid(mAccount.getUuid());
            search.addAllowedFolder("INBOX");
            MessageList.actionDisplaySearch(MessageList.this, search, true, false);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Handle hotkeys
     *
     * <p>
     * This method is called by {@link #dispatchKeyEvent(KeyEvent)} before any view had the chance
     * to consume this key event.
     * </p>
     *
     * @param keyCode
     *         The value in {@code event.getKeyCode()}.
     * @param event
     *         Description of the key event.
     *
     * @return {@code true} if this event was consumed.
     */
    public boolean onCustomKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_Q: {
                if (mMessageListFragment != null && mMessageListFragment.isSingleAccountMode()) {
                    onShowFolderList();
                }
                return true;
            }
            case KeyEvent.KEYCODE_O: {
                mMessageListFragment.onCycleSort();
                return true;
            }
            case KeyEvent.KEYCODE_I: {
                mMessageListFragment.onReverseSort();
                return true;
            }
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_D: {
                mMessageListFragment.onDelete();
                return true;
            }
            case KeyEvent.KEYCODE_S: {
                mMessageListFragment.toggleMessageSelect();
                return true;
            }
            case KeyEvent.KEYCODE_G: {
                mMessageListFragment.onToggleFlagged();
                return true;
            }
            case KeyEvent.KEYCODE_M: {
                mMessageListFragment.onMove();
                return true;
            }
            case KeyEvent.KEYCODE_Y: {
                mMessageListFragment.onCopy();
                return true;
            }
            case KeyEvent.KEYCODE_Z: {
                mMessageListFragment.onToggleRead();
                return true;
            }
            case KeyEvent.KEYCODE_H: {
                Toast toast = Toast.makeText(this, R.string.message_list_help_key, Toast.LENGTH_LONG);
                toast.show();
                return true;
            }

        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Swallow these events too to avoid the audible notification of a volume change
        if (VisualVoicemail.useVolumeKeysForListNavigationEnabled()) {
            if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                if (VisualVoicemail.DEBUG)
                    Log.v(VisualVoicemail.LOG_TAG, "Swallowed key up.");
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void onAccounts() {
        Accounts.listAccounts(this);
        finish();
    }

    private void onShowFolderList() {
        FolderList.actionHandleAccount(this, mAccount);
        finish();
    }

    private void onEditPrefs() {
        Prefs.actionPrefs(this);
    }

    private void onEditAccount() {
        AccountSettings.actionSettings(this, mAccount);
    }

    @Override
    public boolean onSearchRequested() {
        return mMessageListFragment.onSearchRequested();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home: {
                goBack();
                return true;
            }
            // MessageList
            case R.id.check_mail: {
                mMessageListFragment.checkMail();
                return true;
            }
            case R.id.set_sort_date: {
                mMessageListFragment.changeSort(SortType.SORT_DATE);
                return true;
            }
            case R.id.set_sort_sender: {
                mMessageListFragment.changeSort(SortType.SORT_SENDER);
                return true;
            }
            case R.id.set_sort_flag: {
                mMessageListFragment.changeSort(SortType.SORT_FLAGGED);
                return true;
            }
            case R.id.set_sort_unread: {
                mMessageListFragment.changeSort(SortType.SORT_UNREAD);
                return true;
            }
            case R.id.select_all: {
                mMessageListFragment.selectAll();
                return true;
            }
            case R.id.app_settings: {
                onEditPrefs();
                return true;
            }
            case R.id.account_settings: {
                onEditAccount();
                return true;
            }
            case R.id.search: {
                mMessageListFragment.onSearchRequested();
                Log.w(getPackageName(), "search selected");
                return true;
            }
            case R.id.mark_all_as_read: {
                mMessageListFragment.markAllAsRead();
                return true;
            }
            case R.id.show_folder_list: {
                onShowFolderList();
                return true;
            }
            case R.id.about:
                VisualVoicemail.showAboutDialog(MessageList.this);
                return true;

            case R.id.show_greetings:
                MessageList.actionDisplayGreetings(this, mAccount);
                return true;

            case R.id.create_greeting:
                GreetingRecorderDialog recorderDialog = new GreetingRecorderDialog(MessageList.this, mAccount);
                recorderDialog.setCanceledOnTouchOutside(false);
                recorderDialog.show();
                return true;
        }

        if (!mSingleFolderMode) {
            // None of the options after this point are "safe" for search results
            //TODO: This is not true for "unread" and "starred" searches in regular folders
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_list_option, menu);
        mMenu = menu;
        mMenuButtonCheckMail= menu.findItem(R.id.check_mail);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        configureMenu(menu);
        return true;
    }

    /**
     * Hide menu items not appropriate for the current context.
     *
     * <p><strong>Note:</strong>
     * Please adjust the comments in {@code res/menu/message_list_option.xml} if you change the
     * visibility of a menu item in this method.
     * </p>
     *
     * @param menu
     *         The {@link Menu} instance that should be modified. May be {@code null}; in that case
     *         the method does nothing and immediately returns.
     */
    private void configureMenu(Menu menu) {
        if (menu == null) {
            return;
        }

        // Set visibility of account/folder settings menu items
        if (mMessageListFragment == null) {
            menu.findItem(R.id.account_settings).setVisible(false);
            //menu.findItem(R.id.folder_settings).setVisible(false);
        } else {
            menu.findItem(R.id.account_settings).setVisible(mMessageListFragment.isSingleAccountMode());
            //menu.findItem(R.id.folder_settings).setVisible(mMessageListFragment.isSingleFolderMode());
        }

        /*
         * Set visibility of menu items related to the message view
         */
        //if (mDisplayMode == DisplayMode.MESSAGE_LIST || mMessageViewFragment == null || !mMessageViewFragment.isInitialized()) {
            //menu.findItem(R.id.next_message).setVisible(false);
            //menu.findItem(R.id.previous_message).setVisible(false);
            menu.findItem(R.id.single_message_options).setVisible(false);
            menu.findItem(R.id.delete).setVisible(false);
            //menu.findItem(R.id.compose).setVisible(false);
            //menu.findItem(R.id.archive).setVisible(false);
            menu.findItem(R.id.move).setVisible(false);
            menu.findItem(R.id.copy).setVisible(false);
            //menu.findItem(R.id.spam).setVisible(false);
            //menu.findItem(R.id.refile).setVisible(false);
            //menu.findItem(R.id.toggle_unread).setVisible(false);
            //menu.findItem(R.id.select_text).setVisible(false);
            //menu.findItem(R.id.toggle_message_view_theme).setVisible(false);
            //menu.findItem(R.id.show_headers).setVisible(false);
            //menu.findItem(R.id.hide_headers).setVisible(false);
        /*} else {
            // hide prev/next buttons in split mode
            if (mDisplayMode != DisplayMode.MESSAGE_VIEW) {
                menu.findItem(R.id.next_message).setVisible(false);
                menu.findItem(R.id.previous_message).setVisible(false);
            } else {
                MessageReference ref = mMessageViewFragment.getMessageReference();
                boolean initialized = (mMessageListFragment != null &&
                        mMessageListFragment.isLoadFinished());
                boolean canDoPrev = (initialized && !mMessageListFragment.isFirst(ref));
                boolean canDoNext = (initialized && !mMessageListFragment.isLast(ref));

                MenuItem prev = menu.findItem(R.id.previous_message);
                prev.setEnabled(canDoPrev);
                prev.getIcon().setAlpha(canDoPrev ? 255 : 127);

                MenuItem next = menu.findItem(R.id.next_message);
                next.setEnabled(canDoNext);
                next.getIcon().setAlpha(canDoNext ? 255 : 127);
            }

            MenuItem toggleTheme = menu.findItem(R.id.toggle_message_view_theme);
            if (VisualVoicemail.useFixedMessageViewTheme()) {
                toggleTheme.setVisible(false);
            } else {
                // Set title of menu item to switch to dark/light theme
                if (VisualVoicemail.getK9MessageViewTheme() == VisualVoicemail.Theme.DARK) {
                    toggleTheme.setTitle(R.string.message_view_theme_action_light);
                } else {
                    toggleTheme.setTitle(R.string.message_view_theme_action_dark);
                }
                toggleTheme.setVisible(true);
            }

            // Set title of menu item to toggle the read state of the currently displayed message
            if (mMessageViewFragment.isMessageRead()) {
                menu.findItem(R.id.toggle_unread).setTitle(R.string.mark_as_unread_action);
            } else {
                menu.findItem(R.id.toggle_unread).setTitle(R.string.mark_as_read_action);
            }

            // Jellybean has built-in long press selection support
            menu.findItem(R.id.select_text).setVisible(Build.VERSION.SDK_INT < 16);

            menu.findItem(R.id.delete).setVisible(VisualVoicemail.isMessageViewDeleteActionVisible());*/

            /*
             * Set visibility of copy, move, archive, spam in action bar and refile submenu
             */
            /*if (mMessageViewFragment.isCopyCapable()) {
                menu.findItem(R.id.copy).setVisible(VisualVoicemail.isMessageViewCopyActionVisible());
                menu.findItem(R.id.refile_copy).setVisible(true);
            } else {
                menu.findItem(R.id.copy).setVisible(false);
                menu.findItem(R.id.refile_copy).setVisible(false);
            }

            if (mMessageViewFragment.isMoveCapable()) {
                boolean canMessageBeArchived = mMessageViewFragment.canMessageBeArchived();
                boolean canMessageBeMovedToSpam = mMessageViewFragment.canMessageBeMovedToSpam();

                menu.findItem(R.id.move).setVisible(VisualVoicemail.isMessageViewMoveActionVisible());
                menu.findItem(R.id.archive).setVisible(canMessageBeArchived &&
                        VisualVoicemail.isMessageViewArchiveActionVisible());
                menu.findItem(R.id.spam).setVisible(canMessageBeMovedToSpam &&
                        VisualVoicemail.isMessageViewSpamActionVisible());

                menu.findItem(R.id.refile_move).setVisible(true);
                menu.findItem(R.id.refile_archive).setVisible(canMessageBeArchived);
                menu.findItem(R.id.refile_spam).setVisible(canMessageBeMovedToSpam);
            } else {
                menu.findItem(R.id.move).setVisible(false);
                menu.findItem(R.id.archive).setVisible(false);
                menu.findItem(R.id.spam).setVisible(false);

                menu.findItem(R.id.refile).setVisible(false);
            }

            if (mMessageViewFragment.allHeadersVisible()) {
                menu.findItem(R.id.show_headers).setVisible(false);
            } else {
                menu.findItem(R.id.hide_headers).setVisible(false);
            }
        }*/


        /*
         * Set visibility of menu items related to the message list
         */

        // Hide both search menu items by default and enable one when appropriate
        menu.findItem(R.id.search).setVisible(false);
        //menu.findItem(R.id.search_remote).setVisible(false);

        /*if (mDisplayMode == DisplayMode.MESSAGE_VIEW || mMessageListFragment == null ||
                !mMessageListFragment.isInitialized()) {
            menu.findItem(R.id.check_mail).setVisible(false);
            menu.findItem(R.id.set_sort).setVisible(false);
            menu.findItem(R.id.select_all).setVisible(false);
            //menu.findItem(R.id.send_messages).setVisible(false);
            //menu.findItem(R.id.expunge).setVisible(false);
            menu.findItem(R.id.mark_all_as_read).setVisible(false);
            menu.findItem(R.id.show_folder_list).setVisible(false);
        } else {*/
            menu.findItem(R.id.set_sort).setVisible(true);
            menu.findItem(R.id.select_all).setVisible(true);
            //menu.findItem(R.id.compose).setVisible(true);
            menu.findItem(R.id.mark_all_as_read).setVisible(
                    mMessageListFragment.isMarkAllAsReadSupported());

            if (!mMessageListFragment.isSingleAccountMode()) {
                //menu.findItem(R.id.expunge).setVisible(false);
                //menu.findItem(R.id.send_messages).setVisible(false);
                menu.findItem(R.id.show_folder_list).setVisible(false);
            } else {
                //menu.findItem(R.id.send_messages).setVisible(mMessageListFragment.isOutbox());
                //menu.findItem(R.id.expunge).setVisible(mMessageListFragment.isRemoteFolder() && mMessageListFragment.isAccountExpungeCapable());
                menu.findItem(R.id.show_folder_list).setVisible(true);
            }

            menu.findItem(R.id.check_mail).setVisible(mMessageListFragment.isCheckMailSupported());

            // If this is an explicit local search, show the option to search on the server
            /*if (!mMessageListFragment.isRemoteSearch() &&
                    mMessageListFragment.isRemoteSearchAllowed()) {
                menu.findItem(R.id.search_remote).setVisible(true);
            } else*/
            if (!mMessageListFragment.isManualSearch()) {
                menu.findItem(R.id.search).setVisible(true);
            }
        //}
            if (isGreetingsFolder){
                menu.findItem(R.id.show_greetings).setVisible(false);
                menu.findItem(R.id.mark_all_as_read).setVisible(false);
                menu.findItem(R.id.show_folder_list).setVisible(false);
            } else {
                menu.findItem(R.id.create_greeting).setVisible(false);
            }
    }

    protected void onAccountUnavailable() {
        finish();
        // TODO inform user about account unavailability using Toast
        Accounts.listAccounts(this);
    }

    public void setActionBarTitle(String title) {
        mActionBarTitle.setText(title);
    }

    public void setActionBarSubTitle(String subTitle) {
        mActionBarSubTitle.setText(subTitle);
    }

    public void setActionBarUnread(int unread) {
        if (unread == 0) {
            mActionBarUnread.setVisibility(View.GONE);
        } else {
            mActionBarUnread.setVisibility(View.VISIBLE);
            mActionBarUnread.setText(Integer.toString(unread));
        }
    }

    @Override
    public void setMessageListTitle(String title) {
        setActionBarTitle(title);
    }

    @Override
    public void setMessageListSubTitle(String subTitle) {
        setActionBarSubTitle(subTitle);
    }

    @Override
    public void setUnreadCount(int unread) {
        setActionBarUnread(unread);
    }

    @Override
    public void setMessageListProgress(int progress) {
        setProgress(progress);
    }

    @Override
    public void openMessage(MessageReference messageReference) {
        VoicemailAttachmentHelper attachmentHelper = new VoicemailAttachmentHelper(MessageList.this, MessagingController.getInstance(MessageList.this), messageReference);
        if (attachmentHelper.loadVoicemailAttachment()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // We explicitly set the ContentType in addition to the URI because some attachment viewers (such as Polaris office 3.0.x) choke on documents without a mime type
            String contentType = attachmentHelper.getAttachment().getMimeType();
            intent.setDataAndType(attachmentHelper.getAttachmentUriForSharing(), contentType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                // mContext.startActivity(intent);
                this.startActivity(intent);
            } catch (Exception e) {
                Log.e(VisualVoicemail.LOG_TAG, "Could not display attachment of type " + contentType, e);
                Toast toast = Toast.makeText(MessageList.this, getString(R.string.message_view_no_viewer, contentType), Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            Log.e(VisualVoicemail.LOG_TAG, "Error loading voicemail");
            Toast toast = Toast.makeText(MessageList.this, "Error loading voicemail, please try refreshing", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void playMessage(MessageReference messageReference) {
        VoicemailAttachmentHelper attachmentHelper = new VoicemailAttachmentHelper(MessageList.this, MessagingController.getInstance(MessageList.this), messageReference);
        if (attachmentHelper.loadVoicemailAttachment()) {
            AudioPlayerDialog dialog = new AudioPlayerDialog(MessageList.this, attachmentHelper.getCacheUri());
            dialog.show();
        } else {
            Log.e(VisualVoicemail.LOG_TAG, "Error loading voicemail");
            Toast toast = Toast.makeText(MessageList.this, "Error loading voicemail, please try refreshing", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void saveMessage(MessageReference messageReference) {
        VoicemailAttachmentHelper attachmentHelper = new VoicemailAttachmentHelper(MessageList.this, MessagingController.getInstance(MessageList.this), messageReference);
        if (attachmentHelper.loadVoicemailAttachment()) {
            File directory = new File(VisualVoicemail.getAttachmentDefaultPath());
            if (!directory.exists()) directory.mkdirs();
            File file = Utility.createUniqueFile(directory, attachmentHelper.getUniqueAttachmentFilename());
            try {
                InputStream in = attachmentHelper.getAttachmentInputStream();
                OutputStream out = new FileOutputStream(file);
                IOUtils.copy(in, out);
                out.flush();
                out.close();
                in.close();
                // Notify that the file was saved
                Toast.makeText(MessageList.this, String.format(getString(R.string.message_view_status_attachment_saved), file.toString()), Toast.LENGTH_LONG).show();
                //new MediaScannerNotifier(MessageList.this, file.toString());
                return;
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(MessageList.this, "Failed to save the voicemail", Toast.LENGTH_LONG).show();
        } else {
            Log.e(VisualVoicemail.LOG_TAG, "Error loading voicemail");
            Toast toast = Toast.makeText(MessageList.this, "Error loading voicemail, please try refreshing", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void shareMessage(MessageReference messageReference) {
        VoicemailAttachmentHelper attachmentHelper = new VoicemailAttachmentHelper(MessageList.this, MessagingController.getInstance(MessageList.this), messageReference);
        if (attachmentHelper.loadVoicemailAttachment()) {
            LocalMessage message = messageReference.restoreToLocalMessage(MessageList.this);
            String phone, name;
            if (message.getFolder().getName().equals("Greetings")) {
                phone = "greeting";
                name = "greeting";
            } else {
                VvmContacts vvmContacts = new VvmContacts(MessageList.this);
                phone = vvmContacts.extractPhoneFromVoicemailAddress(message.getFrom()[0]);
                name = vvmContacts.getDisplayName(phone);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy H:mm", Locale.ENGLISH);
            String dateStr = sdf.format(message.getSentDate());
            String mimetype = attachmentHelper.getAttachment().getMimeType();
            //Use a chooser to decide whether email or mms
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Voicemail from " + (phone.equals(name)?phone:name+" "+phone)+" at "+dateStr);
            intent.putExtra(Intent.EXTRA_STREAM,  attachmentHelper.getAttachmentUriForSharing());
            intent.setType(mimetype);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            startActivity(intent);
        } else {
            Log.e(VisualVoicemail.LOG_TAG, "Error loading voicemail");
            Toast toast = Toast.makeText(MessageList.this, "Error loading voicemail, please try refreshing", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void showMoreFromSameSender(String senderAddress) {
        LocalSearch tmpSearch = new LocalSearch("From " + senderAddress);
        tmpSearch.addAccountUuids(mSearch.getAccountUuids());
        tmpSearch.and(SearchField.SENDER, senderAddress, Attribute.CONTAINS);

        MessageListFragment fragment = MessageListFragment.newInstance(tmpSearch, false, false);

        addMessageListFragment(fragment, true);
    }

    @Override
    public void onBackStackChanged() {
        findFragments();

        configureMenu(mMenu);
    }

    @Override
    public void onSwipeRightToLeft(MotionEvent e1, MotionEvent e2) {
        mMessageListFragment.onSwipeRightToLeft(e1, e2);
    }

    @Override
    public void onSwipeLeftToRight(MotionEvent e1, MotionEvent e2) {
        mMessageListFragment.onSwipeLeftToRight(e1, e2);
    }

    private final class StorageListenerImplementation implements StorageManager.StorageListener {
        @Override
        public void onUnmount(String providerId) {
            if (mAccount != null && providerId.equals(mAccount.getLocalStorageProviderId())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onAccountUnavailable();
                    }
                });
            }
        }

        @Override
        public void onMount(String providerId) {
            // no-op
        }
    }

    private void addMessageListFragment(MessageListFragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.replace(R.id.message_list_container, fragment);
        if (addToBackStack)
            ft.addToBackStack(null);

        mMessageListFragment = fragment;

        int transactionId = ft.commit();
        if (transactionId >= 0 && mFirstBackStackId < 0) {
            mFirstBackStackId = transactionId;
        }
    }

    @Override
    public boolean startSearch(Account account, String folderName) {
        // If this search was started from a MessageList of a single folder, pass along that folder info
        // so that we can enable remote search.
        if (account != null && folderName != null) {
            final Bundle appData = new Bundle();
            appData.putString(EXTRA_SEARCH_ACCOUNT, account.getUuid());
            appData.putString(EXTRA_SEARCH_FOLDER, folderName);
            startSearch(null, false, appData, false);
        } else {
            // TODO Handle the case where we're searching from within a search result.
            startSearch(null, false, null, false);
        }

        return true;
    }

    private void showMessageViewPlaceHolder() {
        // Add placeholder view if necessary
        if (mMessageViewPlaceHolder.getParent() == null) {
            mMessageViewContainer.addView(mMessageViewPlaceHolder);
        }

        mMessageListFragment.setActiveMessage(null);
    }


    private void removeMessageListFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(mMessageListFragment);
        mMessageListFragment = null;
        ft.commit();
    }

    @Override
    public void remoteSearchStarted() {
        // Remove action button for remote search
        configureMenu(mMenu);
    }

    @Override
    public void goBack() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else if (mMessageListFragment.isManualSearch()) {
            finish();
        } else if (backToInbox) {
            onBackPressed();
        } else if (!mSingleFolderMode) {
            onAccounts();
        } else {
            onShowFolderList();
        }
    }

    @Override
    public void enableActionBarProgress(boolean enable) {
        if (mMenuButtonCheckMail != null && mMenuButtonCheckMail.isVisible()) {
            mActionBarProgress.setVisibility(ProgressBar.GONE);
            if (enable) {
                mMenuButtonCheckMail
                        .setActionView(mActionButtonIndeterminateProgress);
            } else {
                mMenuButtonCheckMail.setActionView(null);
            }
        } else {
            if (mMenuButtonCheckMail != null)
                mMenuButtonCheckMail.setActionView(null);
            if (enable) {
                mActionBarProgress.setVisibility(ProgressBar.VISIBLE);
            } else {
                mActionBarProgress.setVisibility(ProgressBar.GONE);
            }
        }
    }

    private void showMessageList() {
        mMessageListWasDisplayed = true;
        mViewSwitcher.showFirstView();
        mMessageListFragment.setActiveMessage(null);

        showDefaultTitleView();
        configureMenu(mMenu);
    }

    @Override
    public void updateMenu() {
        invalidateOptionsMenu();
    }

    private void showDefaultTitleView() {
        mActionBarMessageList.setVisibility(View.VISIBLE);

        if (mMessageListFragment != null) {
            mMessageListFragment.updateTitle();
        }
    }

    @Override
    public void onSwitchComplete(int displayedChild) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}

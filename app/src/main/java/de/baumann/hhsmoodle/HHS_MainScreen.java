/*
    This file is part of the HHS Moodle WebApp.

    HHS Moodle WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HHS Moodle WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora Native WebApp.

    If not, see <http://www.gnu.org/licenses/>.
 */

package de.baumann.hhsmoodle;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.baumann.hhsmoodle.fragmentsMain.FragmentBookmarks;
import de.baumann.hhsmoodle.fragmentsMain.FragmentInfo;
import de.baumann.hhsmoodle.fragmentsMain.FragmentNotes;
import de.baumann.hhsmoodle.helper.Activity_password;
import de.baumann.hhsmoodle.helper.Activity_settings;
import de.baumann.hhsmoodle.helper.class_SecurePreferences;
import de.baumann.hhsmoodle.helper.helper_main;
import de.baumann.hhsmoodle.helper.helper_notes;
import de.baumann.hhsmoodle.popup.Popup_bookmarks;
import de.baumann.hhsmoodle.popup.Popup_calendar;
import de.baumann.hhsmoodle.popup.Popup_info;
import de.baumann.hhsmoodle.popup.Popup_notes;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class HHS_MainScreen extends AppCompatActivity {

    private ViewPager viewPager;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        class_SecurePreferences sharedPrefSec = new class_SecurePreferences(HHS_MainScreen.this, "sharedPrefSec", "Ywn-YM.XK$b:/:&CsL8;=L,y4", true);
        String pw = sharedPrefSec.getString("protect_PW");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        setContentView(R.layout.activity_screen_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);

        if (pw != null && pw.length() > 0) {
            if (sharedPref.getBoolean("isOpened", true)) {
                helper_main.switchToActivity(HHS_MainScreen.this, Activity_password.class, "", false);
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(toolbar != null) {
            toolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String startURL = sharedPref.getString("favoriteURL", "https://moodle.huebsch.ka.schule-bw.de/moodle/");
                    final String startType = sharedPref.getString("startType", "1");

                    if (startType.equals("2")) {
                        helper_main.isOpened(HHS_MainScreen.this);
                        helper_main.switchToActivity(HHS_MainScreen.this, HHS_Browser.class, startURL, true);
                    } else if (startType.equals("1")){
                        helper_main.isOpened(HHS_MainScreen.this);
                        helper_main.switchToActivity(HHS_MainScreen.this, HHS_MainScreen.class, "", false);
                    }
                }
            });

            if (sharedPref.getBoolean ("longPress", false)){
                toolbar.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        helper_main.isClosed(HHS_MainScreen.this);
                        finishAffinity();
                        return true;
                    }
                });
            }
        }

        boolean show = sharedPref.getBoolean("help_notShow", true);

        if (show){
            final AlertDialog.Builder dialog = new AlertDialog.Builder(HHS_MainScreen.this)
                    .setTitle(R.string.dialog_help_title)
                    .setMessage(helper_main.textSpannable(getString(R.string.dialog_help)))
                    .setPositiveButton(getString(R.string.toast_yes), null)
                    .setNegativeButton(getString(R.string.toast_notAgain), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
                            dialog.cancel();
                            sharedPref.edit()
                                    .putBoolean("help_notShow", false)
                                    .apply();
                        }
                    });
            dialog.show();
        }

        helper_main.grantPermissions(HHS_MainScreen.this);

        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            sharedPref.edit()
                    .putString("handleTextTitle", intent.getStringExtra(Intent.EXTRA_SUBJECT))
                    .putString("handleTextText", intent.getStringExtra(Intent.EXTRA_TEXT))
                    .putString("handleTextIcon", "")
                    .apply();
            helper_notes.editNote(HHS_MainScreen.this);
        }

        File directory = new File(Environment.getExternalStorageDirectory() + "/HHS_Moodle/backup/");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private void setupViewPager(ViewPager viewPager) {

        final String startTab = sharedPref.getString("tabMain", "0");
        final int startTabInt = Integer.parseInt(startTab);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new FragmentInfo(), String.valueOf(getString(R.string.title_info)));
        adapter.addFragment(new FragmentBookmarks(), String.valueOf(getString(R.string.title_bookmarks)));
        adapter.addFragment(new FragmentNotes(), String.valueOf(getString(R.string.title_notes)));

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startTabInt,true);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);// add return null; to display only icons
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (sharedPref.getBoolean ("help", false)){
            menu.getItem(5).setVisible(false); // here pass the index of save menu item
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            helper_main.isOpened(HHS_MainScreen.this);
            helper_main.switchToActivity(HHS_MainScreen.this, Activity_settings.class, "", true);
        }

        if (id == R.id.action_folder) {
            String startDir = Environment.getExternalStorageDirectory() + "/HHS_Moodle/";
            helper_main.openFilePicker(HHS_MainScreen.this, viewPager, startDir);
        }

        if (id == R.id.action_not) {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            String dateCreate = format.format(date);
            sharedPref.edit().putString("handleTextCreate", dateCreate).apply();
            helper_notes.editNote(HHS_MainScreen.this);
        }

        if (id == R.id.action_shortcut) {
            final CharSequence[] options = {
                    getString(R.string.title_info),
                    getString(R.string.title_bookmarks),
                    getString(R.string.title_notes),
                    getString(R.string.bookmark_createNote),
                    getString(R.string.menu_calendar)};

            new AlertDialog.Builder(HHS_MainScreen.this)
                    .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {

                            if (options[item].equals (getString(R.string.title_info))) {
                                Intent i = new Intent(getApplicationContext(), Popup_info.class);
                                i.setAction(Intent.ACTION_MAIN);

                                Intent shortcut = new Intent();
                                shortcut.setAction(Intent.ACTION_MAIN);
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, (getString(R.string.title_info)));
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                        Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));
                                shortcut.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                                sendBroadcast(shortcut);
                                Snackbar.make(viewPager, R.string.toast_shortcut, Snackbar.LENGTH_LONG).show();
                            }

                            if (options[item].equals (getString(R.string.title_bookmarks))) {
                                Intent i = new Intent(getApplicationContext(), Popup_bookmarks.class);
                                i.setAction(Intent.ACTION_MAIN);

                                Intent shortcut = new Intent();
                                shortcut.setAction(Intent.ACTION_MAIN);
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, (getString(R.string.title_bookmarks)));
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                        Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_bookmark));
                                shortcut.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                                sendBroadcast(shortcut);
                                Snackbar.make(viewPager, R.string.toast_shortcut, Snackbar.LENGTH_LONG).show();
                            }

                            if (options[item].equals (getString(R.string.title_notes))) {
                                Intent i = new Intent(getApplicationContext(), Popup_notes.class);
                                i.setAction(Intent.ACTION_MAIN);

                                Intent shortcut = new Intent();
                                shortcut.setAction(Intent.ACTION_MAIN);
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, (getString(R.string.title_notes)));
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                        Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_note));
                                shortcut.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                                sendBroadcast(shortcut);
                                Snackbar.make(viewPager, R.string.toast_shortcut, Snackbar.LENGTH_LONG).show();
                            }

                            if (options[item].equals (getString(R.string.bookmark_createNote))) {
                                Intent i = new Intent(getApplicationContext(), de.baumann.hhsmoodle.HHS_MainScreen.class);
                                i.setAction(Intent.ACTION_SEND);

                                Intent shortcut = new Intent();
                                shortcut.setAction(Intent.ACTION_MAIN);
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, (getString(R.string.bookmark_createNote)));
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                        Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_note_plus));
                                shortcut.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                                sendBroadcast(shortcut);
                                Snackbar.make(viewPager, R.string.toast_shortcut, Snackbar.LENGTH_LONG).show();
                            }

                            if (options[item].equals (getString(R.string.menu_calendar))) {
                                Intent i = new Intent(getApplicationContext(), Popup_calendar.class);

                                Intent shortcut = new Intent();
                                shortcut.setAction(Intent.ACTION_MAIN);
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, (getString(R.string.menu_calendar)));
                                shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                        Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_calendar));
                                shortcut.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                                sendBroadcast(shortcut);
                                Snackbar.make(viewPager, R.string.toast_shortcut, Snackbar.LENGTH_LONG).show();
                            }

                        }
                    }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        helper_main.isClosed(HHS_MainScreen.this);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        helper_main.isOpened(HHS_MainScreen.this);
    }

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        helper_main.isOpened(HHS_MainScreen.this);
    }

    @Override
    protected void onStop() {
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
        helper_main.isClosed(HHS_MainScreen.this);
    }
}
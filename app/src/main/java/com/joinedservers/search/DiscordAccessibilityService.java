package com.joinedservers.search;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class DiscordAccessibilityService extends AccessibilityService {
    static DiscordAccessibilityService instance;
    private FloatingSearch overlay;
    private boolean discordForeground;
    private boolean dismissedUntilNextOpen;
    private boolean manuallyOpened;

    @Override public void onServiceConnected() {
        instance = this;
        overlay = new FloatingSearch(this, () -> { dismissedUntilNextOpen = true; manuallyOpened = false; });
    }

    void openManually() {
        manuallyOpened = true;
        if (overlay != null) overlay.setVisible(true);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && type != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return;
        CharSequence pkg = event.getPackageName();
        if (pkg == null) return;
        String name = pkg.toString();
        if (name.equals(getPackageName()) || name.equals("com.android.systemui")) return;
        boolean nowDiscord = name.equals("com.discord");
        if (nowDiscord && !discordForeground) { dismissedUntilNextOpen = false; manuallyOpened = false; }
        discordForeground = nowDiscord;
        if (overlay != null) overlay.setVisible(manuallyOpened || (nowDiscord && !dismissedUntilNextOpen));
    }

    @Override public void onInterrupt() { if (overlay != null) overlay.setVisible(false); }
    @Override public void onDestroy() {
        if (overlay != null) overlay.destroy();
        instance = null;
        super.onDestroy();
    }
}

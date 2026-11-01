// SPDX-License-Identifier: GPL-3.0-only
package com.gerstudio.cleanbot.helper;

import android.os.IBinder;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Method;

/**
 * The remote's key and trackpad motion sender. Runs on the TV as the shell user, through {@code app_process},
 * and stays up for as long as the remote is open.
 * Commands:
 * - code: injects keycode press and release
 * - t<text>: types text via keymap
 * - p<0|2>: display power mode
 * - m <action> <x> <y>: injects mouse MotionEvent (0=down, 1=up, 2=move)
 * - s <x> <y> <scrollVal>: injects mouse wheel scroll
 * - c<text>: sets clipboard and injects paste keycode (KEYCODE_PASTE 279)
 */
public final class Keys {
    private static final int WAIT_FOR_FINISH = 2;

    public static void main(String[] args) throws Exception {
        Object manager;
        Method inject;
        try {
            Class<?> global = Class.forName("android.hardware.input.InputManagerGlobal");
            manager = global.getDeclaredMethod("getInstance").invoke(null);
            inject = global.getMethod("injectInputEvent", InputEvent.class, int.class);
        } catch (ClassNotFoundException e) {
            Class<?> im = Class.forName("android.hardware.input.InputManager");
            manager = im.getDeclaredMethod("getInstance").invoke(null);
            inject = im.getMethod("injectInputEvent", InputEvent.class, int.class);
        }
        if (args.length == 1) new File(args[0]).delete();

        PrintStream out = new PrintStream(System.out, false, "UTF-8");
        out.print("ready\n");
        out.flush();
        KeyCharacterMap keymap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        for (String line; (line = in.readLine()) != null; ) {
            boolean ok;
            try {
                if (line.startsWith("p")) {
                    out.print(power(Integer.parseInt(line.substring(1).trim())) ? "1\n" : "0\n");
                    out.flush();
                    continue;
                }
                if (line.startsWith("t")) {
                    out.print(type(manager, inject, keymap, line.substring(1)) ? "1\n" : "0\n");
                    out.flush();
                    continue;
                }
                if (line.startsWith("c")) {
                    out.print(pasteClipboard(line.substring(1), manager, inject) ? "1\n" : "0\n");
                    out.flush();
                    continue;
                }
                if (line.startsWith("m")) {
                    String[] parts = line.substring(1).trim().split("\\s+");
                    int action = Integer.parseInt(parts[0]);
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    long now = SystemClock.uptimeMillis();
                    MotionEvent event = MotionEvent.obtain(now, now, action, x, y, 0);
                    event.setSource(InputDevice.SOURCE_MOUSE);
                    ok = (Boolean) inject.invoke(manager, event, 0);
                    event.recycle();
                    out.print(ok ? "1\n" : "0\n");
                    out.flush();
                    continue;
                }
                if (line.startsWith("s")) {
                    String[] parts = line.substring(1).trim().split("\\s+");
                    float x = Float.parseFloat(parts[0]);
                    float y = Float.parseFloat(parts[1]);
                    float scroll = Float.parseFloat(parts[2]);
                    long now = SystemClock.uptimeMillis();
                    MotionEvent.PointerProperties[] pp = new MotionEvent.PointerProperties[1];
                    pp[0] = new MotionEvent.PointerProperties();
                    pp[0].id = 0;
                    pp[0].toolType = MotionEvent.TOOL_TYPE_MOUSE;
                    MotionEvent.PointerCoords[] pc = new MotionEvent.PointerCoords[1];
                    pc[0] = new MotionEvent.PointerCoords();
                    pc[0].x = x;
                    pc[0].y = y;
                    pc[0].setAxisValue(MotionEvent.AXIS_VSCROLL, scroll);
                    MotionEvent event = MotionEvent.obtain(now, now, MotionEvent.ACTION_SCROLL, 1, pp, pc, 0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_MOUSE, 0);
                    ok = (Boolean) inject.invoke(manager, event, 0);
                    event.recycle();
                    out.print(ok ? "1\n" : "0\n");
                    out.flush();
                    continue;
                }
                int code = Integer.parseInt(line.trim());
                long now = SystemClock.uptimeMillis();
                ok = send(manager, inject, now, KeyEvent.ACTION_DOWN, code, 0)
                    && send(manager, inject, now, KeyEvent.ACTION_UP, code, 0);
            } catch (Throwable t) {
                ok = false;
            }
            out.print(ok ? "1\n" : "0\n");
            out.flush();
        }
    }

    private static boolean power(int mode) {
        try {
            Class<?> sc = Class.forName("android.view.SurfaceControl");
            Object display;
            try {
                display = sc.getMethod("getInternalDisplayToken").invoke(null);
            } catch (NoSuchMethodException e) {
                display = sc.getMethod("getBuiltInDisplay", int.class).invoke(null, 0);
            }
            if (display == null) return false;
            sc.getMethod("setDisplayPowerMode", IBinder.class, int.class).invoke(null, display, mode);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean type(Object manager, Method inject, KeyCharacterMap keymap, String text) throws Exception {
        KeyEvent[] events = keymap.getEvents(text.toCharArray());
        if (events == null) return false;
        for (KeyEvent event : events) {
            if (!(Boolean) inject.invoke(manager, event, WAIT_FOR_FINISH)) return false;
        }
        return true;
    }

    private static boolean pasteClipboard(String text, Object manager, Method inject) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"cmd", "clipboard", "set", "text", text});
            p.waitFor();
            long now = SystemClock.uptimeMillis();
            return send(manager, inject, now, KeyEvent.ACTION_DOWN, 279, 0)
                && send(manager, inject, now, KeyEvent.ACTION_UP, 279, 0);
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean send(Object manager, Method inject, long now, int action, int code, int repeat) throws Exception {
        KeyEvent event = new KeyEvent(now, now, action, code, repeat);
        return (Boolean) inject.invoke(manager, event, WAIT_FOR_FINISH);
    }
}

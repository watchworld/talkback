/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.switchaccess.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import com.android.switchaccess.SwitchAccessNodeCompat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ShadowExtractor;

/**
 * Robolectric tests of ExtendedNodeCompat
 */
@Config(
        manifest = Config.NONE,
        emulateSdk = 18,
        shadows = {
                ShadowAccessibilityNodeInfo.class,
                ShadowAccessibilityNodeInfoCompat.class,
                ShadowAccessibilityWindowInfo.class})
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricTestRunner.class)
public class SwitchAccessNodeCompatTest {

    /* Build a simple tree with a parent and two children */
    AccessibilityNodeInfo mParentNode, mChildNode0, mChildNode1;

    @Before
    public void setUp() {
        ShadowAccessibilityNodeInfo.resetObtainedInstances();

        /* Build accessibility node tree */
        mParentNode = AccessibilityNodeInfo.obtain();
        mParentNode.setContentDescription("mParentNode");
        mChildNode0 = AccessibilityNodeInfo.obtain();
        mChildNode0.setContentDescription("mChildNode0");
        mChildNode1 = AccessibilityNodeInfo.obtain();
        mChildNode1.setContentDescription("mChildNode1");

        ShadowAccessibilityNodeInfo shadowParent =
                (ShadowAccessibilityNodeInfo) ShadowExtractor.extract(mParentNode);
        shadowParent.addChild(mChildNode0);
        shadowParent.addChild(mChildNode1);
    }

    @After
    public void tearDown() {
        assertFalse(ShadowAccessibilityNodeInfoCompat.areThereUnrecycledNodes(true));
    }

    @Test
    public void testConstructorWithNoWindowList_windowListShouldBeEmpty() {
        SwitchAccessNodeCompat compat = new SwitchAccessNodeCompat(mParentNode);
        assertEquals(0, compat.getWindowsAbove().size());
        compat.recycle();
        mChildNode0.recycle();
        mChildNode1.recycle();
    }

    @Test
    public void testConstructorWithWindowList_windowListShouldBeKept() {
        List<AccessibilityWindowInfo> windowInfos = new ArrayList<>(2);
        windowInfos.add(AccessibilityWindowInfo.obtain());
        windowInfos.add(AccessibilityWindowInfo.obtain());
        SwitchAccessNodeCompat compat = new SwitchAccessNodeCompat(mParentNode, windowInfos);
        assertEquals(2, compat.getWindowsAbove().size());
        assertTrue(windowInfos.get(0) == compat.getWindowsAbove().get(0));
        assertTrue(windowInfos.get(1) == compat.getWindowsAbove().get(1));
        compat.recycle();
        mChildNode0.recycle();
        mChildNode1.recycle();
    }

    @Test
    public void testGetParent() {
        SwitchAccessNodeCompat compat = new SwitchAccessNodeCompat(mChildNode0);
        SwitchAccessNodeCompat parent = compat.getParent();
        assertEquals(mParentNode, parent.getInfo());
        compat.recycle();
        parent.recycle();
        mParentNode.recycle();
        mChildNode1.recycle();
    }

    @Test
    public void testGetChild() {
        SwitchAccessNodeCompat parent = new SwitchAccessNodeCompat(mParentNode);
        SwitchAccessNodeCompat child0 = parent.getChild(0);
        SwitchAccessNodeCompat child1 = parent.getChild(1);
        assertEquals(mChildNode0, child0.getInfo());
        assertEquals(mChildNode1, child1.getInfo());
        parent.recycle();
        child0.recycle();
        child1.recycle();
        mChildNode0.recycle();
        mChildNode1.recycle();
    }

    @Test
    public void testVisibleBounds_nonOverlappingWindow_matchesNodeBounds() {
        Rect nodeBounds = new Rect(100, 100, 200, 200);
        Rect windowBounds = new Rect(250, 250, 300, 300);
        Rect visibleBounds = getBoundsForNodeCoveredByWindow(nodeBounds, windowBounds);
        assertEquals(nodeBounds, visibleBounds);
        mChildNode0.recycle();
        mChildNode1.recycle();
    }

    @Test
    public void testVisibleBounds_overlappingWindowOnBottomRight_truncatesNodeBoundsOnBottom() {
        Rect nodeBounds = new Rect(100, 100, 200, 200);
        Rect windowBounds = new Rect(140, 150, 300, 300);
        Rect visibleBounds = getBoundsForNodeCoveredByWindow(nodeBounds, windowBounds);
        assertEquals(new Rect(100, 100, 200, 150), visibleBounds);
        mChildNode0.recycle();
        mChildNode1.recycle();
    }

    @Test
    public void testVisibleBounds_overlappingWindowOnTopRight_truncatesNodeBoundsOnRight() {
        Rect nodeBounds = new Rect(100, 100, 200, 200);
        Rect windowBounds = new Rect(160, 50, 300, 150);
        Rect visibleBounds = getBoundsForNodeCoveredByWindow(nodeBounds, windowBounds);
        assertEquals(new Rect(100, 100, 160, 200), visibleBounds);
        mChildNode0.recycle();
        mChildNode1.recycle();
    }

    @Test
    public void testVisibleBounds_overlappingWindowOnTopLeft_truncatesNodeBoundsOnTop() {
        Rect nodeBounds = new Rect(100, 100, 200, 200);
        Rect windowBounds = new Rect(50, 50, 180, 150);
        Rect visibleBounds = getBoundsForNodeCoveredByWindow(nodeBounds, windowBounds);
        assertEquals(new Rect(100, 150, 200, 200), visibleBounds);
        mChildNode0.recycle();
        mChildNode1.recycle();
    }

    @Test
    public void testVisibleBounds_overlappingWindowOnBottomLeft_truncatesNodeBoundsOnLeft() {
        /* Flip the node and window bounds */
        Rect nodeBounds = new Rect(200, 200, 100, 100);
        Rect windowBounds = new Rect(110, 300, 50, 150);
        Rect visibleBounds = getBoundsForNodeCoveredByWindow(nodeBounds, windowBounds);
        assertEquals(new Rect(110, 100, 200, 200), visibleBounds);
        mChildNode0.recycle();
        mChildNode1.recycle();
    }

    private Rect getBoundsForNodeCoveredByWindow(Rect nodeBounds, Rect windowBounds) {
        List<AccessibilityWindowInfo> windowInfos = new ArrayList<>(2);
        windowInfos.add(AccessibilityWindowInfo.obtain());
        ShadowAccessibilityWindowInfo shadowWindow =
                (ShadowAccessibilityWindowInfo) ShadowExtractor.extract(windowInfos.get(0));
        shadowWindow.setBoundsInScreen(windowBounds);
        mParentNode.setBoundsInScreen(nodeBounds);
        SwitchAccessNodeCompat compat = new SwitchAccessNodeCompat(mParentNode, windowInfos);
        Rect visibleBounds = new Rect();
        compat.getVisibleBoundsInScreen(visibleBounds);
        compat.recycle();
        return visibleBounds;
    }
}

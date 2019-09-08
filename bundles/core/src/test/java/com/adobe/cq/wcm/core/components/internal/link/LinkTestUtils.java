/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2019 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package com.adobe.cq.wcm.core.components.internal.link;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.adobe.cq.wcm.core.components.models.mixin.LinkMixin;
import com.google.common.collect.ImmutableMap;

public final class LinkTestUtils {

    public static void assertValidLink(@NotNull LinkMixin link, @NotNull String linkURL) {
        assertTrue(link.isLinkValid(), "linkValid");
        assertEquals(linkURL, link.getLinkURL(), "linkURL");
        assertEquals(ImmutableMap.of("href", linkURL), link.getLinkHtmlAttributes(), "linkHtmlAttributes");
    }

    public static void assertValidLink(@NotNull LinkMixin link, @NotNull String linkURL, @Nullable String linkTarget) {
        if (linkTarget == null) {
            assertValidLink(link,  linkURL);
            return;
        }
        assertTrue(link.isLinkValid(), "linkValid");
        assertEquals(linkURL, link.getLinkURL(), "linkURL");
        assertEquals(ImmutableMap.of("href", linkURL, "target", linkTarget), link.getLinkHtmlAttributes(), "linkHtmlAttributes");
    }

    public static void assertInvalidLink(@NotNull LinkMixin link) {
        assertFalse(link.isLinkValid(), "linkValid");
        assertNull(link.getLinkURL(), "linkURL");
        assertNull(link.getLinkHtmlAttributes(), "linkHtmlAttributes");
    }

}

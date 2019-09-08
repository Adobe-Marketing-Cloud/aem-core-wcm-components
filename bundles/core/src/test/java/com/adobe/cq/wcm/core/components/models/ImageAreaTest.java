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
package com.adobe.cq.wcm.core.components.models;

import static com.adobe.cq.wcm.core.components.internal.link.LinkTestUtils.assertInvalidLink;
import static com.adobe.cq.wcm.core.components.internal.link.LinkTestUtils.assertValidLink;

import org.junit.jupiter.api.Test;

/**
 * Test default method implementations of {@link ImageArea}.
 */
class ImageAreaTest {

    private static final String URL = "/url.html";

    @Test
    void testValidLink() {
        ImageArea underTest = new ImageAreaImpl(URL, null);
        assertValidLink(underTest, URL);
    }

    @Test
    void testValidLinkWithTarget() {
        ImageArea underTest = new ImageAreaImpl(URL, "_blank");
        assertValidLink(underTest, URL, "_blank");
    }

    @Test
    void testInvalidLink() {
        ImageArea underTest = new ImageAreaImpl(null, null);
        assertInvalidLink(underTest);
    }

    private static class ImageAreaImpl implements ImageArea {
        
        private final String url;
        private final String target;
        
        public ImageAreaImpl(String url, String target) {
            this.url = url;
            this.target = target;
        }

        @Override
        public String getHref() {
            return url;
        }

        @Override
        public String getTarget() {
            return target;
        }

    }

}

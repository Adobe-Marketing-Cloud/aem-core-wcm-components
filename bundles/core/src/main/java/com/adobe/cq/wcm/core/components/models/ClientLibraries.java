/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2020 Adobe
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

import java.util.Set;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface ClientLibraries {

    default String[] getCategories() {
        throw new UnsupportedOperationException();
    }

    default String getJsInline() {
        throw new UnsupportedOperationException();
    }

    default String getCssInline() {
        throw new UnsupportedOperationException();
    }

    default String getJsTags() {
        throw new UnsupportedOperationException();
    }

    default String getCssTags() {
        throw new UnsupportedOperationException();
    }

    default String getJsAndCssTags() {
        throw new UnsupportedOperationException();
    }

}

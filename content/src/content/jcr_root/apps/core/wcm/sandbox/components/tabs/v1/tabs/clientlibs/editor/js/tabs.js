/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2018 Adobe Systems Incorporated
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
/* global jQuery, Coral */
(function($, Coral) {
    "use strict";

    var dialogContentSelector = ".cmp-tabs__editor";
    var childreneditorSelector = "[data-cmp-is='childreneditor']";
    var activeItemSelector = "[data-cmp-tabs-v1-dialog-edit-hook='activeItem']";
    var activeSelectSelector = "[data-cmp-tabs-v1-dialog-edit-hook='activeSelect']";

    $(document).on("dialog-loaded", function(e) {
        var $dialog = e.dialog;
        var $dialogContent = $dialog.find(dialogContentSelector);
        var dialogContent = $dialogContent.length > 0 ? $dialogContent[0] : undefined;

        if (dialogContent) {
            var childrenEditor = dialogContent.querySelector(childreneditorSelector);
            var activeSelect = dialogContent.querySelector(activeSelectSelector);
            var activeItem = dialogContent.querySelector(activeItemSelector);

            Coral.commons.ready(childrenEditor, function() {
                updateTabs(childrenEditor, activeSelect, activeItem);
            });

            childrenEditor.on("change", function() {
                updateTabs(childrenEditor, activeSelect, activeItem);
            });

            activeSelect.on("change", function() {
                activeItem.value = activeSelect.value;
            });
        }
    });

    /**
     * Update the list of tabs in the active tab selector
     *
     * @param {HTMLElement} childrenEditor Children editor multifield
     * @param {HTMLElement} activeSelect Active tab select field
     * @param {HTMLElement} activeItem Active tab hidden input
     */
    function updateTabs(childrenEditor, activeSelect, activeItem) {
        var selectedValue = activeSelect.value || activeItem.value;
        activeSelect.items.clear();
        var cmpChildrenEditor = $(childrenEditor).adaptTo("cmp-childreneditor");
        if (cmpChildrenEditor) {
            cmpChildrenEditor.items().forEach(function(item) {
                activeSelect.items.add({
                    selected: item.name === selectedValue,
                    value: item.name,
                    content: {
                        textContent: item.description
                    }
                });
            });
        }
    }
})(jQuery, Coral);

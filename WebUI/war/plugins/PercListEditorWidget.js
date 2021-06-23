/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

(function($) {
    $.PercListEditorWidget = function(options) {

        var api = {
            isEnabled         : isEnabled,
            setListItems      : setListItems,
            getListItems      : getListItems,
            removeListItems   : removeListItems,
            addListItem       : addListItem,
            removeListItem    : removeListItem,
            enable            : enable,
            disable           : disable,
            highlightListItem : highlightListItem,
            scrollToListItem  : scrollToListItem
        };

        // HTML generated by this widget
        // html for title, input field, buttons, and scrollable list of users
        var html =  '<div id="perc-ui-listedit-header">' +
            '        <div id="perc-ui-listedit-title">' +
            '            <div id="perc-ui-permission-users-title">' +
            '                ' + options.title1 +
            '            </div>' +
            '            <div id="perc-ui-permission-users-write">' +
            '                ' + options.title2 +
            '            </div>' +
            '        </div>' +
            '        <div id="perc-ui-permission-addstartbutton"/>' +
            '    </div>' +
            '' +
            '    <div id="perc-ui-listedit-listitemfield">' +
            '        <input id="perc-ui-permission-usernamefield" name="perc-ui-permission-usernamefield"/>' +
            '        <div id="perc-ui-permission-plusbutton" title="Add user"/>' +
            '    </div>' +
            '' +
            '    <div id="perc-ui-permission-user-list-scroll-pane">' +
            '        <ul id="perc-ui-permission-user-list">' +
            '            <li>' +
            '                <span>Username</span>' +
            '                <div class="perc-ui-permission-deletebutton" id="Username"/>' +
            '            </li>' +
            '        </ul>' +
            '    </div>';

        // template html for each of the items
        var listItem = '' +
            '            <li>' +
            '                <span>_username_</span>' +
            '                <div class="perc-ui-permission-deletebutton" id="_username_" title="Remove user"/>' +
            '            </li>';

        // ID of the DIV element where this widget will render
        var containerId = $("#"+options.container);
        containerId.empty();
        containerId.html(html);

        // UI elements
        var list            = containerId.find("#perc-ui-permission-user-list");
        var startAddButton  = containerId.find("#perc-ui-permission-addstartbutton");
        var inputFieldDiv   = containerId.find("#perc-ui-listedit-listitemfield");
        var inputField      = containerId.find("#perc-ui-permission-usernamefield");
        var plusButton      = containerId.find("#perc-ui-permission-plusbutton");
        var scrollPane      = containerId.find("#perc-ui-permission-user-list-scroll-pane");
        var title           = containerId.find("#perc-ui-listedit-title");
        var deleteButtons; // these are dynamically created when items are rendered

        // I18N
        var fieldHelpText = "";
        if (options.help !== undefined && options.help != null)
            fieldHelpText = options.help;
        else
            fieldHelpText = I18N.message("perc.ui.folderPropsDialog.inputField@Enter a username");

        // state variables
        var listItems = [];
        var enabled = true;
        var resultItems = null;

        //
        // setup event handling
        //

        // listen to changes on the toggler element passed in the options
        // if the toggler changes, get its value and if it's in the toggle off array, then disable this whole component
        // otherwise enable it
        options.toggler.on("change",function() {
            updateEnableStatus();
        });

        // show/hide input field to add a new item
        startAddButton.off("click").on("click",function() {
            inputFieldDiv.toggle();
        });

        // handle plus button to add a new item
        plusButton.on("click",function() {
            var itemText = inputField.val();
            addListItem(itemText);
        });

        // handle enter key to add a new item
        inputField
            .on("keypress",function(event) {
                var code = (event.keyCode ? event.keyCode : event.which);
                var itemText = inputField.val();
                if(code === 13)
                    if(findItem(itemText))
                        addListItem(itemText);
            }).on("click",function(){
            inputField.focus();
            updatePlusButton();
            updateInputField("clear");
            list.find("li").removeClass("perc-ui-listedit-itemselected");
        }).on('keyup',function(){
            updatePlusButton();
        }).on("change",function(){
            updatePlusButton();
        }).on("blur",function(){
            updateInputField("help");
        }).on("focus",function(){
            updateInputField("clear");
        });

        if (options.results !== undefined && options.results != null)
        {
            resultItems = options.results;

            // add autocomplete to input field
            inputField.autocomplete(resultItems, {
                minChars: 0,
                max: resultItems.length,
                width: ($.browser.msie) ? 336 : 334,
                scrollHeight: 101
            }).result(function(){
                updatePlusButton();
            });
        }

        // initialize UI
        if($.browser.msie)
            plusButton.css("margin-right", "18px");

        setListItems(options.items);
        updateEnableStatus();
        startAddButton.hide();
        disablePlusButton();
        updateInputField("help");

        return api;

        function updateInputField(action) {
            // get input text
            var itemText = inputField.val();
            if(action === "help" && itemText === "") {
                inputField.css("font-style","italic");
                inputField.css("color","gray");
                inputField.val(fieldHelpText);
            } else if(action === "clear") {
                if(itemText === fieldHelpText)
                    inputField.val("");
                inputField.css("font-style","normal");
                inputField.css("color","black");
            }
        }

        function updatePlusButton() {
            // get input text
            var itemText = inputField.val();

            // if text is in the list of valid item values, enable the button
            var found = findItem(itemText);
            if(found)
                enablePlusButton();
            else
                // otherwise disable the button
                disablePlusButton();
        }

        function findItem(item) {
            for(r=0; r<resultItems.length; r++)
                if(item.toLowerCase() === resultItems[r].toLowerCase())
                    return true;
            return false;
        }

        /**
         * Sets the listItems state variable
         *
         * @param items (array) list of items to add to the list
         */
        function setListItems(items) {
            // copy the items to a local array
            listItems = [];
            if(items !== undefined && items != null && items.length > 0) {
                for(u=0; u<items.length; u++) {
                    listItems.splice(0,0,items[u]);
                    filterAllowedItems(items[u]);
                }
            }


            renderListItems();
        }

        function getListItems() {
            return listItems;
        }

        function isEnabled() {
            return enabled;
        }

        function removeListItems() {
            listItems = [];
            renderListItems();
        }

        function filterAllowedItems(listItem) {
            if (resultItems != null)
            {
                // remove it from result items
                resultItems = $.grep(resultItems, function(value) {
                    return value !== listItem;
                });
                inputField.setOptions({data: resultItems});
            }
        }

        function addListItem(listItem) {
            // basic validation
            if(listItem == null || listItem === "")
                return;

            // dont allow duplicates
            for(li=0; li<listItems.length; li++)
                if(listItem.toLowerCase() === listItems[li].toLowerCase())
                    return;

            // add the new list item
            listItems.splice(0,0,listItem);

            // clear the input field
            inputField.val("");

            filterAllowedItems(listItem);

            renderListItems();

            // find where did the element end up
            var index = -1;
            for(li=0; li<listItems.length; li++) {
                if(listItem === listItems[li]) {
                    index = li;
                    break;
                }
            }

            // scroll to the new li that we just added
            var next  = index+1;
            var newLi = list.find("li:nth-child("+next+")");
            var liHeight = list.find("li").height();
            if(index !== -1)
                scrollPane.scrollTop(index * liHeight);

            // highlight the new li
            newLi.addClass("perc-ui-listedit-itemselected");

            updatePlusButton();
        }

        function renderListItems() {

            // sort case insensitive
            $.perc_utils.sortCaseInsensitive(listItems);

            list.empty();
            for(u=0; u<listItems.length; u++) {
                var li = listItem.replace(/_username_/g, listItems[u]);
                list.append(li);
            }

            // after rendering all the items, attach the delete buttons to a click handler
            deleteButtons = $(".perc-ui-permission-deletebutton");
            deleteButtons.on("click",function() {
                var id = $(this).attr("id");
                removeListItem(id);
            });
        }
        function removeListItem(listItem) {

            // iterate through the list of items looking for the index of the item to be removed
            var removeIndex = -1;
            for(li=0; li<listItems.length; li++)
                if(listItem === listItems[li]) {
                    removeIndex = li;
                    break;
                }

            if(removeIndex !== -1) {
                listItems.splice(removeIndex,1);

                if (resultItems != null)
                {
                    // add back to result items
                    resultItems[resultItems.length] = listItem;
                    $.perc_utils.sortCaseInsensitive(resultItems);
                    inputField.setOptions({data: resultItems});
                }

                renderListItems();
            }
        }

        function enable() {
            enabled = true;
            inputField
                .prop("disabled",false);

            var inputText = inputField.val();
            if(inputText !== fieldHelpText)
                inputField.css("color", "black");
            if(inputText === "")
                updateInputField("help");

            scrollPane
                .removeClass("disabled")
                .css("color","black");
            title.css("color","black");
            deleteButtons.show();
            updatePlusButton();
            startAddButton
                .off("click")
                .removeClass("disabled")
                .on("click",function() {
                    inputFieldDiv.toggle();
                });
        }

        function disable() {
            enabled = false;
            inputField
                .prop("disabled", true)
                .css("color", "gray");
            scrollPane
                .addClass("disabled")
                .css("color","gray");
            title.css("color","gray");
            deleteButtons.hide();
            disablePlusButton();
            startAddButton
                .addClass("disabled")
                .off();
            list.find("li").removeClass("perc-ui-listedit-itemselected");
        }

        function disablePlusButton() {
            plusButton
                .addClass("disabled")
                .off();
        }

        function enablePlusButton() {
            plusButton
                .removeClass("disabled")
                .on("click",function() {
                    var itemText = inputField.val();
                    addListItem(itemText);
                });
        }

        function updateEnableStatus() {
            var togglerValue = options.toggler.val();
            if($.inArray(togglerValue, options.toggleroff) !== -1) {
                disable();
            } else {
                enable();
            }
        }

        function highlightListItem(listItem) {}

        function scrollToListItem(listItem) {}
    };
})(jQuery);

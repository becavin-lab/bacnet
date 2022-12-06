//
// function for_each_node()
//
// this is a small utility function, to replace Javascript's "forEach" funcionality in the case of static node lists.
// Not all browsers do "forEach" correctly for this list type - we've actually seen numerous errors related to this in
// Sentry reports of STRING
//

var for_each_node = function (array, callback, scope) {

    "use strict";

    var i;
    for (i = 0; i < array.length; i += 1) {
        callback.call(scope, array[i]);
    }
};

var js_comm_globals = {};

js_comm_globals.session_id = "";
js_comm_globals.task_id = "";


//
// loadFloatingProteinWindowCoor ()
//
//
//

function loadFloatingProteinWindowCoor (node, action_option, expected_width, expected_height, label, xPos, yPos) {

    "use strict";

    var left_pos = xPos - expected_width / 2;
    if (left_pos < 50) {
        left_pos = 50;
    }
    var top_pos = yPos - 30;
    if (top_pos < 20) {
        top_pos = 30;
    }

    var show_item_info_URL = js_comm_globals.web_cgi_dir + "/showiteminfo?";
    show_item_info_URL += "&noAction=1&search_string_link=1";
    show_item_info_URL += "&node=" + node;


    show_item_info_URL += "&taskId=" + js_comm_globals.task_id;
    show_item_info_URL += "&referer=embedded_svg";
    show_item_info_URL += "&node_type=P";

    var wait_spinner_image = js_comm_globals.web_images_dir + "/loading_bigrotation.gif";

    hideFloatingDiv ("fpWindowDiv");      // just in case it is showing somewhere else.

    displayFloatingDiv ("fpWindowDiv", label, expected_width, expected_height, -1, -1, left_pos, top_pos, "#E6E6E6", null, show_item_info_URL, wait_spinner_image);

    return false;
}



//
// loadFloatingProteinWindow ()
//
//
//

function loadFloatingProteinWindow (event, node, action_option, width, height, label, consider_offset_correction) {

    "use strict";

    var xPositionOffset = 0;
    var yPositionOffset = 0;

    if (consider_offset_correction) {
        var network_object = document.getElementById ("network_object");
        if (network_object) {
            var domRect = network_object.getBoundingClientRect();
            xPositionOffset = domRect.left;
            yPositionOffset = domRect.top;
        }
    }

    event.stopPropagation();   // prevent bubbling up - or else the click event gets processed at the document level as well, and closes the div again.

    return loadFloatingProteinWindowCoor (node, action_option, width, height, label, event.clientX + xPositionOffset, event.clientY + yPositionOffset);
}

//
// function loadFloatingInteractionWindowCoor()
//
//
//

function loadFloatingInteractionWindowCoor (node1, node2, label, xPos, yPos) {

    return 

    "use strict";

    var left_pos = xPos - 350;
    if (left_pos < 50) {
        left_pos = 50;
    }
    var top_pos = yPos - 30;
    if (top_pos < 20) {
        top_pos = 30;
    }

    var show_edge_popup_URL = js_comm_globals.web_cgi_dir + "/showedgepopup?";

    var taskidDiv = document.getElementById('string_taskid');

    var taskId = '_notask';
    if (taskidDiv) {
        taskId = taskidDiv.getAttribute('data-taskid');
    }

    //show_edge_popup_URL += "taskId=" + taskId;
    show_edge_popup_URL += "taskId=_notask";

    var wait_spinner_image = js_comm_globals.web_images_dir + "/loading_bigrotation.gif";

    hideFloatingDiv ("fpWindowDiv");      // just in case it is showing somewhere else already.

    displayFloatingDiv ("fpWindowDiv", label, -1, -1, -1, -1, left_pos, top_pos, "#E6E6E6", null, show_edge_popup_URL + "&amp;node1=" + node1 + "&amp;node2=" + node2 + "&amp;referer=embedded_svg", wait_spinner_image);

    return false;
}

//
// function edge_popup()
//
//
//

function edge_popup (event, node1, node2, label, consider_offset_correction) {

    return 

    "use strict";

    // this function can be called both from inside the normal document, and from inside the 'network <object>' (if there is one).
    // In the latter case, there needs to be an offset in interpreting the mouse coordinates.

    var xPositionOffset = 0;
    var yPositionOffset = 0;

    if (consider_offset_correction) {
        var network_object = document.getElementById ("network_object");
        if (network_object) {
            var domRect = network_object.getBoundingClientRect();
            xPositionOffset = domRect.left;
            yPositionOffset = domRect.top;
        }
    }

    event.stopPropagation();   // prevent bubbling up - or else the click event gets processed at the document level as well, and closes the div again.

    return loadFloatingInteractionWindowCoor (node1, node2, label, event.clientX + xPositionOffset, event.clientY + yPositionOffset);
}

//
// function toggle_structure_section ()
//
//
//

function toggle_structure_section (offset, identifier) {

    "use strict";

    var current_section_nr_text_id = "current.section.count." + identifier;
    var current_section_nr_text = document.getElementById(current_section_nr_text_id);
    var max_section_nr_text_id = "max.section.count." + identifier;
    var max_section_nr_text = document.getElementById(max_section_nr_text_id);
    var current_section_count = current_section_nr_text.textContent;
    var max_section_count = max_section_nr_text.textContent;
    var desired_new_section = Number(current_section_count) + offset;

    if (desired_new_section > 0 && desired_new_section <= Number(max_section_count)) {

        current_section_nr_text.textContent = String(desired_new_section);

        var i = 1;
        var this_section_group_id;
        var this_section_group;

        for (i = 1; i <= Number(max_section_count); i += 1) {
            this_section_group_id = "protein.section.group." + i;
            this_section_group = document.getElementById(this_section_group_id);
            if (i === desired_new_section) {
                this_section_group.style.visibility = "visible";
            } else {
                this_section_group.style.visibility = "hidden";
            }
        }
        var domain_offset_id = "domainoffset" + identifier + "r" + desired_new_section;
        var domain_offset_text = document.getElementById(domain_offset_id);
        var domain_offset = domain_offset_text.textContent;

        var smart_image = document.getElementById("smart_domain_illustration_image");

        smart_image.setAttribute("x", domain_offset);
    }
    return true;
}


function getSTRING(root_url, params) {

    js_comm_globals.web_cgi_dir = root_url + /cgi/;
    js_comm_globals.web_images_dir = root_url + /images/;

    var stringDiv = document.getElementById('stringEmbedded');

    var form = new FormData(form);
    for (let key in params) {
        let value = params[key]
        if (key == 'identifiers') {
            proteins_str = value.join('%0d');
            form.append(key, proteins_str);
        } else {
            form.append(key, value)
        }
    }

    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState == XMLHttpRequest.DONE) {
            stringDiv.innerHTML = xhr.responseText;
            init_network_interactive_functionalities();
            update_network_coordinates_at_server = function() {};
        }
    }

    xhr.open('POST', root_url + "/api/interactive_svg/network", true);
    xhr.send(form);

}

function submit_current_network() {
    var form = document.getElementById('string_embedded_linkout');
    form.submit();
    console.log(form);
}

function getSTRINGpost(root_url, params) {

    js_comm_globals.web_cgi_dir = root_url + /cgi/;
    js_comm_globals.web_images_dir = root_url + /images/;

    var stringDiv = document.getElementById('stringEmbedded');

    var form = new FormData(form);
    for (let key in params) {
        let value = params[key]
        form.append(key, value)
    }

    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState == XMLHttpRequest.DONE) {
            stringDiv.innerHTML = xhr.responseText;
            init_network_interactive_functionalities();
            update_network_coordinates_at_server = function() {};
        }
    }

    xhr.open('POST', root_url + "/api/interactive_svg/network", true);
    xhr.send(form);

}


//
// svgmousemove.js
//
// this adds mouse-dragging functionality and click-handlers to the STRING svg networks ...
//


//
// global variables
//
//
//

var svgWidth = 450.0;
var svgHeight = 450.0;
var parent_document = null;
var network_document = null;
var network_offset_x = 0;
var network_offset_y = 0;
var svg_metainfo_nodes = {};
var svg_network_positions_dirty = 0;


//
// function setMovableNode()
//
//

function setMovableNode (obj) {

    "use strict";

    // initialize flags

    obj.has_moved = false;
    obj.is_in_front = false;
    obj.is_being_dragged = false;
    obj.original_event_target = null;

    // register the starting events. The other events will be registered later, on demand only.

    obj.onmousedown = moveObjectMDown;
    obj.ontouchstart = moveObjectTDown;

    // the next lines may appear a bit convoluted, but they are needed so that the special variable 'this' always has the correct meaning.
    // That is because the 'move' and 'up' events need to be captured at the document level ... to prevent the mouse accidentally
    // leaving the dragged object (if the user moves the mouse too fast). So, the handlers will be attached only on demand, and then
    // redirected to the local handlers, for which 'this' will have the correct meaning again.
    //
    // alternatively, we could store the currently dragged element in a global variable ... but this will create problems on touch-screen devices:
    // they allow the dragging of multiple elements simultaneously, with multiple fingers ...

    obj.onmouseupCallback = function (event) { obj.original_event_target = event.currentTarget; return (moveObjectMUp.apply (obj, arguments)); }
    obj.onmousemoveCallback = function (event) { obj.original_event_target = event.currentTarget; return (moveObjectMMove.apply (obj, arguments)); }
    obj.ontouchendCallback = function (event) { obj.original_event_target = event.currentTarget; return (moveObjectTUp.apply (obj, arguments)); }
    obj.ontouchmoveCallback = function (event) { obj.original_event_target = event.currentTarget; return (moveObjectTMove.apply (obj, arguments)); }
}


//
// function initiate_move ()
//
//

function initiate_move (elm, x_pos, y_pos) {

    "use strict";

    if ((typeof autoColorMode !== 'undefined') && (autoColorMode)) { return } // we disable move in the color mode

    // first, record the start positions of both the mouse as well as the dragged element anchor
    // (these not necessarily fully identical, since the element has a larger-than-zero size on the screen).

    elm.movStartX = x_pos;
    elm.movStartY = y_pos;
    elm.movStartObject = {
        x: elm.node.x,
        y: elm.node.y
    };

    // adjust flags

    elm.has_moved = false;
    elm.is_in_front = false;
    elm.is_being_dragged = true;

    // compute the correct screen offset, if required.
    // We cannot do that earlier, because it depends on the current scroll state etc.

    var network_object = document.getElementById ("network_object");
    if (network_object) {
        var domRect = network_object.getBoundingClientRect();
        network_offset_x = domRect.left;
        network_offset_y = domRect.top;
    }
}


//
// function moveObjectMDown()
//
//

function moveObjectMDown (event) {

    initiate_move (this, event.clientX, event.clientY);

    // in order to never lose events while dragging, we now "capture" the mouse by registering global event handlers (at the document level).

    parent_document.addEventListener ("mousemove", this.onmousemoveCallback, false);
    parent_document.addEventListener ("mouseup", this.onmouseupCallback, false);
    if (parent_document !== network_document) network_document.addEventListener ("mousemove", this.onmousemoveCallback, false);
    if (parent_document !== network_document) network_document.addEventListener ("mouseup", this.onmouseupCallback, false);

    return false;
}


//
// function moveObjectMMove()
//
//

function moveObjectMMove (event) {

    "use strict";

    if (!this.is_being_dragged) return false;

    if (!this.is_in_front) {
        this.parentNode.appendChild (this);      // re-attaching any element to its parent will re-define the z-order in SVG ...
        this.is_in_front = true;
    }

    // If the SVG is set to display=none when the network is loaded the move coords are off.
    // This is the fix.
    
    if (svgHeight == 0 || svgWidth == 0) {
        var svgelm = network_document.getElementById("svg_network_image");
        svgWidth = svgelm.width;
        svgHeight = svgelm.height;
    }

    var new_x_pos = event.clientX;
    var new_y_pos = event.clientY;

    // where did the event originate? We might have to adjust the positions ...

    if (parent_document !== network_document) {
        if (this.original_event_target == parent_document) {
            new_x_pos -= network_offset_x;
            new_y_pos -= network_offset_y;
        }
    }

    this.original_event_target = null;

    if (new_x_pos != this.movStartX || new_y_pos != this.movStartY)
        this.has_moved = true;

    this.node.x = this.movStartObject.x + new_x_pos - this.movStartX;
    this.node.y = this.movStartObject.y + new_y_pos - this.movStartY;

    if (this.node.x < this.node.radius / 2.0)
        this.node.x = this.node.radius / 2.0;
    else if (this.node.x + this.node.radius / 2.0 > svgWidth)
        this.node.x = svgWidth - this.node.radius / 2.0;

    if (this.node.y < this.node.radius / 2.0)
        this.node.y = this.node.radius / 2.0;
    else if (this.node.y + this.node.radius / 2.0 > svgHeight)
        this.node.y = svgHeight - this.node.radius / 2.0;

    this.setAttribute ("transform", "translate(" + (this.node.x - this.node.ix) + "," + (this.node.y - this.node.iy) + ")");

    for (var i = 0; i < this.node.links.length; ++i) {

        var l = this.node.links[i];
        var lx = l.n2.x - l.n1.x;
        var ly = l.n2.y - l.n1.y;
        var tl = Math.sqrt (lx * lx + ly * ly);

        if (tl == 0.0) {
            // handle case when nodes are on top of each other
            lx = 0.0; ly = 0.0;
        } else {
            // unit vector in direction of line
            lx = lx / tl; ly = ly / tl;
        }
        // perpendicular vector
        var px = -ly;
        var py = lx;

        l.elm.setAttribute ("x1", l.n1.x + lx * l.x1 + px * l.y1);
        l.elm.setAttribute ("y1", l.n1.y + ly * l.x1 + py * l.y1);
        l.elm.setAttribute ("x2", l.n2.x + lx * l.x2 + px * l.y2);
        l.elm.setAttribute ("y2", l.n2.y + ly * l.x2 + py * l.y2);

    }

    svg_network_positions_dirty = 1;

    return false;
}


//
// function moveObjectMUp()
//
//

function moveObjectMUp (event) {

    "use strict";

    this.is_being_dragged = false;

    // now, "release" the mouse by removing the  global event handlers (at the document level).

    parent_document.removeEventListener ("mousemove", this.onmousemoveCallback, false);
    parent_document.removeEventListener ("mouseup", this.onmouseupCallback, false);
    if (parent_document !== network_document) network_document.removeEventListener ("mousemove", this.onmousemoveCallback, false);
    if (parent_document !== network_document) network_document.removeEventListener ("mouseup", this.onmouseupCallback, false);

    return false;
}


//
// function moveObjectTDown()
//
//

function moveObjectTDown (event) {

    "use strict";

    if (event.targetTouches.length == 0) return true;

    event.preventDefault();

    var touch = event.targetTouches[0];
    this.touchId = touch.identifier;

    event.clientX = touch.clientX;
    event.clientY = touch.clientY;

    initiate_move (this, event.clientX, event.clientY);

    parent_document.addEventListener ("touchmove", this.ontouchmoveCallback, false);
    parent_document.addEventListener ("touchend", this.ontouchendCallback, false);
    if (parent_document !== network_document) network_document.addEventListener ("touchmove", this.ontouchmoveCallback, false);
    if (parent_document !== network_document) network_document.addEventListener ("touchend", this.ontouchendCallback, false);

    // now, as always, we return false here.
    // it is actually particularly important here so that normal touch behaviour is blocked, including "pinch", "rotate", etc.
    // unfortunately, this will also block the browser's emulation of 'onclick' when the finger is lifted, so we need to deal
    // with that ourselves in the 'touchend' event handler.

    return false;
}


//
// function moveObjectTMove()
//
//

function moveObjectTMove (event) {

    "use strict";

    event.preventDefault();

    for (var i = 0; i < event.touches.length; ++i)
        if (event.touches[i].identifier == this.touchId) break;

    if (i >= event.touches.length)
        return (moveObjectTUp (event));

    var touch = event.touches[i];

    if (touch == null)
        return false;

    event.clientX = touch.clientX;
    event.clientY = touch.clientY;

    moveObjectMMove.apply (this, arguments);

    return false;
}



//
// function moveObjectTUp()
//
//

function moveObjectTUp (event) {

    "use strict";

    this.is_being_dragged = false;

    parent_document.removeEventListener ("touchmove", this.ontouchmoveCallback, false);
    parent_document.removeEventListener ("touchend", this.ontouchendCallback, false);
    if (parent_document !== network_document) network_document.removeEventListener ("touchmove", this.ontouchmoveCallback, false);
    if (parent_document !== network_document) network_document.removeEventListener ("touchend", this.ontouchendCallback, false);

    for (var i = 0; i < event.changedTouches.length; ++i)
        if (event.changedTouches[i].identifier == this.touchId) break;

    var touch = event.changedTouches[i];

    if (touch == null)
        return false;

    event.clientX = touch.clientX;
    event.clientY = touch.clientY;

    handle_network_node_click.apply (this, arguments);

    return false;
}



//
// function update_network_coordinates_at_server ()
//
//

function update_network_coordinates_at_server (taskId, timeout) {

    "use strict";

    if (!svg_network_positions_dirty) return false;

    svg_network_positions_dirty = 0;

    var coordinates_info = svgWidth + "#" + svgHeight + "#";

    for (var i in svg_metainfo_nodes) {
        var n = svg_metainfo_nodes[i];
        coordinates_info = coordinates_info + i + ":" + n.x + ":" + n.y + "#";
    }

    var data = {taskId:taskId, coordinates_info:coordinates_info, timeout:timeout};

    if (typeof(Worker) !== "undefined") {
        var worker = new Worker (js_comm_globals.web_js_dir + "/network_state_communication_worker_" + js_comm_globals.js_script_version + ".js");
        worker.postMessage (data);
    } else {
        var xmlhttp = new XMLHttpRequest();
        xmlhttp.open ("POST", js_comm_globals.web_cgi_dir + "/updatenetworkcoordinates", true);
        xmlhttp.setRequestHeader ("Content-type", "application/x-www-form-urlencoded");
        xmlhttp.send ("taskId=" + data.taskId + "&node_coordinates=" + data.coordinates_info);
    }

    updateCpnonceHash();

}


//
// function handle_network_edge_click ()
//
//

function handle_network_edge_click (event) {

    "use strict";

    if (typeof isAutocolorEnabled == "function" && isAutocolorEnabled()) return;

    event.preventDefault();

    var id_fields = this.id.split (".");

    hideFloatingDiv ("fpWindowDiv");      // just in case it is showing somewhere else.

    edge_popup (event, id_fields[1], id_fields[2], 'Interaction', true);

    event.stopPropagation();
}


//
// function handle_network_node_click ()
//
//

function handle_network_node_click (event) {

    "use strict";

    if (this.has_moved) return;     // don't proceed if we have just come out of a dragging operation.

    var id_fields = this.id.split (".");
    var node_id = id_fields[1];
    var action_option = this.getAttribute ("data-action_option");
    var expected_popup_width = Number (this.getAttribute ("data-exp_width"));
    var expected_popup_height = Number (this.getAttribute ("data-exp_height"));
    var safe_div_label = this.getAttribute ("data-safe_div_label");

    // autocolor mode

    if (typeof isAutocolorEnabled == "function" && isAutocolorEnabled() ) {
        assign_new_color_to_node(node_id, autoColorMode); 
        update_taskdata_nodes_and_colors([node_id], [autoColorMode]);
        return;
    }

    hideFloatingDiv ("fpWindowDiv");      // just in case it is showing somewhere else.

    loadFloatingProteinWindow (event, node_id, action_option, expected_popup_width, expected_popup_height, safe_div_label, true);

    event.stopPropagation();
}

//
// function init_network_interactive_functionalities()
//
//

function init_network_interactive_functionalities (event) {

    "use strict";

    // now, there are two places in which the SVG will be located within the DOM:
    // either directly embedded in the document, or within its own 'sub-document' (via an <object> tag).
    // The latter happens when the user arrives via the static-url format. It's more search engine-friendly, but less performant ..
    //
    // accordingly, we need to establish the correct 'network_document' context here.

    var network_object = document.getElementById ("network_object");

    if (network_object) {
        network_document = network_object.contentDocument;
        parent_document = document;
    } else {
        network_document = document;
        parent_document = network_document;
    }

    if (!network_document) {
        return;
    }

    // discover the size of the SVG element, store in global variables:

    var svgelm = network_document.getElementById("svg_network_image");

    if (!svgelm) return;

    var r = svgelm.getBoundingClientRect();

    svgWidth = r.width;
    svgHeight = r.height;

    var node_wrapper_elements = network_document.querySelectorAll(".nwnodecontainer");
    if (node_wrapper_elements !== null) {
        for_each_node(node_wrapper_elements, function register_node_wrapper_elements (elm) {
            var this_id = elm.id;
            svg_metainfo_nodes[this_id] = {};
            svg_metainfo_nodes[this_id].elm = elm;
            svg_metainfo_nodes[this_id].elm.node = svg_metainfo_nodes[this_id];
            svg_metainfo_nodes[this_id].x = Number (elm.getAttribute ("data-x_pos"));
            svg_metainfo_nodes[this_id].y = Number (elm.getAttribute ("data-y_pos"));
            svg_metainfo_nodes[this_id].radius = Number (elm.getAttribute ("data-radius"));
            svg_metainfo_nodes[this_id].ix = svg_metainfo_nodes[this_id].x;
            svg_metainfo_nodes[this_id].iy = svg_metainfo_nodes[this_id].y;
            svg_metainfo_nodes[this_id].links = [];
            setMovableNode (elm);
            elm.addEventListener ("click", handle_network_node_click);
        });
    }

    var link_wrapper_elements = network_document.querySelectorAll(".nwlinkwrapper");
    if (link_wrapper_elements !== null) {
        for_each_node(link_wrapper_elements, function register_link_wrapper_elements (elm) {
            elm.addEventListener ("click", handle_network_edge_click);
        });
    }

    var edge_elements = network_document.querySelectorAll(".nw_edge");
    if (edge_elements !== null) {
        for_each_node(edge_elements, function register_edge_with_its_nodes(edge) {

            var id_fields = edge.id.split (".");
            var node1 = "node." + id_fields[1];
            var node2 = "node." + id_fields[2];

            var lj = {};
            lj.n1 = svg_metainfo_nodes[node1];
            lj.n2 = svg_metainfo_nodes[node2];
            lj.elm = edge;
            lj.x1 = Number (edge.getAttribute ("x1"));
            lj.x2 = Number (edge.getAttribute ("x2"));
            lj.y1 = Number (edge.getAttribute ("y1"));
            lj.y2 = Number (edge.getAttribute ("y2"));

            var lx = lj.x2 - lj.x1;
            var ly = lj.y2 - lj.y1;
            var tl = Math.sqrt (lx * lx + ly * ly);
            if (tl == 0.0) {
                // handle case when nodes are on top of each other
            lx = 0.0; ly = 0.0;
            } else {
                // unit vector in direction of line
                lx = lx / tl; ly = ly / tl;
            }

            // now, the actual directionality of the vector must come from the directionality of the hypothetical
            // line that connects the node centers. Reason for this: for half-overlapping nodes, in actions
            // mode, they may cause an apparent reversal of directionality because action links often do not
            // reach the node center.

            lx = Math.abs (lx); ly = Math.abs (ly);
            if (lj.n1.ix > lj.n2.ix) { lx *= -1.0; }
            if (lj.n1.iy > lj.n2.iy) { ly *= -1.0; }

            // perpendicular vector

            var px = -ly;
            var py = lx;

            var tmpx, tmpy;

            // transform line endpoint coordinates (x1,y1) and (x2,y2) such that they are relative to the nodes when the line is horizontal (0 degrees)

            lj.x1 -= lj.n1.x; lj.y1 -= lj.n1.y;
            tmpx = lj.x1 * lx + lj.y1 * ly; tmpy = lj.x1 * px + lj.y1 * py; // projection of line vector on line unit vector and perp unit vector
            lj.x1 = tmpx; lj.y1 = tmpy;
            lj.x2 -= lj.n2.x; lj.y2 -= lj.n2.y;
            tmpx = lj.x2 * lx + lj.y2 * ly; tmpy = lj.x2 * px + lj.y2 * py; // projection of line vector on line unit vector and perp unit vector
            lj.x2 = tmpx; lj.y2 = tmpy;

            lj.n1.links.push (lj);
            lj.n2.links.push (lj);

        });
    }

    // now, in order to keep the browser and the server in synch, we occasionally check in the background whether node positions have changed.
    // if so, we inform the server accordingly.

    window.setInterval (function () {
        update_network_coordinates_at_server (js_comm_globals.task_id, 400);
    }, 1000);

}

// and lastly, initialize all event-handlers at the earliest convenience

window.addEventListener ("load", init_network_interactive_functionalities);


// ############# FLOATING DIV LIBRARY #############

/*

Main functions implemented:

- displayFloatingDiv(divId, title, expected_width, expected_height, width, height, left, top, headerColor, hideDelay, url)
    display a div as a floating window.
    It is important that a div with id='divId' is already present in the html page.
    You can display an header with the title of the window (if you set it to null no header will be displayed).
    You can make the window disappear after 'hideDelay' seconds (just set the parameter to null to disable the feature).
    You can substitute the content of the div with the one retrived via an Ajax call, just by specifying the relative 'url'.

- function hideFloatingDiv(divId)
    hide the floating window with div with id='divId'


$$$$$$  ANOTHER IMPORTANT WARNING !!!!!  $$$$$
As currently written, this routine only supports one floating Div per page !!!!!
There are global variables ... funny things will happen if you have two floats in one page
(CvM note: one some pages, we now have several floating Divs ... however, only one should be active at any given time. Seems to work ... )

*/


// Global Variables

var originalDivHtml = {};
var hideDelaySeconds = -1;

// var onmfun; // we store in this variable the onmouseout function of the container div

var bfd_parent_document = null;
var bfd_network_document = null;
var bfd_network_offset_x = 0;
var bfd_network_offset_y = 0;

//variables used by the movement code

var objDiv = null;
var mouseDownOffsetX = 0;
var mouseDownOffsetY = 0;



// *********************** MAIN FUNCTIONS ***********************


//************************************************************************************
// function displayFloatingDivImp
//
//
//************************************************************************************

function displayFloatingDivImp (divId, title, width, height, left, top, headerColor, hideDelay, recenter, previous_width) {

    if (document.getElementById(divId).style.visibility != "visible") {

        var scrOfX = -1;
        var scrOfY = -1;

        if (typeof (window.pageYOffset) == 'number') {    // modern, standards-compliant way

            scrOfY = window.pageYOffset;
            scrOfX = window.pageXOffset;

        } else if ( document.body && ( document.body.scrollLeft || document.body.scrollTop ) ) {        // DOM compliant

            scrOfY = document.body.scrollTop;
            scrOfX = document.body.scrollLeft;

        } else if ( document.documentElement && ( document.documentElement.scrollLeft || document.documentElement.scrollTop ) ) {   // IE6 mode

            scrOfY = document.documentElement.scrollTop;
            scrOfX = document.documentElement.scrollLeft;
        }

        var windowWidth = 10000;
        var windowHeight = 10000;

        if (typeof (window.innerWidth) == 'number') {    // modern, standards-compliant way

            windowWidth = window.innerWidth;
            windowHeight = window.innerHeight;

        } else if ( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {   // IE6 mode

            windowWidth = document.documentElement.clientWidth;
            windowHeight = document.documentElement.clientHeight;

        } else if ( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {        // DOM compliant

            windowWidth = document.body.clientWidth;
            windowHeight = document.body.clientHeight;

        }

        if (top != null) { top = top + scrOfY; }
        if (left != null) { left = left + scrOfX; }

        if (width != null  &&  width > 0) { document.getElementById(divId).style.width = width + 'px'; }
        if (height != null  &&  height > 0) { document.getElementById(divId).style.height = height + 'px'; }
        if (left != null  &&  left > 0) { document.getElementById(divId).style.left = left + 'px'; }
        if (top != null  &&  top > 0) { document.getElementById(divId).style.top = top + 'px'; }

        document.getElementById(divId).style.zIndex = 100000;

        var innerDivHtml = document.getElementById(divId).innerHTML;

        originalDivHtml[divId] = innerDivHtml;

        var resize_element_id = "movable_div_resize_wrapper_" + divId;

        var wrapper_markup_top = '<table class="movable_div_wrapper" id="' + resize_element_id + '"><tr><td>';
        var wrapper_markup_title = '';
        var wrapper_markup_bottom = '</td></tr></table>';

        if (title != null) {

            var wrapper_markup_title =

                '<table class="movable_div_title_container" style="background:' + headerColor + ';">' +
                '<tr>' +
                '<td class="movable_div_title_bar" data-parent_div_to_move_id="' + divId + '">' +
                title +
                '</td>' +
                '<td class="movable_div_closelink_wrapper">' +
                '<a class="floatingdivcloselink" title="click to close" href="#" onclick="hideFloatingDiv(\'' + divId + '\');return false;">' +
                '<img src="' + js_comm_globals.web_images_dir + '/floating_close_icon.png" width="16" height="16"/>' +
                '</a>' +
                '</td>' +
                '</tr>' +
                '</table>';
        }

        // add the wrapper around your div-content

        document.getElementById(divId).innerHTML = wrapper_markup_top + wrapper_markup_title + innerDivHtml + wrapper_markup_bottom;

        // .. and show it (i.e. un-hide it).

        document.getElementById(divId).style.display = "block";
        document.getElementById(divId).style.visibility = "visible";

        // determine the width of the window, but only if not already specified.

        var new_width = document.getElementById(divId).offsetWidth;
        if (width != null  &&  width > 0) { new_width = width; }

        document.getElementById(resize_element_id).style.width = new_width + "px";

        if (recenter) {
            var width_difference = new_width - previous_width;
            var new_left = left - width_difference / 2;
            if (new_left - scrOfX + new_width > windowWidth - 20) { new_left = scrOfX + windowWidth - (20 + new_width); }
            if (new_left < 20 + scrOfX) { new_left = 20 + scrOfX; }
            var abs_move = Math.abs (new_left - left);
            if (abs_move > 3) {
                document.getElementById(divId).style.left = new_left + 'px';
            }
        }

        // register the title-bar to be moveable

        initFloatingDivMovementOption ();

        // start the timer

        if (hideDelay !== null && hideDelay > 0) {
            timerDivId = divId;
            hideDelaySeconds = hideDelay;
            startFDT ();
        }
    }

}

//*************************************************************************************************
// function displayFloatingDiv
//
// when width and height are given as "-1", the div will resize itself to accomodate it's content.
// in that case, though, one can supply an "expected" width and height; these will be
// used initially, while the content is still Ajax-requested.
//*************************************************************************************************

function displayFloatingDiv (divId, title, expected_width, expected_height, width, height, left, top, headerColor, hideDelay, url, loadingImgUrl) {


    if (url != null  &&  url != "") {
        ajaxCallForFD (divId, title, width, height, left, top, headerColor, hideDelay, url);
    }

    var waiting_width = 600;
    if (expected_width != null && expected_width > 0) { waiting_width = expected_width; }
    var waiting_height = 350;
    if (expected_height != null && expected_height > 0) { waiting_height = expected_height; }

    if (loadingImgUrl != null && loadingImgUrl != "") {
        document.getElementById(divId).innerHTML =
            "<div style='width:" + waiting_width + "px;height:" + waiting_height + "px;display:table'>" +
            "<div style='text-align:center; display:table-cell; vertical-align:middle;'>" +
            "<img src='" + loadingImgUrl + "' style='vertical-align: middle;'></img>" +
            "</div></div>";
    }

    displayFloatingDivImp (divId, title, width, height, left, top, headerColor, hideDelay);
}

//************************************************************************************
// function displayFloatingDivAjaxResponse
//
//
//************************************************************************************

function displayFloatingDivAjaxResponse (request, divId, title, width, height, left, top, headerColor, hideDelay) {

    if ((request.readyState == 4) && (request.status == 200)) {
        var previous_width = document.getElementById(divId).offsetWidth;
        hideFloatingDiv (divId);
        document.getElementById(divId).innerHTML = request.responseText;
        displayFloatingDivImp (divId, title, width, height, left, top, headerColor, hideDelay, 1, previous_width);
    }
}

//************************************************************************************
// function ajaxCallForFD
//
//
//************************************************************************************

function ajaxCallForFD (divId, title, width, height, left, top, headerColor, hideDelay, url) {

    var request = getRequestObject ();

    request.onreadystatechange = function() {
        displayFloatingDivAjaxResponse (request, divId, title, width, height, left, top, headerColor, hideDelay);
    };

    if (url.length <= 1024) {

        request.open ("GET", url, true);
        request.send (null);

    } else {

        var cut = url.indexOf ("?");

        var params = url.substr (cut+1);
        var naked_url = url.substr (0,cut);

        request.open ("POST", naked_url, true);

        //Send the proper header information along with the request

        request.setRequestHeader ("Content-type", "application/x-www-form-urlencoded");
        request.setRequestHeader ("Content-length", params.length);
        request.setRequestHeader ("Connection", "close");
        request.send (params);
    }
}


//************************************************************************************
// function hideFloatingDiv
//
//
//************************************************************************************

function hideFloatingDiv (div_id) {

    if (div_id == null) { return; }
    if (div_id == "") { return; }

    var this_div = document.getElementById (div_id);
    if (this_div == null) { return; }

    if (this_div.style.display == 'none') { return; }

    if (originalDivHtml[div_id] != undefined) {
        this_div.innerHTML = originalDivHtml[div_id];
    }
    this_div.style.visibility = "hidden";
    this_div.style.display = "none";
}


//
// function movable_div_initiate_move ()
//
//

function movable_div_initiate_move (elm, x_pos, y_pos) {

    "use strict";

    if (elm.parent_div_to_move != null) {

        mouseDownOffsetX = x_pos - parseInt (elm.parent_div_to_move.style.left);
        mouseDownOffsetY = y_pos - parseInt (elm.parent_div_to_move.style.top);
    }

    var network_object = document.getElementById ("network_object");
    if (network_object) {
        var domRect = network_object.getBoundingClientRect();
        bfd_network_offset_x = domRect.left;
        bfd_network_offset_y = domRect.top;
    }
}


// **********************************************************************************
// ***************************** DRAGGING FUNCTIONS **********************************
// **********************************************************************************

//
// function movable_div_MDown_handler ()
//
//

function movable_div_MDown_handler (event) {

    "use strict";

    movable_div_initiate_move (this, event.clientX, event.clientY);

    // in order to never lose events while dragging, we now "capture" the mouse by registering global event handlers (at the document level).

    bfd_parent_document.addEventListener ("mousemove", this.onmousemoveCallback, false);
    bfd_parent_document.addEventListener ("mouseup", this.onmouseupCallback, false);
    if (bfd_parent_document !== bfd_network_document) bfd_network_document.addEventListener ("mousemove", this.onmousemoveCallback, false);
    if (bfd_parent_document !== bfd_network_document) bfd_network_document.addEventListener ("mouseup", this.onmouseupCallback, false);

    return false;
}


//
// function movable_div_MMove_handler ()
//
//

function movable_div_MMove_handler (event) {

    "use strict";

    if (this.parent_div_to_move !== null) {

        var new_x_pos = event.clientX - mouseDownOffsetX;
        var new_y_pos = event.clientY - mouseDownOffsetY;

        if (bfd_parent_document !== bfd_network_document) {
            if (event.currentTarget == bfd_network_document) {
                new_x_pos += bfd_network_offset_x;
                new_y_pos += bfd_network_offset_y;
            }
        }

        this.parent_div_to_move.style.left = (new_x_pos) + 'px';
        this.parent_div_to_move.style.top = (new_y_pos) + 'px';
    }

    return false;
}


//
// function movable_div_MUp_handler ()
//
//

function movable_div_MUp_handler (event) {

    "use strict";

    bfd_parent_document.removeEventListener ("mousemove", this.onmousemoveCallback, false);
    bfd_parent_document.removeEventListener ("mouseup", this.onmouseupCallback, false);
    if (bfd_parent_document !== bfd_network_document) bfd_network_document.removeEventListener ("mousemove", this.onmousemoveCallback, false);
    if (bfd_parent_document !== bfd_network_document) bfd_network_document.removeEventListener ("mouseup", this.onmouseupCallback, false);

    return false;
}


//
// function movable_div_TDown_handler ()
//
//

function movable_div_TDown_handler (event) {

    "use strict";

    return false;
}


//
// function movable_div_TMove_handler ()
//
//

function movable_div_TMove_handler (event) {

    "use strict";

    return false;
}


//
// function movable_div_Tup_handler ()
//
//

function movable_div_TUp_handler (event) {

    "use strict";

    return false;
}


//
// function registerMovableDivTitleBar ()
//
//

function registerMovableDivTitleBar (elm) {

    "use strict";

    // initialize flags

    elm.has_moved = false;
    elm.is_being_dragged = false;
    elm.original_event_target = null;
    elm.parent_div_to_move = null;

    if (elm.hasAttribute ("data-parent_div_to_move_id")) {
        elm.parent_div_to_move = document.getElementById (elm.getAttribute ("data-parent_div_to_move_id"));
    }

    // register the starting events. The other events will be registered later, on demand only.

    elm.onmousedown = movable_div_MDown_handler;
    elm.ontouchstart = movable_div_TDown_handler;

    // the next lines may appear a bit convoluted, but they are needed so that the special variable 'this' always has the correct meaning.
    // That is because the 'move' and 'up' events need to be captured at the document level ... to prevent the mouse accidentally
    // leaving the dragged object (if the user moves the mouse too fast). So, the handlers will be attached only on demand, and then
    // redirected to the local handlers, for which 'this' will have the correct meaning again.

    elm.onmousemoveCallback = function (event) { elm.original_event_target = event.currentTarget; return (movable_div_MMove_handler.apply (elm, arguments)); }
    elm.ontouchmoveCallback = function (event) { elm.original_event_target = event.currentTarget; return (movable_div_TMove_handler.apply (elm, arguments)); }
    elm.onmouseupCallback = function (event) { elm.original_event_target = event.currentTarget; return (movable_div_MUp_handler.apply (elm, arguments)); }
    elm.ontouchendCallback = function (event) { elm.original_event_target = event.currentTarget; return (movable_div_TUp_handler.apply (elm, arguments)); }
}


//
// function initFloatinDivMovementOption ()
//
//

function initFloatingDivMovementOption () {

    // here we attach the necessary event handlers.
    //
    // we need to capture mouse events at the 'document' level - but in some scenarios there is more than one 'document'-object.
    // Namely, when the STRING network is embedded as an SVG element in its own '<object>' tag ... then, we need to attach event handlers to both,
    // or else we lose the mouse when dragging it to vividly.

    var network_object = document.getElementById ("network_object");

    if (network_object) {
        bfd_network_document = network_object.contentDocument;
        bfd_parent_document = document;
    } else {
        bfd_network_document = document;
        bfd_parent_document = bfd_network_document;
    }

    if (!bfd_network_document) { return; }

    var title_bar_elements = bfd_parent_document.querySelectorAll(".movable_div_title_bar");
    if (title_bar_elements !== null) {
        for_each_node(title_bar_elements, function register (elm) {
            registerMovableDivTitleBar (elm);
        });
    }
}


// **********************************************************************************
// ***************************** TIMER FUNCTIONS ************************************
// **********************************************************************************

var timerTimeSecondsFDT = 0;
var timerIDFDT = 0;
var tStartFDT = null;
var timerDivId = null;


//
// function updateTimerFDT ()
//
//

function updateTimerFDT () {

    if (timerIDFDT) {
        clearTimeout (timerIDFDT);
        clockID = 0;
    }

    if (!tStartFDT) { tStartFDT = new Date (); }

    var tDate = new Date ();
    var tDiff = tDate.getTime () - tStartFDT.getTime ();

    tDate.setTime (tDiff);

    timerTimeSecondsFDT = tDate.getSeconds ();

    if (hideDelaySeconds <= timerTimeSecondsFDT) {
        stopFDT ();
        hideFloatingDiv (timerDivId);
    }

    timerIDFDT = setTimeout ("updateTimerFDT()", 1000);
}


//
// function startFDT ()
//
//

function startFDT () {

    tStartFDT = new Date ();

    timerTimeSecondsFDT = 0;

    timerIDFDT = setTimeout ("updateTimerFDT()", 1000);
}


//
// function stopFDT ()
//
//

function stopFDT () {

    if (timerIDFDT) {
        clearTimeout (timerIDFDT);
        timerIDFDT = 0;
    }

    tStartFDT = null;
}


//
// function resetFDT ()
//
//

function resetFDT () {

    tStartFDT = null;
    timerTimeSecondsFDT = 0;
}


// ############# AJAX LIBRARY #############

//----------------------------
// Author: Andrea Franceschini
// Last Update: May 2008
// Version: 0.1
//----------------------------


/*

Main functions implemented:
- ajaxInsertInElement(address, resultRegion)
		Makes an http-get call using the url contained inside the "address" parameter
        and insert the response into the "resultRegion" element of the DOM

- ajaxSimpleCall(address)
		Makes an http-get call using the url contained inside the "address" parameter

*/




function ajaxInsertInElement(address, resultRegion) {
	var request = getRequestObject();
	request.onreadystatechange = function() { showResponseText(request, resultRegion); };
	request.open("GET", address, true);
	request.send(null);
}

function ajaxSimpleCall(address){
	var request = getRequestObject();
	request.open("GET", address, true);
	request.send(null);
}

function getRequestObject() {
	if (window.ActiveXObject) {
		return(new ActiveXObject("Microsoft.XMLHTTP"));
	} else if (window.XMLHttpRequest) {
			return(new XMLHttpRequest());
	} else {
		return(null);
	}
}


function showResponseText(request, resultRegion) {
	if ((request.readyState == 4) && (request.status == 200)) {
		htmlInsert(resultRegion, request.responseText);
	}
}


function htmlInsert(id, htmlData) {
	document.getElementById(id).innerHTML = htmlData;
}


